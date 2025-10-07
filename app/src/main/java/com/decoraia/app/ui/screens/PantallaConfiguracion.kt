package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PantallaConfiguracion(navController: NavController) {
    var notificaciones by remember { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Configuración", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notificaciones")
            Switch(checked = notificaciones, onCheckedChange = { notificaciones = it })
        }

        Spacer(Modifier.height(32.dp))
        Button(onClick = { navController.navigate("pantallaPrincipal") }) {
            Text("Volver")
        }
    }
}
