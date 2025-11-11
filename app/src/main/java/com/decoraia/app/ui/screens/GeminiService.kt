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

    private const val API_KEY = "AIzaSyCmlDOgseFuFg5jdCqiydb158s2H3T7xlQ"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = API_KEY
    )

    // ========= PROMPT BASE =========
    private val SYSTEM_PROMPT = """
    Eres DecoraIA, experto en decoración de interiores. 
    Responde SIEMPRE en español y en TEXTO PLANO (sin markdown).

    Política de imágenes y URLs:
    - Por defecto, responde SOLO TEXTO y NO incluyas URLs ni imágenes.
    - SOLO incluye URLs de imágenes si el usuario lo pide explícitamente con verbos como:
      "ver", "muestra", "enséñame", "ideas", "fotos", "imágenes", "catálogo".
    - Si el usuario NO pide imágenes, responde solo con texto y NO pongas URLs.

    Modo guía de espacio interior:
    - Si el usuario pide recomendaciones para cualquier espacio del hogar (sala, cocina, habitación/cuarto, comedor,
      hall/recibidor, pasillo, balcón/terraza, estudio/oficina, baño, etc.), entrega una guía integral en texto.
    - Estructura sugerida (6–10 viñetas, concisa):
      1) Estilo recomendado y por qué.
      2) Paleta base + acentos + materiales/texturas.
      3) Distribución y proporciones (medidas orientativas).
      4) Iluminación en capas (general, tarea, acento) y temperatura de color.
      5) Tapetes/textiles (tamaños y capas).
      6) Arte/objetos/decoración (agrupación, altura, escala).
      7) Plantas y acentos naturales.
      8) Tips si es pequeño: alturas, espejos, continuidad de color, muebles elevados, luz cálida, almacenamiento integrado.
      9) Recomendaciones de funcionalidad clave.
      10) 2–3 próximos pasos accionables.
    - NO incluyas enlaces ni imágenes a menos que el usuario lo pida.

    Accesorios compatibles (si el usuario pide ver imágenes): lámpara, jarrón, cuadro, sofá.
    - Si el usuario pide ver imágenes de un accesorio, incluye máx. 2–3 URLs y usa SOLO el catálogo disponible.
    """.trimIndent()

    // ========= Few-shot opcional ========
    private val FEWSHOT: List<Pair<String, String>> = listOf(
        // Solo texto (espacio)
        "Sala pequeña minimalista, solo texto" to
                "Estilo minimalista cálido por su limpieza y sensación de amplitud.\n" +
                "Paleta: base beige y blanco roto; acentos negro/gris suave; madera clara.\n" +
                "Distribución: sofá compacto con brazos delgados; mesa lateral ligera; deja 80–90 cm en pasillos.\n" +
                "Iluminación: general difusa + lámpara de tarea en lectura + acento suave; 2700–3000K.\n" +
                "Textiles: alfombra 160×230 bajo el frente del sofá; lino/algodón.\n" +
                "Arte: 1–3 piezas a 145–155 cm al centro; marcos delgados.\n" +
                "Plantas: 1 mediana en esquina luminosa; maceta mate.\n" +
                "Trucos pequeño: patas visibles, espejos frente a luz, cortinas a techo.\n" +
                "Funcionalidad: mesa nido y ottoman con guardado.\n" +
                "Siguientes pasos: mide el largo del sofá, define paleta exacta y prioriza piezas elevadas.",

        // Accesorio con imágenes si se piden
        "Quiero sofá mediterráneo, solo texto" to
                "Sofá en lino/beige con base de madera natural; cojines mezcla lisos + textura. Mantén patas visibles para ligereza.",

        // Accesorio explícito con intención visual
        "Muestra lámparas industriales" to
                "1) Lámpara de metal negro con acabado mate para contraste" +
                "2) Colgante campana acero cepillado sobre mesa."
    )

    // ========= Normalizadores a Firestore =========

    /** valores en DB: mediterraneo, minimalista, industrial, clasico */
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


    private fun detectTypeToDb(text: String): String {
        val t = text.lowercase()
        val hitsLampara = listOf("lampara","lámpara","lamparas","lámparas","luminaria","colgante","pendant")
        val hitsJarron  = listOf("jarron","jarrón","florero","vase")
        val hitsCuadro  = listOf("cuadro","cuadros","lámina","lamina")
        val hitsSofa    = listOf("sofa","sofá","sofas","sofás")

        return when {
            hitsJarron.any  { it in t } -> "jarron"
            hitsCuadro.any  { it in t } -> "cuadro"
            hitsSofa.any    { it in t } -> "sofa"
            hitsLampara.any { it in t } -> "lampara"
            else -> "lampara"
        }
    }

    // ========= Catálogo desde Firestore =========


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
            "jarron","jarrón","cuadro","cuadros","mesa","mesita","velador","buró","buro",
            "hall","recibidor","pasillo","balcon","balcón","terraza","estudio","oficina"
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
                        "Analiza la imagen y recomienda (estilo, paleta, distribución, iluminación, textiles, arte, tips de espacio)."
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
