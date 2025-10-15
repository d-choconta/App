package com.decoraia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R

private val Cream      = Color(0xFFFBF3E3)
private val CreamDark  = Color(0xFFF2E7D3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa      = Color(0xFFB2754E)
private val Graphite   = Color(0xFF2D2A26)
private val Mint       = Color(0xFF7DB686)
private val CardWarm   = Color(0xFFE4A673)

@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracottaBlob = Path().apply {
            moveTo(w * .62f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * .55f)
            cubicTo(w * .88f, h * .32f, w * .78f, h * .18f, w * .62f, 0f)
            close()
        }
        drawPath(terracottaBlob, Terracotta, style = Fill)

        val creamTongue = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * .36f, 0f)
            cubicTo(w * .18f, h * .25f, w * .18f, h * .50f, 0f, h * .64f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(creamTongue, CreamDark, style = Fill)
    }
}

@Composable
fun PantallaPerfilUI(
    nombre: String,
    email: String,
    celular: String,
    pais: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onFavoritos: () -> Unit,
    onChats: () -> Unit,
    onLogout: () -> Unit,
    onHome: () -> Unit
) {
    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {

            val headerH = 155.dp
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(headerH)
                    .align(Alignment.TopCenter)
            ) {
                TopWaves(Modifier.fillMaxSize())

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(start = 14.dp, top = 14.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Cocoa.copy(alpha = .95f))
                        .border(2.dp, Terracotta, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }

                Text(
                    "Perfil",
                    style = TextStyle(
                        color = Graphite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerH)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Avatar (perfil)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(30.dp))

                // Tarjeta con datos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWarm, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text("Nombre: ${nombre.ifBlank { "(sin nombre)" }}", color = Color.White, fontSize = 14.sp)
                    Text("Correo: ${email.ifBlank { "(sin email)" }}",   color = Color.White, fontSize = 14.sp)
                    Text("Celular: ${celular.ifBlank { "(sin celular)" }}", color = Color.White, fontSize = 14.sp)
                    Text("País: ${pais.ifBlank { "(sin país)" }}",       color = Color.White, fontSize = 14.sp)

                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Mint,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) { Text("Editar") }
                }

                Spacer(Modifier.height(14.dp))

                // Tarjetas con imágenes para Favoritos / Chats
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedCard(
                        onClick = onFavoritos,
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.favoritos),
                                contentDescription = "Favoritos",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )
                            Text(
                                "Favoritos",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            )
                        }
                    }

                    ElevatedCard(
                        onClick = onChats,
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chats),
                                contentDescription = "Chats",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )
                            Text(
                                "Chats",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE9B0B0),
                        contentColor = Graphite
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Log out", fontWeight = FontWeight.SemiBold) }

                Spacer(Modifier.height(8.dp))
            }

            // Home
            FloatingActionButton(
                onClick = onHome,
                containerColor = Terracotta,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(54.dp)
            ) { Icon(Icons.Filled.Home, contentDescription = "Home") }
            }
        }
}