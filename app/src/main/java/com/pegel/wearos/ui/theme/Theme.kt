package com.pegel.wearos.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val DarkColorPalette = Colors(
    primary = Blue,
    primaryVariant = BlueDark,
    secondary = BlueLight,
    secondaryVariant = BlueLight,
    background = Black,
    surface = GrayDark,
    error = Color(0xFFB00020),
    onPrimary = White,
    onSecondary = Black,
    onBackground = White,
    onSurface = White,
    onError = Black
)

private val LightColorPalette = Colors(
    primary = Blue,
    primaryVariant = BlueDark,
    secondary = BlueLight,
    secondaryVariant = BlueLight,
    background = White,
    surface = GrayLight,
    error = Color(0xFFB00020),
    onPrimary = White,
    onSecondary = Black,
    onBackground = Black,
    onSurface = Black,
    onError = White
)

@Composable
fun PegelTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}
