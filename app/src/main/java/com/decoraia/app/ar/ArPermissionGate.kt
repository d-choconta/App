package com.decoraia.app.ar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp                       // ✅ faltaba

@Composable
fun ArPermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var granted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> granted = isGranted }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if (granted) {
        content()
    } else {
        Column(modifier = modifier.padding(24.dp)) {
            Text("Se requiere permiso de cámara para usar Realidad Aumentada.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("Conceder permiso")
            }
        }
    }
}
