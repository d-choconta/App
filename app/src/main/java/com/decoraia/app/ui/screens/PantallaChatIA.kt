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

// ===== Heurísticas para estilo/tipo del catálogo =====
private fun guessStyleToDb(text: String): String {
    val t = text.lowercase()
    return when {
        "mediter" in t -> "mediterraneo"
        "minimal" in t -> "minimalista"
        "industrial" in t -> "industrial"
        "clásic" in t || "clasico" in t || "clasíc" in t -> "clasico"
        else -> "mediterraneo" // por defecto
    }
}

// colección 'products' el type es "lampara"
private fun guessTypeToDb(@Suppress("UNUSED_PARAMETER") text: String): String = "lampara"

// ===== Modelo interno =====
data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean,
    val imageUri: Uri? = null,           // puede ser content:// o https://
    val bitmap: Bitmap? = null,
    val productImageUrl: String? = null  // si la IA devuelve una sola url
)

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
                    val imageUrl = d.getString("imageUrl")
                    val productUrl = d.getString("productImageUrl")
                    MensajeIA(
                        id = d.getTimestamp("createdAt")?.toDate()?.time ?: System.nanoTime(),
                        texto = text,
                        esUsuario = (role == "user"),
                        imageUri = (productUrl ?: imageUrl)?.toUri(),
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

// ===== Upload helpers =====
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

// ===== Flujo principal =====
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

    // pinta el turno del usuario
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
            // subir imagen si la hay
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

            // persistir turno usuario
            val userMap = mutableMapOf<String, Any>(
                "role" to "user",
                "text" to textoPlano,
                "createdAt" to Timestamp.now()
            )
            if (imageUrl != null) userMap["imageUrl"] = imageUrl
            sessionRef.collection("messages").add(userMap).await()
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

            setTitleIfNeeded(textoPlano.ifBlank { "Imagen" })

            // ===== Llamar a Gemini =====
            val (respuesta, _) = GeminiService.askGeminiSuspend(
                prompt = textoPlano,
                imageUri = imageUri,
                bitmap = bitmap,
                context = context
            )

            // 1) Extraer URLs de imágenes devueltas por el modelo
            val urlRegex = Regex("""https?://\S+?\.(?:jpg|jpeg|png|webp)(\?\S+)?""", RegexOption.IGNORE_CASE)
            var urls = urlRegex.findAll(respuesta).map { it.value }.toList()
            val textoLimpio = respuesta.replace(urlRegex, "").replace(Regex("\n{3,}"), "\n\n").trim()

            // 2) FALLBACK al catálogo si el modelo no pegó URLs
            if (urls.isEmpty()) {
                try {
                    val styleDb = guessStyleToDb(textoPlano)
                    val typeDb  = guessTypeToDb(textoPlano)
                    val productos = RAProductsRepo
                        .loadProductos(style = styleDb, typeValue = typeDb)
                        .filter { it.imageUrl.startsWith("http") }
                        .take(3)
                    urls = productos.map { it.imageUrl }
                    Log.d("PantallaChatIA", "Fallback catálogo: style=$styleDb type=$typeDb count=${urls.size}")
                } catch (e: Exception) {
                    Log.e("PantallaChatIA", "Fallback catálogo falló: ${e.message}", e)
                }
            }

            withContext(Dispatchers.Main) {
                // 3) mensaje de texto (si queda)
                if (textoLimpio.isNotBlank()) {
                    listaMensajes.add(
                        MensajeIA(
                            id = System.nanoTime(),
                            texto = textoLimpio,
                            esUsuario = false
                        )
                    )
                }
                // 4) imágenes como mensajes separados
                urls.forEach { u ->
                    listaMensajes.add(
                        MensajeIA(
                            id = System.nanoTime(),
                            texto = "",
                            esUsuario = false,
                            imageUri = u.toUri(),
                            productImageUrl = u
                        )
                    )
                }
                onDone()
            }

            // 5) Guardar en Firestore
            if (textoLimpio.isNotBlank()) {
                sessionRef.collection("messages").add(
                    mapOf("role" to "assistant", "text" to textoLimpio, "createdAt" to Timestamp.now())
                ).await()
            }
            urls.forEach { u ->
                sessionRef.collection("messages").add(
                    mapOf("role" to "assistant", "productImageUrl" to u, "createdAt" to Timestamp.now())
                ).await()
            }
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onDone(); onError("Error: ${e.message}") }
            }
        }
}