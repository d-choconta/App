package com.decoraia.app.ui.screens

import androidx.compose.runtime.Composable
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
