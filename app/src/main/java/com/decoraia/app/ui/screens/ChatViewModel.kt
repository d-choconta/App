package com.decoraia.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val fromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf<ChatMessage>()
    var isSending = mutableStateOf(false)

    /**
     * Envía un mensaje de texto (sin imagen)
     */
    fun send(userText: String, context: Context) {
        messages += ChatMessage(text = userText, fromUser = true)
        isSending.value = true

        viewModelScope.launch {
            val (replyText, replyImage) = GeminiService.askGeminiSuspend(userText, null, context)

            // Se agrega la respuesta del modelo como mensaje
            messages += ChatMessage(
                text = replyText,
                fromUser = false
            )

            isSending.value = false
        }
    }

    /**
     * Envía un mensaje con una imagen adjunta
     */
    fun sendWithImage(userText: String, imageUri: Uri?, context: Context) {
        messages += ChatMessage(text = userText, fromUser = true, imageUri = imageUri)
        isSending.value = true

        viewModelScope.launch {
            val (replyText, replyImage) = GeminiService.askGeminiSuspend(userText, imageUri, context)

            messages += ChatMessage(
                text = replyText,
                fromUser = false
            )

            isSending.value = false
        }
    }

    /**
     * Reinicia la conversación
     */
    fun reset() {
        messages.clear()
        messages += ChatMessage(text = "Conversación reiniciada 😊", fromUser =false)
        }
}