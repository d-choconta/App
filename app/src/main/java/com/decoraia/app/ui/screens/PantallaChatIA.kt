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
enum class StyleDb(val dbValue: String) { MINIMALISTA("minimalista"), INDUSTRIAL("industrial"), MEDITERRANEO("mediterraneo"), CLASICO("clasico") }
enum class AccessoryDb(val dbValue: String) { JARRON("jarron"), CUADRO("cuadro"), LAMPARA("lampara"), SOFA("sofa") }
private val ALL_STYLES_ORDERED = listOf(StyleDb.MEDITERRANEO, StyleDb.MINIMALISTA, StyleDb.INDUSTRIAL, StyleDb.CLASICO)

// =====================
// Constantes / Logs / Mensajes
// =====================
private const val TAG = "PantallaChatIA"
private const val NO_RESULTS_MSG =
    "No encontré imágenes con ese criterio. Prueba así: ‘minimalista lámpara’, ‘industrial sofá’ o dime ‘ver cuadros mediterráneos’."

// =====================
// Sinónimos
// =====================
private val STYLE_SYNONYMS = mapOf(
    "minimalista" to StyleDb.MINIMALISTA, "minimal" to StyleDb.MINIMALISTA,
    "industrial" to StyleDb.INDUSTRIAL,
    "mediterraneo" to StyleDb.MEDITERRANEO, "mediterráneo" to StyleDb.MEDITERRANEO, "mediter" to StyleDb.MEDITERRANEO,
    "clasico" to StyleDb.CLASICO, "clásico" to StyleDb.CLASICO
)
private val ACCESSORY_SYNONYMS = mapOf(
    "jarron" to AccessoryDb.JARRON, "jarrón" to AccessoryDb.JARRON, "jarrones" to AccessoryDb.JARRON,
    "florero" to AccessoryDb.JARRON, "floreros" to AccessoryDb.JARRON, "vase" to AccessoryDb.JARRON, "vases" to AccessoryDb.JARRON,
    "cuadro" to AccessoryDb.CUADRO, "cuadros" to AccessoryDb.CUADRO, "lámina" to AccessoryDb.CUADRO, "lamina" to AccessoryDb.CUADRO, "arte" to AccessoryDb.CUADRO, "art" to AccessoryDb.CUADRO, "painting" to AccessoryDb.CUADRO,
    "lampara" to AccessoryDb.LAMPARA, "lámpara" to AccessoryDb.LAMPARA, "lamparas" to AccessoryDb.LAMPARA, "lámparas" to AccessoryDb.LAMPARA, "luminaria" to AccessoryDb.LAMPARA, "iluminacion" to AccessoryDb.LAMPARA, "lighting" to AccessoryDb.LAMPARA, "pendant" to AccessoryDb.LAMPARA,
    "sofa" to AccessoryDb.SOFA, "sofá" to AccessoryDb.SOFA, "sofas" to AccessoryDb.SOFA, "sofás" to AccessoryDb.SOFA, "sillon" to AccessoryDb.SOFA, "sillón" to AccessoryDb.SOFA
)

private fun parseStyle(text: String): StyleDb? {
    val t = text.lowercase()
    return STYLE_SYNONYMS.entries.firstOrNull { (k, _) ->
        Regex("""(^|[\s,.;:!¡¿?\-/])${Regex.escape(k)}($|[\s,.;:!¡¿?\-/])""").containsMatchIn(t)
    }?.value
}

private fun parseAccessory(text: String): AccessoryDb? {
    val t = text.lowercase()
    return ACCESSORY_SYNONYMS.entries.firstOrNull { (k, _) ->
        Regex("""(^|[\s,.;:!¡¿?\-/])${Regex.escape(k)}($|[\s,.;:!¡¿?\-/])""").containsMatchIn(t)
    }?.value
}

