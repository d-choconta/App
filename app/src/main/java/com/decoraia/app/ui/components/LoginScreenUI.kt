package com.decoraia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

/* ---- Paleta (puedes moverla a un Theme global luego) ---- */
private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val TerracottaDark = Color(0xFFCF8A57)
private val Cocoa = Color(0xFFB2754E)
private val Graphite = Color(0xFF2D2A26)

private val FieldShape = RoundedCornerShape(18.dp)
private val PillShape = RoundedCornerShape(26.dp)

/* Fondo con formas orgánicas superiores */
@Composable
private fun TopWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        Path().apply {
            moveTo(0f, 0f); lineTo(w * .45f, 0f)
            cubicTo(w * .25f, h * .15f, w * .25f, h * .28f, 0f, h * .35f); close()
        }.also { drawPath(it, TerracottaDark, style = Fill) }

        Path().apply {
            moveTo(w * .45f, 0f); lineTo(w, 0f); lineTo(w, h * .45f)
            cubicTo(w * .78f, h * .42f, w * .72f, h * .18f, w * .45f, 0f); close()
        }.also { drawPath(it, Cocoa.copy(alpha = .25f), style = Fill) }
    }
}

/* ---- Composable reutilizable de UI (sin lógica) ---- */
@Composable
fun LoginScreenUI(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    loading: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotClick: () -> Unit
) {
    Surface(color = Cream) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con ondas
            Box(Modifier.fillMaxWidth().height(140.dp)) { TopWaves(Modifier.fillMaxSize()) }

            Text(
                "Inicia\nSesión",
                color = Graphite,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // Círculo con tu logo.png
            Box(
                Modifier.size(180.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(10.dp)
                        .clip(CircleShape)
                        .border(3.dp, Cocoa, CircleShape)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                singleLine = true,
                label = { Text("Usuario") },
                shape = FieldShape,
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
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                shape = FieldShape,
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

            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onForgotClick, modifier = Modifier.align(Alignment.Start)) {
                Text("¿Olvidaste tu contraseña?", color = Cocoa, fontSize = 14.sp)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onLoginClick,
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Graphite),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                else Text("Ingresar", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRegisterClick) { Text("¿No tienes una cuenta?", color = Graphite) }
            Spacer(Modifier.height(24.dp))
            }
        }
}