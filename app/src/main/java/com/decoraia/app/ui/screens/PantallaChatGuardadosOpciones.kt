package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PantallaChatGuardadosOpciones(navController: NavController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Opciones de chats guardados", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { /* compartir todos */ }) { Text("Compartir") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* exportar */ }) { Text("Exportar") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.navigate("pantallaChatGuardados") }) { Text("Volver") }
    }
}
