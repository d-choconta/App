package com.decoraia.app.ui.screens

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.decoraia.app.ui.components.PantallaEditarPerfilUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

@Composable
fun PantallaEditarPerfil(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var nombre by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Carga inicial
    LaunchedEffect(uid) {
        cargando = true
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { snap ->
                    nombre = snap.getString("nombre") ?: (auth.currentUser?.displayName ?: "")
                    celular = snap.getString("celular") ?: ""
                    pais = snap.getString("pais") ?: ""
                }
                .addOnFailureListener {
                    scope.launch { snackbar.showSnackbar("No se pudieron cargar tus datos") }
                }
                .addOnCompleteListener { cargando = false }
        } else {
            cargando = false
            scope.launch { snackbar.showSnackbar("No hay sesión activa") }
        }
    }

    fun guardar() {
        val currentUser = auth.currentUser ?: run {
            scope.launch { snackbar.showSnackbar("No hay sesión activa") }
            return
        }
        val id = uid ?: return

        cargando = true
        val data = mapOf("nombre" to nombre, "celular" to celular, "pais" to pais)

        db.collection("users").document(id)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                val profile = UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre.ifBlank { null })
                    .build()
                currentUser.updateProfile(profile).addOnCompleteListener {
                    if (nuevaPassword.isNotBlank()) {
                        currentUser.updatePassword(nuevaPassword)
                            .addOnSuccessListener {
                                scope.launch { snackbar.showSnackbar("Perfil y contraseña actualizados") }
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                scope.launch {
                                    snackbar.showSnackbar("Perfil guardado. Error al cambiar contraseña: ${e.message}")
                                }
                            }
                            .addOnCompleteListener { cargando = false }
                    } else {
                        cargando = false
                        scope.launch { snackbar.showSnackbar("Perfil actualizado") }
                        navController.popBackStack()
                    }
                }
            }
            .addOnFailureListener { e ->
                cargando = false
                scope.launch { snackbar.showSnackbar("Error al guardar: ${e.message}") }
            }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { _ ->
        PantallaEditarPerfilUI(
            nombre = nombre,      onNombre = { nombre = it },
            celular = celular,    onCelular = { celular = it },
            pais = pais,          onPais = { pais = it },
            password = nuevaPassword, onPassword = { nuevaPassword = it },
            loading = cargando,
            onGuardar = { guardar() },
            onBack = { navController.popBackStack() },
            onHome = {
                navController.navigate("principal") {
                    popUpTo(0) { inclusive = true }
                }
            }
            )
        }
}