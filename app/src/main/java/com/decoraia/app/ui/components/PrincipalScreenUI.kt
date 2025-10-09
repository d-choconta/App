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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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


@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracotta = Path().apply {
            moveTo(5f, 0f); lineTo(w * 0.5f, 1f)
            cubicTo(w * 0.30f, h * 0.10f, w * 0.9f, h * 0.2f, -700f, h * 1f)
            lineTo(1f, h); lineTo(0f, 30f); close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w * 23f, 0f); lineTo(w, w*4f); lineTo(w, h * 1f)
            cubicTo(w * 0.22f, h * 0.55f, w * 0.78f, h * 0.30f, w * 0.70f, 0f)
            close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
    }
}

@Composable
private fun FramedCard(
    frameColor: Color,
    showBorder: Boolean = false,
    @DrawableRes imageRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(frameColor)
            .then(if (showBorder) Modifier.border(2.dp, Terracotta, RoundedCornerShape(24.dp)) else Modifier)
            .padding(20.dp)
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .fillMaxSize()
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
        Box(Modifier.fillMaxSize()) {

            val headerHeight = 260.dp

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
                            fontSize = 55.sp,
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
                            fontSize = 25.sp
                        )
                    )
                    Spacer(Modifier.height(30.dp))
                }
            }

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
                    // IA — marco terracota
                    FramedCard(
                        frameColor = Terracotta.copy(alpha = 0.70f),
                        showBorder = false,
                        imageRes = iaImage,
                        title = "IA",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        onClick = onGoIA
                    )

                    Spacer(Modifier.height(-10.dp))

                    // RA — marco crema con borde terracota
                    FramedCard(
                        frameColor = Cream,
                        showBorder = false,
                        imageRes = raImage,
                        title = "RA",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        onClick = onGoRA
                    )

                    Spacer(Modifier.height(16.dp))
                }

                // Barra inferior con iconos más grandes
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Cocoa.copy(alpha = 0.9f))
                            .border(2.dp, Terracotta, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp) // icono interno grande
                        )
                    }
                    IconButton(
                        onClick = onGoPerfil,
                        modifier = Modifier
                            .size(72.dp) // más grande
                            .clip(CircleShape)
                            .background(Cocoa.copy(alpha = 0.9f))
                            .border(2.dp, Terracotta, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Perfil",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp) // icono interno grande
                        )
                    }
                }
            }
            }
        }
}