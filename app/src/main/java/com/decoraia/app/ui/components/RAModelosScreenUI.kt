package com.decoraia.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.decoraia.app.R
import com.decoraia.app.data.ProductoAR

/* ===== Paleta local ===== */
private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa = Color(0xFFB2754E)

/* ===== Header propio (terracota + banner + back) ===== */
@Composable
private fun HeaderModelos(
    title: String,
    @DrawableRes banner: Int,
    onBack: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Terracotta)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 17.dp, top = 17.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.95f))
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 45.dp)
                .fillMaxWidth()
                .height(135.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(banner),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.25f)))
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

/* ===== Bottom bar propia ===== */
@Composable
private fun BottomBarModelos(onHome: () -> Unit, onProfile: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onHome,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Terracotta)
        ) { Icon(Icons.Filled.Home, contentDescription = "Inicio", tint = Color.White) }

        Box(
            Modifier
                .weight(1f)
                .height(56.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.45f))
        )

        IconButton(
            onClick = onProfile,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Terracotta)
        ) { Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = Color.White) }
    }
}

/* ===== Tarjeta de modelo ===== */
@Composable
private fun ModeloCard(
    modelo: ProductoAR,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes placeholder: Int = R.drawable.logo   // usa tu logo como placeholder genérico
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.90f),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = if (modelo.imageUrl.isBlank()) placeholder else modelo.imageUrl,
                placeholder = painterResource(placeholder),
                error = painterResource(placeholder),
                contentDescription = modelo.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Terracotta.copy(alpha = .85f))
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = Color.White
                )
            }
        }
    }
}

/* ===== Pantalla de Modelos (auto-contenida) ===== */
@Composable
fun RAModelosScreenUI(
    categoriaTitulo: String,
    modelos: List<ProductoAR>,
    loading: Boolean,
    favoriteIds: Set<String>,
    errorMsg: String? = null,
    onBack: () -> Unit,
    onSelectModelo: (ProductoAR) -> Unit,
    onToggleFavorite: (ProductoAR) -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    @DrawableRes headerBanner: Int = R.drawable.jarrones_banner
) {
    Surface(color = Cream) {
        Column(Modifier.fillMaxSize()) {

            HeaderModelos(
                title = categoriaTitulo,
                banner = headerBanner,
                onBack = onBack
            )

            when {
                loading -> Box(
                    Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                !errorMsg.isNullOrEmpty() -> Box(
                    Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) { Text("Error: $errorMsg", color = Color.Red) }

                modelos.isEmpty() -> Box(
                    Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay modelos para este filtro.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF6B6B6B)
                    )
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(modelos) { m ->
                        val isFav = favoriteIds.contains(m.id)
                        ModeloCard(
                            modelo = m,
                            isFavorite = isFav,
                            onToggleFavorite = { onToggleFavorite(m) },
                            onClick = { onSelectModelo(m) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
            }

            BottomBarModelos(onHome = onHome, onProfile = onProfile)
        }
    }
}
