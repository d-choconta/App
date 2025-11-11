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
import androidx.compose.foundation.layout.BoxWithConstraints
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
    var showLogoutDialog by remember { mutableStateOf(false) }

    Surface(color = Cream) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp
            val headerH = if (isTablet) 190.dp else 155.dp
            val horizontalPad = when {
                maxWidth >= 1000.dp -> 28.dp
                maxWidth >= 720.dp  -> 22.dp
                else                -> 16.dp
            }

            val titleSize = if (isTablet) 64.sp else 50.sp
            val avatarSize = if (isTablet) 260.dp else 220.dp
            val cardTitleSize = if (isTablet) 22.sp else 20.sp
            val quickCardHeight = if (isTablet) 130.dp else 110.dp
            val bottomBtnSize = if (isTablet) 80.dp else 72.dp
            val bottomIconSize = if (isTablet) 40.dp else 36.dp
            val actionBtnHeight = if (isTablet) 52.dp else 46.dp

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
                        .size(if (isTablet) 66.dp else 60.dp)
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
                        fontSize = titleSize
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }

            // CONTENIDO: centrado vertical en el alto restante
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerH)
                    .padding(horizontal = horizontalPad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Avatar
                Box(
                    modifier = Modifier
                        .size(avatarSize)
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

                Spacer(Modifier.height(if (isTablet) 8.dp else 2.dp))

                // Datos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWarm, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Nombre: ${nombre.ifBlank { "(sin nombre)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = cardTitleSize
                    )
                    Text(
                        "Correo: ${email.ifBlank { "(sin email)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = cardTitleSize
                    )
                    Text(
                        "Celular: ${celular.ifBlank { "(sin celular)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = cardTitleSize
                    )
                    Text(
                        "País: ${pais.ifBlank { "(sin país)" }}",
                        color = Color.White, fontFamily = MuseoModerno,
                        fontWeight = FontWeight.SemiBold, fontSize = cardTitleSize
                    )

                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Mint,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp)
                    ) { Text("Editar") }
                }

                Spacer(Modifier.height(16.dp))

                // Favoritos / Chats
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedCard(
                        onClick = onFavoritos,
                        modifier = Modifier
                            .weight(1f)
                            .height(quickCardHeight),
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
                            .height(quickCardHeight),
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

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(actionBtnHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE9B0B0),
                        contentColor = Graphite
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Log out", fontWeight = FontWeight.SemiBold) }
            }

            // Bottom floating home
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
                        .size(bottomBtnSize)
                        .clip(CircleShape)
                        .background(Cocoa.copy(alpha = 0.9f))
                        .border(2.dp, Terracotta, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Inicio",
                        tint = Color.White,
                        modifier = Modifier.size(bottomIconSize)
                    )
                }
                Spacer(Modifier.width(bottomBtnSize))
            }

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
                            style = TextStyle(fontFamily = InriaSans, fontSize = 16.sp)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }) {
                            Text("Salir", color = Terracotta, fontSize = 18.sp, fontFamily = InriaSans)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancelar", color = Cocoa, fontSize = 18.sp, fontFamily = InriaSans)
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