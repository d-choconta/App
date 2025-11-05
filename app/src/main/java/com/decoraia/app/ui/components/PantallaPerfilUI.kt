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
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import com.decoraia.app.R
import com.decoraia.app.ui.theme.InriaSans
import com.decoraia.app.ui.theme.MuseoModerno

/* ===== Colores ===== */
private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa = Color(0xFFB2754E)
private val Graphite = Color(0xFF2D2A26)
private val Mint = Color(0xFF7DB686)
private val CardWarm = Color(0xFFE4A673)
private val TerracottaDark = Color(0xFFCF8A57)

/* Fondo superior ondulado */
@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracotta = Path().apply {
            moveTo(0f, 0f); lineTo(w * 0.6f, 0f)
            cubicTo(w * 0.3f, h * 0.2f, w * 0.2f, h * 1f, w * 0.19f, h * 1.7f)
            cubicTo(w * 0.05f, h * 1.9f, w * 0.009f, h * 2.1f, 0f, h * 2.4f)
            close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w, 0f); lineTo(w * 0.4f, 0f)
            cubicTo(w * 0.7f, h * 0.2f, w * 0.8f, h * 1f, w * 0.81f, h * 1.7f)
            cubicTo(w * 0.95f, h * 1.9f, w * 0.991f, h * 2.1f, w, h * 2.4f)
            close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
    }
}

@Composable
fun PantallaPerfilUI(
    nombre: String,
    email: String,
    celular: String,
    pais: String,
    photoUrl: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onFavoritos: () -> Unit,
    onChats: () -> Unit,
    onLogout: () -> Unit,
    onHome: () -> Unit
) {
    // === Estado del diálogo de confirmación de salida ===
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                        .padding(start = 17.dp, top = 17.dp)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Cocoa.copy(alpha = 0.9f))
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
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 50.sp
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
                // Avatar (URL o drawable local)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.perfil),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                // Tarjeta de datos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWarm, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Nombre: ${nombre.ifBlank { "(sin nombre)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )
                    Text(
                        "Correo: ${email.ifBlank { "(sin email)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )
                    Text(
                        "Celular: ${celular.ifBlank { "(sin celular)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )
                    Text(
                        "País: ${pais.ifBlank { "(sin país)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )

                    Spacer(Modifier.height(0.dp))
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Mint,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 26.dp, vertical = 6.dp)
                    ) { Text("Editar") }
                }

                Spacer(Modifier.height(14.dp))

                // Favoritos / Chats
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
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.favoritos),
                                contentDescription = "Favoritos",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                Modifier
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
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chats),
                                contentDescription = "Chats",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                Modifier
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

                // Botón que abre el diálogo de confirmación
                Button(
                    onClick = { showLogoutDialog = true },
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

            Row(
                Modifier
                    .align(Alignment.BottomCenter)
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
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Inicio",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.width(72.dp))
            }

            // ===== Diálogo de confirmación de salida =====
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = {
                        Text(
                            "Cerrar sesión",
                            color = Graphite,
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            )
                        )
                    },
                    text = {
                        Text(
                            "¿Estás seguro de que deseas salir de tu cuenta?",
                            color = Cocoa,
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontSize = 16.sp
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }) {
                            Text(
                                "Salir",
                                color = Terracotta,
                                fontSize = 18.sp,
                                fontFamily = InriaSans
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text(
                                "Cancelar",
                                color = Cocoa,
                                fontSize = 18.sp,
                                fontFamily = InriaSans
                            )
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = Color(0xFFF2E7D3),
                    iconContentColor = Graphite,
                    titleContentColor = Graphite,
                    textContentColor = Graphite
                )
            }
        }
    }
}
