package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.data.ProductoAR
import com.decoraia.app.data.repo.FavoritosRepository
import com.decoraia.app.ui.components.FavoritosScreenUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun PantallaFavoritos(nav: NavHostController) {
    val repo = remember { FavoritosRepository() }
    val auth = remember { FirebaseAuth.getInstance() }
    val uid = auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var modelos by remember { mutableStateOf(emptyList<ProductoAR>()) }
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }


    LaunchedEffect(uid) {
        if (uid == null) {
            loading = false
            errorMsg = "Debes iniciar sesión para ver tus favoritos."
            modelos = emptyList()
            return@LaunchedEffect
        }
        repo.listenFavoritosIds(uid).collectLatest { ids ->
            favoriteIds = ids
            loading = true
            try {
                modelos = repo.loadFavoritosByIds(ids.toList())
                errorMsg = null
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
        // UI optimista (opcional):
        val wasFav = favoriteIds.contains(item.id)
        if (wasFav) {
            favoriteIds = favoriteIds - item.id
        } else {
            favoriteIds = favoriteIds + item.id
        }
        scope.launch {
            try {
                if (wasFav) {
                    repo.removeFavorito(currentUid, item.id)
                } else {
                    repo.addFavorito(currentUid, item)
                }
            } catch (e: Exception) {
                favoriteIds = if (wasFav) favoriteIds + item.id else favoriteIds - item.id
                errorMsg = "Error actualizando favoritos: ${e.message}"
            }
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
        onToggleFavorite = { toggleFavorite(it) },
        onHome = {
            nav.navigate("principal") { popUpTo("principal") { inclusive = true } }
        },
        onProfile = { nav.navigate("perfil") }
    )
}
