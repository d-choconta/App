@file:Suppress("UnusedMaterial3ScaffoldPaddingParameter")

package com.decoraia.app.ui.screens

import android.util.Patterns
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.LoginScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.decoraia.app.R

@Composable
fun PantallaLogin(navController: NavHostController) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passError  by remember { mutableStateOf<String?>(null) }
    var authError  by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val db   = FirebaseFirestore.getInstance()
    val res  = LocalContext.current.resources

    fun traducirErrorFirebase(t: Throwable): String {
        val code = (t as? FirebaseAuthException)?.errorCode ?: ""
        val msg  = t.message?.lowercase().orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL"          -> res.getString(R.string.auth_error_correo_invalido)
            "ERROR_USER_NOT_FOUND"         -> res.getString(R.string.auth_error_usuario_no_existe)
            "ERROR_WRONG_PASSWORD"         -> res.getString(R.string.auth_error_contrasena_incorrecta)
            "ERROR_USER_DISABLED"          -> res.getString(R.string.auth_error_usuario_deshabilitado)
            "ERROR_TOO_MANY_REQUESTS"      -> res.getString(R.string.auth_error_demasiados_intentos)
            "ERROR_NETWORK_REQUEST_FAILED" -> res.getString(R.string.auth_error_red)
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS",
            "INVALID_LOGIN_CREDENTIALS"    -> res.getString(R.string.auth_error_credenciales)
            else -> when {
                "credential is incorrect" in msg || "credential is malformed" in msg || "has expired" in msg ->
                    res.getString(R.string.auth_error_credenciales)
                "password" in msg && "invalid" in msg ->
                    res.getString(R.string.auth_error_contrasena_incorrecta)
                "blocked" in msg && "requests" in msg ->
                    res.getString(R.string.auth_error_demasiados_intentos)
                else -> t.message ?: res.getString(R.string.auth_error_generico)
            }
        }
    }

    fun validarCampos(): Boolean {
        emailError = when {
            email.isBlank() -> res.getString(R.string.val_email_requerido)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                res.getString(R.string.val_email_invalido)
            else -> null
        }
        passError = when {
            password.isBlank()  -> res.getString(R.string.val_pass_requerida)
            password.length < 6 -> res.getString(R.string.val_pass_min)
            else -> null
        }
        return emailError == null && passError == null
    }

    LoginScreenUI(
        email = email,
        onEmailChange = {
            email = it
            if (emailError != null) emailError = null
            if (authError != null) authError = null
        },
        password = password,
        onPasswordChange = {
            password = it
            if (passError != null) passError = null
            if (authError != null) authError = null
        },
        loading = loading,
        onLoginClick = {
            authError = null
            if (!validarCampos()) {
                authError = res.getString(R.string.app_revisa_campos)
                return@LoginScreenUI
            }

            loading = true
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        loading = false
                        authError = res.getString(R.string.app_sin_sesion)
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
                            authError = res.getString(R.string.auth_login_ok_perfil_error)
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