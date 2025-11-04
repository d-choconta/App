package com.decoraia.app.data.repo

import com.decoraia.app.data.ProductoAR
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.Normalizer

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

    /** Quita tildes y pasa a minúsculas (para comparar en Firestore) */
    private fun normalize(value: String): String {
        val tmp = Normalizer.normalize(value, Normalizer.Form.NFD)
        val noAccents = tmp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return noAccents.lowercase().trim()
    }

    /** Convierte gs://bucket/ruta/a.glb -> https://firebasestorage.googleapis.com/v0/b/bucket/o/ruta%2Fa.glb?alt=media */
    private fun gsToHttps(gsUrl: String): String {
        val m = Regex("""^gs://([^/]+)/(.+)$""").find(gsUrl.trim()) ?: return gsUrl
        val (bucket, path) = m.destructured
        val encodedPath = java.net.URLEncoder.encode(path, Charsets.UTF_8.name())
            .replace("+", "%20") // por si hay espacios
        return "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media"
    }

    /** Obtiene productos por estilo y tipo (robusto a tildes/mayúsculas) */
    suspend fun loadProductos(style: String, typeValue: String): List<ProductoAR> {
        val styleN = normalize(style)      // ej. "Industrial" -> "industrial"
        val typeN  = normalize(typeValue)  // ej. "jarrón" -> "jarron"

        val snap = db.collection("products")
            .whereEqualTo("style", styleN)   // Asegúrate que en Firestore guardas style normalizado en minúsculas
            .whereEqualTo("type", typeN)     // y type sin tildes, minúsculas
            .get()
            .await()

        return snap.documents.map { d ->
            val rawModel = d.getString("modelUrl").orEmpty()
            val modelUrl = if (rawModel.startsWith("gs://")) gsToHttps(rawModel) else rawModel

            ProductoAR(
                id       = d.id,
                name     = d.getString("name") ?: d.id,
                imageUrl = d.getString("imageUrl") ?: "",
                modelUrl = modelUrl,
                style    = d.getString("style") ?: "",
                type     = d.getString("type") ?: "",
                tags     = (d.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }

    /** Carga por IDs (favoritos/historial) con el mismo manejo de gs:// */
    suspend fun loadProductosByIds(ids: List<String>): List<ProductoAR> {
        if (ids.isEmpty()) return emptyList()

        val out = mutableListOf<ProductoAR>()
        for (chunk in ids.chunked(10)) {
            val snap = db.collection("products")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            out += snap.documents.map { d ->
                val rawModel = d.getString("modelUrl").orEmpty()
                val modelUrl = if (rawModel.startsWith("gs://")) gsToHttps(rawModel) else rawModel

                ProductoAR(
                    id       = d.id,
                    name     = d.getString("name") ?: d.id,
                    imageUrl = d.getString("imageUrl") ?: "",
                    modelUrl = modelUrl,
                    style    = d.getString("style") ?: "",
                    type     = d.getString("type") ?: "",
                    tags     = (d.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                )
            }
        }
        return out
    }
}
