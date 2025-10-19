package com.decoraia.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

private data class SofaItem(
    val id: String,
    val title: String,
    val modelUrl: String     // <- ahora obligatorio para abrir AR
)

// URLs de ejemplo (funcionan). Luego pon los tuyos .glb
private val sofaItems = listOf(
    SofaItem(
        id = "sofa_moderno",
        title = "Sofá Moderno",
        modelUrl = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb"
    ),
    SofaItem(
        id = "sofa_clasico",
        title = "Sofá Clásico",
        modelUrl = "https://storage.googleapis.com/ar-answers-in-search-models/static/Chair/model.glb"
    ),
    SofaItem(
        id = "sofa_esquinero",
        title = "Sofá Esquinero",
        modelUrl = "https://storage.googleapis.com/ar-answers-in-search-models/static/Houseplant/model.glb"
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSofa(
    navController: NavHostController,
    style: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sofá • $style") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Header(style)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sofaItems) { item ->
                    SofaCard(
                        title = item.title,
                        subtitle = "Toca para abrir en AR",
                        onClick = {
                            navController.navigate(
                                "arviewer?modelUrl=${Uri.encode(item.modelUrl)}"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(style: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text("Objetos • Sofá", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("Estilo: $style", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SofaCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
