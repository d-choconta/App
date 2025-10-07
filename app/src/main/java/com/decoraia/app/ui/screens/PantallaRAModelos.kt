package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaRAModelos(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var modelos by remember { mutableStateOf(listOf<Map<String,Any>>()) }

    LaunchedEffect(Unit) {
        db.collection("raModelos").get().addOnSuccessListener { snap ->
            modelos = snap.documents.map { d -> d.data ?: emptyMap() }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Modelos RA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(modelos.size) { i ->
                val model = modelos[i]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(model["nombre"] as? String ?: "Sin nombre")
                        Spacer(Modifier.height(6.dp))
                        Row {
                            Button(onClick = { /* visualizar modelo */ }) { Text("Ver") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                // marcar like en Firestore
                                val id = model["id"] as? String ?: return@Button
                                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                                FirebaseFirestore.getInstance().collection("raModelos").document(id)
                                    .update("likes", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                            }) { Text("Like") }
                        }
                    }
                }
            }
        }
    }
}
