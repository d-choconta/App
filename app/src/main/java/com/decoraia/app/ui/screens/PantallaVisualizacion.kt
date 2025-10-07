package com.decoraia.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.google.firebase.storage.FirebaseStorage
import coil.compose.rememberAsyncImagePainter

@Composable
fun PantallaVisualizacion(navController: NavController, storagePath: String? = null) {
    // visualiza una imagen/modelo desde Firebase Storage
    var url by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(storagePath) {
        if (!storagePath.isNullOrBlank()) {
            FirebaseStorage.getInstance().reference.child(storagePath).downloadUrl
                .addOnSuccessListener { uri -> url = uri.toString() }
        }
    }

    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        if (url != null) {
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Visualización",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("Seleccione un elemento para ver", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
