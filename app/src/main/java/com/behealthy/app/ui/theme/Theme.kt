package com.behealthy.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF191C1C),
    onSurface = Color(0xFF191C1C),
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8CD6B5),
    secondary = Color(0xFFB3CCBE),
    tertiary = Color(0xFFA6CCE0),
    background = Color(0xFF191C1C),
    surface = Color(0xFF191C1C),
    onPrimary = Color(0xFF003826),
    onSecondary = Color(0xFF1E352A),
    onTertiary = Color(0xFF083543),
    onBackground = Color(0xFFE1E3DF),
    onSurface = Color(0xFFE1E3DF),
)

val TechColorScheme = lightColorScheme(
    primary = TechPrimary,
    secondary = TechSecondary,
    tertiary = TechTertiary,
    background = TechBackground,
    surface = TechSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0B132B),
    onSurface = Color(0xFF0B132B)
)

val SportsColorScheme = lightColorScheme(
    primary = SportsPrimary,
    secondary = SportsSecondary,
    tertiary = SportsTertiary,
    background = SportsBackground,
    surface = SportsSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val CuteColorScheme = lightColorScheme(
    primary = CutePrimary,
    secondary = CuteSecondary,
    tertiary = CuteTertiary,
    background = CuteBackground,
    surface = CuteSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF4A4A4A),
    onSurface = Color(0xFF4A4A4A)
)

val DoraemonColorScheme = lightColorScheme(
    primary = DoraemonPrimary,
    secondary = DoraemonSecondary,
    tertiary = DoraemonTertiary,
    background = DoraemonBackground,
    surface = DoraemonSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A)
)

val MinionColorScheme = lightColorScheme(
    primary = Color(0xFFF5E050),
    secondary = MinionSecondary,
    tertiary = MinionTertiary,
    background = Color(0xFFFFF9C4),
    surface = MinionSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A)
)

val WallEColorScheme = lightColorScheme(
    primary = Color(0xFFE67E22),
    secondary = Color(0xFF34495E),
    tertiary = Color(0xFF27AE60),
    background = WallEBackground,
    surface = WallESurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF2C3E50),
    onSurface = Color(0xFF2C3E50)
)

val NewYearColorScheme = lightColorScheme(
    primary = Color(0xFFD32F2F),
    secondary = Color(0xFFFFB300),
    tertiary = Color(0xFF8B0000),
    background = Color(0xFFFFFAFA),
    surface = NewYearSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF333333),
    onSurface = Color(0xFF333333)
)

val NBAColorScheme = lightColorScheme(
    primary = NBAPrimary,
    secondary = NBASecondary,
    tertiary = NBATertiary,
    background = NBABackground,
    surface = NBASurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val BadmintonColorScheme = lightColorScheme(
    primary = BadmintonPrimary,
    secondary = BadmintonSecondary,
    tertiary = BadmintonTertiary,
    background = BadmintonBackground,
    surface = BadmintonSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val WorldCupColorScheme = lightColorScheme(
    primary = WorldCupPrimary,
    secondary = WorldCupSecondary,
    tertiary = WorldCupTertiary,
    background = WorldCupBackground,
    surface = WorldCupSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = WorldCupTextPrimary,
    onSurface = WorldCupTextPrimary
)

// Zen Style
val ZenPrimary = Color(0xFF4A5D23) // Moss Green
val ZenSecondary = Color(0xFF8D6E63) // Wood Brown
val ZenTertiary = Color(0xFFA1887F)
val ZenBackground = Color(0xFFF1F8E9) // Light Greenish White
val ZenSurface = Color(0xFFFFFFFF)

val ZenColorScheme = lightColorScheme(
    primary = ZenPrimary,
    secondary = ZenSecondary,
    tertiary = ZenTertiary,
    background = ZenBackground,
    surface = ZenSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF333333),
    onSurface = Color(0xFF333333)
)

// Dao Style
val DaoPrimary = Color(0xFF212121) // Black
val DaoSecondary = Color(0xFFBCAAA4) // Muted Brown
val DaoTertiary = Color(0xFF607D8B)
val DaoBackground = Color(0xFFFAFAFA)
val DaoSurface = Color(0xFFFFFFFF)

val DaoColorScheme = lightColorScheme(
    primary = DaoPrimary,
    secondary = DaoSecondary,
    tertiary = DaoTertiary,
    background = DaoBackground,
    surface = DaoSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

fun getThemeColorScheme(themeStyle: ThemeStyle): androidx.compose.material3.ColorScheme {
    return when (themeStyle) {
        ThemeStyle.Tech -> TechColorScheme
        ThemeStyle.Sports -> SportsColorScheme
        ThemeStyle.Cute -> CuteColorScheme
        ThemeStyle.Doraemon -> DoraemonColorScheme
        ThemeStyle.Minions -> MinionColorScheme
        ThemeStyle.WallE -> WallEColorScheme
        ThemeStyle.NewYear -> NewYearColorScheme
        ThemeStyle.NBA -> NBAColorScheme
        ThemeStyle.Badminton -> BadmintonColorScheme
        ThemeStyle.FootballWorldCup -> WorldCupColorScheme
        ThemeStyle.Zen -> ZenColorScheme
        ThemeStyle.Dao -> DaoColorScheme
        ThemeStyle.Default -> LightColorScheme // Default to Light for preview
    }
}

@Composable
fun BeHealthyTheme(
    themeStyle: ThemeStyle = ThemeStyle.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeStyle) {
        ThemeStyle.Tech -> TechColorScheme
        ThemeStyle.Sports -> SportsColorScheme
        ThemeStyle.Cute -> CuteColorScheme
        ThemeStyle.Doraemon -> DoraemonColorScheme
        ThemeStyle.Minions -> MinionColorScheme
        ThemeStyle.WallE -> WallEColorScheme
        ThemeStyle.NewYear -> NewYearColorScheme
        ThemeStyle.NBA -> NBAColorScheme
        ThemeStyle.Badminton -> BadmintonColorScheme
        ThemeStyle.FootballWorldCup -> WorldCupColorScheme
        ThemeStyle.Zen -> ZenColorScheme
        ThemeStyle.Dao -> DaoColorScheme
        ThemeStyle.Default -> {
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // Determine if we should use light status bar icons (dark text)
            // If background is light, we want dark text (isAppearanceLightStatusBars = true)
            // If background is dark, we want light text (isAppearanceLightStatusBars = false)
            // Simple heuristic: check luminance or hardcode based on theme
            val isLightBackground = when (themeStyle) {
                ThemeStyle.Tech -> false // Dark background -> light text
                ThemeStyle.Default -> !darkTheme
                else -> true // Sports, Cute, Doraemon, Minions are mostly light
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightBackground
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
