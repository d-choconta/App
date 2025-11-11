package com.decoraia.app.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa = Color(0xFFB2754E)

@Composable
private fun ChatHeader(
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
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White) }
        IconButton(
            onClick = onHistory,
            modifier = Modifier
                .padding(end = 17.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
                .align(Alignment.CenterEnd)
        ) { Icon(Icons.Filled.History, contentDescription = "Historial", tint = Color.White) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    loading: Boolean,
    selectedImage: Uri?,
    selectedBitmap: Bitmap?,
    onAttachGallery: () -> Unit,
    onAttachCamera: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onSend: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        if (selectedImage != null || selectedBitmap != null) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    when {
                        selectedBitmap != null ->
                            androidx.compose.foundation.Image(
                                bitmap = selectedBitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        selectedImage != null ->
                            AsyncImage(
                                model = selectedImage,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                    }
                }
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
                ) { Icon(Icons.Filled.Add, contentDescription = "Adjuntar", tint = Cocoa) }

                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Galería") }, onClick = { menuOpen = false; onAttachGallery() })
                    DropdownMenuItem(text = { Text("Cámara") }, onClick = { menuOpen = false; onAttachCamera() })
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
                enabled = (text.isNotBlank() || selectedImage != null || selectedBitmap != null) && !loading,
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

@Composable
fun ChatIAScreenUI(
    messages: List<ChatMessageUIModel>,
    listState: LazyListState,
    inputText: String,
    onInputChange: (String) -> Unit,
    loading: Boolean,
    selectedImage: Uri?,
    selectedBitmap: Bitmap?,
    onAttachGallery: () -> Unit,
    onAttachCamera: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    onHistory: () -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    onProductClick: (String?, String?) -> Unit //
) {
    Surface(color = Cream) {
        Column(Modifier.fillMaxSize()) {
            ChatHeader(title = "Chat DecoraIA", onBack = onBack, onHistory = onHistory)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val clickable = msg.productModelUrl != null

                    Box(
                        modifier =
                            if (clickable) Modifier.clickable {
                                onProductClick(msg.productImageUrl, msg.productModelUrl)
                            } else Modifier
                    ) {
                        ChatMessageUI(message = msg)
                    }
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
            }

            ChatInputBar(
                text = inputText,
                onTextChange = onInputChange,
                loading = loading,
                selectedImage = selectedImage,
                selectedBitmap = selectedBitmap,
                onAttachGallery = onAttachGallery,
                onAttachCamera = onAttachCamera,
                onRemoveAttachment = onRemoveAttachment,
                onSend = onSend
            )

            ChatBottomBar(onHome = onHome, onProfile = onProfile)
        }
    }
}