// Intención de catálogo/imagenes.
// Dispara si hay verbos típicos (más estrictos) O si el usuario menciona un accesorio.
private fun shouldShowCatalogIntent(text: String): Boolean {
    val t = text.lowercase()
    val strongTriggers = listOf(
        "muestra", "muéstrame", "muestrame",
        "enséñame", "ensename",
        "suger", "recom", "ideas", "opciones",
        "catálogo", "catalogo", "quiero ver", "ver ",
        "busco", "buscar ",
        "imagen", "imágen", "foto", "fotos",
        "catálogo de", "catalogo de"
    )
    val hasVerbTrigger = strongTriggers.any { it in t }
    val mentionsAccessory = parseAccessory(t) != null
    return hasVerbTrigger || mentionsAccessory
}

private fun isSimilarRequest(text: String): Boolean {
    val t = text.lowercase()
    val keys = listOf("similar","parecid","como esa","como ese","como la","como el","otro de esos","ver algo parecido","algo parecido")
    return keys.any { it in t }
}

private fun isGreetingOrSmalltalk(text: String): Boolean {
    val t = text.trim().lowercase()
    if (t.isBlank()) return true
    val greet = listOf("hola","holi","buenas","buenos dias","buenos días","buenas tardes","buenas noches","que tal","qué tal")
    return greet.any { t.startsWith(it) } && parseAccessory(t) == null && parseStyle(t) == null && !shouldShowCatalogIntent(t)
}

// =====================
// Candidatos de tipo para Firestore
// =====================
private fun typeCandidates(a: AccessoryDb) = when (a) {
    AccessoryDb.CUADRO -> listOf("cuadro","cuadros","arte","art","painting")
    AccessoryDb.LAMPARA -> listOf("lampara","lámpara","lamparas","lámparas","luminaria","iluminacion","lighting","pendant")
    AccessoryDb.SOFA -> listOf("sofa","sofá","sofas","sofás","sillon","sillón")
    AccessoryDb.JARRON -> listOf("jarron","jarrón","jarrones","florero","floreros","vase","vases")
}

// =====================
// Modelo de mensaje
// =====================
data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean,
    val imageUri: Uri? = null,
    val bitmap: Bitmap? = null,
    val productImageUrl: String? = null
)

