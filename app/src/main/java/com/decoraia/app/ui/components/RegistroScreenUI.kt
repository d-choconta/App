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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R
import com.decoraia.app.ui.theme.InriaSans
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

private val Cream          = Color(0xFFFBF3E3)
private val Terracotta     = Color(0xFFE1A172)
private val TerracottaDark = Color(0xFFCF8A57)
private val Cocoa          = Color(0xFFB2754E)
private val Graphite       = Color(0xFF2D2A26)
private val ErrorRed       = Color(0xFFD32F2F)

@Composable
private fun TopWavesRegistro(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val terracotta = Path().apply {
            moveTo(0f, 0f); lineTo(w * 0.50f, 0f)
            cubicTo(w * 0.40f, h * 1f, w * 0.1f, h * 1f, w * 0.19f, h * 1f)
            cubicTo(w * 0.06f, h * 1f, w * 0.2f, h * 3f, 0f, h * 3.9f)
            lineTo(0f, 0f); close()
        }
        drawPath(terracotta, TerracottaDark, style = Fill)

        val cocoa = Path().apply {
            moveTo(w, 0f); lineTo(w * 0.50f, 0f)
            cubicTo(w * 0.60f, h * 1f, w * 0.9f,  h * 1f, w * 0.81f, h * 1f)
            cubicTo(w * 0.94f, h * 1f, w * 0.80f, h * 3f, w, h * 3.9f)
            lineTo(w, 0f); close()
        }
        drawPath(cocoa, Cocoa.copy(alpha = 0.30f), style = Fill)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    nombreError: String? = null,
    emailError: String? = null,
    passwordError: String? = null,
    confirmError: String? = null,
    actionError: String? = null,
    titleYOffset: Dp = (-10).dp,
    heroHeight: Dp = 280.dp,
    heroWidthFraction: Float = 0.80f
) {
    val focus = LocalFocusManager.current

    Surface(color = Cream) {
        Box(Modifier.fillMaxSize()) {
            val headerHeight = 66.dp

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
                    .padding(top = 59.dp)
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

                Spacer(Modifier.height(0.dp))

                val shape = RoundedCornerShape(22.dp)

                // Nombre — Next
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    singleLine = true,
                    isError = nombreError != null,
                    supportingText = { if (nombreError != null) Text(nombreError, color = ErrorRed, fontSize = 12.sp) },
                    textStyle = TextStyle(fontFamily = InriaSans, fontWeight = FontWeight.Normal, fontSize = 20.sp),
                    label = { Text("Nombre de Usuario", style = TextStyle(fontFamily = InriaSans, fontSize = 19.sp)) },
                    shape = shape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = if (nombreError != null) ErrorRed else Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = if (nombreError != null) ErrorRed else Cocoa,
                        focusedBorderColor = if (nombreError != null) ErrorRed else Cocoa,
                        unfocusedBorderColor = if (nombreError != null) ErrorRed else Terracotta,
                        errorBorderColor = ErrorRed, errorLabelColor = ErrorRed, errorCursorColor = ErrorRed
                    ),
                    // 👇 IME "Siguiente"
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(0.dp))

                // Correo — teclado con @ y Next
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = { if (emailError != null) Text(emailError, color = ErrorRed, fontSize = 12.sp) },
                    textStyle = TextStyle(fontFamily = InriaSans, fontWeight = FontWeight.Normal, fontSize = 20.sp),
                    label = { Text("Correo Electrónico", style = TextStyle(fontFamily = InriaSans, fontSize = 19.sp)) },
                    shape = shape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = if (emailError != null) ErrorRed else Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = if (emailError != null) ErrorRed else Cocoa,
                        focusedBorderColor = if (emailError != null) ErrorRed else Cocoa,
                        unfocusedBorderColor = if (emailError != null) ErrorRed else Terracotta,
                        errorBorderColor = ErrorRed, errorLabelColor = ErrorRed, errorCursorColor = ErrorRed
                    ),
                    // 👇 Teclado de email y Next
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(0.dp))

                // Contraseña — Next
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = { if (passwordError != null) Text(passwordError, color = ErrorRed, fontSize = 12.sp) },
                    textStyle = TextStyle(fontFamily = InriaSans, fontWeight = FontWeight.Normal, fontSize = 20.sp),
                    label = { Text("Contraseña", style = TextStyle(fontFamily = InriaSans, fontSize = 19.sp)) },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = shape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = if (passwordError != null) ErrorRed else Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = if (passwordError != null) ErrorRed else Cocoa,
                        focusedBorderColor = if (passwordError != null) ErrorRed else Cocoa,
                        unfocusedBorderColor = if (passwordError != null) ErrorRed else Terracotta,
                        errorBorderColor = ErrorRed, errorLabelColor = ErrorRed, errorCursorColor = ErrorRed
                    ),
                    // 👇 Next
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(0.dp))

                // Confirmar contraseña — Done (enviar)
                OutlinedTextField(
                    value = confirm,
                    onValueChange = onConfirmChange,
                    singleLine = true,
                    isError = confirmError != null,
                    supportingText = { if (confirmError != null) Text(confirmError, color = ErrorRed, fontSize = 12.sp) },
                    textStyle = TextStyle(fontFamily = InriaSans, fontWeight = FontWeight.Normal, fontSize = 20.sp),
                    label = { Text("Confirmar contraseña", style = TextStyle(fontFamily = InriaSans, fontSize = 19.sp)) },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = shape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Graphite,
                        unfocusedTextColor = Graphite,
                        focusedLabelColor = if (confirmError != null) ErrorRed else Cocoa,
                        unfocusedLabelColor = Cocoa.copy(alpha = .9f),
                        cursorColor = if (confirmError != null) ErrorRed else Cocoa,
                        focusedBorderColor = if (confirmError != null) ErrorRed else Cocoa,
                        unfocusedBorderColor = if (confirmError != null) ErrorRed else Terracotta,
                        errorBorderColor = ErrorRed, errorLabelColor = ErrorRed, errorCursorColor = ErrorRed
                    ),
                    // 👇 Done: dispara el registro y cierra el teclado
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focus.clearFocus()
                            onRegisterClick()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!actionError.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(actionError, color = ErrorRed, fontSize = 13.sp)
                }

                Spacer(Modifier.height(0.dp))

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
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Registrar", fontFamily = InriaSans, fontWeight = FontWeight.Light, fontSize = 22.sp)
                    }
                }

                Spacer(Modifier.height(0.dp))

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
