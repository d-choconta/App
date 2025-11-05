package com.decoraia.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R
import com.decoraia.app.ui.theme.MuseoModerno

private val TaupeBackground = Color(0xFFB9A892)
private val White           = Color(0xFFFFFFFF)

@Composable
fun CargaScreenUI(
    appName: String,
    logoRes: Int = R.drawable.logodecoraia
) {
    Surface(color = TaupeBackground) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = appName.uppercase(),
                    color = White,
                    style = TextStyle(
                        fontFamily = MuseoModerno,
                        fontWeight = FontWeight.Normal,
                        fontSize = 36.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                )

                Spacer(Modifier.height(28.dp))

                // Logo dentro de un círculo
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .border(2.dp, White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(0.58f),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(36.dp))

                // Barra de progreso con gradiente y animación simple
                val transition = rememberInfiniteTransition(label = "splash_bar")
                val progress = transition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "splash_anim"
                )

                // Track "invisible" y relleno con gradiente redondeado
                val gradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFC58B61), // terracota suave
                        Color(0xFFE7D6BE)  // crema claro
                    )
                )

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x22FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value)
                            .clip(RoundedCornerShape(50))
                            .background(brush = gradient)
                    )
                }
            }
        }
    }
}
