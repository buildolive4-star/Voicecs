package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SocialVerseColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = Color.White,
    primaryContainer = VioletDeep,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = NeonCyan,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = CyanDeep,
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = ImperialGold,
    onTertiary = Color(0xFF271800),
    tertiaryContainer = GoldDeep,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = Color(0x1FFFFFFF),
    error = RoseMagenta,
    onError = Color.White,
)

private val SocialVerseShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SocialVerseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SocialVerseColorScheme,
        typography = SocialVerseTypography,
        shapes = SocialVerseShapes,
        content = content
    )
}
