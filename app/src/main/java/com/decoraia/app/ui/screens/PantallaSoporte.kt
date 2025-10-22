package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decoraia.app.R

@Composable
fun PantallaSoporte(navController: NavController) {
    var mensaje by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.soporte_titulo), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            label = { Text(stringResource(R.string.soporte_describe_problema)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { mensaje = "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.soporte_enviar_mensaje))
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("pantallaPrincipal") }) {
            Text(stringResource(R.string.action_back))
        }
    }
}

