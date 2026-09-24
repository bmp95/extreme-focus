package app.extremefocus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ExtremeColorScheme = darkColorScheme(
    primary = CrimsonDanger,
    onPrimary = PitchBlack,
    primaryContainer = CrimsonMuted,
    onPrimaryContainer = TextPrimary,
    secondary = AmberWarning,
    onSecondary = PitchBlack,
    tertiary = AccentCyan,
    background = PitchBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ExtremeColorScheme,
        typography = Typography,
        content = content
    )
}
