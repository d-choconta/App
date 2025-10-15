@file:Suppress("UnusedMaterial3ScaffoldPaddingParameter")

package com.decoraia.app.ui.screens

import android.util.Patterns
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.RegistroScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun PantallaRegistro(navController: NavHostController) {
    // Campos
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // Errores por campo + mensaje general (bajo el botón)
    var nombreError  by remember { mutableStateOf<String?>(null) }
    var emailError   by remember { mutableStateOf<String?>(null) }
    var passError    by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var actionError  by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val db   = FirebaseFirestore.getInstance()

    fun validar(): Boolean {
        nombreError = when {
            nombre.isBlank()      -> "Ingresa tu nombre."
            nombre.length < 2     -> "El nombre es muy corto."
            else                  -> null
        }
        emailError = when {
            email.isBlank() -> "Ingresa tu correo."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Correo no válido (ej: usuario@dominio.com)."
            else -> null
        }
        passError = when {
            password.isBlank()  -> "Ingresa una contraseña."
            password.length < 6 -> "Mínimo 6 caracteres."
            else                -> null
        }
        confirmError = when {
            confirm.isBlank()   -> "Repite la contraseña."
            confirm != password -> "Las contraseñas no coinciden."
            else                -> null
        }
        return listOf(nombreError, emailError, passError, confirmError).all { it == null }
    }

    fun traducirErrorFirebase(t: Throwable): String {
        val code = (t as? FirebaseAuthException)?.errorCode ?: ""
        return when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE"   -> "Ese correo ya está registrado."
            "ERROR_INVALID_EMAIL"          -> "El correo no es válido."
            "ERROR_OPERATION_NOT_ALLOWED"  -> "El registro está deshabilitado."
            "ERROR_WEAK_PASSWORD"          -> "La contraseña es muy débil."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Sin conexión. Revisa tu internet."
            else -> t.message ?: "Ocurrió un error. Intenta nuevamente."
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
                        actionError = "No se pudo crear la cuenta."
                        return@addOnSuccessListener
                    }

                    // Display name
                    val profile = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre.trim())
                        .build()
                    user.updateProfile(profile)

                    // Documento en Firestore
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
                            actionError = "Cuenta creada, pero no se pudo guardar tu perfil: ${e.message}"
                            // Puedes navegar igual si lo deseas:
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
        // Errores para mostrar en la UI
        nombreError = nombreError,
        emailError = emailError,
        passwordError = passError,
        confirmError = confirmError,
        actionError = actionError
    )
}
