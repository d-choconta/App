package com.decoraia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.ui.focus.FocusManager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack



// --- Modelo simple para los mensajes
data class MensajeIA(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaChatIA(navController: NavController? = null) {   // <- navController ahora es opcional
    val db = FirebaseFirestore.getInstance()

    val listaMensajes = remember { mutableStateListOf<MensajeIA>() }
    var prompt by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

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

                )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            enviarMensaje(
                                db = db,
                                prompt = prompt,
                                listaMensajes = listaMensajes,
                                focus = focus,
                                onStart = { loading = true; prompt = "" },
                                onDone = { loading = false },
                                onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                            )
                            keyboard?.hide()
                        }
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        enviarMensaje(
                            db = db,
                            prompt = prompt,
                            listaMensajes = listaMensajes,
                            focus = focus,
                            onStart = { loading = true; prompt = "" },
                            onDone = { loading = false },
                            onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                        )
                        keyboard?.hide()
                    },
                    enabled = prompt.isNotBlank() && !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Enviar")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // 🔙 Botón de volver a la pantalla principal
            Button(onClick = { navController?.navigate("pantallaPrincipal") }) {
                Text("Volver")
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

/** Envía el mensaje, guarda en Firestore y consulta a Gemini. */
private fun enviarMensaje(
    db: FirebaseFirestore,
    prompt: String,
    listaMensajes: MutableList<MensajeIA>,
    focus: FocusManager,
    onStart: () -> Unit,
    onDone: () -> Unit,
    onError: (String) -> Unit
) {
    val texto = prompt.trim()
    if (texto.isBlank()) return

    focus.clearFocus()
    onStart()

    // Agrega el mensaje del usuario al chat
    listaMensajes.add(MensajeIA(texto = texto, esUsuario = true))

    // Guarda el prompt (no bloquea si falla)
    runCatching {
        db.collection("chatIA").add(
            mapOf("prompt" to texto, "createdAt" to com.google.firebase.Timestamp.now())
        )
    }

    // Llama al servicio Gemini
    GeminiService.askGemini(texto) { respuesta ->
        listaMensajes.add(MensajeIA(texto = respuesta, esUsuario = false))
        onDone()
        if (respuesta.startsWith("Error:")) {
            onError(respuesta.removePrefix("Error: ").trim())
        }
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