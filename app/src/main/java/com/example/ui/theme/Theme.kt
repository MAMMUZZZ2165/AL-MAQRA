package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode

@Immutable
data class AlMaqraExtendedColors(
  val isDark: Boolean,
  val headerBackground: Color,
  val headerPattern: Color,
  val headerContent: Color,
  val cardBannerBg: Color,
  val cardBannerText: Color,
  val cardBorder: Color,
  val emptyCardBg: Color,
  val metricCardBg: Color,
  val segmentedBg: Color,
  val segmentedSelectedBg: Color,
  val segmentedSelectedText: Color,
  val liveDot: Color,
  val juzCompletedBg: Color,
  val juzCompletedText: Color,
  val juzIncompleteBg: Color,
  val juzIncompleteText: Color,
  val juzInProgressBg: Color,
  val juzInProgressText: Color,
  val goldAccent: Color,
  val mutedLabel: Color,
)

private val LightExtendedColors = AlMaqraExtendedColors(
  isDark = false,
  headerBackground = ForestGreen,
  headerPattern = Color(0xFF23523B),
  headerContent = ParchmentCream,
  cardBannerBg = ParchmentCream,
  cardBannerText = ForestGreen,
  cardBorder = ParchmentBorder,
  emptyCardBg = ParchmentCardCream,
  metricCardBg = ParchmentSurface,
  segmentedBg = ParchmentCardCream,
  segmentedSelectedBg = ForestGreen,
  segmentedSelectedText = ParchmentCream,
  liveDot = LiveCrimson,
  juzCompletedBg = ForestGreen,
  juzCompletedText = ParchmentCream,
  juzIncompleteBg = JuzIncompleteBlush,
  juzIncompleteText = ForestGreen,
  juzInProgressBg = JuzInProgressAmber,
  juzInProgressText = ForestGreen,
  goldAccent = GoldHighlight,
  mutedLabel = ForestGreenMuted,
)

private val DarkExtendedColors = AlMaqraExtendedColors(
  isDark = true,
  headerBackground = ForestGreenHeader,
  headerPattern = ForestGreenHeaderPattern,
  headerContent = ParchmentCream,
  cardBannerBg = ParchmentCream,
  cardBannerText = ForestGreen,
  cardBorder = DarkBorder,
  emptyCardBg = Color(0xFF33423E),
  metricCardBg = DarkSurfaceCard,
  segmentedBg = DarkSurfaceCard,
  segmentedSelectedBg = DarkSurfaceElevated,
  segmentedSelectedText = Color(0xFF55A07D),
  liveDot = LiveCrimson,
  juzCompletedBg = JuzCompletedSage,
  juzCompletedText = ForestGreen,
  juzIncompleteBg = Color(0xFFFDF0F0),
  juzIncompleteText = Color(0xFF23262B),
  juzInProgressBg = JuzInProgressAmber,
  juzInProgressText = Color(0xFF23262B),
  goldAccent = GoldHighlight,
  mutedLabel = DarkTextSecondary,
)

val LocalAlMaqraColors = staticCompositionLocalOf { LightExtendedColors }

private val AlMaqraLightColorScheme = lightColorScheme(
  primary = ForestGreen,
  onPrimary = ParchmentCream,
  primaryContainer = ForestGreenLight,
  onPrimaryContainer = ParchmentCream,
  secondary = ForestGreenMuted,
  onSecondary = ParchmentSurface,
  secondaryContainer = JuzCompletedSage,
  onSecondaryContainer = ForestGreen,
  background = ParchmentCream,
  onBackground = ForestGreen,
  surface = ParchmentSurface,
  onSurface = ForestGreen,
  surfaceVariant = ParchmentCardCream,
  onSurfaceVariant = ForestGreenMuted,
  outline = ForestGreenMuted,
  outlineVariant = ParchmentBorder,
  error = LiveCrimson,
  onError = Color.White,
)

private val AlMaqraDarkColorScheme = darkColorScheme(
  primary = ForestGreenHeader,
  onPrimary = ParchmentCream,
  primaryContainer = ForestGreen,
  onPrimaryContainer = ParchmentCream,
  secondary = Color(0xFF55A07D),
  onSecondary = DarkCharcoalBg,
  secondaryContainer = DarkCardPine,
  onSecondaryContainer = ParchmentCream,
  background = DarkCharcoalBg,
  onBackground = DarkTextPrimary,
  surface = DarkSurfaceCard,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkTextSecondary,
  outlineVariant = DarkBorder,
  error = LiveCrimson,
  onError = Color.White,
)

val AlMaqraShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
)

@Composable
fun AlMaqraTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val useDark = when (themeMode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }

  val colorScheme = if (useDark) AlMaqraDarkColorScheme else AlMaqraLightColorScheme
  val extendedColors = if (useDark) DarkExtendedColors else LightExtendedColors

  CompositionLocalProvider(LocalAlMaqraColors provides extendedColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AlMaqraTypography,
      shapes = AlMaqraShapes,
      content = content,
    )
  }
}
