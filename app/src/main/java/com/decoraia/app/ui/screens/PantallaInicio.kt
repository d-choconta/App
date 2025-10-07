    package com.decoraia.app.ui.screens

    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.Button
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavHostController

    @Composable
    fun PantallaInicio(navController: NavHostController) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate("login") }) {
                Text("Iniciar Sesión")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate("registro") }) {
                Text("Empecemos")
            }
        }
    }
