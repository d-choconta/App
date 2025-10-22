package com.decoraia.app.ui.screens

import android.util.Patterns
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.decoraia.app.R
import com.decoraia.app.ui.components.OlvidoContrasenaUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

@Composable
fun PantallaOlvidoContrasena(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val res = LocalContext.current.resources

    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    fun validar(): Boolean {
        emailError = when {
            email.isBlank() -> res.getString(R.string.val_email_requerido)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                res.getString(R.string.val_email_invalido)
            else -> null
        }
        return emailError == null
    }

    fun traducirErrorFirebase(t: Throwable): String {
        val code = (t as? FirebaseAuthException)?.errorCode ?: ""
        val msg = t.message?.lowercase().orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL"          -> res.getString(R.string.auth_error_correo_invalido)
            "ERROR_USER_NOT_FOUND"         -> res.getString(R.string.auth_error_usuario_no_existe)
            "ERROR_TOO_MANY_REQUESTS"      -> res.getString(R.string.auth_error_demasiados_intentos)
            "ERROR_NETWORK_REQUEST_FAILED" -> res.getString(R.string.auth_error_red)
            else -> when {
                "no user record" in msg -> res.getString(R.string.auth_error_usuario_no_existe)
                "blocked" in msg && "requests" in msg -> res.getString(R.string.auth_error_demasiados_intentos)
                else -> t.message ?: res.getString(R.string.auth_error_generico)
            }
        }
    }

    OlvidoContrasenaUI(
        usuario = usuario,
        onUsuarioChange = { usuario = it },
        email = email,
        onEmailChange = {
            email = it
            emailError = null
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
                    successMsg = res.getString(R.string.pwd_reset_sent)
                }
                .addOnFailureListener { e ->
                    loading = false
                    actionError = traducirErrorFirebase(e)
                }
        },
        onBack = { navController.popBackStack() }
    )
}
