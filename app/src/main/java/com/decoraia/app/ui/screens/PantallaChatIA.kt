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
import com.decoraia.app.ui.components.ChatMessageUI
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

data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean,
    val imageUri: Uri? = null,
    val bitmap: Bitmap? = null
)
/** Extrae opciones numeradas del último mensaje del asistente (1) …, 1. …, 1: …, 1- …) */
private data class OpcionesExtraidas(val items: List<String>)

private fun extraerOpcionesDelUltimoAsistente(historial: List<MensajeIA>): OpcionesExtraidas? {
    val ultimoAsistente = historial.lastOrNull { !it.esUsuario }?.texto ?: return null
    val opciones = mutableListOf<String>()
    val regex = Regex("""^\s*(\d+)[\)\.\-:]\s*(.+)$""") // 1) xxx | 1. xxx | 1- xxx | 1: xxx
    ultimoAsistente.lines().forEach { line ->
        val m = regex.find(line.trim())
        if (m != null) {
            val texto = m.groupValues[2].trim()
            if (texto.isNotBlank()) opciones += texto
        }
    }
    return if (opciones.isNotEmpty()) OpcionesExtraidas(opciones) else null
}

/** Detecta “opción 1”, “la 2”, “primera/segunda/tercera…”. Devuelve índice 0-based. */
private fun detectarIndiceElegido(userText: String, total: Int): Int? {
    val t = userText.lowercase()

    // número explícito (1, 2, 3…)
    Regex("""\b(\d{1,2})\b""").find(t)?.let { m ->
        val n = m.groupValues[1].toIntOrNull()
        if (n != null && n in 1..total) return n - 1
    }

    // ordinales más comunes en español
    val mapa = mapOf(
        "primera" to 0, "1ra" to 0, "1era" to 0,
        "segunda" to 1, "2da" to 1,
        "tercera" to 2, "3ra" to 2,
        "cuarta" to 3,  "4ta" to 3,
        "quinta" to 4,  "5ta" to 4
    )
    for ((k, v) in mapa) if (t.contains(k) && v < total) return v

    // frases tipo “me quedo con la X”, “la opción X”
    Regex("""opci[oó]n\s+(\d{1,2})""").find(t)?.let { m ->
        val n = m.groupValues[1].toIntOrNull()
        if (n != null && n in 1..total) return n - 1
    }
    return null
}

/** Prompt compacto con CONTEXTO para que no repita introducción */
private fun buildContextualPrompt(historial: List<MensajeIA>, userText: String): String {
    val slice = historial.takeLast(12) // últimos turnos
    val sb = StringBuilder()
    sb.appendLine(
        """
        Eres un asesor de decoración. No repitas saludos ni presentaciones. 
        Continúa la conversación según el contexto y avanza sin reexplicar lo anterior.
        """.trimIndent()
    )
    sb.appendLine("\nContexto reciente:")
    slice.forEach { m ->
        val who = if (m.esUsuario) "Usuario" else "Asistente"
        if (m.texto.isNotBlank()) sb.appendLine("- $who: ${m.texto.take(400)}")
    }
    sb.appendLine("\nUsuario ahora dice: $userText")
    sb.appendLine("\nResponde breve y accionable, siguiendo esa intención.")
    return sb.toString()
}

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

    val pickFromGallery = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri
            selectedBitmap = null
        }
    }

    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            selectedBitmap = bmp
            selectedImage = null
        } else {
            scope.launch { snackbarHostState.showSnackbar("No se pudo tomar la foto") }
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            takePhoto.launch()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") }
        }
    }

    var sessionId by remember { mutableStateOf<String?>(null) }
    var pusoTitulo by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            snackbarHostState.showSnackbar("Debes iniciar sesión para chatear")
            return@LaunchedEffect
        }

        if (chatId != null) {
            sessionId = chatId
            try {
                val qs = db.collection("sessions").document(chatId)
                    .collection("messages")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get()
                    .await()

                listaMensajes.clear()
                val historial = qs.documents.map { d ->
                    val role = d.getString("role").orEmpty()
                    val text = d.getString("text").orEmpty()
                    val imageUrl = d.getString("imageUrl")
                    MensajeIA(
                        id = d.getTimestamp("createdAt")?.toDate()?.time ?: System.nanoTime(),
                        texto = text,
                        esUsuario = (role == "user"),
                        imageUri = imageUrl?.toUri()
                    )
                }
                listaMensajes.addAll(historial)

            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Error cargando mensajes: ${e.message}") }
            }
        }
    }

    // Efecto para hacer scroll automático
    LaunchedEffect(listaMensajes.size) {
        if (listaMensajes.isNotEmpty()) {
            listState.animateScrollToItem(listaMensajes.size - 1)
        }
    }

    val uiMessages = listaMensajes.map { m ->
        ChatMessageUI(
            id = m.id.toString(), // Usar el ID estable
            text = m.texto,
            imageUri = m.imageUri,
            isUser = m.esUsuario
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                onRemoveAttachment = {
                    selectedImage = null
                    selectedBitmap = null
                },
                onSend = {
                    scope.launch {
                        val uid = auth.currentUser?.uid
                        if (uid == null) {
                            snackbarHostState.showSnackbar("Debes iniciar sesión para chatear")
                            return@launch
                        }

                        val sid = sessionId ?: run {
                            val ref = db.collection("sessions")
                                .add(mapOf(
                                    "ownerId" to uid,
                                    "title" to "Nueva conversación",
                                    "createdAt" to Timestamp.now(),
                                    "lastMessageAt" to Timestamp.now(),
                                    "active" to true
                                )).await().id
                            sessionId = ref
                            ref
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
                            onStart = {
                                loading = true
                                prompt = ""
                            },
                            onDone = {
                                loading = false
                                selectedImage = null
                                selectedBitmap = null
                            },
                            onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                            setTitleIfNeeded = { firstText ->
                                if (!pusoTitulo && firstText.isNotBlank()) {
                                    db.collection("sessions").document(sid)
                                        .update("title", firstText.take(40))
                                    pusoTitulo = true
                                }
                            }
                        )
                        keyboard?.hide()
                    }
                },
                onBack = { navController?.popBackStack() },
                onHistory = { navController?.navigate("chatguardados") },
                onHome = {
                    navController?.navigate("principal") { popUpTo(0) { inclusive = true } }
                },
                onProfile = { navController?.navigate("perfil") }
            )
        }
    }
}

