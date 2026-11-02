package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudioColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = DarkStudioBg,
  primaryContainer = DarkStudioCard,
  onPrimaryContainer = NeonCyan,
  secondary = NeonAmber,
  onSecondary = DarkStudioBg,
  secondaryContainer = DarkStudioCard,
  onSecondaryContainer = NeonAmber,
  tertiary = NewtonePurple,
  onTertiary = DarkStudioBg,
  background = DarkStudioBg,
  onBackground = TextPrimary,
  surface = DarkStudioSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkStudioCard,
  onSurfaceVariant = TextSecondary,
  outline = DarkStudioBorder,
  error = CoralRed,
  onError = DarkStudioBg
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Always use dark studio color scheme for eye comfort during long production sessions
  MaterialTheme(
    colorScheme = StudioColorScheme,
    typography = Typography,
    content = content
  )
}
