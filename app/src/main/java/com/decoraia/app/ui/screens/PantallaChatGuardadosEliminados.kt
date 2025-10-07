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
fun PantallaChatGuardadosEliminados(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var items by remember { mutableStateOf(listOf<Map<String,Any>>()) }

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("chatGuardadosEliminados").get()
                .addOnSuccessListener { snap -> items = snap.documents.mapNotNull { it.data } }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chats eliminados", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(items.size) { i ->
                val it = items[i]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(it["titulo"] as? String ?: "Chat eliminado")
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
