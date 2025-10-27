package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean,
    val imageUri: Uri? = null
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

    var sessionId by remember { mutableStateOf<String?>(null) }
    var pusoTitulo by remember { mutableStateOf(false) }

    // === Carga del historial de chat ===
    LaunchedEffect(chatId) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            snackbarHostState.showSnackbar("Debes iniciar sesión para chatear")
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
                        val imageUrl = d.getString("imageUri")
                        listaMensajes += MensajeIA(
                            texto = text,
                            esUsuario = (role == "user"),
                            imageUri = imageUrl?.let { Uri.parse(it) }
                        )
                    }
                }
                .addOnFailureListener { e ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Error cargando mensajes: ${e.message}")
                    }
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
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ChatIAScreenUI(
                messages = uiMessages,
                inputText = prompt,
                onInputChange = { prompt = it },
                loading = loading,
                selectedImage = selectedImage,
                onAttachGallery = { pickFromGallery.launch("image/*") },
                onAttachCamera = {
                    requestCameraPermission.launch(android.Manifest.permission.CAMERA)
                },
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
                            onError = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            },
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

//** Envía texto e imagen y guarda en Firestore */
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

    // Agregar el mensaje del usuario con texto e imagen
    val mensajeUsuario = MensajeIA(
        texto = texto.ifBlank { "(imagen adjunta)" },
        esUsuario = true,
        imageUri = imageUri
    )
    listaMensajes.add(mensajeUsuario)

    // Guardar mensaje en Firestore
    val mensajeMap = mutableMapOf<String, Any>(
        "role" to "user",
        "createdAt" to Timestamp.now()
    )
    if (texto.isNotBlank()) mensajeMap["text"] = texto
    if (imageUri != null) mensajeMap["imageUri"] = imageUri.toString()

    sessionRef.collection("messages").add(mensajeMap)
    sessionRef.update("lastMessageAt", Timestamp.now())

    // Establecer título si es la primera vez
    setTitleIfNeeded(texto.ifBlank { "Imagen" })

    // Procesar con Gemini
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val (respuestaTexto, _) = GeminiService.askGeminiSuspend(
                if (texto.isBlank()) "Analiza esta imagen" else texto,
                imageUri,
                context
            )

            listaMensajes.add(MensajeIA(texto = respuestaTexto, esUsuario = false))
            onDone()

            sessionRef.collection("messages").add(
                mapOf(
                    "role" to "assistant",
                    "text" to respuestaTexto,
                    "createdAt" to Timestamp.now()
                )
            )
            sessionRef.update("lastMessageAt", Timestamp.now())
        } catch (e: Exception) {
            onDone()
            onError(e.message ?: "Error desconocido")
        }
    }
}


/** Crea URI temporal para fotos */
private fun createTempImageUri(context: android.content.Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("camera_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file
    )
}
