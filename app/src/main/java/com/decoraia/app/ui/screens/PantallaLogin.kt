@file:Suppress("UnusedMaterial3ScaffoldPaddingParameter")

package com.decoraia.app.ui.screens

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.LoginScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaLogin(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // errores por campo y general (solo texto, sin snackbar)
    var emailError by remember { mutableStateOf<String?>(null) }
    var passError  by remember { mutableStateOf<String?>(null) }
    var authError  by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun validarCampos(): Boolean {
        emailError = when {
            email.isBlank() -> "Ingresa tu correo."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Correo no válido (ej: usuario@dominio.com)."
            else -> null
        }
        passError = when {
            password.isBlank() -> "Ingresa tu contraseña."
            password.length < 6 -> "Mínimo 6 caracteres."
            else -> null
        }
        return emailError == null && passError == null
    }

    fun traducirErrorFirebase(t: Throwable): String {
        val code = (t as? FirebaseAuthException)?.errorCode ?: ""
        val msg  = t.message?.lowercase().orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL"              -> "El correo no es válido."
            "ERROR_USER_NOT_FOUND"             -> "No existe una cuenta con ese correo."
            "ERROR_WRONG_PASSWORD"             -> "Contraseña incorrecta."
            "ERROR_USER_DISABLED"              -> "La cuenta está deshabilitada."
            "ERROR_TOO_MANY_REQUESTS"          -> "Demasiados intentos. Intenta más tarde."
            "ERROR_NETWORK_REQUEST_FAILED"     -> "Sin conexión. Revisa tu internet."
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS",
            "INVALID_LOGIN_CREDENTIALS"        -> "Las credenciales son incorrectas o han expirado."
            else -> when {
                "credential is incorrect" in msg || "credential is malformed" in msg || "has expired" in msg ->
                    "Las credenciales son incorrectas o han expirado."
                "password" in msg && "invalid" in msg -> "Contraseña incorrecta."
                "blocked" in msg && "requests" in msg -> "Demasiados intentos. Intenta más tarde."
                else -> t.message ?: "Ocurrió un error. Intenta nuevamente."
            }
        }
    }

    // Sin Scaffold/snackbar: llamamos directo a la UI
    LoginScreenUI(
        email = email,
        onEmailChange = { email = it; if (emailError != null) emailError = null; authError = null },
        password = password,
        onPasswordChange = { password = it; if (passError != null) passError = null; authError = null },
        loading = loading,
        onLoginClick = {
            authError = null
            if (!validarCampos()) {
                authError = "Revisa los campos marcados en rojo"
                return@LoginScreenUI
            }

            loading = true
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        loading = false
                        authError = "No se pudo obtener tu sesión."
                        return@addOnSuccessListener
                    }
                    db.collection("users").document(uid)
                        .update("lastLogin", FieldValue.serverTimestamp())
                        .addOnSuccessListener {
                            loading = false
                            navController.navigate("principal") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            loading = false
                            Log.e("LOGIN", "Error actualizando lastLogin: ${e.message}")
                            // Igual navegamos, pero mostramos texto en rojo (sin snackbar)
                            authError = "Sesión iniciada, pero no se pudo actualizar tu perfil."
                            navController.navigate("principal") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                }
                .addOnFailureListener { e ->
                    loading = false
                    Log.e("LOGIN", "Error login", e)
                    authError = traducirErrorFirebase(e)
                }
        },
        onRegisterClick = { navController.navigate("registro") },
        onForgotClick   = { navController.navigate("olvidocontrasena") },
        onBack          = { navController.popBackStack() },
        emailError      = emailError,
        passwordError   = passError,
        authError       = authError
    )
}
