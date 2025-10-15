package com.decoraia.app.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.data.ProductoAR
import com.decoraia.app.data.RAProductsRepo
import com.decoraia.app.ui.components.RAModelosScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaRAModelos(
    nav: NavHostController,
    style: String,
    categoryId: String
) {

    val categoria = remember(categoryId) {
        RAProductsRepo.categoriasFijas.firstOrNull { it.id == categoryId }
            ?: RAProductsRepo.categoriasFijas.first()
    }

    var loading by remember { mutableStateOf(true) }
    var modelos by remember { mutableStateOf(emptyList<ProductoAR>()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }
    val db   = remember { FirebaseFirestore.getInstance() }
    val uid  = auth.currentUser?.uid
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }


    LaunchedEffect(style, categoryId) {
        scope.launch {
            try {
                loading = true
                errorMsg = null
                modelos = RAProductsRepo.loadProductos(
                    style = style,
                    typeValue = categoria.typeValue
                )
            } catch (e: Exception) {
                errorMsg = e.message
                modelos = emptyList()
            } finally {
                loading = false
            }
        }
    }


    DisposableEffect(uid) {
        var registration: ListenerRegistration? = null
        if (uid != null) {
            registration = db.collection("users")
                .document(uid)
                .collection("favorites")
                .addSnapshotListener { snap, _ ->
                    val ids = mutableSetOf<String>()
                    snap?.documents?.forEach { d ->
                        d.getString("productId")?.let { ids.add(it) }
                    }
                    favoriteIds = ids
                }
        }
        onDispose {
            registration?.remove()
        }
    }

    fun toggleFavorite(prod: ProductoAR) {
        if (uid == null) return
        val favsCol = db.collection("users").document(uid).collection("favorites")
        val isFav = favoriteIds.contains(prod.id)

        scope.launch {
            try {
                if (isFav) {
                    val q = favsCol.whereEqualTo("productId", prod.id).get().await()
                    q.documents.forEach { it.reference.delete().await() }
                } else {
                    val data = mapOf(
                        "productId" to prod.id,
                        "type" to prod.type,
                        "addedAt" to com.google.firebase.Timestamp.now()
                    )
                    favsCol.add(data).await()
                }
            } catch (e: Exception) {

            }
        }
    }

    RAModelosScreenUI(
        categoriaTitulo = categoria.label,
        modelos = modelos,
        loading = loading,
        favoriteIds = favoriteIds,
        errorMsg = errorMsg,
        onBack = { nav.popBackStack() },
        onSelectModelo = { modelo ->
            nav.navigate("arviewer?modelUrl=${modelo.modelUrl}")
        },
        onToggleFavorite = { producto -> toggleFavorite(producto) },
        onHome = { nav.navigate("principal") { popUpTo("principal") { inclusive = true } } },
        onProfile = { nav.navigate("perfil") }
    )
}
