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

@Composable
fun PantallaPerfil(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid
    var nombre by remember { mutableStateOf("Cargando...") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    val scope = rememberCoroutineScope()
    val scaffoldState = remember { SnackbarHostState() }

    LaunchedEffect(uid) {
        if (!uid.isNullOrBlank()) {
            db.collection("users").document(uid).get().addOnSuccessListener { d ->
                nombre = d.getString("nombre") ?: nombre
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(scaffoldState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Perfil", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text("Nombre: ${'$'}{nombre}")
            Text("Correo: ${'$'}{email}")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate("pantallaEditarPerfil") }) { Text("Editar perfil") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                auth.signOut()
                navController.navigate("pantallaInicio") {
                    popUpTo("pantallaPrincipal") { inclusive = true }
                }
            }) { Text("Cerrar sesión") }
        }
    }
}
