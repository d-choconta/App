package com.decoraia.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.decoraia.app.ui.components.ChatIAScreenUI
import com.decoraia.app.ui.components.ChatMessageUIModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.decoraia.app.data.repo.RAProductsRepo

// =====================
// Dominio: estilos / accesorios
// =====================
enum class StyleDb(val dbValue: String) {
    MINIMALISTA("minimalista"),
    INDUSTRIAL("industrial"),
    MEDITERRANEO("mediterraneo"),
    CLASICO("clasico")
}

enum class AccessoryDb(val dbValue: String) {
    JARRON("jarron"),
    CUADRO("cuadro"),
    LAMPARA("lampara"),
    SOFA("sofa")
}

private val ALL_STYLES_ORDERED = listOf(
    StyleDb.MEDITERRANEO, StyleDb.MINIMALISTA, StyleDb.INDUSTRIAL, StyleDb.CLASICO
)

// =====================
// Sinónimos (UI + Chat)
// =====================
private val STYLE_SYNONYMS: Map<String, StyleDb> = mapOf(
    "minimalista" to StyleDb.MINIMALISTA, "minimal" to StyleDb.MINIMALISTA,
    "industrial"   to StyleDb.INDUSTRIAL,
    "mediterraneo" to StyleDb.MEDITERRANEO, "mediterráneo" to StyleDb.MEDITERRANEO, "mediter" to StyleDb.MEDITERRANEO,
    "clasico"      to StyleDb.CLASICO, "clásico" to StyleDb.CLASICO
)

private val ACCESSORY_SYNONYMS: Map<String, AccessoryDb> = mapOf(
    "jarron" to AccessoryDb.JARRON, "jarrón" to AccessoryDb.JARRON, "florero" to AccessoryDb.JARRON, "vase" to AccessoryDb.JARRON,
    "cuadro" to AccessoryDb.CUADRO, "cuadros" to AccessoryDb.CUADRO, "lámina" to AccessoryDb.CUADRO, "lamina" to AccessoryDb.CUADRO,
    "lampara" to AccessoryDb.LAMPARA, "lámpara" to AccessoryDb.LAMPARA, "luminaria" to AccessoryDb.LAMPARA, "colgante" to AccessoryDb.LAMPARA, "pendant" to AccessoryDb.LAMPARA,
    "sofa" to AccessoryDb.SOFA, "sofá" to AccessoryDb.SOFA, "sofas" to AccessoryDb.SOFA, "sofás" to AccessoryDb.SOFA
)

// Todos los espacios del hogar
private val HOME_SPACE_KEYWORDS = listOf(
    "sala","living","cuarto","habitacion","habitación","dormitorio","recamara","recámara",
    "comedor","cocina","baño","bano","hall","recibidor","entrada","pasillo",
    "balcon","balcón","terraza","estudio","oficina","biblioteca","home office",
    "zona de tv","sala de estar","family room","lobby","vestíbulo","closet","lavanderia","lavadero"
)

// =====================
// Parsers + Wrappers
// =====================
private fun parseStyle(text: String): StyleDb? {
    val t = text.lowercase()
    return STYLE_SYNONYMS.entries.firstOrNull { (k, _) -> k in t }?.value
}
private fun parseAccessory(text: String): AccessoryDb? {
    val t = text.lowercase()
    return ACCESSORY_SYNONYMS.entries.firstOrNull { (k, _) -> k in t }?.value
}

// Compat con repos existentes
private fun guessStyleToDb(text: String): String = parseStyle(text)?.dbValue ?: StyleDb.MEDITERRANEO.dbValue
private fun guessTypeToDb(text: String): String = parseAccessory(text)?.dbValue ?: AccessoryDb.LAMPARA.dbValue

// =====================
// Normalización + intención (helpers)
// =====================
private fun normalizeForIntent(raw: String): String {
    var t = raw.lowercase()
    t = t.replace('á','a').replace('é','e').replace('í','i').replace('ó','o').replace('ú','u').replace('ñ','n')
    // typos comunes
    t = t.replace(" en tex", " en texto")
        .replace("texlo", "texto")
        .replace("imgen", "imagen")
    t = Regex("\\s+").replace(t, " ").trim()
    return t
}

