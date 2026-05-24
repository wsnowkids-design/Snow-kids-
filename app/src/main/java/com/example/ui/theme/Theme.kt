package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkSagePrimary,
    onPrimary = DarkSageOnPrimary,
    primaryContainer = DarkSageContainer,
    secondary = DarkSlateSecondary,
    onSecondary = DarkSlateOnSecondary,
    secondaryContainer = DarkSlateContainer,
    tertiary = DarkLeafTertiary,
    tertiaryContainer = DarkLeafContainer,
    background = DarkCharcoalBackground,
    onBackground = DarkSandOnBackground,
    surface = DarkDeepSurface,
    onSurface = DarkSandOnBackground,
    surfaceVariant = DarkEarthyBorderOutline,
    onSurfaceVariant = DarkSlateSecondary,
    outline = DarkEarthyBorderOutline
)

private val LightColorScheme = lightColorScheme(
    primary = EarthyGreenPrimary,
    onPrimary = EarthyGreenOnPrimary,
    primaryContainer = EarthyGreenContainer,
    onPrimaryContainer = CharcoalTextOnSurface,
    secondary = NaturalSlateSecondary,
    onSecondary = EarthyGreenOnPrimary,
    secondaryContainer = NaturalSlateContainer,
    onSecondaryContainer = CharcoalTextOnSurface,
    tertiary = LeafTertiary,
    tertiaryContainer = LeafTertiaryContainer,
    background = CustomCreamBackground,
    onBackground = CharcoalTextOnSurface,
    surface = BrightPearlSurface,
    onSurface = CharcoalTextOnSurface,
    surfaceVariant = SlateSoftContainer,
    onSurfaceVariant = NaturalSlateSecondary,
    outline = EarthyBorderOutline,
    outlineVariant = SoftBorderOutline,
    error = ErrorCrimson,
    errorContainer = ErrorCrimsonContainer,
    onErrorContainer = OnErrorCrimsonContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamic color to false by default to ensure the "Natural Tones" palette is always shown clearly
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
