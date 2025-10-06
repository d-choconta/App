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
fun PantallaRegistro(navController: NavHostController) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
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
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
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
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
                        scope.launch {
                            scaffoldState.showSnackbar("Completa todos los campos")
                        }
                        return@Button
                    }
                    if (password != confirm) {
                        scope.launch {
                            scaffoldState.showSnackbar("Las contraseñas no coinciden")
                        }
                        return@Button
                    }

                    loading = true
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            loading = false
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val uid = user?.uid
                                if (uid != null) {
                                    val userDoc = mapOf(
                                        "name" to (if (nombre.isBlank()) "" else nombre),
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
                                                scaffoldState.showSnackbar("Error guardando perfil")
                                            }
                                        }
                                }
                            } else {
                                Log.e("REGISTER", "Error crear user", task.exception)
                                scope.launch {
                                    scaffoldState.showSnackbar("Error: ${task.exception?.message}")
                                }
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
                else Text("Registrar")
            }
        }
    }
}
