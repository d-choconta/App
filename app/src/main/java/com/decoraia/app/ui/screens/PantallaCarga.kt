package com.decoraia.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.decoraia.app.R
import com.decoraia.app.ui.components.CargaScreenUI

@Composable
fun PantallaCarga(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1400)
        navController.navigate("inicio") {
            popUpTo("carga") { inclusive = true }
        }
    }

    CargaScreenUI(
        appName = stringResource(R.string.app_name),
        logoRes = R.drawable.logodecoraia
    )
}
