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

// Modelo simple para los ítems de sofá
private data class SofaItem(
    val id: String,          // lo usaremos como categoryId o subcategoría
    val title: String,
    val modelUrl: String? = null // si tienes un GLB/GLTF directo para AR
)

// Ejemplo de data: puedes reemplazarlo por tu data real
private val sofaItems = listOf(
    SofaItem(id = "sofa_moderno", title = "Sofá Moderno", modelUrl = null),
    SofaItem(id = "sofa_clasico", title = "Sofá Clásico", modelUrl = null),
    SofaItem(id = "sofa_esquinero", title = "Sofá Esquinero", modelUrl = null),
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
                        item = item,
                        onOpenAR = { modelUrl ->
                            // Abre tu visor AR si tienes URL de modelo
                            navController.navigate("arviewer?modelUrl=${Uri.encode(modelUrl)}")
                        },
                        onOpenModels = {
                            // Reutiliza tu pantalla de modelos con categoryId="sofa" o la subcategoría
                            // Si quieres filtrar por subcategoría, puedes pasar item.id
                            navController.navigate("ramodelos/${Uri.encode(style)}/sofa")
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
    item: SofaItem,
    onOpenAR: (String) -> Unit,
    onOpenModels: () -> Unit
) {
    // Card simple; si tuvieras imágenes, añádelas aquí
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenModels() }, // por defecto abre la lista de modelos
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text("Toca para ver modelos", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Botón opcional para ir directo a AR si hay modelUrl
            if (item.modelUrl != null) {
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = { onOpenAR(item.modelUrl) }) {
                    Text("Ver en AR")
                }
            }
        }
    }
}
