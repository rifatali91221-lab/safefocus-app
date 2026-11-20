package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SafeFocusColorScheme = darkColorScheme(
    primary = NormalVioletPrimary,
    onPrimary = PureLavenderWhite,
    primaryContainer = DeepVioletSurface,
    onPrimaryContainer = LightVioletText,
    secondary = NormalVioletAccent,
    onSecondary = PureLavenderWhite,
    secondaryContainer = DeepVioletSurface,
    onSecondaryContainer = LightVioletBorder,
    tertiary = LightVioletBorder,
    onTertiary = DeepVioletBg,
    background = DeepVioletBg,
    onBackground = PureLavenderWhite,
    surface = DeepVioletSurface,
    onSurface = PureLavenderWhite,
    surfaceVariant = DeepVioletSurface,
    onSurfaceVariant = LightVioletText,
    outline = LightVioletBorder.copy(alpha = 0.35f),
    outlineVariant = LightVioletBorder.copy(alpha = 0.16f),
)

@Composable
fun SafeFocusTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SafeFocusColorScheme,
        typography = SafeFocusTypography,
        content = content
    )
}