suspend fun uploadImageToStorage(sessionId: String, imageUri: Uri, context: Context): String? {
    val storage = FirebaseStorage.getInstance()
    val imageRef = storage.reference.child("sessions/$sessionId/${UUID.randomUUID()}.jpg")

    return try {
        context.contentResolver.openInputStream(imageUri)?.let { inputStream ->
            imageRef.putStream(inputStream).await()
            imageRef.downloadUrl.await().toString()
        }
    } catch (e: Exception) {
        Log.e("PantallaChatIA", "Error al subir la imagen", e)
        null
    }
}

suspend fun uploadBitmapToStorage(sessionId: String, bitmap: Bitmap, context: Context): String? {
    val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
    FileOutputStream(tempFile).use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
    }
    val result = uploadImageToStorage(sessionId, tempFile.toUri(), context)
    tempFile.delete()
    return result
}

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

    // --- 1) “Memoria”: normalizar lo que escribió el usuario (resuelve "opción 2", "la segunda", etc.)
    val opciones = extraerOpcionesDelUltimoAsistente(listaMensajes)
    val textoUsuarioNormalizado: String = if (opciones != null) {
        val idx = detectarIndiceElegido(textoPlano, opciones.items.size)
        if (idx != null) opciones.items[idx] else textoPlano
    } else {
        textoPlano
    }

    // --- 2) Construir prompt con contexto reciente para que NO repita saludos
    val promptConContexto = buildContextualPrompt(historial = listaMensajes, userText = textoUsuarioNormalizado)

    // --- 3) Pinta inmediatamente el mensaje del usuario en la UI (con el texto normalizado)
    val userMessage = MensajeIA(
        id = System.nanoTime(),
        texto = textoUsuarioNormalizado,
        esUsuario = true,
        imageUri = imageUri,
        bitmap = bitmap
    )
    listaMensajes.add(userMessage)

    CoroutineScope(Dispatchers.IO).launch {
        val sessionRef = db.collection("sessions").document(sessionId)
        try {
            // --- 4) Sube imagen si existe y actualiza el mensaje con su URL
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

            // --- 5) Persiste el turno del usuario (usa el texto normalizado)
            val mensajeMap = mutableMapOf<String, Any>(
                "role" to "user",
                "text" to textoUsuarioNormalizado,
                "createdAt" to Timestamp.now()
            )
            if (imageUrl != null) mensajeMap["imageUrl"] = imageUrl
            sessionRef.collection("messages").add(mensajeMap).await()
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

            // Título la primera vez
            setTitleIfNeeded(textoUsuarioNormalizado.ifBlank { "Imagen" })

            // --- 6) Llama a Gemini con CONTEXTO (evita saludos repetidos)
            val (respuestaDeGemini, _) = GeminiService.askGeminiSuspend(
                prompt = promptConContexto,
                imageUri = imageUri, // si enviaste galería
                bitmap   = bitmap,   // si enviaste cámara
                context  = context
            )

            // --- 7) Pinta respuesta y persiste
            withContext(Dispatchers.Main) {
                listaMensajes.add(
                    MensajeIA(
                        id = System.nanoTime(),
                        texto = respuestaDeGemini,
                        esUsuario = false
                    )
                )
                onDone()
            }

            sessionRef.collection("messages").add(
                mapOf(
                    "role" to "assistant",
                    "text" to respuestaDeGemini,
                    "createdAt" to Timestamp.now()
                )
            ).await()
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

        } catch (e: Exception) {
            Log.e("GeminiService", "Error en enviarMensajeConGemini", e)
            withContext(Dispatchers.Main) {
                onDone()
                onError("Error: ${e.message}")
            }
        }
    }
}
