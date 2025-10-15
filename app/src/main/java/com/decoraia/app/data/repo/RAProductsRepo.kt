package com.decoraia.app.data

import com.google.firebase.firestore.FieldValue
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

    val estilosFijos = listOf("Clásico", "Mediterráneo", "Minimalista", "Industrial")

    enum class Categoria(val id: String, val label: String, val typeValue: String) {
        JARRONES("jarrones", "Jarrones", "jarrón"),
        CUADROS("cuadros", "Cuadros", "cuadro"),
        LAMPARAS("lamparas", "Lámparas", "lámpara"),
        SOFAS("sofas", "Sofás", "sofá")
    }
    val categoriasFijas = listOf(Categoria.JARRONES, Categoria.CUADROS, Categoria.LAMPARAS, Categoria.SOFAS)

    // -- Productos por estilo + tipo
    suspend fun loadProductos(style: String, typeValue: String): List<ProductoAR> {
        val snap = db.collection("products")
            .whereEqualTo("style", style)
            .whereEqualTo("type", typeValue)
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

    // ==================== Favoritos (users/{uid}/favorites) ====================

    suspend fun getFavoriteIds(uid: String): Set<String> {
        val col = db.collection("users").document(uid).collection("favorites")
        val snap = col.get().await()
        // soporta tanto docId = productId como documentos con campo productId
        return snap.documents.mapNotNull { d ->
            d.id.takeIf { it.isNotBlank() }?.let { id ->
                // si el docId NO es el productId, usa el campo
                val productIdField = d.getString("productId")
                if (!productIdField.isNullOrBlank()) productIdField else id
            }
        }.toSet()
    }


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

    suspend fun removeFavorite(uid: String, productId: String) {
        val col = db.collection("users").document(uid).collection("favorites")
        // primero intenta borrar por docId
        val byId = col.document(productId).get().await()
        if (byId.exists()) {
            col.document(productId).delete().await()
            return
        }
        // si tus docs tienen id aleatorio, borra por campo productId
        val q = col.whereEqualTo("productId", productId).get().await()
        q.documents.forEach { it.reference.delete().await() }
    }
}
