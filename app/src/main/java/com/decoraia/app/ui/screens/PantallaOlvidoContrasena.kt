package com.decoraia.app.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.decoraia.app.ui.components.OlvidoContrasenaUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun PantallaOlvidoContrasena(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }

    var usuario by remember { mutableStateOf("") }   // visual (no se envía a Firebase)
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { _ ->
        OlvidoContrasenaUI(
            usuario = usuario,
            onUsuarioChange = { usuario = it },
            email = email,
            onEmailChange = { email = it },
            loading = loading,
            onEnviarCodigo = {
                if (email.isBlank()) {
                    scope.launch { snackbarHostState.showSnackbar("Ingresa un correo válido") }
                    return@OlvidoContrasenaUI
                }
                loading = true
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        loading = false
                        scope.launch { snackbarHostState.showSnackbar("Correo de recuperación enviado") }
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    .addOnFailureListener { e ->
                        loading = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        }
                    }
            },
            onBack = { navController.popBackStack() }
        )
    }
}
