package com.decoraia.app.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

// --- 1. DATA CLASS (para la lista de mensajes) ---
data class ChatMessageData(
    val id: String,
    val text: String? = null,
    val imageUri: Uri? = null,
    val isUser: Boolean
)

// --- 2. COMPOSABLE (para pintar cada mensaje en pantalla) ---
@Composable
fun ChatMessageUI(message: ChatMessageData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        message.imageUri?.let {
            AsyncImage(
                model = it,
                contentDescription = "Imagen del mensaje",
                modifier = Modifier
                    .sizeIn(maxWidth = 240.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        message.text?.takeIf { it.isNotBlank() }?.let {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (message.isUser) Color(0xFF2196F3)
                        else Color(0xFFE0E0E0)
                    )
                    .padding(10.dp)
            ) {
                Text(
                    text = it,
                    color = if (message.isUser) Color.White else Color.Black
                )
            }
        }
    }
}
