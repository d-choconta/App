package com.decoraia.app.ui.screens

import android.util.Patterns
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.decoraia.app.ui.components.OlvidoContrasenaUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

@Composable
fun PantallaOlvidoContrasena(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }

    var usuario by remember { mutableStateOf("") }  // decorativo
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // mensajes en pantalla
    var emailError by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    fun validar(): Boolean {
        emailError = when {
            email.isBlank() -> "Ingresa tu correo."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Correo no válido (ej: usuario@dominio.com)."
            else -> null
        }
        return emailError == null
    }

    fun traducirErrorFirebase(t: Throwable): String {
        val code = (t as? FirebaseAuthException)?.errorCode ?: ""
        val msg  = t.message?.lowercase().orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL"          -> "El correo no es válido."
            "ERROR_USER_NOT_FOUND"         -> "No existe una cuenta con ese correo."
            "ERROR_TOO_MANY_REQUESTS"      -> "Demasiados intentos. Intenta más tarde."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Sin conexión. Revisa tu internet."
            else -> when {
                "no user record" in msg      -> "No existe una cuenta con ese correo."
                "blocked" in msg && "requests" in msg -> "Demasiados intentos. Intenta más tarde."
                else -> t.message ?: "Ocurrió un error. Intenta nuevamente."
            }
        }
    }

    OlvidoContrasenaUI(
        usuario = usuario,
        onUsuarioChange = { usuario = it },
        email = email,
        onEmailChange = {
            email = it
            if (emailError != null) emailError = null
            actionError = null
            successMsg = null
        },
        loading = loading,
        emailError = emailError,
        actionError = actionError,
        successMessage = successMsg,
        onEnviarCodigo = {
            actionError = null
            successMsg = null
            if (!validar()) return@OlvidoContrasenaUI

            loading = true
            auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener {
                    loading = false
                    successMsg = "Te enviamos un correo para restablecer tu contraseña."
                }
                .addOnFailureListener { e ->
                    loading = false
                    actionError = traducirErrorFirebase(e)
                }
        },
        onBack = { navController.popBackStack() }
    )
}
