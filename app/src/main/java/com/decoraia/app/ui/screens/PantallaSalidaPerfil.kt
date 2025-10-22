package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decoraia.app.R

@Composable
fun PantallaSalidaPerfil(navController: NavController) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.perfil_saliste), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            navController.navigate("pantallaInicio") {
                popUpTo("pantallaPrincipal") { inclusive = true }
            }
        }) {
            Text(stringResource(R.string.volver_al_inicio))
        }
    }
}
