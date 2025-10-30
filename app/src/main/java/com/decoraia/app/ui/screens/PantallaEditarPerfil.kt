package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val db   = remember { FirebaseFirestore.getInstance() }
    val uid  = auth.currentUser?.uid

    var nombre by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    // Foto de perfil
    var photoUrl by remember { mutableStateOf("") }

    // Diálogo para URL
    var showUrlDialog by remember { mutableStateOf(false) }
    var tempUrl by remember { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ====== Colores locales (misma paleta que la UI) ======
    val Cream       = Color(0xFFFBF3E3)
    val CreamDark   = Color(0xFFF2E7D3)
    val Terracotta  = Color(0xFFE1A172)
    val Cocoa       = Color(0xFFB2754E)
    val Graphite    = Color(0xFF2D2A26)

    LaunchedEffect(uid) {
        cargando = true
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { snap ->
                    nombre   = snap.getString("nombre") ?: (auth.currentUser?.displayName ?: "")
                    celular  = snap.getString("celular") ?: ""
                    pais     = snap.getString("pais")    ?: ""
                    photoUrl = snap.getString("photoUrl")
                        ?: (auth.currentUser?.photoUrl?.toString().orEmpty())
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

    fun guardarFotoPorUrl(url: String) {
        val currentUser = auth.currentUser ?: return
        val id = uid ?: return

        val profile = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(url))
            .build()
        currentUser.updateProfile(profile)

        db.collection("users").document(id)
            .set(mapOf("photoUrl" to url), SetOptions.merge())
            .addOnSuccessListener {
                photoUrl = url
                scope.launch { snackbar.showSnackbar("Foto de perfil actualizada") }
            }
            .addOnFailureListener { e ->
                scope.launch { snackbar.showSnackbar("Error al guardar foto: ${e.message}") }
            }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { _ ->
        PantallaEditarPerfilUI(
            nombre = nombre, onNombre = { nombre = it },
            celular = celular, onCelular = { celular = it },
            pais = pais, onPais = { pais = it },
            password = nuevaPassword, onPassword = { nuevaPassword = it },
            loading = cargando,
            onGuardar = { guardar() },
            onBack = { navController.popBackStack() },
            onHome = {
                navController.navigate("principal") {
                    popUpTo(0) { inclusive = true }
                }
            },
            photoUrl = photoUrl,
            onChangePhotoClick = {
                tempUrl = photoUrl
                showUrlDialog = true
            }
        )
    }

    // ===== Diálogo de URL con estilos de la app =====
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Cambiar foto de perfil", color = Graphite) },
            text = {
                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    label = { Text("URL de imagen (https://…)", color = Cocoa) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = Terracotta.copy(alpha = .85f),
                        cursorColor = Cocoa,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val url = tempUrl.trim()
                    if (url.startsWith("http")) {
                        guardarFotoPorUrl(url)
                    } else {
                        scope.launch { snackbar.showSnackbar("URL inválida") }
                    }
                    showUrlDialog = false
                }) { Text("Guardar", color = Terracotta) }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text("Cancelar", color = Cocoa)
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = CreamDark,
            iconContentColor = Graphite,
            titleContentColor = Graphite,
            textContentColor = Graphite
        )
    }
}
