package com.decoraia.app.data.repo

import com.decoraia.app.data.ProductoAR
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RAProductsRepo {
    private val db = FirebaseFirestore.getInstance()

    val estilosFijos = listOf("Clásico", "Mediterráneo", "Minimalista", "Industrial")

    enum class Categoria(val id: String, val label: String, val typeValue: String) {
        JARRONES("jarrones", "Jarrones", "jarrón"),
        CUADROS("cuadros", "Cuadros", "cuadro"),
        LAMPARAS("lamparas", "Lámparas", "lámpara"),
        SOFAS("sofas", "Sofás", "sofá")
    }

    val categoriasFijas = listOf(
        Categoria.JARRONES, Categoria.CUADROS, Categoria.LAMPARAS, Categoria.SOFAS
    )

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
}