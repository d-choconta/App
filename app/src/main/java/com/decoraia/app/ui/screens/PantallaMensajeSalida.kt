package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decoraia.app.R

@Composable
fun PantallaMensajeSalida(
    navController: NavController,
    mensaje: String = stringResource(R.string.exit_confirm_message)
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(mensaje, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = { navController.popBackStack() }) { Text(stringResource(R.string.action_cancel)) }
            Spacer(Modifier.width(12.dp))
            Button(onClick = {
                navController.navigate("pantallaInicio") {
                    popUpTo("pantallaPrincipal") { inclusive = true }
                }
            }) { Text(stringResource(R.string.action_exit)) }
        }
    }
}
