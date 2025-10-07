package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PantallaSoporte(navController: NavController) {
    var mensaje by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Soporte técnico", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            label = { Text("Describe tu problema") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            mensaje = ""
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Enviar mensaje")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("pantallaPrincipal") }) {
            Text("Volver")
        }
    }
}
