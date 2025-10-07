package com.decoraia.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.decoraia.app.ui.screens.PantallaInicio
import com.decoraia.app.ui.screens.PantallaLogin
import com.decoraia.app.ui.screens.PantallaRegistro

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Inicio.route
    ) {
        composable(Route.Inicio.route)   { PantallaInicio(navController) }
        composable(Route.Login.route)    { PantallaLogin(navController) }
        composable(Route.Registro.route) { PantallaRegistro(navController) }
    }
}
