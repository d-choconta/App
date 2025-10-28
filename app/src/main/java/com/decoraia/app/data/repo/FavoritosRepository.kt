package com.decoraia.app.data.repo

import com.decoraia.app.data.ProductoAR
import com.decoraia.app.data.RAProductsRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavoritosRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /** Escucha el set de IDs de favoritos del usuario. */
    fun listenFavoritosIds(uid: String): Flow<Set<String>> = callbackFlow {
        val reg = db.collection("users").document(uid)
            .collection("favorites")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    trySend(emptySet()).isSuccess
                    return@addSnapshotListener
                }
                val ids = snap?.documents?.mapNotNull { d ->
                    d.getString("productId") ?: d.id
                }?.toSet() ?: emptySet()
                trySend(ids).isSuccess
            }
        awaitClose { reg.remove() }
    }

    /** Añade/actualiza favorito. */
    suspend fun addFavorito(uid: String, item: ProductoAR) {
        val col = db.collection("users").document(uid).collection("favorites")
        val ref = col.document(item.id)
        val data = mapOf(
            "productId" to item.id,
            "name" to item.name,
            "modelUrl" to item.modelUrl,
            "imageUrl" to item.imageUrl,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        ref.set(data, SetOptions.merge()).await()
    }

    /** Quita favorito. Si no existe docId=productId, hace fallback consultando por campo. */
    suspend fun removeFavorito(uid: String, productId: String) {
        val col = db.collection("users").document(uid).collection("favorites")
        val byId = col.document(productId).get().await()
        if (byId.exists()) {
            col.document(productId).delete().await()
            return
        }
        val q = col.whereEqualTo("productId", productId).get().await()
        for (d in q.documents) d.reference.delete().await()
    }

    /** modelos por IDs usando fuente actual. */
    suspend fun loadFavoritosByIds(ids: List<String>): List<ProductoAR> {
        if (ids.isEmpty()) return emptyList()
        return RAProductsRepo.loadProductosByIds(ids)
    }
}
