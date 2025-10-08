@file:Suppress("UnusedMaterial3ScaffoldPaddingParameter")

package com.decoraia.app.ui.screens

import android.util.Log
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.RegistroScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PantallaRegistro(navController: NavHostController) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { _ ->
        RegistroScreenUI(
            nombre = nombre,
            onNombreChange = { nombre = it },
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            confirm = confirm,
            onConfirmChange = { confirm = it },
            loading = loading,

            onBack = { navController.popBackStack() },


            onHaveAccountClick = { navController.popBackStack() },

            // 📝 Registrar
            onRegisterClick = {
                if (nombre.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
                    scope.launch { snackbar.showSnackbar("Completa todos los campos") }
                    return@RegistroScreenUI
                }
                if (password != confirm) {
                    scope.launch { snackbar.showSnackbar("Las contraseñas no coinciden") }
                    return@RegistroScreenUI
                }

                loading = true
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        loading = false
                        if (task.isSuccessful) {
                            auth.currentUser?.uid?.let { uid ->
                                val userDoc = mapOf(
                                    "name" to nombre.trim(),
                                    "email" to email.trim(),
                                    "phone" to "",
                                    "country" to "",
                                    "photoUrl" to "",
                                    "createdAt" to FieldValue.serverTimestamp(),
                                    "lastLogin" to FieldValue.serverTimestamp()
                                )
                                db.collection("users").document(uid).set(userDoc)
                                    .addOnSuccessListener {
                                        navController.navigate("principal") {
                                            popUpTo("registro") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("REGISTER", "Error guardando user: ${e.message}")
                                        scope.launch {
                                            snackbar.showSnackbar("Error guardando perfil")
                                        }
                                    }
                            }
                        } else {
                            Log.e("REGISTER", "Error crear user", task.exception)
                            scope.launch {
                                snackbar.showSnackbar("Error: ${task.exception?.message}")
                            }
                        }
                    }
            }
        )
    }
}
