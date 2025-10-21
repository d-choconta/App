package com.decoraia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement

data class SessionItem(
    val id: String,
    val title: String,
    val lastMessageAt: Timestamp?
)

/** Borra la sesión y todos sus mensajes (subcolección "messages") en lotes de 450 */
private fun deleteSessionCascade(
    db: FirebaseFirestore,
    sessionId: String,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    db.collection("sessions").document(sessionId).collection("messages")
        .get()
        .addOnFailureListener { e -> onError("No se pudieron leer mensajes: ${e.message}") }
        .addOnSuccessListener { snapshot ->
            val chunks = snapshot.documents.chunked(450)
            fun deleteNextChunk(index: Int) {
                if (index >= chunks.size) {
                    db.collection("sessions").document(sessionId)
                        .delete()
                        .addOnFailureListener { e -> onError("Error eliminando chat: ${e.message}") }
                        .addOnSuccessListener { onSuccess() }
                    return
                }
                val batch = db.batch()
                for (doc in chunks[index]) batch.delete(doc.reference)
                batch.commit()
                    .addOnFailureListener { e -> onError("Error borrando mensajes: ${e.message}") }
                    .addOnSuccessListener { deleteNextChunk(index + 1) }
            }
            deleteNextChunk(0)
        }
}

private fun formatDate(date: java.util.Date): String =
    java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm", java.util.Locale.getDefault())
        .format(date)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChatGuardados(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val sesiones = remember { mutableStateListOf<SessionItem>() }
    val deletingIds = remember { mutableStateListOf<String>() }
    var loading by remember { mutableStateOf(true) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var sessionToDelete by remember { mutableStateOf<SessionItem?>(null) }

    // Escucha en tiempo real
    LaunchedEffect(Unit) {
        db.collection("sessions")
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                loading = false
                if (e != null) {
                    scope.launch { snackbar.showSnackbar("Error: ${e.message}") }
                    return@addSnapshotListener
                }
                sesiones.clear()
                snap?.documents?.forEach { d ->
                    sesiones += SessionItem(
                        id = d.id,
                        title = d.getString("title").orEmpty().ifBlank { "Sin título" },
                        lastMessageAt = d.getTimestamp("lastMessageAt")
                    )
                }
            }
    }

    // Diálogo confirmación eliminar
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Eliminar chat") },
            text = { Text("Esta acción borrará el chat y todos sus mensajes. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    val s = sessionToDelete ?: return@TextButton
                    sessionToDelete = null

                    // 🔥 UI optimista
                    val idx = sesiones.indexOfFirst { it.id == s.id }
                    val backupItem = if (idx >= 0) sesiones.removeAt(idx) else null
                    deletingIds += s.id
                    loading = sesiones.isEmpty()

                    scope.launch {
                        deleteSessionCascade(
                            db = db,
                            sessionId = s.id,
                            onError = { msg ->
                                // restaurar si falla
                                if (backupItem != null && sesiones.none { it.id == backupItem.id }) {
                                    sesiones.add(idx.coerceAtMost(sesiones.size), backupItem)
                                }
                                deletingIds.remove(s.id)
                                loading = false
                                scope.launch { snackbar.showSnackbar(msg) }
                            },
                            onSuccess = {
                                deletingIds.remove(s.id)
                                loading = false
                            }
                        )
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("Historial de chats") }) }
    ) { padding ->
        when {
            loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            sesiones.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No hay chats aún") }

            else -> LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sesiones, key = { it.id }) { s ->
                    val isDeleting = deletingIds.contains(s.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isDeleting) {
                                navController.navigate("chatia/${s.id}")
                            }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(s.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = s.lastMessageAt?.toDate()?.let { formatDate(it) } ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { navController.navigate("chatia/${s.id}") },
                                    enabled = !isDeleting
                                ) { Text("Abrir") }

                                Spacer(Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = { sessionToDelete = s },
                                    enabled = !isDeleting
                                ) {
                                    Text(if (isDeleting) "Eliminando…" else "Eliminar")
                                }

                                if (isDeleting) {
                                    Spacer(Modifier.width(8.dp))
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
