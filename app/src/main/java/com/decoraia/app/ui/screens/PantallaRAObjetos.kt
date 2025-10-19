package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.decoraia.app.data.RAProductsRepo
import com.decoraia.app.ui.components.RAObjetosScreenUI

// Helper: decide la ruta según la categoría
private fun routeForCategory(style: String, categoryId: String): String {
    val s = Uri.encode(style)
    val id = categoryId.lowercase()

    return when (id) {
        // Sofá
        "sofa", "sofá", "sofas", "sofás" -> "sofa/$s"

        // Cuadros
        "cuadros", "cuadro" -> "cuadros/$s"

        // Jarrones
        "jarrones", "jarron", "jarrón" -> "jarrones/$s"

        // Lámparas
        "lamparas", "lámparas", "lampara", "lámpara" -> "lamparas/$s"

        // Resto: usa tu pantalla genérica de modelos
        else -> "ramodelos/$s/$categoryId"
    }
}

@Composable
fun PantallaRAObjetos(
    nav: NavHostController,
    style: String
) {
    val categorias = remember { RAProductsRepo.categoriasFijas }

    RAObjetosScreenUI(
        styleTitle = style,
        categorias = categorias,
        onBack = { nav.popBackStack() },
        onSelectCategoria = { cat ->
            val route = routeForCategory(style, cat.id)
            println("RAObjetos -> click id='${cat.id}' | style='$style' | route='$route'")
            nav.navigate(route)
        },
        onHome = { nav.navigate("principal") { popUpTo("principal") { inclusive = true } } },
        onProfile = { nav.navigate("perfil") }
    )
}
