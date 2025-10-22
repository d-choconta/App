package com.decoraia.app.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.decoraia.app.ui.components.ChatGuardadosUI
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch

data class SessionItem(
    val id: String,
    val title: String,
    val lastMessageAt: Timestamp?
)

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

@Composable
fun PantallaChatGuardados(navController: NavController) {
    val db = remember { FirebaseFirestore.getInstance() }

    val sesiones = remember { mutableStateListOf<SessionItem>() }
    val deletingIds = remember { mutableStateListOf<String>() }
    var loading by remember { mutableStateOf(true) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var sessionToDelete by remember { mutableStateOf<SessionItem?>(null) }

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

    fun openSession(id: String) {
        navController.navigate("chatia/$id")
    }
    fun requestDelete(s: SessionItem) { sessionToDelete = s }
    fun confirmDelete() {
        val s = sessionToDelete ?: return
        sessionToDelete = null

        val idx = sesiones.indexOfFirst { it.id == s.id }
        val backupItem = if (idx >= 0) sesiones.removeAt(idx) else null
        deletingIds += s.id
        loading = sesiones.isEmpty()

        deleteSessionCascade(
            db = db,
            sessionId = s.id,
            onError = { msg ->
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

    val dateFormatter: (Timestamp?) -> String = { ts ->
        ts?.toDate()?.let { formatDate(it) } ?: "—"
    }

    // Sin Scaffold externo: tu UI ya lo maneja internamente
    ChatGuardadosUI(
        sessions = sesiones,
        loading = loading,
        deletingIds = deletingIds.toSet(),
        sessionToDelete = sessionToDelete,
        onBack = { navController.popBackStack() },
        onHome = {
            navController.navigate("principal") {
                popUpTo(0) { inclusive = true }
            }
        },
        onProfile = { navController.navigate("perfil") },
        onOpen = { openSession(it.id) },
        onAskDelete = { requestDelete(it) },
        onConfirmDelete = { confirmDelete() },
        onDismissDelete = { sessionToDelete = null },
        dateFormatter = dateFormatter,
        snackbarHostState = snackbar
    )
}
