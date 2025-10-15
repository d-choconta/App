package com.decoraia.app.ui.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.decoraia.app.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val start = if (auth.currentUser != null) "principal" else "carga"

    NavHost(navController = navController, startDestination = start) {

        composable("acercade") { PantallaAcercaDe(navController) }
        composable("ajustescuenta") { PantallaAjustesCuenta(navController) }
        composable("carga") { PantallaCarga(navController) }
        composable("chatguardados") { PantallaChatGuardados(navController) }
        composable("chatguardadoseliminados") { PantallaChatGuardadosEliminados(navController) }
        composable("chatguardadosopciones") { PantallaChatGuardadosOpciones(navController) }
        composable("chatia") { PantallaChatIA(navController) }
        composable("configuracion") { PantallaConfiguracion(navController) }
        composable("descripcion") { PantallaDescripcion(navController) }
        composable("editarperfil") { PantallaEditarPerfil(navController) } // ✅ deja solo una
        composable("favoritos") { PantallaFavoritos(navController) }       // ✅ existe esta ruta
        composable("inicio") { PantallaInicio(navController) }
        composable("login") { PantallaLogin(navController) }
        composable("mensajesalida") { PantallaMensajeSalida(navController) }
        composable("olvidocontrasena") { PantallaOlvidoContrasena(navController) }
        composable("perfil") { PantallaPerfil(navController) }
        composable("principal") { PantallaPrincipal(navController) }
        composable("registro") { PantallaRegistro(navController) }
        composable("salidaperfil") { PantallaSalidaPerfil(navController) }
        composable("soporte") { PantallaSoporte(navController) }
        composable("visualizacion") { PantallaVisualizacion(navController) }

        // RA Estilos (sin args)
        composable("raestilos") { PantallaRAEstilos(navController) }

        // RA Objetos (recibe estilo)
        composable(
            route = "raobjetos/{style}",
            arguments = listOf(navArgument("style") { type = NavType.StringType })
        ) { backStackEntry ->
            val style = Uri.decode(backStackEntry.arguments?.getString("style").orEmpty())
            PantallaRAObjetos(navController, style = style)
        }

        // RA Modelos (recibe estilo + categoría)
        composable(
            route = "ramodelos/{style}/{categoryId}",
            arguments = listOf(
                navArgument("style") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val style = Uri.decode(backStackEntry.arguments?.getString("style").orEmpty())
            val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
            PantallaRAModelos(navController, style = style, categoryId = categoryId)
        }

        // Visor AR opcional
        composable(
            route = "arviewer?modelUrl={modelUrl}",
            arguments = listOf(
                navArgument("modelUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            PantallaVisualizacion(navController)
        }
    }
}
