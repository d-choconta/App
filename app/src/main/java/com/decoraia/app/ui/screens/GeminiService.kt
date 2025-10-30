package com.decoraia.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
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




    private val SYSTEM_PROMPT = """
Eres DecoraIA, un asistente EXPERTO en DISEÑO y DECORACIÓN DE INTERIORES.
Responde SIEMPRE en español y en TEXTO PLANO (sin markdown).
Objetivo: recomendaciones claras, accionables y presupuestables.
Da opciones por rango de precio y justifica brevemente.
Si la consulta NO es de decoración, dilo en una línea y redirige con 1-2 preguntas útiles
(medidas del espacio, iluminación, estilo deseado y presupuesto).
""".trimIndent()

    private val FEWSHOT: List<Pair<String, String>> = listOf(
        "Tengo pared blanca y sofá gris. ¿Qué lámparas sugieres?" to
                "Con pared blanca + sofá gris, busca contraste cálido: 1) lámpara de pie latón cepillado; 2) sobremesa cerámica beige; 3) colgante negro mate. Bombillas 2700–3000K. Añade tira LED cálida detrás del sofá para acento.",
        "¿Qué alfombra para sala pequeña con piso madera clara?" to
                "Elige alfombra de pelo corto 160×230 en tonos neutros (beige/greige) con textura sutil. Colócala bajo el frente del sofá para ampliar visualmente. Si hay niños/mascotas, usa polipropileno fácil de limpiar."
    )

    private fun isOffTopic(text: String): Boolean {
        val t = text.lowercase()
        val hints = listOf(
            "sala", "comedor", "cocina", "baño", "bano", "habitación", "dormitorio", "pared",
            "sofá", "sofa", "lámpara", "lampara", "alfombra", "mueble", "interior", "decor",
            "estilo", "iluminación", "iluminacion", "paleta", "color", "distribución", "distribucion",
            "jarron", "cuarto", "minimalista", "mediterraneo", "industrial", "clasico", "tonos", "mesa",
            "sofa", "cortina", "estantería", "silla", "rincón", "vitrina", "espejo", "colchón", "manta",
            "cojín", "lino", "techo", "piso", "suelo", "ventana", "balcón", "accesorios", "estilo nórdico",
            "vintage", "retro", "bohemio", "moderno", "escandinavo", "feng shui", "paisajismo", "diseño",
            "arte", "cuadro", "pintura", "escultura", "lámparas de pie", "lámparas de techo", "candelabro",
            "decoración floral", "plantas", "florero", "jarrón de cristal", "tapiz", "póster", "revestimiento",
            "papel pintado", "mosaico", "azulejos", "ladrillo", "pared de acento", "papel mural", "mobiliario",
            "decoración rústica", "decoración boho", "puf", "taburete", "alfombrillas", "tejido", "cortinas",
            "persianas", "alfombra shaggy", "despacho", "oficina", "sala de estar", "estudio", "vitrinas",
            "cajones", "paredes de piedra", "ladrillo visto", "vigas expuestas", "bañera", "ducha", "grifo",
            "accesorios de baño", "espejo de baño", "lavabo", "fregadero", "toallas", "albornoces", "estantes",
            "librerías", "chimenea", "chimenea eléctrica", "sillón", "sillón reclinable", "camas", "cabecero",
            "mesita de noche", "póster decorativo", "contemporáneo", "modernista", "tapicería", "almohada",
            "lino natural", "acabados", "cristal", "metal", "madera", "vidrio", "mármol", "piedra natural",
            "cerámica", "horno", "estufa", "armario", "guardarropa", "cuadro abstracto", "cuadro moderno",
            "puertas correderas", "mobiliario modular", "muebles vintage", "accesorios de pared", "decoración náutica",
            "elementos de madera", "estilo colonial", "estilo francés","estilo Frances", "estilo industrial chic", "estilo clásico moderno",
            "alfombra persa", "alfombra de lana", "barro", "macetas", "juguetes decorativos", "estilo art déco",
            "estilo victoriano", "estilo loft", "estilo country", "estilo zen", "decoración con espejos", "decoración ecológica",
            "decoración sostenible", "mueble multifuncional", "espacio abierto", "estilo contemporáneo", "muebles minimalistas",
            "mesa de centro", "mesa auxiliar", "mesa de comedor", "alacena", "estante flotante", "zona de lectura",
            "mobiliario de jardín", "flores artificiales", "decoración de pared", "pared de ladrillo", "revestimiento de madera",
            "revestimiento metálico", "decoración geométrica", "decoración étnica", "decoración japonesa", "mueble de TV",
            "cajonera", "tapices de pared", "artículos de lujo", "piso flotante", "paneles acústicos", "lámparas decorativas",
            "cortinas de lino", "vigas de madera", "decoración tropical", "decoración náutica", "accesorios de cocina",
            "organizadores de cocina", "utensilios de cocina", "conjunto de comedor", "banco de jardín", "estantería flotante",
            "mobiliario de oficina", "galería de arte", "decoración futurista", "muebles de concreto", "suelo de cerámica",
            "tarima", "mesas auxiliares", "accesorios de comedor", "lámparas de mesa", "colores neutros", "colores cálidos",
            "colores fríos", "decoración con madera reciclada", "escaparatismo", "combinación de texturas", "proporción",
            "proporciones del espacio", "elementos naturales", "decoración artística", "arte contemporáneo", "decoración con cristales",
            "estantería modular", "elementos reciclados", "diseño ecléctico", "lámparas de cristal", "decoración en tonos tierra",
            "decoración monocromática", "arte mural", "decoración vintage", "decoración rustica moderna", "estilo moderno industrial",
            "estilo industrial vintage", "cuadro fotográfico", "cuadro en lienzo", "decoración shabby chic", "decoración de primavera",
            "decoración minimalista moderna", "funda nórdica", "tienes de estilo bohemio", "tapices étnicos", "decoración de lujo",
            "alfombra de fibras naturales", "mobiliario vintage", "lámpara colgante", "luces LED", "lámpara empotrada",
            "mesa de hierro forjado", "decoración de baño moderna", "decoración con madera reciclada", "elementos industriales",
            "decoración industrial moderna", "decoración de salón", "decoración con plantas", "decoración con piedras",
            "decoración con hierro", "accesorios de cocina vintage", "diseño de interiores moderno", "decoración en blanco y negro",
            "cuadro minimalista", "decoración de oficina moderna", "accesorios modernos", "decoración de terraza", "decoración de entrada",
            "decoración con vidrio", "mobiliario de lujo", "diseño de interiores clásico", "decoración con elementos naturales",
            "papel de pared moderno", "decoración con vinilos", "zona de descanso", "decoración tropical chic", "decoración boho chic",
            "decoración con madera envejecida", "estilo de decoración único", "elementos de decoración antiguos", "decoración con papel pintado",
            "estilo náutico", "estilo de vida decorativo", "colores pastel", "decoración romántica", "decoración con piedra natural",
            "lámpara de mesa moderna", "tejidos decorativos", "diseño orgánico", "estilo costero", "decoración industrial chic",
            "muebles para exterior", "decoración minimalista escandinava", "alfombra con patrones", "decoración moderna retro",
            "decoración de temporada", "complementos decorativos", "decoración con madera natural", "accesorios decorativos para sala",
            "decoración luminosa", "estilo artístico", "decoración en tonos neutros", "plantas de interior", "estilo rústico moderno",
            "decoración funcional", "mobiliario flexible", "decoración zen contemporánea", "decoración a medida", "decoración acogedora",
            "estilo romántico", "artículos de decoración en metal", "decoración con espejos grandes", "decoración en tonos metálicos"
        )
        return hints.none { it in t }
    }


    private fun offTopicReply(): String =
        "Soy un asistente especializado en decoración de interiores. ¿Me cuentas medidas del espacio, estilo que te gusta, iluminación y presupuesto para ayudarte mejor?"

    suspend fun askGeminiSuspend(
        prompt: String,
        imageUri: Uri?,
        context: Context
    ): Pair<String, Bitmap?> = withContext(Dispatchers.IO) {
        val done = CompletableDeferred<Pair<String, Bitmap?>>()
        askGemini(prompt, imageUri, context) { text, bmp -> done.complete(text to bmp) }
        done.await()
    }

    fun askGemini(
        prompt: String,
        imageUri: Uri?,
        context: Context,
        callback: (String, Bitmap?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (imageUri == null && isOffTopic(prompt)) {
                    callback(offTopicReply(), null)
                    return@launch
                }

                val input = content {
                    text(SYSTEM_PROMPT)
                    FEWSHOT.forEach { (u, a) ->
                        text("Usuario: $u")
                        text("Asistente: $a")
                    }

                    if (imageUri != null) {
                        context.contentResolver.openInputStream(imageUri)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            image(bmp)
                        }
                    }

                    val userText = if (prompt.isBlank() && imageUri != null) {
                        "Analiza la imagen y dame recomendaciones de decoración (estilo, color, iluminación y distribución)."
                    } else prompt

                    text("Usuario: $userText")
                    text("Asistente:")
                }

                val response = generativeModel.generateContent(input)
                val texto = (response.text ?: "").trim()
                val bitmapRespuesta: Bitmap? = null

                val finalText = if (texto.isNotBlank()) texto
                else if (bitmapRespuesta != null) "Imagen generada"
                else "Sin respuesta del modelo"

                callback(finalText, bitmapRespuesta)

            } catch (e: Exception) {
                Log.e("GeminiService", "Error al generar contenido", e)
                val msg = when {
                    e.message?.contains("503", ignoreCase = true) == true ||
                            e.message?.contains("overload", ignoreCase = true) == true ->
                        "El modelo está ocupado temporalmente. Intenta de nuevo en unos segundos."
                    else -> "Error al generar respuesta: ${e.message}"
                }
                callback(msg, null)
            }
        }
    }
}