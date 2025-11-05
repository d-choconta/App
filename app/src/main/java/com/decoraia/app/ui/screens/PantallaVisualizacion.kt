package com.decoraia.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun PantallaVisualizacion(
    navController: NavController,
    modelUrlArg: String?
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(modelUrlArg) {
        try {
            val httpsUrl = modelUrlArg?.trim().orEmpty()
            if (httpsUrl.isBlank()) {
                error = "No hay URL del modelo."
                return@LaunchedEffect
            }

            android.util.Log.d("AR_FLOW", "Visualizacion - url recibida: $httpsUrl")

            val sceneViewerUri = Uri.parse("https://arvr.google.com/scene-viewer/1.0")
                .buildUpon()
                .appendQueryParameter("file", httpsUrl)
                .appendQueryParameter("mode", "ar_preferred")
                .build()

            // Log final
            android.util.Log.d("AR_FLOW", "Visualizacion - sceneViewerUri: $sceneViewerUri")

            val intent = Intent(Intent.ACTION_VIEW, sceneViewerUri).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            error = e.message ?: "No se pudo abrir el visor AR."
        } finally {
            navController.popBackStack()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            error != null -> Text(error!!, Modifier.align(Alignment.Center))
            else -> CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

    BackHandler { navController.popBackStack() }
}
