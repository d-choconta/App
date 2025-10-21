package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.decoraia.app.BuildConfig
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusManager
import androidx.core.content.FileProvider
import java.io.File

// --- Modelo simple para los mensajes (UI) ---
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
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // ---- Adjuntos (imagen) ----
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var showAttachMenu by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery
    val pickFromGallery = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) selectedImage = uri
    }

    // Cámara
    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) selectedImage = tempCameraUri
    }

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

    // 1) Preparar sesión (abrir existente o crear nueva)
    LaunchedEffect(chatId) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para chatear") }
            return@LaunchedEffect
        }

        if (chatId != null) {
            // 👉 Abrir chat existente
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
        } else {
            // 👉 Crear chat nuevo
            db.collection("sessions")
                .add(
                    mapOf(
                        "ownerId" to uid,
                        "title" to "Nueva conversación",
                        "createdAt" to Timestamp.now(),
                        "lastMessageAt" to Timestamp.now(),
                        "active" to true
                    )
                )
                .addOnSuccessListener { ref -> sessionId = ref.id }
                .addOnFailureListener { e ->
                    scope.launch { snackbarHostState.showSnackbar("No se pudo crear la sesión: ${e.message}") }
                }
        }
    }

    // Auto scroll
    LaunchedEffect(listaMensajes.size) {
        if (listaMensajes.isNotEmpty()) {
            listState.animateScrollToItem(listaMensajes.lastIndex)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chat con DecoraIA") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { navController?.navigate("chatguardados") }) {
                        Text("Historial")
                    }
                }
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().navigationBarsPadding()) {

                // Mini-preview si hay imagen
                if (selectedImage != null) {
                    Row(
                        Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImage),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("1 imagen adjunta", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { selectedImage = null }) { Text("Quitar") }
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón +
                    Box {
                        IconButton(onClick = { showAttachMenu = true }, enabled = !loading) {
                            Icon(Icons.Filled.Add, contentDescription = "Adjuntar")
                        }
                        DropdownMenu(
                            expanded = showAttachMenu,
                            onDismissRequest = { showAttachMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Galería") },
                                onClick = {
                                    showAttachMenu = false
                                    pickFromGallery.launch("image/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cámara") },
                                onClick = {
                                    showAttachMenu = false
                                    requestCameraPermission.launch(android.Manifest.permission.CAMERA)
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe tu mensaje...") },
                        singleLine = true,
                        enabled = !loading,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                val sid = sessionId ?: run {
                                    scope.launch { snackbarHostState.showSnackbar("Creando sesión… inténtalo de nuevo") }
                                    return@KeyboardActions
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
                                    onDone = { loading = false; selectedImage = null },
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
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sid = sessionId ?: run {
                                scope.launch { snackbarHostState.showSnackbar("Creando sesión… inténtalo de nuevo") }
                                return@Button
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
                                onDone = { loading = false; selectedImage = null },
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
                        },
                        enabled = (prompt.isNotBlank() || selectedImage != null) && !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Enviar")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { navController?.navigate("principal") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text("Volver") }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(listaMensajes, key = { it.id }) { msg ->
                BurbujaChat(msg.texto, msg.esUsuario)
                Spacer(Modifier.height(8.dp))
            }
            if (loading) {
                item {
                    Text(
                        "Escribiendo…",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
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

    // 1) Agrega mensaje del usuario
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

    // 2) Guarda adjunto si existe
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

/** Burbujas de chat */
@Composable
private fun BurbujaChat(text: String, esUsuario: Boolean) {
    val color = if (esUsuario) Color(0xFFDCF8C6) else Color(0xFFEFEFEF)
    val alineacion = if (esUsuario) Arrangement.End else Arrangement.Start
    val forma = if (esUsuario)
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    else
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = alineacion
    ) {
        Box(
            Modifier
                .clip(forma)
                .background(color)
                .padding(12.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(text)
        }
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