private val TEXT_ONLY_TRIGGERS = listOf(
    "en texto", "solo texto", "sin imagen", "sin imagenes",
    "sin fotos", "no muestres fotos", "no mostrar fotos",
    "explicacion sin imagenes", "solo explicacion", "solo explicaciones"
)

private val SHOW_IMAGE_TRIGGERS = listOf(
    "muestrame", "muestra", "ensename", "enseñame", "ver", "ver opciones",
    "ver imagenes", "ver fotos", "ideas", "opciones", "catalogo", "catálogo",
    "busco", "fotos", "imagenes", "imagen", "dame", "enviame", "ponme", "traeme", "tráeme"
)

// Intención para mostrar catálogo/imagenes
private fun shouldShowCatalogIntent(text: String): Boolean {
    val t = normalizeForIntent(text)
    if (TEXT_ONLY_TRIGGERS.any { it in t }) return false
    return SHOW_IMAGE_TRIGGERS.any { it in t }
}

// ¿El usuario pide asesoría integral para un espacio interior?
private fun isHomeSpaceRequest(text: String): Boolean {
    val t = text.lowercase()
    val mentionsSpace = HOME_SPACE_KEYWORDS.any { it in t }
    val mentionsAccessory = parseAccessory(text) != null
    return mentionsSpace && !mentionsAccessory
}

private fun mentionsSmallSpace(text: String): Boolean {
    val t = text.lowercase()
    return listOf("pequeño","pequeno","pocos metros","reducido","angosto","estrecho","mini","muy chico").any { it in t }
}

// =====================
// Helpers de opciones (chat)
// =====================
data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean,
    val imageUri: Uri? = null,
    val bitmap: Bitmap? = null,
    val productImageUrl: String? = null
)

private data class OpcionesExtraidas(val items: List<String>)

private fun extraerOpcionesDelUltimoAsistente(historial: List<MensajeIA>): OpcionesExtraidas? {
    val ultimoAsistente = historial.lastOrNull { !it.esUsuario }?.texto ?: return null
    val opciones = mutableListOf<String>()
    val regex = Regex("""^\s*(\d+)[\)\.\-:]\s*(.+)$""")
    ultimoAsistente.lines().forEach { line ->
        val m = regex.find(line.trim())
        if (m != null) {
            val texto = m.groupValues[2].trim()
            if (texto.isNotBlank()) opciones += texto
        }
    }
    return if (opciones.isNotEmpty()) OpcionesExtraidas(opciones) else null
}

private fun detectarIndiceElegido(userText: String, total: Int): Int? {
    val t = userText.lowercase()
    Regex("""\b(\d{1,2})\b""").find(t)?.let { m ->
        val n = m.groupValues[1].toIntOrNull()
        if (n != null && n in 1..total) return n - 1
    }
    val mapa = mapOf("primera" to 0, "1ra" to 0, "1era" to 0, "segunda" to 1, "2da" to 1, "tercera" to 2, "3ra" to 2, "cuarta" to 3, "4ta" to 3)
    for ((k, v) in mapa) if (t.contains(k) && v < total) return v
    Regex("""opci[oó]n\s+(\d{1,2})""").find(t)?.let { m ->
        val n = m.groupValues[1].toIntOrNull()
        if (n != null && n in 1..total) return n - 1
    }
    return null
}

