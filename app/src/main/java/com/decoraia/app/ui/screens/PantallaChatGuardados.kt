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
fun PantallaChatGuardados(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var chats by remember { mutableStateOf(listOf<Map<String,Any>>()) }

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("chatsGuardados").get()
                .addOnSuccessListener { snap -> chats = snap.documents.mapNotNull { it.data } }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chats guardados", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(chats.size) { i ->
                val c = chats[i]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(c["titulo"] as? String ?: "Chat")
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Button(onClick = { /* abrir chat */ }) { Text("Abrir") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                // mover a eliminados
                                val docId = c["id"] as? String ?: return@Button
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(uid!!)
                                    .collection("chatGuardados").document(docId).delete()
                            }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}
