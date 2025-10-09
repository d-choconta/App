package com.decoraia.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R
import com.decoraia.app.ui.theme.InriaSans
import com.decoraia.app.ui.theme.MuseoModerno

/* Paleta */
private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val TerracottaDark = Color(0xFFCF8A57)
private val Cocoa = Color(0xFFB2754E)
private val Graphite = Color(0xFF2D2A26)

/* Header a todo el ancho */
@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracotta = Path().apply {
            moveTo(0f, 0f); lineTo(w * 0.48f, 0f)
            cubicTo(w * 0.10f, h * 0.20f, w * 0.12f, h * 0.50f, 0f, h * 0.70f)
            lineTo(0f, h); lineTo(0f, 0f); close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w * 0.45f, 0f); lineTo(w, 0f); lineTo(w, h * 0.95f)
            cubicTo(w * 0.92f, h * 0.55f, w * 0.78f, h * 0.30f, w * 0.70f, 0f)
            close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
    }
}

/* Tarjeta full-bleed: imagen + overlay + título (sin márgenes) */
@Composable
private fun FullBleedCard(
    @DrawableRes imageRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .aspectRatio(16f / 9f)           // alto proporcional, ocupa TODO el ancho
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.30f))
        )
        Text(
            text = title,
            color = Color.White,
            style = TextStyle(
                fontFamily = InriaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/* ----------- UI PRINCIPAL (FULL WIDTH) ----------- */
@Composable
fun PrincipalScreenUI(
    onGoIA: () -> Unit,
    onGoRA: () -> Unit,
    onGoPerfil: () -> Unit,
    onLogout: () -> Unit,
    @DrawableRes iaImage: Int = R.drawable.ia_banner,
    @DrawableRes raImage: Int = R.drawable.ra_banner
) {
    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {

            val headerHeight = 240.dp

            // Header sin padding lateral (full-bleed real)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .align(Alignment.TopStart)
            ) {
                TopWaves(Modifier.fillMaxSize())

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        "DECORAIA",
                        color = Terracotta,
                        style = TextStyle(
                            fontFamily = MuseoModerno,
                            fontWeight = FontWeight.Light,
                            fontSize = 54.sp,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "¿Cómo decorarás hoy?",
                        color = Graphite,
                        style = TextStyle(
                            fontFamily = InriaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Contenido full-bleed debajo del header (sin padding horizontal)
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = headerHeight)
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // IA full width
                    FullBleedCard(
                        imageRes = iaImage,
                        title = "IA",
                        onClick = onGoIA
                    )

                    Spacer(Modifier.height(16.dp))

                    // RA full width
                    FullBleedCard(
                        imageRes = raImage,
                        title = "RA",
                        onClick = onGoRA
                    )

                    Spacer(Modifier.height(16.dp))
                }

                // Barra inferior (puedes dejarla también full-bleed)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Cocoa.copy(alpha = 0.9f))
                            .border(2.dp, Terracotta, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = onGoPerfil,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Cocoa.copy(alpha = 0.9f))
                            .border(2.dp, Terracotta, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                }
            }
            }
        }
}