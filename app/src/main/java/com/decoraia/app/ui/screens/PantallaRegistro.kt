@file:Suppress("UnusedMaterial3ScaffoldPaddingParameter")

package com.decoraia.app.ui.screens

import android.util.Patterns
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.decoraia.app.R
import com.decoraia.app.ui.components.RegistroScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun PantallaRegistro(navController: NavHostController) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var nombreError  by remember { mutableStateOf<String?>(null) }
    var emailError   by remember { mutableStateOf<String?>(null) }
    var passError    by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var actionError  by remember { mutableStateOf<String?>(null) }

    val res  = LocalContext.current.resources
    val auth = FirebaseAuth.getInstance()
    val db   = FirebaseFirestore.getInstance()

    fun validar(): Boolean {
        nombreError = when {
            nombre.isBlank()  -> res.getString(R.string.val_nombre_requerido)
            nombre.length < 2 -> res.getString(R.string.val_nombre_corto)
            else              -> null
        }
        emailError = when {
            email.isBlank() -> res.getString(R.string.val_email_requerido)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                res.getString(R.string.val_email_invalido)
            else -> null
        }
        passError = when {
            password.isBlank()  -> res.getString(R.string.val_pass_requerida)
            password.length < 6 -> res.getString(R.string.val_pass_min)
            else                -> null
        }
        confirmError = when {
            confirm.isBlank()   -> res.getString(R.string.val_confirm_requerida)
            confirm != password -> res.getString(R.string.val_pass_no_coincide)
            else                -> null
        }
        return listOf(nombreError, emailError, passError, confirmError).all { it == null }
    }

    fun traducirErrorFirebase(t: Throwable): String {
        val code = (t as? FirebaseAuthException)?.errorCode ?: ""
        return when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE"   -> res.getString(R.string.reg_error_email_en_uso)
            "ERROR_INVALID_EMAIL"          -> res.getString(R.string.auth_error_correo_invalido)
            "ERROR_OPERATION_NOT_ALLOWED"  -> res.getString(R.string.reg_error_operacion_no_permitida)
            "ERROR_WEAK_PASSWORD"          -> res.getString(R.string.reg_error_contrasena_debil)
            "ERROR_NETWORK_REQUEST_FAILED" -> res.getString(R.string.auth_error_red)
            else -> t.message ?: res.getString(R.string.auth_error_generico)
        }
    }

    RegistroScreenUI(
        nombre = nombre,
        onNombreChange = { nombre = it; nombreError = null; actionError = null },
        email = email,
        onEmailChange = { email = it; emailError = null; actionError = null },
        password = password,
        onPasswordChange = { password = it; passError = null; actionError = null },
        confirm = confirm,
        onConfirmChange = { confirm = it; confirmError = null; actionError = null },
        loading = loading,
        onRegisterClick = {
            actionError = null
            if (!validar()) return@RegistroScreenUI

            loading = true
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user == null) {
                        loading = false
                        actionError = res.getString(R.string.reg_error_usuario_nulo)
                        return@addOnSuccessListener
                    }

                    val profile = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre.trim())
                        .build()
                    user.updateProfile(profile)

                    val data = mapOf(
                        "name"      to nombre.trim(),
                        "email"     to email.trim(),
                        "phone"     to "",
                        "country"   to "",
                        "photoUrl"  to "",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "lastLogin" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(user.uid)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            loading = false
                            navController.navigate("principal") {
                                popUpTo("registro") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            loading = false
                            actionError = res.getString(
                                R.string.reg_cuenta_creada_pero_perfil_error,
                                e.message ?: "—"
                            )
                            navController.navigate("principal") {
                                popUpTo("registro") { inclusive = true }
                            }
                        }
                }
                .addOnFailureListener { e ->
                    loading = false
                    actionError = traducirErrorFirebase(e)
                }
        },
        onBack = { navController.popBackStack() },
        onHaveAccountClick = {
            navController.navigate("login") {
                popUpTo("registro") { inclusive = true }
            }
        },
        nombreError = nombreError,
        emailError = emailError,
        passwordError = passError,
        confirmError = confirmError,
        actionError = actionError
    )
}
