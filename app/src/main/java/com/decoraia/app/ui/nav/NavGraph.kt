package com.decoraia.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

// --- Imports de TODAS las screens ---
import com.decoraia.app.ui.screens.PantallaAcercaDe
import com.decoraia.app.ui.screens.PantallaAjustesCuenta
import com.decoraia.app.ui.screens.PantallaCarga
import com.decoraia.app.ui.screens.PantallaChatGuardados
import com.decoraia.app.ui.screens.PantallaChatGuardadosEliminados
import com.decoraia.app.ui.screens.PantallaChatGuardadosOpciones
import com.decoraia.app.ui.screens.PantallaChatIA
import com.decoraia.app.ui.screens.PantallaConfiguracion
import com.decoraia.app.ui.screens.PantallaDescripcion
import com.decoraia.app.ui.screens.PantallaEditarPerfil
import com.decoraia.app.ui.screens.PantallaFavoritos
import com.decoraia.app.ui.screens.PantallaInicio
import com.decoraia.app.ui.screens.PantallaLogin
import com.decoraia.app.ui.screens.PantallaMensajeSalida
import com.decoraia.app.ui.screens.PantallaOlvidoContrasena
import com.decoraia.app.ui.screens.PantallaPerfil
import com.decoraia.app.ui.screens.PantallaPrincipal
import com.decoraia.app.ui.screens.PantallaRAEstilos
import com.decoraia.app.ui.screens.PantallaRAModelos
import com.decoraia.app.ui.screens.PantallaRAModelosLike
import com.decoraia.app.ui.screens.PantallaRAModelosQuitarLike
import com.decoraia.app.ui.screens.PantallaRAObjetos
import com.decoraia.app.ui.screens.PantallaRegistro
import com.decoraia.app.ui.screens.PantallaSalidaPerfil
import com.decoraia.app.ui.screens.PantallaSoporte
import com.decoraia.app.ui.screens.PantallaVisualizacion

@Composable
fun AppNavGraph(navController: NavHostController) {
    // Arranca en "principal" si hay sesión; si no, "carga"
    val auth = FirebaseAuth.getInstance()
    val start = if (auth.currentUser != null) "principal" else "carga"

    NavHost(navController = navController, startDestination = start) {

        // -------- Rutas simples --------
        composable("acercade") { PantallaAcercaDe(navController) }
        composable("ajustescuenta") { PantallaAjustesCuenta(navController) }
        composable("carga") { PantallaCarga(navController) }
        composable("chatguardados") { PantallaChatGuardados(navController) }
        composable("chatguardadoseliminados") { PantallaChatGuardadosEliminados(navController) }
        composable("chatguardadosopciones") { PantallaChatGuardadosOpciones(navController) }
        composable("chatia") { PantallaChatIA(navController) }
        composable("configuracion") { PantallaConfiguracion(navController) }
        composable("descripcion") { PantallaDescripcion(navController) }
        composable("editarperfil") { PantallaEditarPerfil(navController) }
        composable("favoritos") { PantallaFavoritos(navController) }
        composable("inicio") { PantallaInicio(navController) }
        composable("login") { PantallaLogin(navController) }
        composable("mensajesalida") { PantallaMensajeSalida(navController) }
        composable("olvidocontrasena") { PantallaOlvidoContrasena(navController) }
        composable("perfil") { PantallaPerfil(navController) }
        composable("principal") { PantallaPrincipal(navController) }
        composable("raestilos") { PantallaRAEstilos(navController) }
        composable("ramodelos") { PantallaRAModelos(navController) }
        composable("raobjetos") { PantallaRAObjetos(navController) }
        composable("registro") { PantallaRegistro(navController) }
        composable("salidaperfil") { PantallaSalidaPerfil(navController) }
        composable("soporte") { PantallaSoporte(navController) }
        composable("visualizacion") { PantallaVisualizacion(navController) }

        // -------- Rutas con argumento (modelId) --------
        composable(
            route = "ramodeloslike/{modelId}",
            arguments = listOf(navArgument("modelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val modelId = backStackEntry.arguments?.getString("modelId").orEmpty()
            PantallaRAModelosLike(navController, modelId)
        }

        composable(
            route = "ramodelosquitarlike/{modelId}",
            arguments = listOf(navArgument("modelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val modelId = backStackEntry.arguments?.getString("modelId").orEmpty()
            PantallaRAModelosQuitarLike(navController, modelId)
        }
    }
}
