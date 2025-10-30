package com.decoraia.app.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.decoraia.app.ui.components.PantallaPerfilUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaPerfil(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db   = remember { FirebaseFirestore.getInstance() }
    val uid  = auth.currentUser?.uid

    var nombre   by remember { mutableStateOf(auth.currentUser?.displayName ?: "") }
    var email    by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var celular  by remember { mutableStateOf("") }
    var pais     by remember { mutableStateOf("") }
    var photoUrl by remember {
        mutableStateOf(auth.currentUser?.photoUrl?.toString().orEmpty())
    }

    // Escucha cambios en Firestore y actualiza datos/URL
    DisposableEffect(uid) {
        if (uid == null) {
            onDispose { }
        } else {
            val reg = db.collection("users").document(uid)
                .addSnapshotListener { snap, _ ->
                    if (snap != null && snap.exists()) {
                        nombre   = snap.getString("nombre")   ?: nombre
                        celular  = snap.getString("celular")  ?: ""
                        pais     = snap.getString("pais")     ?: ""
                        photoUrl = snap.getString("photoUrl") ?: photoUrl
                    }
                }
            onDispose { reg.remove() }
        }
    }

    PantallaPerfilUI(
        nombre   = nombre,
        email    = email,
        celular  = celular,
        pais     = pais,
        photoUrl = photoUrl,
        onBack   = { navController.popBackStack() },
        onEdit   = { navController.navigate("editarperfil") },
        onFavoritos = { navController.navigate("favoritos") },
        onChats  = { navController.navigate("chatguardados") },
        onLogout = {
            auth.signOut()
            navController.navigate("inicio") {
                popUpTo(0) { inclusive = true }
            }
        },
        onHome = {
            navController.navigate("principal") {
                launchSingleTop = true
                popUpTo(0) { inclusive = true }
            }
        }
    )
}
