package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.PrincipalScreenUI
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PantallaPrincipal(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()

    PrincipalScreenUI(
        onGoIA = { navController.navigate("chatia") },
        onGoRA = { navController.navigate("raestilos") },
        onGoPerfil = { navController.navigate("perfil") },
        onLogout = {
            auth.signOut()
            navController.navigate("inicio") {
                popUpTo("principal") { inclusive = true }
            }
        },
    )
}
