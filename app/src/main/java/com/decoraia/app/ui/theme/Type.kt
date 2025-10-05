package com.decoraia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.decoraia.app.R

// MuseoModerno
val MuseoModerno = FontFamily(
    Font(R.font.museomoderno_variablefont_wght, weight = FontWeight.Normal),
    Font(R.font.museomoderno_italic_variablefont_wght, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// InriaSans
val InriaSans = FontFamily(
    Font(R.font.inriasans_light, weight = FontWeight.Light),
    Font(R.font.inriasans_regular, weight = FontWeight.Normal),
    Font(R.font.inriasans_bold, weight = FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle( // Título principal
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Light,
        fontSize = 60.sp,
        lineHeight = 64.sp
    ),
    headlineLarge = TextStyle( // Títulos de secciones
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 50.sp,
        lineHeight = 54.sp
    ),
    labelLarge = TextStyle( // Botones
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp
    ),
    bodyMedium = TextStyle( // Texto en campos
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle( // Subtítulos / secciones
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle( // Sección escogida
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp
    )
)
