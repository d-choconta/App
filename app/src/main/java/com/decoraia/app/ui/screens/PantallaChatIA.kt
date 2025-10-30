package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.decoraia.app.BuildConfig
import com.decoraia.app.ui.components.ChatIAScreenUI
import com.decoraia.app.ui.components.ChatMessageUI
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean,
    val imageUri: Uri? = null
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

    val listaMensajes = remember { mutableStateListOf<MensajeIA>() }
    var prompt by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    // === Cámara y galería ===
    val pickFromGallery = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) selectedImage = uri }

    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImage = tempCameraUri
        } else {
            scope.launch { snackbarHostState.showSnackbar("No se pudo tomar la foto") }
        }
        // limpiar el temporal para la próxima vez
        tempCameraUri = null
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // crear Uri temporal y abrir cámara
            tempCameraUri = createTempImageUri(context)
            tempCameraUri?.let { takePhoto.launch(it) }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") }
        }
    }

    var sessionId by remember { mutableStateOf<String?>(null) }
    var pusoTitulo by remember { mutableStateOf(false) }

    // === Carga del historial de chat (usando await + imageLocalPath) ===
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
                for (d in qs.documents) {
                    val role = d.getString("role").orEmpty()
                    val text = d.getString("text").orEmpty()

                    // Preferimos la ruta persistente local; mantenemos compat con el antiguo imageUri
                    val imageLocalPath = d.getString("imageLocalPath")
                    val legacyUriStr   = d.getString("imageUri")

                    val finalUri: Uri? = when {
                        !imageLocalPath.isNullOrBlank() -> buildPersistUri(context, imageLocalPath)
                        !legacyUriStr.isNullOrBlank()   -> Uri.parse(legacyUriStr)
                        else -> null
                    }

                    listaMensajes += MensajeIA(
                        texto = text,
                        esUsuario = (role == "user"),
                        imageUri = finalUri
                    )
                }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Error cargando mensajes: ${e.message}") }
            }
        }
    }

    // === Conversión para UI ===
    val uiMessages = listaMensajes.mapIndexed { idx, m ->
        ChatMessageUI(
            id = "m$idx",
            text = m.texto,
            imageUri = m.imageUri,
            isUser = m.esUsuario
        )
    }

    // === Interfaz ===
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
                inputText = prompt,
                onInputChange = { prompt = it },
                loading = loading,
                selectedImage = selectedImage,
                onAttachGallery = { pickFromGallery.launch("image/*") },
                onAttachCamera = { requestCameraPermission.launch(android.Manifest.permission.CAMERA) },
                onRemoveAttachment = { selectedImage = null },
                onSend = {
                    scope.launch {
                        val uid = auth.currentUser?.uid
                        if (uid == null) {
                            snackbarHostState.showSnackbar("Debes iniciar sesión para chatear")
                            return@launch
                        }

                        val sid = sessionId ?: run {
                            val ref = db.collection("sessions")
                                .add(
                                    mapOf(
                                        "ownerId" to uid,
                                        "title" to "Nueva conversación",
                                        "createdAt" to Timestamp.now(),
                                        "lastMessageAt" to Timestamp.now(),
                                        "active" to true
                                    )
                                )
                                .await()
                            val newId = ref.id
                            sessionId = newId
                            newId
                        }

                        enviarMensajeConGemini(
                            context = context,
                            db = db,
                            sessionId = sid,
                            prompt = prompt,
                            imageUri = selectedImage,
                            listaMensajes = listaMensajes,
                            focus = focus,
                            onStart = { loading = true; prompt = "" },
                            onDone = { loading = false; selectedImage = null },
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
                    navController?.navigate("principal") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfile = { navController?.navigate("perfil") }
            )
        }
    }
}

