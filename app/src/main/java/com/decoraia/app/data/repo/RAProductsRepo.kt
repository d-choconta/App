// com/decoraia/app/data/RAProductsRepo.kt
package com.decoraia.app.data

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ProductoAR(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val modelUrl: String = "",
    val style: String = "",
    val type: String = "",
    val tags: List<String> = emptyList()
)

object RAProductsRepo {
    private val db = FirebaseFirestore.getInstance()

    // Estilos fijos usados en la UI (deben coincidir con "style" en Firestore)
    val estilosFijos = listOf("Clásico", "Mediterráneo", "Minimalista", "Industrial")

    // Categorías fijas: id (ruta), label (UI), typeValue (campo "type" en Firestore)
    enum class Categoria(val id: String, val label: String, val typeValue: String) {
        JARRONES("jarrones", "Jarrones", "jarrón"),
        CUADROS("cuadros", "Cuadros", "cuadro"),
        LAMPARAS("lamparas", "Lámparas", "lámpara"),
        SOFAS("sofas", "Sofás", "sofá")
    }
    val categoriasFijas = listOf(
        Categoria.JARRONES, Categoria.CUADROS, Categoria.LAMPARAS, Categoria.SOFAS
    )

    /** Carga productos filtrando por estilo + tipo. */
    suspend fun loadProductos(style: String, typeValue: String): List<ProductoAR> {
        val snap = db.collection("products")
            .whereEqualTo("style", style)        // p. ej. "Minimalista"
            .whereEqualTo("type", typeValue)     // p. ej. "jarrón"
            .get()
            .await()

        return snap.documents.map { d ->
            ProductoAR(
                id = d.id,
                name = d.getString("name") ?: d.id,
                imageUrl = d.getString("imageUrl") ?: "",
                modelUrl = d.getString("modelUrl") ?: "",
                style = d.getString("style") ?: "",
                type = d.getString("type") ?: "",
                tags = (d.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }

    suspend fun loadProductosByIds(ids: List<String>): List<ProductoAR> {
        if (ids.isEmpty()) return emptyList()
        val out = mutableListOf<ProductoAR>()
        for (chunk in ids.chunked(10)) {
            val snap = db.collection("products")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()
            out += snap.documents.map { d ->
                ProductoAR(
                    id = d.id,
                    name = d.getString("name") ?: d.id,
                    imageUrl = d.getString("imageUrl") ?: "",
                    modelUrl = d.getString("modelUrl") ?: "",
                    style = d.getString("style") ?: "",
                    type = d.getString("type") ?: "",
                    tags = (d.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
            }
        }
        return out
    }

    // ==================== Favoritos (users/{uid}/favorites) ====================

    /** Devuelve los productId guardados como favoritos del usuario. */
    suspend fun getFavoriteIds(uid: String): Set<String> {
        val col = db.collection("users").document(uid).collection("favorites")
        val snap = col.get().await()
        // Soporta docId = productId o documentos con campo "productId"
        return snap.documents.mapNotNull { d ->
            val field = d.getString("productId")
            if (!field.isNullOrBlank()) field else d.id
        }.toSet()
    }

    /** Agrega a favoritos (usa docId = productId para simplificar). */
    suspend fun addFavorite(uid: String, product: ProductoAR) {
        val doc = db.collection("users").document(uid)
            .collection("favorites").document(product.id)
        doc.set(
            mapOf(
                "productId" to product.id,
                "type" to product.type,
                "addedAt" to com.google.firebase.Timestamp.now()
            )
        ).await()
    }

    /** Quita de favoritos. Si el docId no es el productId, borra por query. */
    suspend fun removeFavorite(uid: String, productId: String) {
        val col = db.collection("users").document(uid).collection("favorites")
        val byId = col.document(productId).get().await()
        if (byId.exists()) {
            col.document(productId).delete().await()
            return
        }
        val q = col.whereEqualTo("productId", productId).get().await()
        q.documents.forEach { it.reference.delete().await() }
    }
}
