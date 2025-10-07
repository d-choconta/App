package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaRAObjetos(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var objetos by remember { mutableStateOf(listOf<Pair<String,String>>()) } // Pair(nombre, url)
    LaunchedEffect(Unit) {
        db.collection("raObjetos").get().addOnSuccessListener { snap ->
            objetos = snap.documents.mapNotNull { d ->
                val nombre = d.getString("nombre")
                val url = d.getString("modelUrl")
                if (nombre != null && url != null) nombre to url else null
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Objetos RA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        LazyRow {
            items(objetos.size) { i ->
                val (nombre, url) = objetos[i]
                Card(modifier = Modifier.padding(8.dp).width(180.dp)) {
                    Column(Modifier.padding(8.dp)) {
                        Text(nombre)
                        Spacer(Modifier.height(8.dp))
                        Text("Modelo: ${'$'}{url.take(30)}...")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { /* colocar objeto en escena RA usando url */ }) {
                            Text("Colocar")
                        }
                    }
                }
            }
        }
    }
}
