package com.decoraia.app.ui.screens

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object GeminiService {

    private val apiKey = "AIzaSyCmlDOgseFuFg5jdCqiydb158s2H3T7xlQ"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )
    suspend fun askGeminiSuspend(prompt: String): String {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            var respuesta = "Sin respuesta"
            val done = kotlinx.coroutines.CompletableDeferred<String>()
            askGemini(prompt) { res ->
                done.complete(res)
            }
            respuesta = done.await()
            respuesta
        }
    }

    /**
     * Envía la pregunta a Gemini, pidiendo respuesta en texto plano (sin Markdown)
     */
    fun askGemini(preguntaUsuario: String, callback: (String) -> Unit) {
        Log.d("GeminiService", "Usando modelo: gemini-2.5-flash | key length: ${apiKey.length}")

        // Forzamos respuesta sin formato
        val prompt = "Responde en texto plano, sin Markdown ni formato (sin **, #, ni listas). " +
                "Escribe de forma natural en español: $preguntaUsuario"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(prompt)
                val texto = response.text ?: "Sin respuesta del modelo"
                Log.d("GeminiService", "Respuesta de Gemini: $texto")
                callback(texto)
            } catch (e: Exception) {
                Log.e("GeminiService", "Error al generar contenido", e)
                callback("Error: ${e.message}")
            }
            }
        }
}