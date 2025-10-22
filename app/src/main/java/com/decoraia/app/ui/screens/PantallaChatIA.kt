package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.decoraia.app.BuildConfig
import com.decoraia.app.ui.components.ChatIAScreenUI
import com.decoraia.app.ui.components.ChatMessageUI
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChatIA(
    navController: NavController? = null,
    chatId: String? = null
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val listaMensajes = remember { mutableStateListOf<MensajeIA>() }
    var prompt by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // ---- Adjuntos (imagen) ----
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickFromGallery = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) selectedImage = uri }

    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) selectedImage = tempCameraUri }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            tempCameraUri = createTempImageUri(context)
            tempCameraUri?.let { takePhoto.launch(it) }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permiso de cámara denegado") }
        }
    }

    // Estado de la sesión en Firestore
    var sessionId by remember { mutableStateOf<String?>(null) }
    var pusoTitulo by remember { mutableStateOf(false) }

    // 1) SOLO abrir una sesión existente (no crear si es null)
    LaunchedEffect(chatId) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para chatear") }
            return@LaunchedEffect
        }

        if (chatId != null) {
            sessionId = chatId
            db.collection("sessions").document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { qs ->
                    listaMensajes.clear()
                    qs.forEach { d ->
                        val role = d.getString("role").orEmpty()
                        val text = d.getString("text").orEmpty()
                        listaMensajes += MensajeIA(
                            texto = text,
                            esUsuario = (role == "user")
                        )
                    }
                }
                .addOnFailureListener { e ->
                    scope.launch { snackbarHostState.showSnackbar("Error cargando mensajes: ${e.message}") }
                }
        }
    }

    val uiMessages: List<ChatMessageUI> =
        listaMensajes.mapIndexed { idx, m ->
            ChatMessageUI(
                id = "m$idx",
                text = m.texto,
                imageUrl = null,
                isUser = m.esUsuario
            )
        }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ChatIAScreenUI(
                messages = uiMessages,
                inputText = prompt,
                onInputChange = { prompt = it },
                loading = loading,
                selectedImage = selectedImage,

                onAttachGallery = { pickFromGallery.launch("image/*") },
                onAttachCamera  = { requestCameraPermission.launch(android.Manifest.permission.CAMERA) },
                onRemoveAttachment = { selectedImage = null },

                // Enviar: crea sesión si no existe y luego envía
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

                        enviarMensajeConAdjunto(
                            context = context,
                            db = db,
                            sessionId = sid,
                            prompt = prompt,
                            imageUri = selectedImage,
                            listaMensajes = listaMensajes,
                            focus = focus,
                            onStart = { loading = true; prompt = "" },
                            onDone  = { loading = false; selectedImage = null },
                            onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                            setTitleIfNeeded = { firstUserText ->
                                if (!pusoTitulo && firstUserText.isNotBlank()) {
                                    db.collection("sessions").document(sid)
                                        .update("title", firstUserText.take(40))
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

/** Envía texto y opcionalmente imagen, guarda en Firestore y llama a Gemini. */
private fun enviarMensajeConAdjunto(
    context: android.content.Context,
    db: FirebaseFirestore,
    sessionId: String,
    prompt: String,
    imageUri: Uri?,
    listaMensajes: MutableList<MensajeIA>,
    focus: FocusManager,
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

    // 1) Mensaje del usuario
    if (texto.isNotBlank()) {
        listaMensajes.add(MensajeIA(texto = texto, esUsuario = true))
        sessionRef.collection("messages").add(
            mapOf(
                "role" to "user",
                "text" to texto,
                "createdAt" to Timestamp.now()
            )
        )
        setTitleIfNeeded(texto)
    } else {
        setTitleIfNeeded("Imagen")
    }

    // 2) Adjuntos
    if (imageUri != null) {
        sessionRef.collection("messages").add(
            mapOf(
                "role" to "user",
                "imageUri" to imageUri.toString(),
                "text" to texto,
                "createdAt" to Timestamp.now()
            )
        )
    }

    sessionRef.update("lastMessageAt", Timestamp.now())

    // 3) Gemini
    val handleAssistant: (String) -> Unit = { respuesta ->
        listaMensajes.add(MensajeIA(texto = respuesta, esUsuario = false))
        onDone()

        sessionRef.collection("messages").add(
            mapOf(
                "role" to "assistant",
                "text" to respuesta,
                "createdAt" to Timestamp.now()
            )
        )
        sessionRef.update("lastMessageAt", Timestamp.now())

        if (respuesta.startsWith("Error:")) {
            onError(respuesta.removePrefix("Error: ").trim())
        }
    }

    try {
        if (imageUri != null) {
            // implementa askGeminiWithImage
            GeminiService.askGemini(
                if (texto.isBlank()) "Analiza esta imagen" else texto
            ) { handleAssistant(it) }
        } else {
            GeminiService.askGemini(texto) { handleAssistant(it) }
        }
    } catch (e: Exception) {
        onDone()
        onError(e.message ?: "Error desconocido")
    }
}

/** Crea un Uri temporal para la foto de cámara */
private fun createTempImageUri(context: android.content.Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("camera_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file
    )
}