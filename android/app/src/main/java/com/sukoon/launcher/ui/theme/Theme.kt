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
    surface = Color(0xFF121214),
    surfaceHover = Color(0xFF1A1A1D),
    borderSubtle = Color(0xFF29292C),
    borderStrong = Color(0xFF3A3A40),
    textPrimary = Color(0xFFF4F0E8),
    textSecondary = Color(0xFFA19D95),
    textMuted = Color(0xFF6C6862),
    accentWarm = Color(0xFFD9B88C),
    accentWarmSoft = Color(0x21D9B88C),
    accentFocus = Color(0xFF78947C),
    dangerSoft = Color(0xFFCF7770),
    mushafBg = Color(0xFF0E0D0B),
    mushafText = Color(0xFFEEE7D8),
    mushafBorder = Color(0x38D9B88C),
    overlayBg = Color(0xFF09090B)
)

val LightSukoonColors = SukoonColors(
    background = Color(0xFFF7F5F0),
    surface = Color(0xFFFFFFFF),
    surfaceHover = Color(0xFFEEEAE2),
    borderSubtle = Color(0xFFE4DFD6),
    borderStrong = Color(0xFFCFC7BA),
    textPrimary = Color(0xFF242321),
    textSecondary = Color(0xFF77736D),
    textMuted = Color(0xFF9B968E),
    accentWarm = Color(0xFF9A6B36),
    accentWarmSoft = Color(0x1F9A6B36),
    accentFocus = Color(0xFF547A5A),
    dangerSoft = Color(0xFFAD514B),
    mushafBg = Color(0xFFFBF8F1),
    mushafText = Color(0xFF1C1915),
    mushafBorder = Color(0x479A6B36),
    overlayBg = Color(0xFFF7F5F0)
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
