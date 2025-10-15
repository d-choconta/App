package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.decoraia.app.data.RAProductsRepo
import com.decoraia.app.ui.components.RAObjetosScreenUI

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
            val encodedStyle = Uri.encode(style)
            nav.navigate("ramodelos/$encodedStyle/${cat.id}")
        },
        onHome = { nav.navigate("principal") { popUpTo("principal") { inclusive = true } } },
        onProfile = { nav.navigate("perfil") }
    )
}