// =====================
// Helpers selección
// =====================
private fun detectarIndiceElegido(userText: String, total: Int): Int? {
    if (total <= 0) return null
    val t = userText.lowercase()
    Regex("""\b(\d{1,2})\b""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { n -> if (n in 1..total) return n - 1 }
    val mapa = mapOf("primera" to 0,"1ra" to 0,"1era" to 0,"segunda" to 1,"2da" to 1,"tercera" to 2,"3ra" to 2,"cuarta" to 3,"4ta" to 3,"quinta" to 4,"5ta" to 4)
    for ((k,v) in mapa) if (t.contains(k) && v < total) return v
    Regex("""opci[oó]n\s+(\d{1,2})""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { n -> if (n in 1..total) return n - 1 }
    return null
}

private fun buildContextualPrompt(historial: List<MensajeIA>, userText: String): String {
    val slice = historial.takeLast(12)
    return buildString {
        appendLine("Eres un asesor de decoración. No repitas saludos ni presentaciones. Continúa la conversación según el contexto y avanza sin reexplicar lo anterior.")
        appendLine("\nContexto reciente:")
        slice.forEach { m -> if (m.texto.isNotBlank()) appendLine("- ${if (m.esUsuario) "Usuario" else "Asistente"}: ${m.texto.take(400)}") }
        appendLine("\nUsuario ahora dice: $userText")
        appendLine("\nResponde breve y accionable, siguiendo esa intención.")
    }
}

// =====================
// Utilidad catálogo (devuelve urls y el estilo usado)
// =====================
private suspend fun queryUrlsWithTypeCandidates(
    style: StyleDb?, accessory: AccessoryDb, maxItems: Int
): Pair<List<String>, StyleDb?> {
    val cand = typeCandidates(accessory)

    if (style != null) {
        for (t in cand) {
            val p = RAProductsRepo.loadProductos(style.dbValue, t)
            if (p.isNotEmpty()) {
                val urls = p.asSequence().mapNotNull { it.imageUrl }.filter { it.startsWith("http") }.take(maxItems).toList()
                Log.d(TAG, "queryUrlsWithTypeCandidates(preferred): urls=${urls.size}, stylePreferida=${style.dbValue}, type=$t")
                return urls to style
            }
        }
    }
    for (s in ALL_STYLES_ORDERED) {
        for (t in cand) {
            val p = RAProductsRepo.loadProductos(s.dbValue, t)
            if (p.isNotEmpty()) {
                val urls = p.asSequence().mapNotNull { it.imageUrl }.filter { it.startsWith("http") }.take(maxItems).toList()
                Log.d(TAG, "queryUrlsWithTypeCandidates(fallback): urls=${urls.size}, usado=${s.dbValue}, type=$t")
                return urls to s
            }
        }
    }
    Log.d(TAG, "queryUrlsWithTypeCandidates: sin resultados para accessory=${accessory.dbValue}")
    return emptyList<String>() to null
}

suspend fun cargarProductosPorCategoria(style: StyleDb, accessory: AccessoryDb, maxItems: Int = 8): List<String> {
    var (urls, _) = queryUrlsWithTypeCandidates(style, accessory, maxItems)
    if (urls.isEmpty() && accessory != AccessoryDb.LAMPARA) {
        urls = queryUrlsWithTypeCandidates(style, AccessoryDb.LAMPARA, maxItems).first
        if (urls.isEmpty()) urls = queryUrlsWithTypeCandidates(null, AccessoryDb.LAMPARA, maxItems).first
    }
    return urls
}

// =====================
// Composable Chat
// =====================
@Composable
fun PantallaChatIA(navController: NavController? = null, chatId: String? = null) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val listaMensajes = remember { mutableStateListOf<MensajeIA>() }
    val ultimoCatalogo = remember { mutableStateListOf<String>() }

    // NUEVO: memoria del último estilo/objeto usado para “similar”
    val lastStyleUsed = remember { mutableStateOf<StyleDb?>(null) }
    val lastAccessoryUsed = remember { mutableStateOf<AccessoryDb?>(null) }

    var prompt by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val pickFromGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) { selectedImage = uri; selectedBitmap = null } }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp -> if (bmp != null) { selectedBitmap = bmp; selectedImage = null } else scope.launch { snackbarHostState.showSnackbar("No se pudo tomar la foto") } }
    val requestCameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) takePhoto.launch() else scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") } }

    var sessionId by remember { mutableStateOf<String?>(null) }
    var pusoTitulo by remember { mutableStateOf(false) }

    // Cargar historial (sin duplicados)
    LaunchedEffect(chatId) {
        val uid = auth.currentUser?.uid ?: run { snackbarHostState.showSnackbar("Debes iniciar sesión para chatear"); return@LaunchedEffect }
        if (chatId != null) {
            sessionId = chatId
            try {
                val qs = db.collection("sessions").document(chatId).collection("messages").orderBy("createdAt", Query.Direction.ASCENDING).get().await()
                listaMensajes.clear()
                val seen = mutableSetOf<String>()
                for (d in qs.documents) {
                    val role = d.getString("role").orEmpty()
                    val text = d.getString("text").orEmpty()
                    val userImageUrl = d.getString("imageUrl")
                    val catalogImageUrl = d.getString("productImageUrl")
                    val createdId = d.getTimestamp("createdAt")?.toDate()?.time ?: System.nanoTime()
                    when {
                        role == "assistant" && !catalogImageUrl.isNullOrBlank() && seen.add(catalogImageUrl) ->
                            listaMensajes.add(MensajeIA(createdId, "", false, null, null, catalogImageUrl))
                        role == "assistant" && text.isNotBlank() ->
                            listaMensajes.add(MensajeIA(createdId, text, false))
                        role == "user" ->
                            listaMensajes.add(MensajeIA(createdId, text, true, userImageUrl?.toUri()))
                    }
                }
                // reconstruir último bloque para “me gusta la X”
                ultimoCatalogo.clear()
                val lastUrls = mutableListOf<String>()
                var started = false
                for (i in listaMensajes.indices.reversed()) {
                    val m = listaMensajes[i]
                    if (!m.esUsuario && m.productImageUrl != null) { started = true; lastUrls.add(0, m.productImageUrl) }
                    else if (started) break
                }
                ultimoCatalogo.addAll(lastUrls)
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Error cargando mensajes: ${e.message}") }
            }
        }
    }

    LaunchedEffect(listaMensajes.size) { if (listaMensajes.isNotEmpty()) listState.animateScrollToItem(listaMensajes.size - 1) }

    val uiMessages = listaMensajes.map { ChatMessageUIModel(it.id.toString(), it.texto, it.imageUri, it.productImageUrl, it.esUsuario) }

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
                        val uid = auth.currentUser?.uid ?: run { snackbarHostState.showSnackbar("Debes iniciar sesión para chatear"); return@launch }
                        val sid = sessionId ?: run {
                            val ref = db.collection("sessions").add(mapOf("ownerId" to uid,"title" to "Nueva conversación","createdAt" to Timestamp.now(),"lastMessageAt" to Timestamp.now(),"active" to true)).await().id
                            sessionId = ref; ref
                        }
                        enviarMensajeConGemini(
                            context, db, sid, prompt, selectedImage, selectedBitmap,
                            listaMensajes, ultimoCatalogo, lastStyleUsed, lastAccessoryUsed,
                            focus,
                            onStart = { loading = true; prompt = "" },
                            onDone = { loading = false; selectedImage = null; selectedBitmap = null },
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
        Log.e(TAG, "Error al subir la imagen", e); null
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
// Flujo principal
// =====================
private fun enviarMensajeConGemini(
    context: Context,
    db: FirebaseFirestore,
    sessionId: String,
    prompt: String,
    imageUri: Uri?,
    bitmap: Bitmap?,
    listaMensajes: MutableList<MensajeIA>,
    ultimoCatalogoMostrado: MutableList<String>,
    lastStyleUsed: MutableState<StyleDb?>,
    lastAccessoryUsed: MutableState<AccessoryDb?>,
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

    val userMessage = MensajeIA(id = System.nanoTime(), texto = textoPlano, esUsuario = true, imageUri = imageUri, bitmap = bitmap)
    listaMensajes.add(userMessage)

    CoroutineScope(Dispatchers.IO).launch {
        val sessionRef = db.collection("sessions").document(sessionId)
        try {
            Log.d(TAG, "Entrada usuario='${textoPlano}', intentCatalogo=${shouldShowCatalogIntent(textoPlano)}, acc=${parseAccessory(textoPlano)}, style=${parseStyle(textoPlano)}")

            val imageUrl: String? = when {
                imageUri != null -> uploadImageToStorage(sessionId, imageUri, context)
                bitmap != null -> uploadBitmapToStorage(sessionId, bitmap, context)
                else -> null
            }
            withContext(Dispatchers.Main) {
                val pos = listaMensajes.indexOf(userMessage)
                if (pos != -1 && imageUrl != null) {
                    listaMensajes[pos] = listaMensajes[pos].copy(imageUri = imageUrl.toUri(), bitmap = null, id = System.nanoTime())
                }
            }
            val userMap = mutableMapOf<String, Any>("role" to "user", "text" to textoPlano, "createdAt" to Timestamp.now())
            if (imageUrl != null) userMap["imageUrl"] = imageUrl
            sessionRef.collection("messages").add(userMap).await()
            sessionRef.update("lastMessageAt", Timestamp.now()).await()
            setTitleIfNeeded(textoPlano.ifBlank { "Imagen" })

            // Selección "opción N"
            run {
                detectarIndiceElegido(textoPlano, ultimoCatalogoMostrado.size)?.let { idx ->
                    val r = "¡Perfecto! Te gustó la opción ${idx + 1}. ¿Quieres ver similares, quieres seguir con otro estilo?"
                    withContext(Dispatchers.Main) { listaMensajes.add(MensajeIA(System.nanoTime(), r, false)); onDone() }
                    sessionRef.collection("messages").add(mapOf("role" to "assistant","text" to r,"createdAt" to Timestamp.now())).await()
                    sessionRef.update("lastMessageAt", Timestamp.now()).await()
                    return@launch
                }
                val num = Regex("""\b(\d{1,2})\b""").find(textoPlano)?.groupValues?.get(1)?.toIntOrNull()
                if (num != null) {
                    val r = "¡Hecho! Tomo nota de la opción $num. ¿Te muestro alternativas similares o prefieres otro estilo/objeto?"
                    withContext(Dispatchers.Main) { listaMensajes.add(MensajeIA(System.nanoTime(), r, false)); onDone() }
                    sessionRef.collection("messages").add(mapOf("role" to "assistant","text" to r,"createdAt" to Timestamp.now())).await()
                    sessionRef.update("lastMessageAt", Timestamp.now()).await()
                    return@launch
                }
            }

            // SALUDO
            if (isGreetingOrSmalltalk(textoPlano)) {
                val saludo = "¡Hola! Soy tu asistente de decoración. Dime el estilo (mediterráneo, minimalista, industrial o clásico) y el objeto (jarrones, cuadros, lámparas o sofás) y te muestro opciones."
                withContext(Dispatchers.Main) { listaMensajes.add(MensajeIA(System.nanoTime(), saludo, false)); onDone() }
                sessionRef.collection("messages").add(mapOf("role" to "assistant","text" to saludo,"createdAt" to Timestamp.now())).await()
                sessionRef.update("lastMessageAt", Timestamp.now()).await()
                return@launch
            }

            // PETICIÓN DE "SIMILAR"
            if (isSimilarRequest(textoPlano)) {
                val acc = parseAccessory(textoPlano) ?: lastAccessoryUsed.value
                val sty = parseStyle(textoPlano) ?: lastStyleUsed.value
                if (acc == null && sty == null) {
                    val txt = "¿De qué objeto quieres ver algo similar (sofá, lámpara, cuadro, jarrón) y en qué estilo (minimalista, industrial, mediterráneo, clásico)?"
                    withContext(Dispatchers.Main) { listaMensajes.add(MensajeIA(System.nanoTime(), txt, false)); onDone() }
                    sessionRef.collection("messages").add(mapOf("role" to "assistant","text" to txt,"createdAt" to Timestamp.now())).await()
                    sessionRef.update("lastMessageAt", Timestamp.now()).await()
                    return@launch
                }
                val accUse = acc ?: AccessoryDb.LAMPARA
                val target = 3
                val (urls, usedStyle) = queryUrlsWithTypeCandidates(sty, accUse, target)
                val finalUrls = if (urls.isNotEmpty()) urls else queryUrlsWithTypeCandidates(null, accUse, target).first
                withContext(Dispatchers.Main) {
                    ultimoCatalogoMostrado.clear()
                    ultimoCatalogoMostrado.addAll(finalUrls)
                    if (finalUrls.isEmpty()) {
                        listaMensajes.add(MensajeIA(System.nanoTime(), NO_RESULTS_MSG, false))
                    } else {
                        finalUrls.forEach { u -> listaMensajes.add(MensajeIA(System.nanoTime(), "", false, null, null, u)) }
                        lastAccessoryUsed.value = accUse
                        lastStyleUsed.value = usedStyle ?: lastStyleUsed.value
                    }
                    onDone()
                }
                if (finalUrls.isEmpty()) {
                    sessionRef.collection("messages").add(mapOf("role" to "assistant","text" to NO_RESULTS_MSG,"createdAt" to Timestamp.now())).await()
                } else {
                    finalUrls.forEach { u -> sessionRef.collection("messages").add(mapOf("role" to "assistant","productImageUrl" to u,"createdAt" to Timestamp.now())).await() }
                }
                sessionRef.update("lastMessageAt", Timestamp.now()).await()
                return@launch
            }

            // MODELO
            val contextualPrompt = buildContextualPrompt(listaMensajes, textoPlano)
            val (respuesta, _) = GeminiService.askGeminiSuspend(prompt = contextualPrompt, imageUri = imageUri, bitmap = bitmap, context = context)

            // URLs del modelo + texto limpio
            val urlRegex = Regex("""https?://\S+?\.(?:jpg|jpeg|png|webp)(\?\S+)?""", RegexOption.IGNORE_CASE)
            val modelUrls = urlRegex.findAll(respuesta).map { it.value }.toList()
            val textoLimpio = respuesta.replace(urlRegex, "").replace(Regex("\n{3,}"), "\n\n").trim()

            // Catálogo si el usuario pidió explícitamente ver opciones o mencionó un accesorio
            var catalogUrls = emptyList<String>()
            var usedStyleByCatalog: StyleDb? = null
            if (modelUrls.isEmpty() && (shouldShowCatalogIntent(textoPlano) || parseAccessory(textoPlano) != null)) {
                val requestedAcc = parseAccessory(textoPlano) ?: AccessoryDb.LAMPARA
                val preferred = parseStyle(textoPlano)
                val target = 3
                val (u1, s1) = queryUrlsWithTypeCandidates(preferred, requestedAcc, target)
                var urls = u1; usedStyleByCatalog = s1
                if (urls.isEmpty() && requestedAcc != AccessoryDb.LAMPARA) {
                    val (alt, s2) = queryUrlsWithTypeCandidates(preferred, AccessoryDb.LAMPARA, target)
                    urls = if (alt.isNotEmpty()) alt else queryUrlsWithTypeCandidates(null, AccessoryDb.LAMPARA, target).first
                    usedStyleByCatalog = s2 ?: usedStyleByCatalog
                }
                catalogUrls = urls
            }

            // Elegir qué mostrar
            var urls: List<String> = if (modelUrls.isNotEmpty()) modelUrls else catalogUrls

            val yaMostradas = buildSet {
                listaMensajes.mapNotNullTo(this) { it.productImageUrl?.trim() }
                listaMensajes.mapNotNullTo(this) { it.imageUri?.toString()?.trim() }
            }
            urls = urls.map { it.trim() }.filter { it.isNotBlank() }.distinct().filter { it !in yaMostradas }

            // NUEVO: si no hay URLs y el modelo dio texto útil, no fuerces catálogo
            val textoModeloUtil = textoLimpio.length >= 20
            if (urls.isEmpty() && (!shouldShowCatalogIntent(textoPlano) || textoModeloUtil)) {
                Log.w(TAG, "FALLBACK texto: modelUrls=0, catalogUrls=${catalogUrls.size}, textoLimpio='${textoLimpio.take(80)}...'")
                withContext(Dispatchers.Main) {
                    listaMensajes.add(MensajeIA(System.nanoTime(), textoLimpio.ifBlank { NO_RESULTS_MSG }, false))
                    onDone()
                }
                sessionRef.collection("messages").add(
                    mapOf("role" to "assistant","text" to textoLimpio.ifBlank { NO_RESULTS_MSG },"createdAt" to Timestamp.now())
                ).await()
                sessionRef.update("lastMessageAt", Timestamp.now()).await()
                return@launch
            }

            withContext(Dispatchers.Main) {
                // actualizar memoria para “similar” cuando hubo catálogo
                if (urls.isNotEmpty() && shouldShowCatalogIntent(textoPlano)) {
                    lastAccessoryUsed.value = parseAccessory(textoPlano) ?: lastAccessoryUsed.value ?: AccessoryDb.LAMPARA
                    lastStyleUsed.value = parseStyle(textoPlano) ?: usedStyleByCatalog ?: lastStyleUsed.value
                    ultimoCatalogoMostrado.clear()
                    ultimoCatalogoMostrado.addAll(urls)
                }

                if (urls.isNotEmpty()) {
                    urls.forEach { u -> listaMensajes.add(MensajeIA(System.nanoTime(), "", false, null, null, u)) }
                } else {
                    Log.w(TAG, "FALLBACK sin URLs ni texto útil: modelUrls=0, catalogUrls=${catalogUrls.size}")
                    val texto = if (!shouldShowCatalogIntent(textoPlano) && textoLimpio.isNotBlank()) textoLimpio else NO_RESULTS_MSG
                    listaMensajes.add(MensajeIA(System.nanoTime(), texto, false))
                }
                onDone()
            }

            // Persistencia
            if (urls.isNotEmpty()) {
                urls.forEach { u -> sessionRef.collection("messages").add(mapOf("role" to "assistant","productImageUrl" to u,"createdAt" to Timestamp.now())).await() }
            } else {
                val t = if (!shouldShowCatalogIntent(textoPlano) && textoLimpio.isNotBlank()) textoLimpio else NO_RESULTS_MSG
                sessionRef.collection("messages").add(mapOf("role" to "assistant","text" to t,"createdAt" to Timestamp.now())).await()
            }
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onDone(); onError("Error: ${e.message}") }
            }
        }
}