package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.data.ProductoAR
import com.decoraia.app.data.RAProductsRepo
import com.decoraia.app.ui.components.FavoritosScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaFavoritos(nav: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var modelos by remember { mutableStateOf(emptyList<ProductoAR>()) }
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    DisposableEffect(uid) {
        var reg: ListenerRegistration? = null
        if (uid != null) {
            reg = db.collection("users")
                .document(uid)
                .collection("favorites")
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        errorMsg = err.message
                        return@addSnapshotListener
                    }
                    val ids = snap?.documents?.mapNotNull { d ->
                        d.getString("productId") ?: d.id
                    }?.toSet() ?: emptySet()
                    favoriteIds = ids
                }
        }
        onDispose { reg?.remove() }
    }

    LaunchedEffect(favoriteIds, uid) {
        if (uid == null) {
            loading = false
            errorMsg = "Debes iniciar sesión para ver favoritos."
            modelos = emptyList()
            return@LaunchedEffect
        }
        scope.launch {
            try {
                loading = true
                errorMsg = null
                modelos = RAProductsRepo.loadProductosByIds(favoriteIds.toList())
            } catch (e: Exception) {
                errorMsg = e.message
                modelos = emptyList()
            } finally {
                loading = false
            }
        }
    }

    fun toggleFavorite(item: ProductoAR) {
        val currentUid = uid ?: return
        scope.launch {
            try {
                if (favoriteIds.contains(item.id)) {
                    RAProductsRepo.removeFavorite(currentUid, item.id)
                } else {
                    RAProductsRepo.addFavorite(currentUid, item)
                }
            } catch (_: Exception) { }
        }
    }

    FavoritosScreenUI(
        favoritos = modelos,
        loading = loading,
        errorMsg = errorMsg,
        onBack = { nav.popBackStack() },
        onSelectModelo = { modelo ->
            val encoded = Uri.encode(modelo.modelUrl)
            nav.navigate("arviewer?modelUrl=$encoded")
        },
        onToggleFavorite = { producto -> toggleFavorite(producto) },
        onHome = {
            nav.navigate("principal") {
                popUpTo("principal") { inclusive = true }
            }
        },
        onProfile = { nav.navigate("perfil") }
    )
}
