package com.decoraia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.decoraia.app.R

// ---------- Font Families ----------
val MuseoModerno = FontFamily(
    Font(R.font.museomoderno_variablefont_wght, weight = FontWeight.Normal),
    Font(
        R.font.museomoderno_italic_variablefont_wght,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    )
)

val InriaSans = FontFamily(
    Font(R.font.inriasans_light,   weight = FontWeight.Light),
    Font(R.font.inriasans_regular, weight = FontWeight.Normal),
    Font(R.font.inriasans_bold,    weight = FontWeight.Bold)
)

// ---------- Typography (roles M3) ----------
val Typography = Typography(
    // TITLES / HEADLINES (MuseoModerno)
    displayLarge = TextStyle(
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Light,
        fontSize = 60.sp,
        lineHeight = 64.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Normal,
        fontSize = 50.sp,
        lineHeight = 54.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MuseoModerno,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),

    // BODY (InriaSans) — muy usado por defecto
    bodyLarge = TextStyle(
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InriaSans,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp
    ),

    // LABELS (InriaSans) — botones, chips, etc.
    labelLarge = TextStyle(
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InriaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)
