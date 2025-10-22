package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decoraia.app.R

@Composable
fun PantallaVisualizacion(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.visualizacion_titulo), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.action_back))
        }
    }
}
