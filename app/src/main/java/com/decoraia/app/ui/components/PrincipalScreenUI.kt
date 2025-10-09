package com.decoraia.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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


private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val TerracottaDark = Color(0xFFCF8A57)
private val Cocoa = Color(0xFFB2754E)
private val Graphite = Color(0xFF2D2A26)

/* Header con ondas */
@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracotta = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.48f, 0f)
            cubicTo(
                w * 0.10f, h * 0.20f,
                w * 0.12f, h * 0.50f,
                0f,        h * 0.70f
            )
            lineTo(0f, h); lineTo(0f, 0f); close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w * 0.45f, 0f)
            lineTo(w, 0f); lineTo(w, h * 0.95f)
            cubicTo(w * 0.92f, h * 0.55f, w * 0.78f, h * 0.30f, w * 0.70f, 0f)
            close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
    }
}

@Composable
private fun FeatureCard(
    @DrawableRes imageRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Terracotta.copy(alpha = 0.25f))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
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

/* ----------- UI PRINCIPAL ----------- */
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            // Header
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                TopWaves(Modifier.fillMaxSize())

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp, start = 8.dp, end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        "DECORAIA",
                        color = Terracotta,
                        style = TextStyle(
                            fontFamily = MuseoModerno,
                            fontWeight = FontWeight.Light,
                            fontSize = 42.sp,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "¿Cómo decorarás hoy?",
                        color = Graphite,
                        style = TextStyle(
                            fontFamily = InriaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 22.sp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            FeatureCard(
                imageRes = iaImage,
                title = "IA",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
            ) { onGoIA() }

            Spacer(Modifier.height(16.dp))

            FeatureCard(
                imageRes = raImage,
                title = "RA",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
            ) { onGoRA() }

            Spacer(Modifier.height(18.dp))


            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
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
