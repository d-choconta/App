package com.decoraia.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun PantallaVisualizacion(
    navController: NavHostController,
    modelUrl: String?
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Visualización AR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (modelUrl.isNullOrBlank()) {
            Text("No se recibió ningún modelo para visualizar.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Volver") }
            return@Column
        }

        Text("Modelo listo para AR:")
        Text(modelUrl, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val encoded = Uri.encode(modelUrl)
                // Scene Viewer – AR preferido (con fallback a 3D si ARCore no está)
                val sceneViewerUri = Uri.parse(
                    "https://arvr.google.com/scene-viewer/1.0" +
                            "?file=$encoded&mode=ar_preferred"
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = sceneViewerUri
                    // Paquete de la app de Google para mejor compatibilidad
                    setPackage("com.google.android.googlequicksearchbox")
                }

                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    // Fallback: abrir en navegador si no existe la app
                    val browserIntent = Intent(Intent.ACTION_VIEW, sceneViewerUri)
                    try {
                        context.startActivity(browserIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir el visor AR.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        ) {
            Text("Abrir en AR")
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}
