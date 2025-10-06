package com.decoraia.app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
    val scaffoldState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(scaffoldState) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        scope.launch {
                            scaffoldState.showSnackbar("Completa todos los campos")
                        }
                        return@Button
                    }

                    loading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            loading = false
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    // ✅ actualizar lastLogin en Firestore
                                    db.collection("users").document(uid)
                                        .update("lastLogin", FieldValue.serverTimestamp())
                                        .addOnSuccessListener {
                                            navController.navigate("principal") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("LOGIN", "Error actualizando lastLogin: ${e.message}")
                                            scope.launch {
                                                scaffoldState.showSnackbar("Error actualizando perfil")
                                            }
                                        }
                                }
                            } else {
                                Log.e("LOGIN", "Error login", task.exception)
                                scope.launch {
                                    scaffoldState.showSnackbar("Error: ${task.exception?.message}")
                                }
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
                else Text("Ingresar")
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("registro") }) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
