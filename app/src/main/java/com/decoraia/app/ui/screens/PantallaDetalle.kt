package com.decoraia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

data class MensajeDetalle(
    val id: String,
    val texto: String,
    val esUsuario: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChatDetalle(navController: NavController, sessionId: String) {
    val db = FirebaseFirestore.getInstance()
    val mensajes = remember { mutableStateListOf<MensajeDetalle>() }
    var loading by remember { mutableStateOf(true) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Escucha los mensajes de esta sesión
    LaunchedEffect(sessionId) {
        db.collection("sessions").document(sessionId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, e ->
                loading = false
                if (e != null) {
                    scope.launch { snackbar.showSnackbar("Error: ${e.message}") }
                    return@addSnapshotListener
                }
                mensajes.clear()
                snap?.documents?.forEach { d ->
                    val text = d.getString("text").orEmpty()
                    val role = d.getString("role").orEmpty()
                    mensajes += MensajeDetalle(
                        id = d.id,
                        texto = text,
                        esUsuario = (role == "user")
                    )
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                mensajes.isEmpty() -> Text("Sin mensajes", Modifier.align(Alignment.Center), color = Color.Gray)
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mensajes) { m -> BurbujaDetalle(m) }
                }
            }
        }
    }
}

@Composable
private fun BurbujaDetalle(m: MensajeDetalle) {
    val color = if (m.esUsuario) Color(0xFFDCF8C6) else Color(0xFFEFEFEF)
    val alignment = if (m.esUsuario) Arrangement.End else Arrangement.Start
    val shape = if (m.esUsuario)
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 12.dp)
    else
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 0.dp)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
        Box(
            Modifier
                .clip(shape)
                .background(color)
                .padding(12.dp)
                .widthIn(max = 300.dp)
        ) { Text(m.texto) }
    }
}