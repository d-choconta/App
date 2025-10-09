package com.decoraia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R

// Paleta
private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa = Color(0xFFB2754E)
private val Graphite = Color(0xFF2D2A26)

@Composable
fun OlvidoContrasenaUI(
    usuario: String,
    onUsuarioChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    loading: Boolean,
    onEnviarCodigo: () -> Unit,
    onBack: () -> Unit
) {
    // Fondo NEUTRO (sin imagen de fondo)
    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {

            // Flecha atrás
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .size(44.dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .background(Cocoa.copy(alpha = 0.92f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 90.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¿Olvidaste tu\ncontraseña?",
                    color = Graphite,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 34.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Imagen dentro del círculo (sin borde), usando olvidocontrasena.jpg
                Image(
                    painter = painterResource(id = R.drawable.olvidocontrasena),
                    contentDescription = "Olvido contraseña",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .shadow(6.dp, CircleShape, clip = true),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(20.dp))

                // Usuario
                OutlinedTextField(
                    value = usuario,
                    onValueChange = onUsuarioChange,
                    singleLine = true,
                    label = { Text("Usuario") },
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = Cocoa,
                        focusedBorderColor = Cocoa,
                        unfocusedBorderColor = Terracotta
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Correo electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    singleLine = true,
                    label = { Text("Correo electrónico") },
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = Cocoa,
                        focusedBorderColor = Cocoa,
                        unfocusedBorderColor = Terracotta
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))

                // Botón Enviar Código
                Button(
                    onClick = onEnviarCodigo,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Graphite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = Graphite
                        )
                    } else {
                        Text("Enviar Código", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
