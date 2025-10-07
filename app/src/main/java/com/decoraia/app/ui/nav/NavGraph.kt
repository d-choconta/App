
package com.decoraia.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.decoraia.app.ui.screens.PantallaCarga
import com.decoraia.app.ui.screens.PantallaChatIA
import com.decoraia.app.ui.screens.PantallaEditarPerfil
import com.decoraia.app.ui.screens.PantallaInicio
import com.decoraia.app.ui.screens.PantallaLogin
import com.decoraia.app.ui.screens.PantallaPerfil
import com.decoraia.app.ui.screens.PantallaRegistro
import com.decoraia.app.ui.screens.PantallaPrincipal
import com.decoraia.app.ui.screens.PantallaRAEstilos
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val start = if (auth.currentUser != null) "principal" else "carga"

    NavHost(navController = navController, startDestination = start) {
        composable("carga") { PantallaCarga(navController) }
        composable("inicio") { PantallaInicio(navController) }
        composable("login") { PantallaLogin(navController) }
        composable("registro") { PantallaRegistro(navController) }
        composable("principal") { PantallaPrincipal(navController) }
        composable("chatia") { PantallaChatIA(navController) }
        composable("perfil") { PantallaPerfil(navController) }
        composable("raestilos") { PantallaRAEstilos(navController) }
        composable("editarperfil") { PantallaEditarPerfil(navController) }
    }
}



