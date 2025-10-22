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
fun PantallaAjustesCuenta(navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.title_account_settings), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(stringResource(R.string.lbl_nombre)) }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text(stringResource(R.string.lbl_telefono)) }
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("pantallaPerfil") }) {
            Text(stringResource(R.string.action_save))
        }
    }
}
