package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch

@Composable
fun PantallaRAModelosLike(navController: NavController, modelId: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Dar like al modelo", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                when {
                    modelId.isBlank() -> {
                        scope.launch { snackbarHostState.showSnackbar("modelId inválido") }
                    }
                    uid == null -> {
                        scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión") }
                    }
                    else -> {
                        db.collection("raModelos").document(modelId)
                            .update("likes", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener {
                                scope.launch { snackbarHostState.showSnackbar("Like agregado") }
                                // Opcional: volver atrás tras éxito
                                // navController.popBackStack()
                            }
                            .addOnFailureListener {
                                scope.launch { snackbarHostState.showSnackbar("Error al dar like") }
                            }
                    }
                }
            }) {
                Text("Like")
            }
        }
    }
}
