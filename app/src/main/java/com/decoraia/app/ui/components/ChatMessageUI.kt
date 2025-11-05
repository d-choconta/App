package com.decoraia.app.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class ChatMessageUIModel(
    val id: String,
    val text: String? = null,
    val imageUri: Uri? = null,            // imagen del usuario (Storage o content://)
    val productImageUrl: String? = null,  // mostrar 1 imagen de producto
    val isUser: Boolean
)

@Composable
fun ChatMessageUI(message: ChatMessageUIModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        // Imagen (usuario o IA)
        message.imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Imagen adjunta",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.height(6.dp))
        }

        message.productImageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Producto recomendado",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.height(6.dp))
        }

        // Texto
        message.text?.takeIf { it.isNotBlank() }?.let { txt ->
            val bg = if (message.isUser) Color(0xFF2196F3) else Color(0xFFEFE5D6)
            val fg = if (message.isUser) Color.White else Color(0xFF2E2E2E)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .padding(10.dp)
            ) {
                Text(txt, style = MaterialTheme.typography.bodyMedium, color = fg)
            }
        }
    }
}
