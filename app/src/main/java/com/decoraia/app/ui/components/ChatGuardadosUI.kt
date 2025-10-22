package com.decoraia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.ui.screens.SessionItem
import com.google.firebase.Timestamp

/* Paleta*/
private val Cream      = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa      = Color(0xFFB2754E)

/* Header*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatHistoryHeader(
    title: String,
    onBack: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(Cream)
            .statusBarsPadding()
    ) {
        Text(
            text = title,
            color = Cocoa,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 17.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
                .align(Alignment.CenterStart)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
        }
    }
}
@Composable
private fun BottomBar(onHome: () -> Unit, onProfile: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onHome,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
        ) {
            Icon(Icons.Filled.Home, contentDescription = "Inicio", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Box(
            Modifier
                .weight(1f)
                .height(56.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.45f))
        )

        IconButton(
            onClick = onProfile,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
        ) {
            Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun SessionCard(
    item: SessionItem,
    subtitle: String,
    isDeleting: Boolean,
    onOpen: () -> Unit,
    onAskDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isDeleting) { onOpen() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = Cocoa)
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B6B6B)
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onOpen,
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Abrir") }

                Spacer(Modifier.width(10.dp))

                OutlinedButton(
                    onClick = onAskDelete,
                    enabled = !isDeleting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Cocoa),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text(if (isDeleting) "Eliminando…" else "Eliminar")
                }

                if (isDeleting) {
                    Spacer(Modifier.width(10.dp))
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ChatGuardadosUI(
    sessions: List<SessionItem>,
    loading: Boolean,
    deletingIds: Set<String>,
    sessionToDelete: SessionItem?,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    onOpen: (SessionItem) -> Unit,
    onAskDelete: (SessionItem) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    dateFormatter: (Timestamp?) -> String,
    snackbarHostState: SnackbarHostState
) {
    Surface(color = Cream) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { ChatHistoryHeader(title = "Historial de chats", onBack = onBack) },
            containerColor = Color.Transparent
        ) { padding ->

            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when {
                    loading -> Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    sessions.isEmpty() -> Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) { Text("No hay chats aún", color = Color(0xFF6B6B6B)) }

                    else -> LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sessions, key = { it.id }) { s ->
                            val isDeleting = deletingIds.contains(s.id)
                            SessionCard(
                                item = s,
                                subtitle = dateFormatter(s.lastMessageAt),
                                isDeleting = isDeleting,
                                onOpen = { onOpen(s) },
                                onAskDelete = { onAskDelete(s) }
                            )
                        }
                    }
                }

                BottomBar(onHome = onHome, onProfile = onProfile)
            }
        }
    }

    // Diálogo de confirmación
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Eliminar chat") },
            text = { Text("Esta acción borrará el chat y todos sus mensajes. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text("Cancelar") }
            }
            )
        }
}