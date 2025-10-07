package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaRAModelosLike(navController: NavController, modelId: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val scaffold = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dar like al modelo", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            if (uid != null) {
                db.collection("raModelos").document(modelId)
                    .update("likes", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                    .addOnSuccessListener { scope.launch { scaffold.showSnackbar("Like agregado") } }
            }
        }) {
            Text("Like")
        }
    }
}
