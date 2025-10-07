package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PantallaMensajeSalida(navController: NavController, mensaje: String = "¿Estás seguro que quieres salir?") {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(mensaje, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = { navController.popBackStack() }) { Text("Cancelar") }
            Spacer(Modifier.width(12.dp))
            Button(onClick = { navController.navigate("pantallaInicio") { popUpTo("pantallaPrincipal") { inclusive = true } } }) { Text("Salir") }
        }
    }
}
