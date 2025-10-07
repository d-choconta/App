package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PantallaEditarPerfil(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid
    var nombre by remember { mutableStateOf("") }
    val scaffold = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        if (!uid.isNullOrBlank()) {
            db.collection("users").document(uid).get().addOnSuccessListener {
                nombre = it.getString("nombre") ?: ""
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(scaffold) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Editar perfil", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (uid != null) {
                    db.collection("users").document(uid).update("nombre", nombre)
                        .addOnSuccessListener {
                            scope.launch { scaffold.showSnackbar("Perfil actualizado") }
                        }
                }
            }) {
                Text("Guardar")
            }
        }
    }
}
