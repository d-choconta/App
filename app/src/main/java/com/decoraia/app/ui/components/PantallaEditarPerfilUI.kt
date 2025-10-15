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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R

/* Paleta */
private val Cream      = Color(0xFFFBF3E3)
private val CreamDark  = Color(0xFFF2E7D3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa      = Color(0xFFB2754E)
private val Graphite   = Color(0xFF2D2A26)
private val Mint       = Color(0xFF7DB686)
/* Panel claro tipo mock */
private val PanelPeach = Color(0xFFECC8AC)

/* Ondas superiores */
@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // mancha terracota arriba-derecha
        val terracottaBlob = Path().apply {
            moveTo(w * .70f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * .55f)
            cubicTo(w * .88f, h * .30f, w * .80f, h * .15f, w * .65f, 0f)
            close()
        }
        drawPath(terracottaBlob, Terracotta, style = Fill)

        // lengua crema oscura desde la izquierda
        val creamTongue = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * .45f, 0f)
            cubicTo(w * .18f, h * .28f, w * .20f, h * .58f, 0f, h * .78f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(creamTongue, CreamDark, style = Fill)
    }
}

/* ============ UI ============ */
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
    onHome: () -> Unit
) {
    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {

            /* Header con ondas + flecha + título */
            val headerH = 190.dp
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
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Perfil",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Graphite,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }

            /* Contenido */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerH)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                /* Avatar grande + badge “+” */
                Box(
                    modifier = Modifier
                        .size(156.dp)                // <-- más grande
                        .clip(CircleShape)
                        .background(Terracotta.copy(alpha = 0.55f))
                        .border(6.dp, Terracotta, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Badge con "+"
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CreamDark)
                            .border(2.dp, Terracotta, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Cambiar foto",
                            tint = Terracotta
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* Panel con campos */
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

            /* Casita fija abajo-izquierda */
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