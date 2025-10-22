package com.decoraia.app.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class ChatMessage(val id: Long = System.nanoTime(), val text: String, val fromUser: Boolean)

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf<ChatMessage>()
    var isSending = mutableStateOf(false)

    fun send(userText: String) {
        messages += ChatMessage(text = userText, fromUser = true)
        isSending.value = true
        viewModelScope.launch {
            val reply = GeminiService.askGeminiSuspend(userText)
            messages += ChatMessage(text = reply, fromUser = false)
            isSending.value = false
        }
    }

    fun reset() {
        messages.clear()
        messages += ChatMessage(text = "Conversación reiniciada 😊", fromUser = false)
    }
}
