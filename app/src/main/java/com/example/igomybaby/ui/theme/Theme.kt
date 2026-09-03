package com.example.igomybaby.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary              = LightPrimary,
    onPrimary            = LightPrimaryOn,
    primaryContainer     = LightPrimaryFaint,
    onPrimaryContainer   = LightPrimaryDeep,
    secondary            = LightWarn,
    onSecondary          = LightOnSurface,
    secondaryContainer   = LightWarnFaint,
    onSecondaryContainer = LightOnSurface,
    tertiary             = LightAlert,
    onTertiary           = LightPrimaryOn,
    tertiaryContainer    = LightAlertFaint,
    onTertiaryContainer  = LightOnSurface,
    background           = LightBg,
    onBackground         = LightOnSurface,
    surface              = LightSurface,
    onSurface            = LightOnSurface,
    surfaceVariant       = LightSurfaceAlt,
    onSurfaceVariant     = LightOnSurfaceMed,
    outline              = LightBorder,
    outlineVariant       = LightBorder,
)

private val DarkColorScheme = darkColorScheme(
    primary              = DarkPrimary,
    onPrimary            = DarkPrimaryOn,
    primaryContainer     = DarkPrimaryFaint,
    onPrimaryContainer   = DarkPrimaryDeep,
    secondary            = DarkWarn,
    onSecondary          = DarkOnSurface,
    secondaryContainer   = DarkWarnFaint,
    onSecondaryContainer = DarkOnSurface,
    tertiary             = DarkAlert,
    onTertiary           = DarkPrimaryOn,
    tertiaryContainer    = DarkAlertFaint,
    onTertiaryContainer  = DarkOnSurface,
    background           = DarkBg,
    onBackground         = DarkOnSurface,
    surface              = DarkSurface,
    onSurface            = DarkOnSurface,
    surfaceVariant       = DarkSurfaceAlt,
    onSurfaceVariant     = DarkOnSurfaceMed,
    outline              = DarkBorder,
    outlineVariant       = DarkBorder,
)

@Composable
fun IGoMyBabyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors   = if (darkTheme) darkAppColors else lightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content,
        )
    }
}
