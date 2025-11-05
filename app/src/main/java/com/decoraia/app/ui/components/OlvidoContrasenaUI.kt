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
import com.decoraia.app.ui.theme.InriaSans

// Paleta
private val Cream      = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa      = Color(0xFFB2754E)
private val Graphite   = Color(0xFF2D2A26)
private val ErrorRed   = Color(0xFFD32F2F)
private val SuccessGre = Color(0xFF2E7D32)

@Composable
fun OlvidoContrasenaUI(
    usuario: String,
    onUsuarioChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    loading: Boolean,
    onEnviarCodigo: () -> Unit,
    onBack: () -> Unit,
    emailError: String? = null,
    actionError: String? = null,
    successMessage: String? = null
) {
    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {

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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 90.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¿Olvidaste tu",
                    color = Graphite,
                    style = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 50.sp,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "contraseña?",
                    color = Graphite,
                    style = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 50.sp,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.olvidocontrasena),
                    contentDescription = "Olvido contraseña",
                    modifier = Modifier
                        .size(240.dp)
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
                    label = {
                        Text(
                            "Usuario",
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
                            )
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
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
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) Text(emailError, color = ErrorRed, fontSize = 12.sp)
                    },
                    label = {
                        Text(
                            "Correo electrónico",
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
                            )
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = if (emailError != null) ErrorRed else Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = if (emailError != null) ErrorRed else Cocoa,
                        focusedBorderColor = if (emailError != null) ErrorRed else Cocoa,
                        unfocusedBorderColor = if (emailError != null) ErrorRed else Terracotta,
                        errorBorderColor = ErrorRed,
                        errorLabelColor = ErrorRed,
                        errorCursorColor = ErrorRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))

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
                        Text("Enviar Código", fontSize = 22.sp, fontWeight = FontWeight.Normal, fontFamily = InriaSans)
                    }
                }

                // Mensajes bajo el botón
                when {
                    !actionError.isNullOrBlank() -> {
                        Spacer(Modifier.height(8.dp))
                        Text(actionError!!, color = ErrorRed, fontSize = 13.sp)
                    }
                    !successMessage.isNullOrBlank() -> {
                        Spacer(Modifier.height(8.dp))
                        Text(successMessage!!, color = SuccessGre, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
