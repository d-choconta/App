package com.decoraia.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    secondary = BrownAccent,
    background = BeigeBackground,
    surface = BeigeBackground,
    onPrimary = BeigeBackground,
    onBackground = BrownAccent
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