private fun buildContextualPrompt(
    historial: List<MensajeIA>,
    userText: String,
    wantsImages: Boolean,
    isHomeSpace: Boolean,
    accessoryRequested: Boolean
): String {
    val slice = historial.takeLast(12)
    val sb = StringBuilder()
    sb.appendLine("Eres un asesor de decoración. NO repitas saludos ni presentaciones.")
    sb.appendLine("Continúa la conversación según el contexto y avanza sin reexplicar lo anterior.")

    // Política de entrega de medios
    if (!wantsImages) {
        sb.appendLine("El usuario NO pidió imágenes: responde SOLO TEXTO, NO incluyas URLs ni fotos.")
    } else {
        if (accessoryRequested) {
            sb.appendLine("El usuario SÍ pidió ver imágenes de un accesorio: puedes incluir hasta 2 o 3 URLs del catálogo.")
        } else {
            sb.appendLine("El usuario pidió 'ver', pero no especificó accesorio: responde SOLO TEXTO y pregunta qué accesorio quiere ver (lámpara, jarrón, cuadro, sofá).")
        }
    }

    // Guía integral para espacios del hogar
    if (isHomeSpace) {
        sb.appendLine("""
            El usuario pide recomendaciones para un espacio interior del hogar.
            Genera una guía integral con:
            - Estilo recomendado, paleta base+acentos, materiales y texturas.
            - Distribución y escalas (medidas orientativas), iluminación en capas.
            - Textiles y tapetes (tamaños), arte/paredes (altura/agrupación), plantas/detalles.
            - Si es pequeño: trucos de amplitud visual y funcionalidad.
            - Cierra con 2–3 próximos pasos accionables.
            Sé conciso: 6–10 viñetas en total.
        """.trimIndent())

        if (mentionsSmallSpace(userText)) {
            sb.appendLine("Incluye trucos para espacios pequeños (alturas, espejos, continuidad de color, muebles elevados, almacenamiento integrado).")
        }
    }

    sb.appendLine("\nContexto reciente:")
    slice.forEach { m ->
        val who = if (m.esUsuario) "Usuario" else "Asistente"
        if (m.texto.isNotBlank()) sb.appendLine("- $who: ${m.texto.take(400)}")
    }
    sb.appendLine("\nUsuario ahora dice: $userText")
    sb.appendLine("\nResponde breve, accionable y siguiendo las reglas anteriores.")
    return sb.toString()
}

// =====================
// Utilidad de catálogo (SIEMPRE mismo accesorio)
// =====================
private suspend fun cargarMasProductosPorCategoria(
    style: StyleDb,
    accessory: AccessoryDb,
    maxItems: Int = 8,
    yaMostradas: Set<String>
): List<String> {
    val productos = RAProductsRepo.loadProductos(style.dbValue, accessory.dbValue)
    return productos.asSequence()
        .mapNotNull { it.imageUrl }
        .filter { it.startsWith("http") && it !in yaMostradas }
        .take(maxItems)
        .toList()
}

suspend fun cargarMasOpciones(
    styleUiText: String,
    accessoryUiText: String,
    yaMostradas: Set<String>,
    onResult: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val style = parseStyle(styleUiText) ?: return onError("Estilo no reconocido")
    val acc = parseAccessory(accessoryUiText) ?: return onError("Accesorio no reconocido")

    try {
        val urls = cargarMasProductosPorCategoria(style, acc, maxItems = 8, yaMostradas)
        onResult(urls)
    } catch (e: Exception) {
        onError("No fue posible cargar productos: ${e.message}")
    }
}

suspend fun onCategoriaSeleccionada(
    styleUiText: String,
    accessoryUiText: String,
    onResult: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val style = parseStyle(styleUiText) ?: return onError("Estilo no reconocido")
    val acc   = parseAccessory(accessoryUiText) ?: return onError("Accesorio no reconocido")

    try {
        val urls = cargarMasProductosPorCategoria(style, acc, maxItems = 8, yaMostradas = emptySet())
        onResult(urls)
    } catch (e: Exception) {
        onError("No fue posible cargar productos: ${e.message}")
    }
}

