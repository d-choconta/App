package com.decoraia.app.ui.components
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height


        val terracotta = Path().apply {
            moveTo(5f, 0f); lineTo(w * 0.5f, 1f)
            cubicTo(w * 0.1f, h * 0.10f, w * 0.9f, h * 0.20f, -700f, h * 1f)
            lineTo(1f, h); lineTo(0.1f, 30f); close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)


        val cocoa = Path().apply {
            moveTo(w * 23f, 0f); lineTo(w, w*4f); lineTo(w, h * 1f)
            cubicTo(w * 0.22f, h * 0.55f, w * 0.78f, h * 0.30f, w * 0.70f, 0f)
            close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.3f),style=Fill)
    }
}



/* ---- Login UI ---- */
@Composable
fun LoginScreenUI(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    loading: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotClick: () -> Unit,
    onBack: () -> Unit
) {
    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {


            val headerHeight =90.dp
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .align(Alignment.TopStart)
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
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = headerHeight)
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Inicia\nSesión",
                    color = Graphite,
                    style = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 50.sp,
                        lineHeight = 52.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(15.dp))


                // Logo
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .border(3.dp, Cocoa, CircleShape)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(170.dp),
                        contentScale = ContentScale.Crop
                    )

                }

                Spacer(Modifier.height(8.dp))

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
                            "Usuario",
                            style = TextStyle(
                                fontFamily = InriaSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
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
                Spacer(Modifier.height(12.dp))

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
                                fontSize = 20.sp
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

                Spacer(Modifier.height(3.dp))

                TextButton(onClick = onForgotClick, modifier = Modifier.align(Alignment.Start)) {
                    Text("¿Olvidaste tu contraseña?", color = Cocoa, fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal, fontSize = 17.sp)
                }
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onLoginClick,
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
                            "Ingresar",
                            fontFamily = InriaSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 22.sp,
                            color = Graphite
                        )
                    }

                }
                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onRegisterClick) {
                    Text(
                        "¿No tienes una cuenta?",
                        color = Cocoa,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = InriaSans
                    )
                }
                Spacer(Modifier.height(24.dp))

            }
            }
        }
}