package com.sukoon.launcher.ui.theme

import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.sukoon.launcher.R

val IbmArabicFont = FontFamily(
    Font(R.font.ibm_plex_arabic_light, FontWeight.Light),
    Font(R.font.ibm_plex_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_arabic_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_arabic_bold, FontWeight.Bold)
)

val TajawalFont = FontFamily(
    Font(R.font.tajawal_light, FontWeight.Light),
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold)
)

val AlmaraiFont = FontFamily(
    Font(R.font.almarai_light, FontWeight.Light),
    Font(R.font.almarai_regular, FontWeight.Normal),
    Font(R.font.almarai_bold, FontWeight.Medium),
    Font(R.font.almarai_bold, FontWeight.Bold)
)

@OptIn(ExperimentalTextApi::class)
val CairoFont = FontFamily(
    Font(R.font.cairo_var, FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.cairo_var, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.cairo_var, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.cairo_var, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

@OptIn(ExperimentalTextApi::class)
val ElMessiriFont = FontFamily(
    Font(R.font.el_messiri_var, FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.el_messiri_var, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.el_messiri_var, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.el_messiri_var, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

@OptIn(ExperimentalTextApi::class)
val NotoKufiFont = FontFamily(
    Font(R.font.noto_kufi_var, FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.noto_kufi_var, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.noto_kufi_var, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.noto_kufi_var, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
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
    AppFontOption("cairo", "القاهرة", "Cairo", CairoFont),
    AppFontOption("tajawal", "تجوال", "Tajawal", TajawalFont),
    AppFontOption("almarai", "المراعي", "Almarai", AlmaraiFont),
    AppFontOption("messiri", "المسيري", "El Messiri", ElMessiriFont),
    AppFontOption("kufi", "كوفي", "Noto Kufi", NotoKufiFont),
    AppFontOption("amiri", "أميري (نسخ)", "Amiri (Serif)", AmiriFont),
    AppFontOption("system", "خط النظام", "System Sans", FontFamily.SansSerif)
)

fun fontOptionFor(id: String): AppFontOption =
    APP_FONT_OPTIONS.firstOrNull { it.id == id } ?: APP_FONT_OPTIONS.first()

fun fontFamilyFor(id: String): FontFamily = fontOptionFor(id).family

fun fontWeightFor(id: String): FontWeight = when (id) {
    "light" -> FontWeight.Light
    "bold" -> FontWeight.Bold
    else -> FontWeight.Normal
}

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

data class AppThemeOption(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val isDark: Boolean,
    val colors: SukoonColors
) {
    fun localizedName(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
}

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

val EmeraldNightColors = SukoonColors(
    background = Color(0xFF07110E),
    surface = Color(0xFF0E1B16),
    surfaceHover = Color(0xFF152620),
    borderSubtle = Color(0xFF1F332B),
    borderStrong = Color(0xFF2E4A3F),
    textPrimary = Color(0xFFECF4F0),
    textSecondary = Color(0xFF94ADA1),
    textMuted = Color(0xFF5D756A),
    accentWarm = Color(0xFF52B788),
    accentWarmSoft = Color(0x2452B788),
    accentFocus = Color(0xFF74C69D),
    dangerSoft = Color(0xFFE07A73),
    mushafBg = Color(0xFF0A1511),
    mushafText = Color(0xFFE9F2ED),
    mushafBorder = Color(0x3D52B788),
    overlayBg = Color(0xFF07110E)
)

val MidnightSlateColors = SukoonColors(
    background = Color(0xFF0A0F1A),
    surface = Color(0xFF121927),
    surfaceHover = Color(0xFF1A2438),
    borderSubtle = Color(0xFF233047),
    borderStrong = Color(0xFF334463),
    textPrimary = Color(0xFFEFF4FB),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF607087),
    accentWarm = Color(0xFF7DD3FC),
    accentWarmSoft = Color(0x227DD3FC),
    accentFocus = Color(0xFF6EE7B7),
    dangerSoft = Color(0xFFF87171),
    mushafBg = Color(0xFF0D1320),
    mushafText = Color(0xFFEAEFF7),
    mushafBorder = Color(0x387DD3FC),
    overlayBg = Color(0xFF0A0F1A)
)

val WarmEspressoColors = SukoonColors(
    background = Color(0xFF120E0B),
    surface = Color(0xFF1C1612),
    surfaceHover = Color(0xFF261E19),
    borderSubtle = Color(0xFF332922),
    borderStrong = Color(0xFF473930),
    textPrimary = Color(0xFFF6EFE8),
    textSecondary = Color(0xFFB3A294),
    textMuted = Color(0xFF7A6A5E),
    accentWarm = Color(0xFFE09F67),
    accentWarmSoft = Color(0x24E09F67),
    accentFocus = Color(0xFF81B29A),
    dangerSoft = Color(0xFFE07A5F),
    mushafBg = Color(0xFF16110D),
    mushafText = Color(0xFFF3EAE0),
    mushafBorder = Color(0x3CE09F67),
    overlayBg = Color(0xFF120E0B)
)

val NordicFrostColors = SukoonColors(
    background = Color(0xFFF3F6FA),
    surface = Color(0xFFFFFFFF),
    surfaceHover = Color(0xFFE6ECF4),
    borderSubtle = Color(0xFFDCE3EE),
    borderStrong = Color(0xFFC2CDDE),
    textPrimary = Color(0xFF161E2E),
    textSecondary = Color(0xFF5A677D),
    textMuted = Color(0xFF8A97AC),
    accentWarm = Color(0xFF3B5998),
    accentWarmSoft = Color(0x1C3B5998),
    accentFocus = Color(0xFF2D8A68),
    dangerSoft = Color(0xFFC84B4B),
    mushafBg = Color(0xFFF8FAFC),
    mushafText = Color(0xFF18202F),
    mushafBorder = Color(0x403B5998),
    overlayBg = Color(0xFFF3F6FA)
)

val APP_THEME_OPTIONS: List<AppThemeOption> = listOf(
    AppThemeOption("dark_oled", "داكن OLED", "Dark OLED", true, DarkSukoonColors),
    AppThemeOption("light_paper", "فاتح ورقي", "Warm Paper", false, LightSukoonColors),
    AppThemeOption("emerald_night", "زمردي ليلي", "Emerald Night", true, EmeraldNightColors),
    AppThemeOption("midnight_slate", "أزرق ليلي", "Midnight Slate", true, MidnightSlateColors),
    AppThemeOption("warm_espresso", "إسبريسو دافئ", "Warm Espresso", true, WarmEspressoColors),
    AppThemeOption("nordic_frost", "سماوي فاتح", "Nordic Frost", false, NordicFrostColors)
)

fun themeOptionFor(themeId: String, darkFallback: Boolean = true): AppThemeOption {
    APP_THEME_OPTIONS.firstOrNull { it.id == themeId }?.let { return it }
    return if (darkFallback) APP_THEME_OPTIONS[0] else APP_THEME_OPTIONS[1]
}

val LocalSukoonColors = staticCompositionLocalOf { DarkSukoonColors }
val LocalAppFont = staticCompositionLocalOf { IbmArabicFont }
val LocalAppFontWeight = staticCompositionLocalOf { FontWeight.Normal }

@Composable
fun SukoonTheme(
    themeId: String = "dark_oled",
    darkTheme: Boolean = true,
    appFontId: String = "ibm",
    appFontWeightId: String = "normal",
    appLanguage: String = "ar",
    content: @Composable () -> Unit
) {
    val themeOpt = themeOptionFor(themeId, darkTheme)
    val font = fontFamilyFor(appFontId)
    val baseWeight = fontWeightFor(appFontWeightId)
    val strings = stringsFor(appLanguage)
    CompositionLocalProvider(
        LocalSukoonColors provides themeOpt.colors,
        LocalAppFont provides font,
        LocalAppFontWeight provides baseWeight,
        LocalAppStrings provides strings
    ) {
        ProvideTextStyle(value = TextStyle(fontFamily = font, fontWeight = baseWeight)) {
            content()
        }
    }
}