// ** Envía texto e imagen y guarda en Firestore con imageLocalPath **
private fun enviarMensajeConGemini(
    context: android.content.Context,
    db: FirebaseFirestore,
    sessionId: String,
    prompt: String,
    imageUri: Uri?,
    listaMensajes: MutableList<MensajeIA>,
    focus: androidx.compose.ui.focus.FocusManager,
    onStart: () -> Unit,
    onDone: () -> Unit,
    onError: (String) -> Unit,
    setTitleIfNeeded: (String) -> Unit
) {
    val texto = prompt.trim()
    if (texto.isBlank() && imageUri == null) return

    focus.clearFocus()
    onStart()

    val sessionRef = db.collection("sessions").document(sessionId)

    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            // 1) Copiamos la imagen a almacenamiento interno persistente
            val imageLocalPath: String? = imageUri?.let { persistImageLocal(context, it) }
            val persistedUri: Uri? = imageLocalPath?.let { buildPersistUri(context, it) }

            // 2) Mostramos en UI (usa la Uri persistente si existe)
            withContext(Dispatchers.Main) {
                listaMensajes.add(
                    MensajeIA(
                        texto = texto.ifBlank { "" },
                        esUsuario = true,
                        imageUri = persistedUri ?: imageUri
                    )
                )
            }

            // ✅ Resolver si el usuario eligió una opción (1, 2, “primera”, etc.)
            val opciones = extraerOpcionesDelUltimoAsistente(listaMensajes)
            val indiceElegido = opciones?.let { detectarIndiceElegido(texto, it.items.size) }

            val textoFinalUsuario = if (indiceElegido != null && opciones != null) {
                val elegida = opciones.items[indiceElegido]
                "El usuario eligió la opción ${indiceElegido + 1}: \"$elegida\". " +
                        "Continúa con esa opción y da los siguientes pasos sin repetir introducción."
            } else {
                texto
            }

            // ✅ Prompt con contexto para evitar que repita su mensaje de inicio
            val promptParaGemini = if (textoFinalUsuario.isBlank() && (persistedUri ?: imageUri) != null) {
                "Analiza esta imagen"
            } else {
                buildContextualPrompt(listaMensajes, textoFinalUsuario)
            }

            // 3) Guardamos en Firestore SOLO el nombre del archivo
            val mensajeMap = mutableMapOf<String, Any>(
                "role" to "user",
                "createdAt" to Timestamp.now()
            )
            if (texto.isNotBlank()) mensajeMap["text"] = texto
            if (!imageLocalPath.isNullOrBlank()) mensajeMap["imageLocalPath"] = imageLocalPath

            sessionRef.collection("messages").add(mensajeMap).await()
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

            // Establecer título si es la primera vez
            setTitleIfNeeded(texto.ifBlank { "Imagen" })

            // 4) Procesar con Gemini (pasando la URI persistente si existe)
            val (respuestaTexto, _) = GeminiService.askGeminiSuspend(
                promptParaGemini,
                persistedUri ?: imageUri,
                context
            )

            withContext(Dispatchers.Main) {
                listaMensajes.add(MensajeIA(texto = respuestaTexto, esUsuario = false))
                onDone()
            }

            sessionRef.collection("messages").add(
                mapOf(
                    "role" to "assistant",
                    "text" to respuestaTexto,
                    "createdAt" to Timestamp.now()
                )
            ).await()
            sessionRef.update("lastMessageAt", Timestamp.now()).await()

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onDone()
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}

/** Crea URI temporal para fotos (coincide con ${applicationId}.fileprovider del Manifest) */
private fun createTempImageUri(context: android.content.Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("camera_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file
    )
}

/** Copia la imagen seleccionada a almacenamiento interno persistente y devuelve el nombre del archivo */
private fun persistImageLocal(context: android.content.Context, source: Uri): String {
    val dir = File(context.filesDir, "images").apply { mkdirs() }
    val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
    val dest = File(dir, fileName)
    context.contentResolver.openInputStream(source)?.use { input ->
        FileOutputStream(dest).use { out -> input.copyTo(out) }
    } ?: error("No se pudo leer la imagen de origen")
    return fileName
}

/** Construye content:// Uri desde el fileName usando FileProvider */
private fun buildPersistUri(context: android.content.Context, fileName: String): Uri {
    val file = File(context.filesDir, "images/$fileName")
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file
    )
}