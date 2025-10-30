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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R
import com.decoraia.app.ui.theme.InriaSans

private val Cream           = Color(0xFFFBF3E3)
private val Terracotta      = Color(0xFFE1A172)
private val TerracottaDark  = Color(0xFFCF8A57)
private val Cocoa           = Color(0xFFB2754E)
private val Graphite        = Color(0xFF2D2A26)
private val ErrorRed        = Color(0xFFD32F2F)

@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracotta = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.6f, 0f)
            cubicTo(w * 0.45f, h * 1f, w * 0.2f, h * 1.5f, w * 0.19f, h * 1.7f)
            cubicTo(w * 0.12f, h * 2f, w * 0.3f, h * 3f, 0f, h * 3.9f)
            lineTo(0f, 0f); close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w, 0f); lineTo(w * 0.4f, 0f)
            cubicTo(w * 0.55f, h * 1f, w * 0.80f, h * 1.5f, w * 0.81f, h * 1.7f)
            cubicTo(w * 0.88f, h * 2f, w * 0.70f, h * 3f, w * 1.00f, h * 3.9f)
            lineTo(w, 0f); close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
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
    onBack: () -> Unit,
    emailError: String? = null,
    passwordError: String? = null,
    authError: String? = null
) {
    val passwordFocus = remember { FocusRequester() }

    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {

            val headerHeight = 90.dp
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
                    .padding(top = 90.dp)
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
                        modifier = Modifier.size(170.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ---- Correo ----
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    singleLine = true,
                    isError = emailError != null,
                    enabled = !loading,
                    supportingText = {
                        if (emailError != null) Text(emailError, color = ErrorRed, fontSize = 12.sp)
                    },
                    textStyle = TextStyle(
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp
                    ),
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocus.requestFocus() }
                    ),
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

                Spacer(Modifier.height(5.dp))

                // ---- Contraseña ----
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    singleLine = true,
                    isError = passwordError != null,
                    enabled = !loading,
                    supportingText = {
                        if (passwordError != null) Text(passwordError, color = ErrorRed, fontSize = 12.sp)
                    },
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (!loading) onLoginClick() }
                    ),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = if (passwordError != null) ErrorRed else Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = if (passwordError != null) ErrorRed else Cocoa,
                        focusedBorderColor = if (passwordError != null) ErrorRed else Cocoa,
                        unfocusedBorderColor = if (passwordError != null) ErrorRed else Terracotta,
                        errorBorderColor = ErrorRed,
                        errorLabelColor = ErrorRed,
                        errorCursorColor = ErrorRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocus)
                )

                Spacer(Modifier.height(0.dp))

                TextButton(onClick = onForgotClick, modifier = Modifier.align(Alignment.Start), enabled = !loading) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        color = Cocoa,
                        fontFamily = InriaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp
                    )
                }
                Spacer(Modifier.height(0.dp))

                Button(
                    onClick = onLoginClick,
                    enabled = !loading,
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

                if (!authError.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(authError!!, color = ErrorRed, fontSize = 13.sp)
                }

                Spacer(Modifier.height(0.dp))

                TextButton(onClick = onRegisterClick, enabled = !loading) {
                    Text(
                        "¿No tienes una cuenta?",
                        color = Cocoa,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = InriaSans
                    )
                }
                Spacer(Modifier.height(0.dp))
            }
            }
        }
}