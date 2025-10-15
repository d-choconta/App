package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.RAEstilosScreenUI

@Composable
fun PantallaRAEstilos(nav: NavHostController) {
    val estilos = remember {
        listOf("Clásico", "Mediterráneo", "Minimalista", "Industrial")
    }

    RAEstilosScreenUI(
        estilos = estilos,
        onBack = { nav.popBackStack() },
        onSelectStyle = { estilo ->
            val encoded = Uri.encode(estilo)
            nav.navigate("raobjetos/$encoded")
        },
        onHome = { nav.navigate("principal") { popUpTo("principal") { inclusive = true } } },
        onProfile = { nav.navigate("perfil") }
    )
}
