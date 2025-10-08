package com.decoraia.app.ui.screens

import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.LoginScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        // UI (sin lógica) tomada del componente
        LoginScreenUI(
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            loading = loading,
            onLoginClick = {
                if (email.isBlank() || password.isBlank()) {
                    scope.launch { snackbar.showSnackbar("Completa todos los campos") }
                    return@LoginScreenUI
                }
                loading = true
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        loading = false
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                db.collection("users").document(uid)
                                    .update("lastLogin", FieldValue.serverTimestamp())
                                    .addOnSuccessListener {
                                        navController.navigate("principal") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("LOGIN", "Error actualizando lastLogin: ${e.message}")
                                        scope.launch { snackbar.showSnackbar("Error actualizando perfil") }
                                    }
                            }
                        } else {
                            Log.e("LOGIN", "Error login", task.exception)
                            scope.launch {
                                snackbar.showSnackbar("Error: ${task.exception?.message}")
                            }
                        }
                    }
            },
            onRegisterClick = { navController.navigate("registro") },
            onForgotClick = { /* navController.navigate("recuperar") */ }
            )
        }
}