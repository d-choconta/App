package com.decoraia.app.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

/* Paleta */
private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa = Color(0xFFB2754E)

/* Modelo del mensaje */
data class ChatMessageUI(
    val id: String,
    val text: String? = null,
    val imageUri: Uri? = null,
    val isUser: Boolean
)

/* Header */
@Composable
fun ChatHeader(
    title: String,
    onBack: () -> Unit,
    onHistory: () -> Unit
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
        }

        IconButton(
            onClick = onHistory,
            modifier = Modifier
                .padding(end = 17.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = "Historial",
                tint = Color.White
            )
        }
    }
}

/* Burbuja del chat */
@Composable
private fun ChatBubble(message: ChatMessageUI) {
    val isUser = message.isUser
    val align = if (isUser) Arrangement.End else Arrangement.Start
    val bg = if (isUser) Color(0xFFFFFFFF) else Color(0xFFF6EFE6)
    val shape = if (isUser)
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 0.dp, bottomStart = 18.dp)
    else
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 0.dp)

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = align
    ) {
        Column(
            Modifier
                .widthIn(max = 290.dp)
                .clip(shape)
                .background(bg)
                .padding(10.dp)
        ) {
            message.imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                if (!message.text.isNullOrBlank()) Spacer(Modifier.height(6.dp))
            }
            if (!message.text.isNullOrBlank()) {
                Text(
                    message.text!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E2E2E)
                )
            }
        }
    }
}

/* Barra de entrada */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    loading: Boolean,
    selectedImage: Uri?,
    onAttachGallery: () -> Unit,
    onAttachCamera: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onSend: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        if (selectedImage != null) {
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImage),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
                Text("1 imagen adjunta", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onRemoveAttachment) { Text("Quitar") }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var menuOpen by remember { mutableStateOf(false) }

            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    enabled = !loading,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Terracotta, CircleShape)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Adjuntar", tint = Cocoa)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Galería") },
                        onClick = { menuOpen = false; onAttachGallery() }
                    )
                    DropdownMenuItem(
                        text = { Text("Cámara") },
                        onClick = { menuOpen = false; onAttachCamera() }
                    )
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Escribe tu mensaje...") },
                singleLine = true,
                enabled = !loading,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Cocoa
                )
            )

            FilledIconButton(
                onClick = onSend,
                enabled = (text.isNotBlank() || selectedImage != null) && !loading,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Terracotta)
            ) {
                if (loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                }
            }
        }
    }
}

/* Barra inferior */
@Composable
private fun ChatBottomBar(
    onHome: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
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
        ) { Icon(Icons.Filled.Home, contentDescription = "Inicio", tint = Color.White, modifier = Modifier.size(36.dp)) }

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
        ) { Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(36.dp)) }
    }
}

/* Pantalla principal */
@Composable
fun ChatIAScreenUI(
    messages: List<ChatMessageUI>,
    inputText: String,
    onInputChange: (String) -> Unit,
    loading: Boolean,
    selectedImage: Uri?,
    onAttachGallery: () -> Unit,
    onAttachCamera: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    onHistory: () -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(color = Cream) {
        Column(Modifier.fillMaxSize()) {
            ChatHeader(
                title = "Chat DecoraIA",
                onBack = onBack,
                onHistory = onHistory
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
                if (loading) {
                    item {
                        Text(
                            "Escribiendo…",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                item { Spacer(Modifier.height(6.dp)) }
            }

            ChatInputBar(
                text = inputText,
                onTextChange = onInputChange,
                loading = loading,
                selectedImage = selectedImage,
                onAttachGallery = onAttachGallery,
                onAttachCamera = onAttachCamera,
                onRemoveAttachment = onRemoveAttachment,
                onSend = onSend
            )

            ChatBottomBar(onHome = onHome, onProfile = onProfile)
        }
    }
}
