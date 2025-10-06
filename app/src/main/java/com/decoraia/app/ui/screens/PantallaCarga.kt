package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

@Composable
fun PantallaCarga(navController: NavHostController) {
    LaunchedEffect(Unit) {
        // animación / splash
        delay(1400)
        navController.navigate("inicio") {
            popUpTo("carga") { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("DecoraIA", /* styling: usa tu tema */)
    }
}
