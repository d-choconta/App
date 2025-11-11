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
import androidx.compose.foundation.layout.BoxWithConstraints
import coil.compose.AsyncImage
import com.decoraia.app.R
import com.decoraia.app.ui.theme.InriaSans

/* Paleta */
private val Cream      = Color(0xFFFBF3E3)
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
    photoUrl: String?,
    onChangePhotoClick: () -> Unit
) {
    Surface(color = Cream) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp

            val headerH = if (isTablet) 190.dp else 155.dp
            val horizontalPad = when {
                maxWidth >= 1000.dp -> 28.dp
                maxWidth >= 720.dp  -> 22.dp
                else                -> 20.dp
            }
            val titleSize = if (isTablet) 64.sp else 50.sp
            val avatarSize = when {
                maxWidth >= 1000.dp -> 380.dp
                maxWidth >= 720.dp  -> 340.dp
                else                -> 220.dp
            }
            val fieldText = if (isTablet) 20.sp else 18.sp
            val saveBtnHeight = if (isTablet) 50.dp else 44.dp
            val bottomBtnSize = if (isTablet) 80.dp else 72.dp
            val bottomIconSize = if (isTablet) 40.dp else 36.dp
            val fabSize = if (isTablet) 56.dp else 48.dp
            val fabIcon = if (isTablet) 30.dp else 26.dp

            // Header
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
                        fontSize = titleSize
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }

            // CONTENIDO centrado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerH)
                    .padding(horizontal = horizontalPad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Avatar grande + FAB
                Box(
                    modifier = Modifier.size(avatarSize),
                    contentAlignment = Alignment.Center
                ) {
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

                    SmallFloatingActionButton(
                        onClick = onChangePhotoClick,
                        containerColor = Terracotta,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp)
                            .size(fabSize)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Cambiar foto",
                            modifier = Modifier.size(fabIcon)
                        )
                    }
                }

                Spacer(Modifier.height(if (isTablet) 12.dp else 0.dp))

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
                            label = { Text("Nombre de Usuario", fontSize = fieldText) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = tfShape, colors = tfColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = celular, onValueChange = onCelular,
                            label = { Text("Celular", fontSize = fieldText) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = tfShape, colors = tfColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = pais, onValueChange = onPais,
                            label = { Text("País", fontSize = fieldText) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = tfShape, colors = tfColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = password, onValueChange = onPassword,
                            label = { Text("Contraseña", fontSize = fieldText) },
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
                                .height(saveBtnHeight)
                        ) { Text("Guardar", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }

            // Botón home inferior
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
            }
        }
}