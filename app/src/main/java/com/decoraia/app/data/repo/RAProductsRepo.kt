package com.decoraia.app.data.repo

import com.decoraia.app.data.ProductoAR
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.util.Locale

object RAProductsRepo {

    private val db = FirebaseFirestore.getInstance()

    val estilosFijos = listOf("Clásico", "Mediterráneo", "Minimalista", "Industrial")

    enum class Categoria(val id: String, val label: String, val typeValue: String) {
        JARRONES("jarrones", "Jarrones", "jarron"),
        CUADROS("cuadros", "Cuadros", "cuadro"),
        LAMPARAS("lamparas", "Lámparas", "lampara"),
        SOFAS("sofas", "Sofás", "sofa")
    }

    val categoriasFijas = listOf(
        Categoria.JARRONES, Categoria.CUADROS, Categoria.LAMPARAS, Categoria.SOFAS
    )

    private fun normalize(value: String): String {
        val tmp = Normalizer.normalize(value, Normalizer.Form.NFD)
        val noAccents = tmp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return noAccents.lowercase(Locale.ROOT).trim()
    }

    private fun mapDoc(d: com.google.firebase.firestore.DocumentSnapshot): ProductoAR {
        return ProductoAR(
            id       = d.id,
            name     = d.getString("name") ?: d.id,
            imageUrl = d.getString("imageUrl") ?: "",
            modelUrl = d.getString("modelUrl") ?: "",
            style    = d.getString("style") ?: "",
            type     = d.getString("type") ?: "",
            tags     = (d.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }

    suspend fun loadProductos(style: String, typeValue: String): List<ProductoAR> {
        val styleN = normalize(style)
        val typeN = normalize(typeValue)

        val snap = db.collection("products")
            .whereEqualTo("style", styleN)
            .whereEqualTo("type", typeN)
            .get()
            .await()

        return snap.documents.map(::mapDoc)
    }

    suspend fun loadProductosByIds(ids: List<String>): List<ProductoAR> {
        if (ids.isEmpty()) return emptyList()
        val out = mutableListOf<ProductoAR>()
        for (chunk in ids.chunked(10)) {
            val snap = db.collection("products")
                .whereIn(FieldPath.documentId(), chunk)
                .get().await()
            out += snap.documents.map(::mapDoc)
        }
        return out
    }
}