// =====================
// Pantalla de Chat
// =====================
@Composable
fun PantallaChatIA(
    navController: NavController? = null,
    chatId: String? = null
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val listaMensajes = remember { mutableStateListOf<MensajeIA>() }
    var prompt by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    // Pickers
    val pickFromGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { selectedImage = uri; selectedBitmap = null }
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) { selectedBitmap = bmp; selectedImage = null }
        else scope.launch { snackbarHostState.showSnackbar("No se pudo tomar la foto") }
    }
    val requestCameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) takePhoto.launch() else scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") }
    }

    var sessionId by remember { mutableStateOf<String?>(null) }
    var pusoTitulo by remember { mutableStateOf(false) }

    // Cargar historial
    LaunchedEffect(chatId) {
        val uid = auth.currentUser?.uid ?: run {
            snackbarHostState.showSnackbar("Debes iniciar sesión para chatear"); return@LaunchedEffect
        }
        if (chatId != null) {
            sessionId = chatId
            try {
                val qs = db.collection("sessions").document(chatId)
                    .collection("messages")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get().await()

                listaMensajes.clear()
                val historial = qs.documents.map { d ->
                    val role = d.getString("role").orEmpty()
                    val text = d.getString("text").orEmpty()
                    val userUploadedImageUrl = d.getString("imageUrl")
                    val productUrl = d.getString("productImageUrl")
                    MensajeIA(
                        id = d.getTimestamp("createdAt")?.toDate()?.time ?: System.nanoTime(),
                        texto = text,
                        esUsuario = (role == "user"),
                        imageUri = userUploadedImageUrl?.toUri(),
                        bitmap = null,
                        productImageUrl = productUrl
                    )
                }
                listaMensajes.addAll(historial)
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Error cargando mensajes: ${e.message}") }
            }
        }
    }

    // Scroll auto
    LaunchedEffect(listaMensajes.size) {
        if (listaMensajes.isNotEmpty()) listState.animateScrollToItem(listaMensajes.size - 1)
    }

    // Mapear a la UI
    val uiMessages: List<ChatMessageUIModel> = listaMensajes.map { m ->
        ChatMessageUIModel(
            id = m.id.toString(),
            text = m.texto,
            imageUri = m.imageUri,
            productImageUrl = m.productImageUrl,
            isUser = m.esUsuario
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ChatIAScreenUI(
                messages = uiMessages,
                listState = listState,
                inputText = prompt,
                onInputChange = { prompt = it },
                loading = loading,
                selectedImage = selectedImage,
                selectedBitmap = selectedBitmap,
                onAttachGallery = { pickFromGallery.launch("image/*") },
                onAttachCamera = { requestCameraPermission.launch(android.Manifest.permission.CAMERA) },
                onRemoveAttachment = { selectedImage = null; selectedBitmap = null },
                onSend = {
                    scope.launch {
                        if (loading) return@launch
                        val uid = auth.currentUser?.uid ?: run {
                            snackbarHostState.showSnackbar("Debes iniciar sesión para chatear"); return@launch
                        }
                        val sid = sessionId ?: run {
                            val ref = db.collection("sessions").add(
                                mapOf(
                                    "ownerId" to uid,
                                    "title" to "Nueva conversación",
                                    "createdAt" to Timestamp.now(),
                                    "lastMessageAt" to Timestamp.now(),
                                    "active" to true
                                )
                            ).await().id
                            sessionId = ref; ref
                        }

                        enviarMensajeConGemini(
                            context = context,
                            db = db,
                            sessionId = sid,
                            prompt = prompt,
                            imageUri = selectedImage,
                            bitmap = selectedBitmap,
                            listaMensajes = listaMensajes,
                            focus = focus,
                            onStart = { loading = true; prompt = "" },
                            onDone = {
                                loading = false
                                selectedImage = null
                                selectedBitmap = null
                            },
                            onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                            setTitleIfNeeded = { firstText ->
                                if (!pusoTitulo && firstText.isNotBlank()) {
                                    db.collection("sessions").document(sid).update("title", firstText.take(40))
                                    pusoTitulo = true
                                }
                            }
                        )
                        keyboard?.hide()
                    }
                },
                onBack = { navController?.popBackStack() },
                onHistory = { navController?.navigate("chatguardados") },
                onHome = { navController?.navigate("principal") { popUpTo(0) { inclusive = true } } },
                onProfile = { navController?.navigate("perfil") }
            )
        }
    }
}

// =====================
// Upload helpers
// =====================
suspend fun uploadImageToStorage(sessionId: String, imageUri: Uri, context: Context): String? {
    val storage = FirebaseStorage.getInstance()
    val imageRef = storage.reference.child("sessions/$sessionId/${UUID.randomUUID()}.jpg")
    return try {
        context.contentResolver.openInputStream(imageUri)?.let { input ->
            imageRef.putStream(input).await()
            imageRef.downloadUrl.await().toString()
        }
    } catch (e: Exception) {
        Log.e("PantallaChatIA", "Error al subir la imagen", e); null
    }
}

suspend fun uploadBitmapToStorage(sessionId: String, bitmap: Bitmap, context: Context): String? {
    val temp = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
    FileOutputStream(temp).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    val url = uploadImageToStorage(sessionId, temp.toUri(), context)
    temp.delete()
    return url
}

