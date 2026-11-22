package com.sukoon.launcher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.sukoon.launcher.R

val IbmArabicFont = FontFamily(
    Font(R.font.ibm_plex_arabic_light, FontWeight.Light),
    Font(R.font.ibm_plex_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_arabic_medium, FontWeight.Medium)
)

val TajawalFont = FontFamily(
    Font(R.font.tajawal_light, FontWeight.Light),
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium)
)

val AlmaraiFont = FontFamily(
    Font(R.font.almarai_light, FontWeight.Light),
    Font(R.font.almarai_regular, FontWeight.Normal),
    Font(R.font.almarai_bold, FontWeight.Medium),
    Font(R.font.almarai_bold, FontWeight.Bold)
)

val AmiriFont = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Light),
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Medium),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

data class AppFontOption(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val family: FontFamily
) {
    fun localizedName(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
}

val APP_FONT_OPTIONS: List<AppFontOption> = listOf(
    AppFontOption("ibm", "IBM Plex", "IBM Plex", IbmArabicFont),
    AppFontOption("tajawal", "تجوال", "Tajawal", TajawalFont),
    AppFontOption("almarai", "المراعي", "Almarai", AlmaraiFont),
    AppFontOption("amiri", "أميري (نسخ)", "Amiri (Serif)", AmiriFont),
    AppFontOption("system", "خط النظام", "System Sans", FontFamily.SansSerif)
)

fun fontOptionFor(id: String): AppFontOption =
    APP_FONT_OPTIONS.firstOrNull { it.id == id } ?: APP_FONT_OPTIONS.first()

fun fontFamilyFor(id: String): FontFamily = fontOptionFor(id).family

data class SukoonColors(
    val background: Color,
    val surface: Color,
    val surfaceHover: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentWarm: Color,
    val accentWarmSoft: Color,
    val accentFocus: Color,
    val dangerSoft: Color,
    val mushafBg: Color,
    val mushafText: Color,
    val mushafBorder: Color,
    val overlayBg: Color
)

val DarkSukoonColors = SukoonColors(
    background = Color(0xFF09090B),
    surface = Color(0x0AFFFFFF),
    surfaceHover = Color(0x14FFFFFF),
    borderSubtle = Color(0x15FFFFFF),
    borderStrong = Color(0x2BFFFFFF),
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFF9898A0),
    textMuted = Color(0xFF5E5E66),
    accentWarm = Color(0xFFD9B88C),
    accentWarmSoft = Color(0x24D9B88C),
    accentFocus = Color(0xFF86EFAC),
    dangerSoft = Color(0xFFF87171),
    mushafBg = Color(0xFF0F0E0C),
    mushafText = Color(0xFFEEE7D8),
    mushafBorder = Color(0x38D9B88C),
    overlayBg = Color(0xF509090B)
)

val LightSukoonColors = SukoonColors(
    background = Color(0xFFF7F5F0),
    surface = Color(0x0B18181B),
    surfaceHover = Color(0x1518181B),
    borderSubtle = Color(0x1718181B),
    borderStrong = Color(0x3318181B),
    textPrimary = Color(0xFF141416),
    textSecondary = Color(0xFF5C5C63),
    textMuted = Color(0xFF8B8B93),
    accentWarm = Color(0xFF9A6B36),
    accentWarmSoft = Color(0x1F9A6B36),
    accentFocus = Color(0xFF15803D),
    dangerSoft = Color(0xFFDC2626),
    mushafBg = Color(0xFFFBF8F1),
    mushafText = Color(0xFF1C1915),
    mushafBorder = Color(0x479A6B36),
    overlayBg = Color(0xF7F7F5F0)
)

val LocalSukoonColors = staticCompositionLocalOf { DarkSukoonColors }
val LocalAppFont = staticCompositionLocalOf { IbmArabicFont }

@Composable
fun SukoonTheme(
    darkTheme: Boolean = true,
    appFontId: String = "ibm",
    appLanguage: String = "ar",
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkSukoonColors else LightSukoonColors
    val font = fontFamilyFor(appFontId)
    val strings = stringsFor(appLanguage)
    CompositionLocalProvider(
        LocalSukoonColors provides colors,
        LocalAppFont provides font,
        LocalAppStrings provides strings
    ) {
        content()
    }
}
