package com.decoraia.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val texto: String,
    val esUsuario: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()
    val isSending = mutableStateOf(false)

    fun send(userText: String, context: Context) {
        if (userText.isBlank()) return

        messages += ChatMessage(texto = userText, esUsuario = true)
        isSending.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (replyText, replyImage) = GeminiService.askGeminiSuspend(userText, null, context)
                val imageUri = replyImage?.let { bitmapToUri(context, it) }

                withContext(Dispatchers.Main) {
                    messages += ChatMessage(
                        texto = replyText,
                        esUsuario = false,
                        imageUri = imageUri
                    )
                    isSending.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messages += ChatMessage(
                        texto = "Error al procesar: ${e.message ?: "desconocido"}",
                        esUsuario = false
                    )
                    isSending.value = false
                }
            }
        }
    }

    fun sendWithImage(userText: String, imageUri: Uri?, context: Context) {
        if (userText.isBlank() && imageUri == null) return

        messages += ChatMessage(texto = userText, esUsuario = true, imageUri = imageUri)
        isSending.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (replyText, replyImage) = GeminiService.askGeminiSuspend(userText, imageUri, context)
                val replyUri = replyImage?.let { bitmapToUri(context, it) }

                withContext(Dispatchers.Main) {
                    messages += ChatMessage(
                        texto = replyText,
                        esUsuario = false,
                        imageUri = replyUri
                    )
                    isSending.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messages += ChatMessage(
                        texto = "Error al procesar: ${e.message ?: "desconocido"}",
                        esUsuario = false
                    )
                    isSending.value = false
                }
            }
        }
    }

    private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "reply_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return Uri.fromFile(file)
    }

    fun reset() {
        messages.clear()
        messages += ChatMessage(texto = "Conversación reiniciada 😊", esUsuario = false)
    }
}
