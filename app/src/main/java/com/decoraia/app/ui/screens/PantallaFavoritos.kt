package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaFavoritos(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var items by remember { mutableStateOf(listOf<Map<String,Any>>()) }

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("favoritos").get().addOnSuccessListener { snap ->
                    items = snap.documents.mapNotNull { it.data }
                }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favoritos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(items.size) { i ->
                val it = items[i]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(it["titulo"] as? String ?: "Sin título")
                        Spacer(Modifier.height(6.dp))
                        Row {
                            Button(onClick = { /* ver item */ }) { Text("Ver") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                // quitar favorito
                                val docId = it["id"] as? String ?: return@Button
                                FirebaseFirestore.getInstance().collection("users").document(uid!!)
                                    .collection("favoritos").document(docId).delete()
                            }) { Text("Quitar") }
                        }
                    }
                }
            }
        }
    }
}
