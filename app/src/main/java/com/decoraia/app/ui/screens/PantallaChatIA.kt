package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PantallaChatIA(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scaffoldState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(scaffoldState) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(padding)) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Pregunta a la IA") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (prompt.isBlank()) return@Button
                // Guardar prompt en Firestore y simular respuesta IA
                val doc = mapOf("prompt" to prompt, "createdAt" to com.google.firebase.Timestamp.now())
                db.collection("chatIA").add(doc)
                    .addOnSuccessListener {
                        scope.launch {
                            response = "Respuesta simulada para: ${'$'}{prompt.take(50)}"
                        }
                    }
                    .addOnFailureListener {
                        scope.launch { scaffoldState.showSnackbar("Error: ${'$'}{it.message}") }
                    }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar a IA")
            }

            Spacer(Modifier.height(12.dp))
            Text("Respuesta:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(response)
        }
    }
}
