package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaRAEstilos(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var estilos by remember { mutableStateOf(listOf<String>()) }
    var selected by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        db.collection("raEstilos").get()
            .addOnSuccessListener { snap ->
                estilos = snap.documents.mapNotNull { it.getString("nombre") }
            }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Estilos RA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(estilos.size) { idx ->
                val estilo = estilos[idx]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .toggleable(value = selected.contains(estilo), onValueChange = {
                            selected = if (selected.contains(estilo)) selected - estilo else selected + estilo
                        }),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(estilo)
                    Checkbox(checked = selected.contains(estilo), onCheckedChange = null)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { /* aplicar estilos seleccionados en la escena RA */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Aplicar estilos")
        }
    }
}
