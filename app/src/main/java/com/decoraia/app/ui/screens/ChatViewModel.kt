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
import com.decoraia.app.data.ProductoAR

// Modelo para mostrar mensajes en el chat
data class ChatMessageData(
    val id: String,
    val text: String? = null,
    val imageUri: Uri? = null,               // Imagen local (enviada por el usuario)
    val productImageUrl: String? = null,     // Imagen de un solo producto (opcional)
    val productos: List<ProductoAR>? = null, // Lista de productos del catálogo
    val isUser: Boolean
)

@Composable
fun ChatMessageUI(message: ChatMessageData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        // (1) Imagen local del usuario (Uri)
        message.imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Imagen enviada",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // (2) Imagen individual (si existe)
        message.productImageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Imagen del producto",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // (3) Varias imágenes de productos (si existen en la lista)
        message.productos?.takeIf { it.isNotEmpty() }?.forEach { producto ->
            AsyncImage(
                model = producto.imageUrl,
                contentDescription = producto.name.ifBlank { "Producto recomendado" },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // (4) Texto del mensaje
        message.text?.takeIf { it.isNotBlank() }?.let { txt ->
            val bgColor = if (message.isUser) Color(0xFF2196F3) else Color(0xFFEFE5D6)
            val textColor = if (message.isUser) Color.White else Color(0xFF2E2E2E)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .padding(10.dp)
            ) {
                Text(
                    text = txt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}
