package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PantallaPrincipal(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("chatia") }) {
            Text("Ir a IA (Chat)")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.navigate("raestilos") }) {
            Text("Ir a RA (Estilos)")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.navigate("perfil") }) {
            Text("Ver Perfil")
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            auth.signOut()
            navController.navigate("inicio") {
                popUpTo("principal") { inclusive = true }
            }
        }) {
            Text("Cerrar sesión")
            }
        }
}