// =====================
// Flujo principal (Chat)
// =====================
private fun enviarMensajeConGemini(
    context: Context,
    db: FirebaseFirestore,
    sessionId: String,
    prompt: String,
    imageUri: Uri?,
    bitmap: Bitmap?,
    listaMensajes: MutableList<MensajeIA>,
    focus: FocusManager,
    onStart: () -> Unit,
    onDone: () -> Unit,
    onError: (String) -> Unit,
    setTitleIfNeeded: (String) -> Unit
) {
    val textoPlano = prompt.trim()
    if (textoPlano.isBlank() && imageUri == null && bitmap == null) return

    focus.clearFocus()
    onStart()

    val userMessage = MensajeIA(
        id = System.nanoTime(),
        texto = textoPlano,
        esUsuario = true,
        imageUri = imageUri,
        bitmap = bitmap
    )
    listaMensajes.add(userMessage)

    CoroutineScope(Dispatchers.IO).launch {
        val sessionRef = db.collection("sessions").document(sessionId)
        try {
            // Subir imagen si hay
            val imageUrl: String? = when {
                imageUri != null -> uploadImageToStorage(sessionId, imageUri, context)
                bitmap != null   -> uploadBitmapToStorage(sessionId, bitmap, context)
                else             -> null
            }

            withContext(Dispatchers.Main) {
                val pos = listaMensajes.indexOf(userMessage)
                if (pos != -1 && imageUrl != null) {
                    listaMensajes[pos] = listaMensajes[pos].copy(
                        imageUri = imageUrl.toUri(),
                        bitmap = null,
                        id = System.nanoTime()
                    )
                }
            }

            // Persistir turno usuario
            val userMap = mutableMapOf<String, Any>(
                "role" to "user",
                "text" to textoPlano,
                "createdAt" to Timestamp.now()
            )
            if (imageUrl != null) userMap["imageUrl"] = imageUrl
            sessionRef.collection("messages").add(userMap).await()
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

            setTitleIfNeeded(textoPlano.ifBlank { "Imagen" })

            // ===== CONTEXTO / Intenciones =====
            val opciones = extraerOpcionesDelUltimoAsistente(listaMensajes)
            val userTextForModel = opciones?.let {
                detectarIndiceElegido(textoPlano, it.items.size)?.let { idx ->
                    "El usuario eligió la opción ${idx + 1}: ${it.items[idx]}"
                }
            } ?: textoPlano

            val wantsImages = shouldShowCatalogIntent(textoPlano)
            val accessoryRequested = parseAccessory(textoPlano) != null
            val isHomeSpace = isHomeSpaceRequest(textoPlano)
            // imágenes si el usuario las quiere
            val wantsAccessoryImages = wantsImages && accessoryRequested

            val contextualPrompt = buildContextualPrompt(
                listaMensajes, userTextForModel, wantsImages, isHomeSpace, accessoryRequested
            )

            // ===== Llamar a Gemini con contexto =====
            val (respuesta, _) = GeminiService.askGeminiSuspend(
                prompt = contextualPrompt,
                imageUri = imageUri,
                bitmap = bitmap,
                context = context
            )

            // 1) URLs que diga el modelo (limpiamos si no corresponde)
            val urlRegex = Regex("""https?://\S+?\.(?:jpg|jpeg|png|webp)(\?\S+)?""", RegexOption.IGNORE_CASE)
            var modelUrls = if (wantsAccessoryImages) {
                urlRegex.findAll(respuesta).map { it.value }.toList()
            } else emptyList()

            var textoLimpio = respuesta
                .let { if (!wantsAccessoryImages) it.replace(urlRegex, "") else it }
                .replace(Regex("\n{3,}"), "\n\n")
                .trim()

            // 2) Catálogo del MISMO accesorio (multi-estilo) si no hubo URLs y el usuario pidió ver imágenes de accesorio
            var usedCatalog = false
            val catalogUrls: MutableList<String> = mutableListOf()
            var catalogFoundForRequestedType = false
            val stylesUsed: MutableList<StyleDb> = mutableListOf()

            if (wantsAccessoryImages && modelUrls.isEmpty()) {
                try {
                    val acc = parseAccessory(textoPlano) ?: AccessoryDb.LAMPARA
                    val preferred = parseStyle(textoPlano)
                    val targetCount = 3

                    suspend fun loadUrls(style: StyleDb, accessory: AccessoryDb, take: Int): List<String> {
                        val productos = RAProductsRepo.loadProductos(style.dbValue, accessory.dbValue)
                        if (productos.isEmpty()) return emptyList()
                        catalogFoundForRequestedType = true
                        return productos.asSequence()
                            .mapNotNull { it.imageUrl }
                            .filter { it.startsWith("http") }
                            .take(take)
                            .toList()
                    }

                    if (preferred != null) {
                        val urlsPref = loadUrls(preferred, acc, targetCount)
                        if (urlsPref.isNotEmpty()) {
                            catalogUrls.addAll(urlsPref)
                            stylesUsed.add(preferred)
                        }
                    }
                    if (catalogUrls.size < targetCount) {
                        val remaining = ALL_STYLES_ORDERED.filter { it != preferred }
                        for (style in remaining) {
                            val need = targetCount - catalogUrls.size
                            if (need <= 0) break
                            val u = loadUrls(style, acc, need)
                            if (u.isNotEmpty()) {
                                catalogUrls.addAll(u)
                                stylesUsed.add(style)
                            }
                        }
                    }
                    usedCatalog = catalogUrls.isNotEmpty()
                    Log.d("PantallaChatIA", "Catalog used=$usedCatalog acc=$acc preferred=${preferred?.dbValue} styles=${stylesUsed.map{it.dbValue}}")
                } catch (e: Exception) {
                    Log.e("PantallaChatIA", "Fallback catálogo falló: ${e.message}", e)
                }
            }

            // 3) Elegir URLs finales y deduplicar (incluye ya mostradas)
            var urls = if (modelUrls.isNotEmpty()) modelUrls else catalogUrls
            val yaMostradas = buildSet {
                listaMensajes.mapNotNullTo(this) { it.productImageUrl?.trim() }
                listaMensajes.mapNotNullTo(this) { it.imageUri?.toString()?.trim() }
            }

            urls = urls.map { it.trim() }.filter { it.isNotBlank() }.distinct().filter { it !in yaMostradas }

            // 4) Ajustar texto coherente
            if (usedCatalog && urls.isNotEmpty()) {
                textoLimpio = if (catalogFoundForRequestedType) {
                    "Estas son 2–3 referencias visuales del accesorio que mencionaste. ¿Quieres ver más?"
                } else {
                    val estilosMsg = stylesUsed.joinToString { it.dbValue }
                    "No encontré exactamente ese estilo, pero te muestro alternativas cercanas ($estilosMsg). ¿Te sirven?"
                }
            } else if (!usedCatalog && modelUrls.isEmpty()) {
                if (textoLimpio.isBlank()) {
                    val accPedido = parseAccessory(textoPlano)
                    textoLimpio = when (accPedido) {
                        AccessoryDb.SOFA   -> "No encontré sofás con ese criterio. ¿Probamos con otro estilo (mediterráneo, industrial o clásico)?"
                        AccessoryDb.CUADRO -> "No encontré cuadros con ese criterio. ¿Probamos otro estilo o tamaño?"
                        AccessoryDb.JARRON -> "No encontré jarrones con ese criterio. ¿Probamos otro estilo o material?"
                        else               -> "Por ahora no encontré opciones para ese criterio. ¿Quieres que intentemos con otro estilo o accesorio?"
                    }
                }
            }

            // 5) Render en UI (IMÁGENES solo si wantsAccessoryImages)
            withContext(Dispatchers.Main) {
                if (textoLimpio.isNotBlank()) {
                    listaMensajes.add(MensajeIA(id = System.nanoTime(), texto = textoLimpio, esUsuario = false))
                }
                if (wantsAccessoryImages) {
                    urls.forEach { u ->
                        listaMensajes.add(
                            MensajeIA(
                                id = System.nanoTime(),
                                texto = "",
                                esUsuario = false,
                                imageUri = null,
                                productImageUrl = u
                            )
                        )
                    }
                }
                onDone()
            }

            // 6) Persistir en Firestore
            if (textoLimpio.isNotBlank()) {
                sessionRef.collection("messages").add(
                    mapOf("role" to "assistant", "text" to textoLimpio, "createdAt" to Timestamp.now())
                ).await()
            }
            if (wantsAccessoryImages) {
                urls.forEach { u ->
                    sessionRef.collection("messages").add(
                        mapOf("role" to "assistant", "productImageUrl" to u, "createdAt" to Timestamp.now())
                    ).await()
                }
            }
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onDone(); onError("Error: ${e.message}") }
        }
    }
}
