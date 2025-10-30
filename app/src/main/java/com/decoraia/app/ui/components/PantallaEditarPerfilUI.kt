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
import androidx.compose.material.icons.filled.Add
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
import coil.compose.AsyncImage
import com.decoraia.app.R
import com.decoraia.app.ui.theme.InriaSans

/* Paleta */
private val Cream      = Color(0xFFFBF3E3)
private val CreamDark  = Color(0xFFF2E7D3)
private val Terracotta = Color(0xFFE1A172)
private val TerracottaDark  = Color(0xFFCF8A57)
private val Cocoa      = Color(0xFFB2754E)
private val Graphite   = Color(0xFF2D2A26)
private val Mint       = Color(0xFF7DB686)
private val PanelPeach = Color(0xFFECC8AC)

/* Ondas superiores */
@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracotta = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.6f, 0f)
            cubicTo(w * 0.3f, h * 0.2f, w * 0.2f, h * 1f, w * 0.19f, h * 1.7f)
            cubicTo(w * 0.05f, h * 1.9f, w * 0.009f, h * 2.1f, 0f, h * 2.4f)
            lineTo(0f, 0f); close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w, 0f); lineTo(w * 0.4f, 0f)
            cubicTo(w * 0.7f, h * 0.2f, w * 0.8f, h * 1f, w * 0.81f, h * 1.7f)
            cubicTo(w * 0.95f, h * 1.9f, w * 0.991f, h * 2.1f, w, h * 2.4f)
            lineTo(w, 0f); close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
    }
}

/* UI */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarPerfilUI(
    nombre: String,      onNombre: (String) -> Unit,
    celular: String,     onCelular: (String) -> Unit,
    pais: String,        onPais: (String) -> Unit,
    password: String,    onPassword: (String) -> Unit,
    loading: Boolean,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    // NUEVOS
    photoUrl: String?,
    onChangePhotoClick: () -> Unit
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
                        .padding(start = 17.dp, top = 17.dp)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Cocoa.copy(alpha = 0.9f))
                        .border(2.dp, Terracotta, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Perfil",
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
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ===== Avatar + botón =====
                Box(
                    modifier = Modifier
                        .size(220.dp),   // OJO: sin clip aquí para no recortar el FAB
                    contentAlignment = Alignment.Center
                ) {
                    // Círculo con borde que SÍ recorta solo la foto
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .border(3.dp, Cocoa, CircleShape)
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                placeholder = painterResource(R.drawable.perfil),
                                error = painterResource(R.drawable.perfil),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.perfil),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }

                    // FAB (+) visible y con borde
                    SmallFloatingActionButton(
                        onClick = onChangePhotoClick,
                        containerColor = Terracotta,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp) // un poco hacia adentro
                            .size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Cambiar foto",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(0.dp))

                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = PanelPeach),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        val tfShape = RoundedCornerShape(22.dp)
                        val tfColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Terracotta,
                            unfocusedBorderColor = Terracotta.copy(alpha = .8f),
                            cursorColor = Cocoa,
                            focusedTextColor = Graphite,
                            unfocusedTextColor = Graphite,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )

                        OutlinedTextField(
                            value = nombre, onValueChange = onNombre,
                            label = { Text("Nombre de Usuario") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = tfShape, colors = tfColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = celular, onValueChange = onCelular,
                            label = { Text("Celular") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = tfShape, colors = tfColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = pais, onValueChange = onPais,
                            label = { Text("País") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = tfShape, colors = tfColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = password, onValueChange = onPassword,
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = tfShape, colors = tfColors, singleLine = true
                        )

                        Button(
                            onClick = onGuardar,
                            enabled = !loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Mint,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

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
        }
    }
}
