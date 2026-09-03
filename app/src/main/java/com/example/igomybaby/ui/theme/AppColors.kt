package com.example.igomybaby.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val bg: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceSunken: Color,
    val border: Color,
    val onSurface: Color,
    val onSurfaceMed: Color,
    val onSurfaceDim: Color,
    val primary: Color,
    val primaryDeep: Color,
    val primaryFaint: Color,
    val primaryOn: Color,
    val alert: Color,
    val alertFaint: Color,
    val warn: Color,
    val warnFaint: Color,
)

val lightAppColors = AppColors(
    bg            = LightBg,
    surface       = LightSurface,
    surfaceAlt    = LightSurfaceAlt,
    surfaceSunken = LightSurfaceSunken,
    border        = LightBorder,
    onSurface     = LightOnSurface,
    onSurfaceMed  = LightOnSurfaceMed,
    onSurfaceDim  = LightOnSurfaceDim,
    primary       = LightPrimary,
    primaryDeep   = LightPrimaryDeep,
    primaryFaint  = LightPrimaryFaint,
    primaryOn     = LightPrimaryOn,
    alert         = LightAlert,
    alertFaint    = LightAlertFaint,
    warn          = LightWarn,
    warnFaint     = LightWarnFaint,
)

val darkAppColors = AppColors(
    bg            = DarkBg,
    surface       = DarkSurface,
    surfaceAlt    = DarkSurfaceAlt,
    surfaceSunken = DarkSurfaceSunken,
    border        = DarkBorder,
    onSurface     = DarkOnSurface,
    onSurfaceMed  = DarkOnSurfaceMed,
    onSurfaceDim  = DarkOnSurfaceDim,
    primary       = DarkPrimary,
    primaryDeep   = DarkPrimaryDeep,
    primaryFaint  = DarkPrimaryFaint,
    primaryOn     = DarkPrimaryOn,
    alert         = DarkAlert,
    alertFaint    = DarkAlertFaint,
    warn          = DarkWarn,
    warnFaint     = DarkWarnFaint,
)

val LocalAppColors = staticCompositionLocalOf { lightAppColors }

val appColors: AppColors
    @Composable get() = LocalAppColors.current
