package com.decoraia.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.decoraia.app.data.ProductoAR
import com.decoraia.app.data.repo.RAProductsRepo
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object GeminiService {

    // ⚠ En producción mueve esto a BuildConfig o Remote Config.
    private const val API_KEY = "AIzaSyCmlDOgseFuFg5jdCqiydb158s2H3T7xlQ"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = API_KEY
    )

    // ========= PROMPT BASE =========
    private val SYSTEM_PROMPT = """
        Eres DecoraIA, experto en decoración de interiores. 
        Responde SIEMPRE en español y en TEXTO PLANO (sin markdown).
        
        Instrucciones IMPORTANTES:
        - Si te doy un CATÁLOGO, recomienda SOLO productos de ese catálogo.
        - Devuelve 2 o 3 opciones máximo.
        - Para cada opción: una línea de explicación y LUEGO una línea con SOLO la URL de la imagen (http...) sin texto adicional.
        Ejemplo de formato:
        1) Lámpara X — por qué encaja (material/color/tamaño).
        https://ejemplo.com/imagen.jpg
    """.trimIndent()

    // ========= Few-shot opcional ========
    private val FEWSHOT: List<Pair<String, String>> = listOf(
        "Sala con pared blanca y sofá gris, quiero lámpara." to
                "1) Lámpara de pie en latón cepillado para calidez.\nhttps://ejemplo.com/laton.jpg\n2) Sobremesa cerámica beige para contraste suave.\nhttps://ejemplo.com/ceramica.jpg",
        "Alfombra para sala pequeña con piso madera clara" to
                "Alfombra de pelo corto 160×230 en beige/greige. Colócala bajo el frente del sofá para ampliar visualmente."
    )

    // ========= Normalizadores a Firestore =========

    /** A tus valores en DB: mediterraneo, minimalista, industrial, clasico */
    private fun normalizeStyleToDb(text: String): String? {
        val t = text.lowercase()
        return when {
            "mediter" in t -> "mediterraneo"
            "minimal" in t -> "minimalista"
            "industrial" in t -> "industrial"
            "clásic" in t || "clasico" in t || "clasíc" in t -> "clasico"
            else -> null
        }
    }

    /** A tu campo type en DB: lampara (por defecto) */
    private fun detectTypeToDb(text: String): String {
        val t = text.lowercase()
        val hits = listOf(
            "lampara","lámpara","lamparas","lámparas","mesa de noche","velador","buro","buró","mesita"
        )
        return if (hits.any { it in t }) "lampara" else "lampara"
    }

    // ========= Catálogo desde Firestore =========

    /** Carga hasta 6 productos válidos (con imageUrl http...) */
    private suspend fun findCatalog(styleHint: String?, userText: String): List<ProductoAR> {
        val styleDb = styleHint ?: normalizeStyleToDb(userText)
        val typeDb  = detectTypeToDb(userText)

        if (styleDb == null) return emptyList()

        return try {
            val productos = RAProductsRepo.loadProductos(style = styleDb, typeValue = typeDb)
            val result = productos.filter { it.imageUrl.startsWith("http") }.take(6)
            Log.d("GeminiService", "CATALOGO: style=$styleDb type=$typeDb count=${result.size}")
            result
        } catch (e: Exception) {
            Log.e("GeminiService", "Error Firestore: ${e.message}", e)
            emptyList()
        }
    }

    /** Convierte catálogo a texto que el modelo pueda usar */
    private fun catalogAsText(productos: List<ProductoAR>): String {
        if (productos.isEmpty()) return "(sin productos)\n"
        return buildString {
            productos.take(6).forEachIndexed { idx, p ->
                val nombre = p.name.ifBlank { "Producto ${idx + 1}" }
                appendLine("- Producto: $nombre | Estilo: ${p.style} | Tipo: ${p.type}")
                if (p.imageUrl.startsWith("http")) appendLine("  IMG: ${p.imageUrl}")
            }
        }
    }

    // ========= Off-topic simple =========

    private fun isOffTopic(text: String): Boolean {
        val t = text.lowercase()
        val hints = listOf(
            "sala","comedor","cocina","baño","bano","habitación","dormitorio","pared",
            "sofá","sofa","lámpara","lampara","alfombra","mueble","interior","decor",
            "estilo","iluminación","iluminacion","paleta","color","distribución","distribucion",
            "jarron","jarrón","cuadro","cuadros","mesa","mesita","velador","buró","buro"
        )
        return hints.none { it in t }
    }

    private fun offTopicReply(): String =
        "Soy un asistente de decoración. Cuéntame medidas, estilo que te gusta, iluminación y presupuesto para ayudarte mejor."

    // ========= API amigable =========

    suspend fun askGeminiSuspend(
        prompt: String,
        imageUri: Uri?,
        bitmap: Bitmap?,
        context: Context,
        explicitStyle: String? = null
    ): Pair<String, Bitmap?> = withContext(Dispatchers.IO) {
        val done = CompletableDeferred<Pair<String, Bitmap?>>()
        askGemini(prompt, imageUri, bitmap, context, explicitStyle) { text, bmp ->
            done.complete(text to bmp)
        }
        done.await()
    }

    fun askGemini(
        prompt: String,
        imageUri: Uri?,
        bitmap: Bitmap?,
        context: Context,
        explicitStyle: String? = null,
        callback: (String, Bitmap?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (imageUri == null && bitmap == null && isOffTopic(prompt)) {
                    callback(offTopicReply(), null)
                    return@launch
                }

                // 1) Intentar catálogo
                val catalogProducts = findCatalog(explicitStyle, prompt)

                // 2) Preparar input
                val input = content {
                    text(SYSTEM_PROMPT)
                    FEWSHOT.forEach { (u, a) ->
                        text("Usuario: $u"); text("Asistente: $a")
                    }

                    // imagen del usuario (opcional)
                    val imageBitmap = when {
                        bitmap != null -> bitmap
                        imageUri != null -> {
                            context.contentResolver.openInputStream(imageUri)?.use { st ->
                                BitmapFactory.decodeStream(st)
                            }
                        }
                        else -> null
                    }
                    imageBitmap?.let { image(it) }

                    // catálogo (opcional)
                    if (catalogProducts.isNotEmpty()) {
                        text("CATÁLOGO DISPONIBLE:")
                        text(catalogAsText(catalogProducts))
                        text("Usa solo el catálogo anterior. NO inventes nombres ni imágenes.")
                    }

                    val userText = if (prompt.isBlank() && imageBitmap != null) {
                        "Analiza la imagen y recomienda lámparas adecuadas (estilo, color, material y tamaño)."
                    } else prompt

                    text("Usuario: $userText")
                    text("Asistente:")
                }

                // 3) Llamada
                val response = generativeModel.generateContent(input)
                val texto = (response.text ?: "").trim()

                // 4) fallback
                val finalText = when {
                    texto.isNotBlank() -> texto
                    catalogProducts.isEmpty() ->
                        "No tengo imágenes disponibles ahora mismo para ese estilo/tipo. Verifica que existan productos con imageUrl en tu catálogo."
                    else -> "Sin respuesta del modelo"
                }

                callback(finalText, null)

            } catch (e: Exception) {
                Log.e("GeminiService", "Error al generar contenido", e)
                val msg =
                    if (e.message?.contains("503", true) == true || e.message?.contains("overload", true) == true)
                        "El modelo está ocupado temporalmente. Intenta de nuevo en unos segundos."
                    else
                        "Error al generar respuesta: ${e.message}"
                callback(msg, null)
            }
        }
    }
}