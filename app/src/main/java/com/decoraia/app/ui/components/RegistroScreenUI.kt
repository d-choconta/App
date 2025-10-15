package com.decoraia.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R
import com.decoraia.app.ui.theme.InriaSans

private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val TerracottaDark = Color(0xFFCF8A57)
private val Cocoa = Color(0xFFB2754E)
private val Graphite = Color(0xFF2D2A26)

@Composable
private fun TopWavesRegistro(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height


        val terracotta = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.50f, 0f)

            cubicTo(
                w * 0.40f, h * 1f,
                w * 0.1f, h * 1f,
                w * 0.19f, h * 1f
            )

            cubicTo(
                w * 0.06f, h * 1f,
                w * 0.2f, h * 3f,
                0f,       h * 3.9f
            )

            lineTo(0f, 0f)
            close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w, 0f)
            lineTo(w * 0.50f, 0f)


            cubicTo(
                w * 0.60f, h * 1f,
                w * 0.9f,  h * 1f,
                w * 0.81f, h * 1f
            )


            cubicTo(
                w * 0.94f, h * 1f,
                w * 0.80f, h * 3f,
                w,        h * 3.9f
            )

            lineTo(w, 0f)
            close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
    }
}

/* ----------- UI de Registro ----------- */
@Composable
fun RegistroScreenUI(
    nombre: String,
    onNombreChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirm: String,
    onConfirmChange: (String) -> Unit,
    loading: Boolean,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit,
    onHaveAccountClick: () -> Unit,
    @DrawableRes heroImage: Int = R.drawable.registro,

    titleYOffset: Dp = (-10).dp,
    heroHeight: Dp = 280.dp,
    heroWidthFraction: Float = 0.80f
) {
    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {
            val headerHeight = 70.dp

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .align(Alignment.TopStart)
            ) {
                TopWavesRegistro(Modifier.fillMaxSize())
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(start = 17.dp, top = 17.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Cocoa.copy(alpha = 0.9f))
                        .border(2.dp, Terracotta, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "¿Primera vez?",
                    color = Graphite,
                    style = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 50.sp,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.offset(y = titleYOffset)
                )

                Spacer(Modifier.height(0.dp))


                Image(
                    painter = painterResource(heroImage),
                    contentDescription = "Registro",
                    modifier = Modifier
                        .height(heroHeight)
                        .fillMaxWidth(heroWidthFraction),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(-2.dp))


                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp
                    ),
                    label = {
                        Text(
                            "Nombre de Usuario",
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 19.sp
                            )
                        )
                    },
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

                Spacer(Modifier.height(8.dp))

                // Correo
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp
                    ),
                    label = {
                        Text(
                            "Correo Electrónico",
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 19.sp
                            )
                        )
                    },
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

                Spacer(Modifier.height(8.dp))

                // Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp
                    ),
                    label = {
                        Text(
                            "Contraseña",
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 19.sp
                            )
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
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

                Spacer(Modifier.height(8.dp))

                // Confirmar contraseña
                OutlinedTextField(
                    value = confirm,
                    onValueChange = onConfirmChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp
                    ),
                    label = {
                        Text(
                            "Confirmar contraseña",
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 19.sp
                            )
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
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

                Spacer(Modifier.height(15.dp))

                Button(
                    onClick = onRegisterClick,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Graphite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            "Registrar",
                            fontFamily = InriaSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onHaveAccountClick) {
                    Text(
                        "¿Ya tienes una cuenta?",
                        color = Cocoa,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = InriaSans
                    )
                }

                Spacer(Modifier.height(15.dp))
            }
            }
        }
}