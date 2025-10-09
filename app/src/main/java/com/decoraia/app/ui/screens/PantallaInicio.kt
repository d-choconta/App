package com.decoraia.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.InicioScreenUI

@Composable
fun PantallaInicio(navController: NavHostController) {
    InicioScreenUI(
        onLoginClick    = { navController.navigate("login") },
        onRegistroClick = { navController.navigate("registro") }
    )
}
