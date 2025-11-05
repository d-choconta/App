package com.decoraia.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.min
import com.decoraia.app.R
import com.decoraia.app.ui.theme.MuseoModerno

/* ===== Colores ===== */
private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa = Color(0xFFB2754E)

/* ===== HEADER ===== */
@Composable
private fun HeaderEstilos(
    title: String,
    @DrawableRes banner: Int,
    onBack: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Terracotta)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 30.dp)
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
                style = TextStyle(
                    fontFamily = MuseoModerno,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 40.sp,
                    color = Color.White
                )
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 17.dp, top = 17.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = Color.White
            )
        }
    }
}

/* ===== CARD ===== */
private data class StyleItem(val name: String, @DrawableRes val image: Int)

@Composable
private fun EstiloRowCard(
    item: StyleItem,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.Transparent)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(item.image),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.30f)))
        Text(
            text = item.name,
            style = TextStyle(
                fontFamily = MuseoModerno,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/* ===== BOTTOM BAR ===== */
@Composable
private fun BottomActions(onHome: () -> Unit, onProfile: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onHome,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
        ) {
            Icon(
                Icons.Filled.Home,
                contentDescription = "Inicio",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

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
                .size(72.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Perfil",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/* ===== UI PRINCIPAL ===== */
@Composable
fun RAEstilosScreenUI(
    estilos: List<String>,
    onBack: () -> Unit,
    onSelectStyle: (String) -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    @DrawableRes headerBanner: Int = R.drawable.estilos_banner,
    @DrawableRes imgClasico: Int = R.drawable.estilo_clasico,
    @DrawableRes imgMediterraneo: Int = R.drawable.estilo_mediterraneo,
    @DrawableRes imgMinimalista: Int = R.drawable.estilo_minimalista,
    @DrawableRes imgIndustrial: Int = R.drawable.estilo_industrial,
    @DrawableRes imgPlaceholder: Int = R.drawable.estilo_minimalista
) {
    fun imageFor(name: String): Int = when (name.trim().lowercase()) {
        "clásico", "clasico" -> imgClasico
        "mediterráneo", "mediterraneo" -> imgMediterraneo
        "minimalista" -> imgMinimalista
        "industrial" -> imgIndustrial
        else -> imgPlaceholder
    }

    val items = estilos.map { StyleItem(it, imageFor(it)) }

    Surface(color = Cream) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val cardHeight =
                (maxWidth * 0.28f).coerceIn(110.dp, 170.dp)

            Column(Modifier.fillMaxSize()) {
                HeaderEstilos("Estilos", headerBanner, onBack)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(items) { item ->
                        EstiloRowCard(
                            item = item,
                            height = cardHeight,
                            onClick = { onSelectStyle(item.name) }
                        )
                    }
                }

                BottomActions(onHome = onHome, onProfile = onProfile)
            }
        }
    }
}
