package com.sukoon.launcher.ui.screens

import android.media.AudioManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.AppInfo
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.PAGE_META_MAP
import com.sukoon.launcher.data.PRAYER_CITIES
import com.sukoon.launcher.data.PrayerTimesCalculator
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.service.MUEZZIN_OPTIONS
import com.sukoon.launcher.service.muezzinOptionFor
import com.sukoon.launcher.ui.VolumeLevels
import com.sukoon.launcher.ui.theme.APP_FONT_OPTIONS
import com.sukoon.launcher.ui.theme.APP_THEME_OPTIONS
import com.sukoon.launcher.ui.theme.LocalAnimationsEnabled
import com.sukoon.launcher.ui.theme.themeOptionFor
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import com.sukoon.launcher.ui.theme.fontOptionFor
import com.sukoon.launcher.ui.theme.themeOptionFor
import kotlin.math.abs

// =========================================
// 1. App Drawer Overlay (Text-Only, NO Icons)
// =========================================
@Composable
fun AppDrawerOverlay(
    state: LauncherState,
    installedApps: List<AppInfo>,
    onLoadAppIcon: suspend (String) -> ImageBitmap?,
    onAppClick: (AppInfo) -> Unit,
    onToggleFavorite: (AppInfo) -> Unit,
    onToggleDistracting: (AppInfo) -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }

    val filtered = remember(installedApps, query) {
        if (query.isBlank()) installedApps
        else installedApps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlayBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                textStyle = TextStyle(fontFamily = appFont, fontSize = 16.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accentWarm),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(modifier = Modifier.padding(vertical = 8.dp)) {
                        if (query.isEmpty()) {
                            Text(strings.searchAppsHint, fontFamily = appFont, fontSize = 16.sp, color = colors.textMuted)
                        }
                        inner()
                    }
                }
            )
            SmallChip(strings.close) { onClose() }
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

        Text(
            text = strings.drawerLegend,
            fontFamily = appFont,
            fontSize = 11.sp,
            color = colors.textMuted
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                val isFav = app.packageName in state.favoritePackages
                val isDist = app.packageName in state.distractingPackages

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClose()
                            onAppClick(app)
                        }
                        .padding(vertical = 11.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        AppIconView(
                            packageName = app.packageName,
                            displayStyle = state.appDisplayStyle,
                            size = 32.dp,
                            loadIcon = onLoadAppIcon
                        )
                        Text(
                            text = app.label,
                            fontFamily = appFont,
                            fontSize = if (state.appDisplayStyle == "text") 19.sp else 17.sp,
                            fontWeight = FontWeight.Light,
                            color = colors.textPrimary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isDist) "${strings.mindfulTag} ✓" else strings.mindfulTag,
                            fontFamily = appFont,
                            fontSize = 11.sp,
                            color = if (isDist) colors.accentWarm else colors.textMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .border(
                                    1.dp,
                                    if (isDist) colors.accentWarm.copy(alpha = 0.5f) else colors.borderSubtle,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onToggleDistracting(app) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                        Text(
                            text = if (isFav) "★" else "☆",
                            fontSize = 20.sp,
                            color = if (isFav) colors.accentWarm else colors.textMuted,
                            modifier = Modifier
                                .clickable { onToggleFavorite(app) }
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================
// 2. Universal Search + Quick Volume Control
// =========================================
@Composable
fun UniversalSearchOverlay(
    state: LauncherState,
    installedApps: List<AppInfo>,
    volumes: VolumeLevels,
    onRefreshVolumes: () -> Unit,
    onSetVolume: (Int, Int) -> Unit,
    onToggleMute: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onNavigateToPage: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { onRefreshVolumes() }

    val calcResult = remember(query) { evaluateSimpleMath(query) }
    val matchedApps = remember(installedApps, query) {
        if (query.isBlank()) emptyList()
        else installedApps.filter { it.label.contains(query.trim(), ignoreCase = true) }.take(8)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlayBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                textStyle = TextStyle(fontFamily = appFont, fontSize = 16.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accentWarm),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(modifier = Modifier.padding(vertical = 8.dp)) {
                        if (query.isEmpty()) {
                            Text(strings.searchOrCalcHint, fontFamily = appFont, fontSize = 15.sp, color = colors.textMuted)
                        }
                        inner()
                    }
                }
            )
            SmallChip(strings.close) { onClose() }
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Volume Control Panel
            VolumeControlCard(
                volumes = volumes,
                onSetVolume = onSetVolume,
                onToggleMute = onToggleMute
            )

            // Live Calculator Result
            if (calcResult != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accentWarmSoft)
                        .border(1.dp, colors.accentWarm, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$query =", fontFamily = appFont, fontSize = 14.sp, color = colors.textSecondary)
                    Text(calcResult, fontFamily = appFont, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = colors.accentWarm)
                }
            }

            // Quick Shortcuts when search is empty
            if (query.isBlank()) {
                Text(strings.quickJump, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                listOf(
                    Triple(
                        PAGE_META_MAP["quran"]?.localizedLabel(strings.isArabic) ?: strings.holyQuran,
                        strings.quickJumpQuranSubFmt(state.quranPage.formatDigits(strings.isArabic)),
                        "quran"
                    ),
                    Triple(strings.adhkarAndTasbih, strings.quickJumpDhikrSub, "dhikr"),
                    Triple(strings.dailyHabits, strings.quickJumpHabitsSub, "habits"),
                    Triple(strings.quickJumpSettingsTitle, strings.quickJumpSettingsSub, "settings")
                ).forEach { (title, sub, target) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onClose()
                                if (target == "settings") onOpenSettings() else onNavigateToPage(target)
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(title, fontFamily = appFont, fontSize = 16.sp, color = colors.textPrimary)
                        Text(sub, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                    }
                }
            } else if (matchedApps.isNotEmpty()) {
                Text(strings.appsLabel, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                matchedApps.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onClose()
                                onAppClick(app)
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(app.label, fontFamily = appFont, fontSize = 17.sp, color = colors.textPrimary)
                        Text(strings.open, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun VolumeControlCard(
    volumes: VolumeLevels,
    onSetVolume: (Int, Int) -> Unit,
    onToggleMute: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.quickVolumeControl,
                fontFamily = appFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.accentWarm
            )
            SmallChip(if (volumes.media == 0 && volumes.ring == 0) strings.unmute else strings.quickMute) {
                onToggleMute()
            }
        }

        VolumeSliderRow(strings.mediaVol, volumes.media, volumes.mediaMax) {
            onSetVolume(AudioManager.STREAM_MUSIC, it)
        }
        VolumeSliderRow(strings.ringVol, volumes.ring, volumes.ringMax) {
            onSetVolume(AudioManager.STREAM_RING, it)
        }
        VolumeSliderRow(strings.alarmVol, volumes.alarm, volumes.alarmMax) {
            onSetVolume(AudioManager.STREAM_ALARM, it)
        }
    }
}

@Composable
private fun VolumeSliderRow(
    label: String,
    value: Int,
    max: Int,
    onChange: (Int) -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val pct = if (max > 0) (value * 100) / max else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, fontFamily = appFont, fontSize = 12.sp, color = colors.textSecondary, modifier = Modifier.width(50.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..max.toFloat(),
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = colors.accentWarm,
                activeTrackColor = colors.accentWarm,
                inactiveTrackColor = colors.borderSubtle
            )
        )
        Text(
            text = "${pct.formatDigits(strings.isArabic)}${strings.percentSymbol}",
            fontFamily = appFont,
            fontSize = 11.sp,
            color = colors.textMuted,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End
        )
    }
}

// =========================================
// 3. Settings & Customization Overlay (v2 Organized Architecture)
// =========================================
@Composable
fun SettingsOverlay(
    state: LauncherState,
    volumes: VolumeLevels,
    isAccessibilityConnected: Boolean,
    isNotificationAccessGranted: Boolean,
    isLocating: Boolean,
    previewingMuezzinId: String?,
    onSetLanguage: (String) -> Unit,
    onSetAppFont: (String) -> Unit,
    onSetAppFontWeight: (String) -> Unit,
    onSetAppDisplayStyle: (String) -> Unit,
    onSetAppTheme: (String) -> Unit,
    onSetPrayerCity: (String) -> Unit,
    onRequestLocation: () -> Unit,
    onTogglePrayerReminder: (Boolean) -> Unit,
    onSetPrayerReminderMinutes: (Int) -> Unit,
    onToggleAdhan: (Boolean) -> Unit,
    onSetAdhanMuezzin: (String) -> Unit,
    onToggleMuezzinPreview: (String) -> Unit,
    onTogglePrayerAutoFocus: (Boolean) -> Unit,
    onSetPrayerAutoFocusMinutes: (Int) -> Unit,
    onTogglePrayerTracker: (Boolean) -> Unit,
    onOpenQibla: () -> Unit,
    onSetQuranLineHeight: (Float) -> Unit,
    onMovePage: (Int, Int) -> Unit,
    onTogglePageEnabled: (String) -> Unit,
    onSetDarkTheme: (Boolean) -> Unit,
    onToggleAnimations: (Boolean) -> Unit,
    onToggleFocus: (Boolean) -> Unit,
    onSetMindfulDuration: (Int) -> Unit,
    onToggleMindfulTimeBox: (Boolean) -> Unit,
    onSetHaptics: (Boolean) -> Unit,
    onRefreshVolumes: () -> Unit,
    onSetVolume: (Int, Int) -> Unit,
    onToggleMute: () -> Unit,
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenDefaultLauncherSettings: () -> Unit,
    onRestartSetup: () -> Unit,
    onResetApp: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val animOn = LocalAnimationsEnabled.current
    LaunchedEffect(Unit) { onRefreshVolumes() }

    // null = 2x2 Category Hub; "display" | "spiritual" | "focus" | "system" = dedicated sub-page
    var activeCategory by remember { mutableStateOf<String?>(null) }
    var openPanel by remember { mutableStateOf<String?>(null) } // fonts | city | tabs | volume
    var cityQuery by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = activeCategory != null) {
        activeCategory = null
        openPanel = null
    }

    val selectedCity = remember(state.prayerCityId, state.useGpsLocation, state.customLat, state.customLng, state.customLocationLabel) {
        PrayerTimesCalculator.resolveCityForState(state)
    }
    val selectedFont = remember(state.appFontId) { fontOptionFor(state.appFontId) }
    val selectedTheme = remember(state.appThemeId, state.darkTheme) { themeOptionFor(state.appThemeId, state.darkTheme) }
    val filteredCities = remember(cityQuery) {
        val q = cityQuery.trim()
        if (q.isEmpty()) PRAYER_CITIES
        else PRAYER_CITIES.filter {
            it.nameAr.contains(q, ignoreCase = true) ||
                it.nameEn.contains(q, ignoreCase = true) ||
                it.countryAr.contains(q, ignoreCase = true) ||
                it.countryEn.contains(q, ignoreCase = true)
        }
    }

    val enabledIds = remember(state.pageConfig) {
        state.pageConfig.filter { it.enabled || it.id == "home" }.map { it.id }
    }
    val homeEnabledIdx = enabledIds.indexOf("home")

    fun categoryTitle(cat: String?): String = when (cat) {
        "display" -> strings.catDisplay
        "spiritual" -> strings.catSpiritual
        "focus" -> strings.catFocus
        "system" -> strings.catSystem
        else -> strings.settingsTitle
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlayBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header (adapts cleanly between Category Hub & Category Sub-Page)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeCategory == null) {
                Column {
                    Text(strings.customizeSukoon, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                    Text(strings.settingsTitle, fontFamily = appFont, fontSize = 22.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SmallChip(strings.backArrow) {
                        activeCategory = null
                        openPanel = null
                    }
                    Text(
                        text = categoryTitle(activeCategory),
                        fontFamily = appFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }
            SmallChip(strings.done, active = true) { onClose() }
        }

        // Quick horizontal category switcher when inside a sub-page
        if (activeCategory != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "display" to strings.catDisplay,
                    "spiritual" to strings.catSpiritual,
                    "focus" to strings.catFocus,
                    "system" to strings.catSystem
                ).forEach { (id, label) ->
                    SmallChip(label = label, active = activeCategory == id) {
                        activeCategory = id
                        openPanel = null
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

        AnimatedContent(
            targetState = activeCategory,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                if (animOn) {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "settingsCategoryNav"
        ) { currentCat ->
            if (currentCat == null) {
                // ===== CATEGORY HUB: 2x2 ORGANIZED CARDS SIDE-BY-SIDE =====
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsCategoryTile(
                            emoji = "🎨",
                            title = strings.catDisplay,
                            subtitle = strings.catDisplaySub,
                            badge = selectedTheme.localizedName(strings.isArabic),
                            modifier = Modifier.weight(1f)
                        ) { activeCategory = "display" }

                        SettingsCategoryTile(
                            emoji = "🕌",
                            title = strings.catSpiritual,
                            subtitle = strings.catSpiritualSub,
                            badge = selectedCity.localizedName(strings.isArabic),
                            modifier = Modifier.weight(1f)
                        ) { activeCategory = "spiritual" }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsCategoryTile(
                            emoji = "🎯",
                            title = strings.catFocus,
                            subtitle = strings.catFocusSub,
                            badge = if (state.focusMode) strings.focusOn else strings.normalMode,
                            modifier = Modifier.weight(1f)
                        ) { activeCategory = "focus" }

                        SettingsCategoryTile(
                            emoji = "⚙️",
                            title = strings.catSystem,
                            subtitle = strings.catSystemSub,
                            badge = if (isNotificationAccessGranted) "✓" else strings.open,
                            modifier = Modifier.weight(1f)
                        ) { activeCategory = "system" }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Calm At-a-Glance Status Summary Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.fontSection, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                            Text(selectedFont.localizedName(strings.isArabic), fontFamily = appFont, fontSize = 12.sp, color = colors.textPrimary)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.muezzinVoiceLabel, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                            Text(
                                text = if (state.adhanEnabled) muezzinOptionFor(state.adhanMuezzinId).localizedName(strings.isArabic) else strings.hiddenLabel,
                                fontFamily = appFont,
                                fontSize = 12.sp,
                                color = if (state.adhanEnabled) colors.accentWarm else colors.textSecondary
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.tabsOrderSection.substringBefore(" ("), fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                            Text(
                                "${enabledIds.size.formatDigits(strings.isArabic)} / ${state.pageConfig.size.formatDigits(strings.isArabic)}",
                                fontFamily = appFont,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            } else {
                // ===== DEDICATED CATEGORY SUB-PAGE =====
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ===== GROUP 1: DISPLAY & APPEARANCE =====
                    if (currentCat == "display") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.catDisplay, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)

                    // 3-Card App Display Style Selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(strings.appDisplaySection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                            Text(strings.appDisplaySubtitle, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("text", strings.styleTextOnly, strings.styleTextOnlySub),
                                Triple("monochrome", strings.styleMonochrome, strings.styleMonochromeSub),
                                Triple("original", strings.styleOriginal, strings.styleOriginalSub)
                            ).forEach { (id, title, sub) ->
                                val active = state.appDisplayStyle == id
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) colors.accentWarmSoft else colors.background)
                                        .border(1.dp, if (active) colors.accentWarm else colors.borderSubtle, RoundedCornerShape(10.dp))
                                        .clickable { onSetAppDisplayStyle(id) }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Mini visual preview
                                    Row(
                                        modifier = Modifier.height(26.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        when (id) {
                                            "text" -> Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(Modifier.width(34.dp).height(4.dp).clip(RoundedCornerShape(50)).background(colors.textPrimary.copy(alpha = 0.8f)))
                                                Box(Modifier.width(22.dp).height(3.dp).clip(RoundedCornerShape(50)).background(colors.textMuted))
                                            }
                                            "monochrome" -> {
                                                Box(Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(colors.surfaceHover).border(1.dp, colors.borderStrong, RoundedCornerShape(6.dp)))
                                                Box(Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(colors.surfaceHover).border(1.dp, colors.borderStrong, RoundedCornerShape(6.dp)))
                                            }
                                            else -> {
                                                Box(Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF25D366)))
                                                Box(Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFEF4444)))
                                            }
                                        }
                                    }
                                    Text(
                                        text = title,
                                        fontFamily = appFont,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (active) colors.accentWarm else colors.textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = sub,
                                        fontFamily = appFont,
                                        fontSize = 9.sp,
                                        color = colors.textMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Grouped Card: Theme + Language + Collapsible Fonts
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        // Color Themes Row (6 Professional Themes with swatches)
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(strings.themesSection, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(
                                    text = themeOptionFor(state.appThemeId, state.darkTheme).localizedName(strings.isArabic),
                                    fontFamily = appFont,
                                    fontSize = 12.sp,
                                    color = colors.accentWarm
                                )
                            }
                            APP_THEME_OPTIONS.chunked(2).forEach { rowThemes ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowThemes.forEach { tOpt ->
                                        val active = state.appThemeId == tOpt.id
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (active) colors.accentWarmSoft else colors.background)
                                                .border(1.dp, if (active) colors.accentWarm else colors.borderSubtle, RoundedCornerShape(10.dp))
                                                .clickable { onSetAppTheme(tOpt.id) }
                                                .padding(horizontal = 10.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(tOpt.colors.background)
                                                    .border(2.dp, tOpt.colors.accentWarm, CircleShape)
                                            )
                                            Text(
                                                text = tOpt.localizedName(strings.isArabic),
                                                fontFamily = appFont,
                                                fontSize = 12.sp,
                                                color = if (active) colors.accentWarm else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Font Weight Row (Light / Regular / Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.fontWeightSection, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SmallChip(strings.weightLight, active = state.appFontWeight == "light") { onSetAppFontWeight("light") }
                                SmallChip(strings.weightRegular, active = state.appFontWeight == "normal") { onSetAppFontWeight("normal") }
                                SmallChip(strings.weightBold, active = state.appFontWeight == "bold") { onSetAppFontWeight("bold") }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Language Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.languageSection, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SmallChip("العربية", active = state.appLanguage == "ar") { onSetLanguage("ar") }
                                SmallChip("English", active = state.appLanguage == "en") { onSetLanguage("en") }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Collapsible Fonts Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openPanel = if (openPanel == "fonts") null else "fonts" }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.fontSection, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Text(
                                text = "${selectedFont.localizedName(strings.isArabic)}  ${if (openPanel == "fonts") "▴" else "▾"}",
                                fontFamily = appFont,
                                fontSize = 12.sp,
                                color = colors.accentWarm
                            )
                        }
                        if (openPanel == "fonts") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                APP_FONT_OPTIONS.chunked(2).forEach { rowOptions ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        rowOptions.forEach { opt ->
                                            SmallChip(
                                                label = opt.localizedName(strings.isArabic),
                                                active = state.appFontId == opt.id,
                                                fontOverride = opt.family,
                                                modifier = Modifier.weight(1f)
                                            ) { onSetAppFont(opt.id) }
                                        }
                                        if (rowOptions.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Smooth Animations & Motion Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.animationsTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(strings.animationsSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Switch(
                                checked = state.animationsEnabled,
                                onCheckedChange = onToggleAnimations,
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                            )
                        }
                    }
                }
            }

            // ===== GROUP 2: SPIRITUAL =====
            if (currentCat == "spiritual") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.catSpiritual, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        // Prayer City Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openPanel = if (openPanel == "city") null else "city" }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.prayerCitySection, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(
                                    text = if (state.useGpsLocation) "${selectedCity.localizedName(strings.isArabic)} • ${strings.gpsActiveLabel}"
                                    else selectedCity.localizedFull(strings.isArabic),
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    color = if (state.useGpsLocation) colors.accentWarm else colors.textSecondary
                                )
                            }
                            Text(
                                text = "${if (openPanel == "city") strings.close else strings.changeCityBtn} ${if (openPanel == "city") "▴" else "▾"}",
                                fontFamily = appFont,
                                fontSize = 12.sp,
                                color = colors.accentWarm
                            )
                        }

                        if (openPanel == "city") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // GPS Detect Button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.accentWarmSoft)
                                        .border(1.dp, colors.accentWarm.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .clickable(enabled = !isLocating) { onRequestLocation() }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isLocating) strings.detectingLocation else strings.detectLocationBtn,
                                        fontFamily = appFont,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.accentWarm
                                    )
                                }

                                BasicTextField(
                                    value = cityQuery,
                                    onValueChange = { cityQuery = it },
                                    textStyle = TextStyle(fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary),
                                    cursorBrush = SolidColor(colors.accentWarm),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.background)
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            if (cityQuery.isEmpty()) {
                                                Text(strings.searchCityHint, fontFamily = appFont, fontSize = 13.sp, color = colors.textMuted)
                                            }
                                            inner()
                                        }
                                    }
                                )
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(filteredCities, key = { it.id }) { city ->
                                        val isSel = city.id == state.prayerCityId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) colors.accentWarmSoft else Color.Transparent)
                                                .clickable {
                                                    onSetPrayerCity(city.id)
                                                    cityQuery = ""
                                                    openPanel = null
                                                }
                                                .padding(horizontal = 10.dp, vertical = 9.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(city.localizedName(strings.isArabic), fontFamily = appFont, fontSize = 13.sp, color = if (isSel) colors.accentWarm else colors.textPrimary)
                                            Text(city.localizedCountry(strings.isArabic) + if (isSel) " ✓" else "", fontFamily = appFont, fontSize = 11.sp, color = if (isSel) colors.accentWarm else colors.textMuted)
                                        }
                                    }
                                }
                            }
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Qibla Compass Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClose()
                                    onOpenQibla()
                                }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.qiblaTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(strings.qiblaSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Text(strings.openChevron, fontFamily = appFont, fontSize = 12.sp, color = colors.accentWarm)
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Adhan Toggle + Muezzin Voice Picker with Live Preview
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.adhanTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                    Text(strings.adhanSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Switch(
                                    checked = state.adhanEnabled,
                                    onCheckedChange = onToggleAdhan,
                                    colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                                )
                            }

                            Text(
                                text = "${strings.muezzinVoiceLabel}: ${muezzinOptionFor(state.adhanMuezzinId).localizedName(strings.isArabic)}",
                                fontFamily = appFont,
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                MUEZZIN_OPTIONS.forEach { mOpt ->
                                    val selected = state.adhanMuezzinId == mOpt.id
                                    val playing = previewingMuezzinId == mOpt.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selected) colors.accentWarmSoft else colors.background)
                                            .border(
                                                1.dp,
                                                if (selected) colors.accentWarm else colors.borderSubtle,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onSetAdhanMuezzin(mOpt.id) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = mOpt.localizedName(strings.isArabic) + if (selected) " ✓" else "",
                                            fontFamily = appFont,
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                            color = if (selected) colors.accentWarm else colors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        SmallChip(
                                            label = if (playing) strings.stopAudio else strings.previewAudio,
                                            active = playing
                                        ) { onToggleMuezzinPreview(mOpt.id) }
                                    }
                                }
                            }
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Pre-Prayer Notification Reminder Row
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.prayerReminderTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                    Text(strings.prayerReminderSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Switch(
                                    checked = state.prayerReminderEnabled,
                                    onCheckedChange = onTogglePrayerReminder,
                                    colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                                )
                            }

                            if (state.prayerReminderEnabled) {
                                Text(strings.reminderBeforeLabel, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(5, 10, 15, 20, 30).forEach { mins ->
                                        SmallChip(
                                            label = strings.minutesShortFmt(mins.formatDigits(strings.isArabic)),
                                            active = state.prayerReminderMinutesBefore == mins,
                                            modifier = Modifier.weight(1f)
                                        ) { onSetPrayerReminderMinutes(mins) }
                                    }
                                }
                            }
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Auto-Focus at Prayer Times Row
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.prayerAutoFocusTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                    Text(strings.prayerAutoFocusSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Switch(
                                    checked = state.prayerAutoFocusEnabled,
                                    onCheckedChange = onTogglePrayerAutoFocus,
                                    colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                                )
                            }

                            if (state.prayerAutoFocusEnabled) {
                                Text(strings.prayerAutoFocusDurationLabel, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(15, 20, 30).forEach { mins ->
                                        SmallChip(
                                            label = strings.minutesShortFmt(mins.formatDigits(strings.isArabic)),
                                            active = state.prayerAutoFocusMinutes == mins,
                                            modifier = Modifier.weight(1f)
                                        ) { onSetPrayerAutoFocusMinutes(mins) }
                                    }
                                }
                            }
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Daily Prayer Tracker Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.prayerTrackerTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(strings.prayerTrackerSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Switch(
                                checked = state.prayerTrackerEnabled,
                                onCheckedChange = onTogglePrayerTracker,
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                            )
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Quran Line Spacing Row
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(strings.quranSpacingSection, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    1.85f to strings.spacingCompact,
                                    2.15f to strings.spacingComfortable,
                                    2.45f to strings.spacingWide,
                                    2.75f to strings.spacingExtraWide
                                ).forEach { (value, label) ->
                                    SmallChip(
                                        label = label,
                                        active = abs(state.quranLineHeightMultiplier - value) < 0.12f,
                                        modifier = Modifier.weight(1f)
                                    ) { onSetQuranLineHeight(value) }
                                }
                            }
                        }
                    }
                }
            }

            // ===== GROUP 3: FOCUS & NAVIGATION =====
            if (currentCat == "focus") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.catFocus, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        // Focus Mode Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.focusOn.takeIf { state.focusMode } ?: strings.normalMode, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Switch(
                                checked = state.focusMode,
                                onCheckedChange = onToggleFocus,
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Mindful Duration Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.mindfulDurationLabel, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(3, 5, 10).forEach { sec ->
                                    SmallChip("${sec.formatDigits(strings.isArabic)}s", active = state.mindfulPauseSeconds == sec) {
                                        onSetMindfulDuration(sec)
                                    }
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Time-Boxed Mindful Pause Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.mindfulTimeBoxTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(strings.mindfulTimeBoxSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Switch(
                                checked = state.mindfulTimeBoxEnabled,
                                onCheckedChange = onToggleMindfulTimeBox,
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Haptics Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.hapticsLabel, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Switch(
                                checked = state.hapticsEnabled,
                                onCheckedChange = onSetHaptics,
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Collapsible Tabs Reorder Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openPanel = if (openPanel == "tabs") null else "tabs" }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.tabsOrderSection, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary, modifier = Modifier.weight(1f))
                            Text(
                                text = "${enabledIds.size.formatDigits(strings.isArabic)}/${state.pageConfig.size.formatDigits(strings.isArabic)} ${if (openPanel == "tabs") "▴" else "▾"}",
                                fontFamily = appFont,
                                fontSize = 12.sp,
                                color = colors.accentWarm
                            )
                        }

                        if (openPanel == "tabs") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val total = state.pageConfig.size
                                for (visualIdx in 0 until total) {
                                    val configIdx = total - 1 - visualIdx
                                    val cfg = state.pageConfig[configIdx]
                                    val meta = PAGE_META_MAP[cfg.id] ?: continue
                                    val isHome = cfg.id == "home"
                                    val posLabel = when {
                                        !cfg.enabled && !isHome -> strings.hiddenLabel
                                        isHome -> strings.centerHomeLabel
                                        else -> {
                                            val idx = enabledIds.indexOf(cfg.id)
                                            if (idx > homeEnabledIdx) strings.rightOfHomeFmt((idx - homeEnabledIdx).formatDigits(strings.isArabic))
                                            else strings.leftOfHomeFmt((homeEnabledIdx - idx).formatDigits(strings.isArabic))
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isHome) colors.accentWarmSoft else colors.background)
                                            .border(1.dp, if (isHome) colors.accentWarm else colors.borderSubtle, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(meta.localizedLabel(strings.isArabic), fontFamily = appFont, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                            Text(posLabel, fontFamily = appFont, fontSize = 10.sp, color = colors.textSecondary)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            SmallChip("↑") { if (visualIdx > 0) onMovePage(configIdx, configIdx + 1) }
                                            SmallChip("↓") { if (visualIdx < total - 1) onMovePage(configIdx, configIdx - 1) }
                                            if (!meta.required) {
                                                SmallChip(if (cfg.enabled) strings.enabledLabel else strings.hiddenLabel, active = cfg.enabled) {
                                                    onTogglePageEnabled(cfg.id)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ===== GROUP 4: SYSTEM & VOLUME =====
            if (currentCat == "system") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.catSystem, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        // Collapsible Volume Control
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openPanel = if (openPanel == "volume") null else "volume" }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.quickVolumeControl, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                            Text(if (openPanel == "volume") "▴" else "▾", fontSize = 13.sp, color = colors.textSecondary)
                        }
                        if (openPanel == "volume") {
                            Box(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                                VolumeControlCard(volumes = volumes, onSetVolume = onSetVolume, onToggleMute = onToggleMute)
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Focus Notification Shield Permission
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenNotificationAccessSettings() }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.notifAccessSettingsTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(
                                    text = if (isNotificationAccessGranted) strings.notifAccessActive else strings.notifAccessPrompt,
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    color = if (isNotificationAccessGranted) colors.accentFocus else colors.accentWarm
                                )
                            }
                            Text(strings.openChevron, fontFamily = appFont, fontSize = 12.sp, color = colors.textSecondary)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Accessibility
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAccessibilitySettings() }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.fingerprintTasbihTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(
                                    text = if (isAccessibilityConnected) strings.accessibilityActive else strings.accessibilityPrompt,
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    color = if (isAccessibilityConnected) colors.accentFocus else colors.accentWarm
                                )
                            }
                            Text(strings.openChevron, fontFamily = appFont, fontSize = 12.sp, color = colors.textSecondary)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Default Launcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDefaultLauncherSettings() }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.defaultLauncherTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(strings.defaultLauncherSubtitle, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                            }
                            Text(strings.openChevron, fontFamily = appFont, fontSize = 12.sp, color = colors.textSecondary)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Restart Setup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClose()
                                    onRestartSetup()
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.restartSetupTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary)
                                Text(strings.restartSetupSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                            }
                            Text("↻", fontSize = 15.sp, color = colors.textSecondary)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                        // Reset App
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showResetConfirm = true }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.resetAppTitle, fontFamily = appFont, fontSize = 14.sp, color = colors.dangerSoft)
                                Text(strings.resetAppSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = colors.surface,
            title = { Text(strings.resetAppTitle, fontFamily = appFont, color = colors.textPrimary) },
            text = { Text(strings.confirmResetAppMsg, fontFamily = appFont, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    onClose()
                    onResetApp()
                }) {
                    Text(strings.reset, fontFamily = appFont, color = colors.dangerSoft)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(strings.cancel, fontFamily = appFont, color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryTile(
    emoji: String,
    title: String,
    subtitle: String,
    badge: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    Column(
        modifier = modifier
            .heightIn(min = 148.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentWarmSoft)
                    .border(1.dp, colors.accentWarm.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 18.sp)
            }
            Text(
                text = badge,
                fontFamily = appFont,
                fontSize = 10.sp,
                color = colors.accentWarm,
                maxLines = 1
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontFamily = appFont,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                fontFamily = appFont,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = colors.textSecondary,
                maxLines = 2
            )
        }
    }
}

// =========================================
// 4. Mindful Pause 5-Second Modal
// =========================================
@Composable
fun MindfulPauseOverlay(
    app: AppInfo,
    secondsLeft: Int,
    timeBoxEnabled: Boolean = true,
    onStay: () -> Unit,
    onProceed: (Int?) -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlayBg)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.accentWarmSoft)
                    .border(1.dp, colors.accentWarm, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (secondsLeft > 0) secondsLeft.formatDigits(strings.isArabic) else "✓",
                    fontFamily = appFont,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = colors.accentWarm
                )
            }

            Text(
                text = strings.beforeOpeningFmt(app.label),
                fontFamily = appFont,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )

            Text(
                text = strings.mindfulPromptFmt(app.label),
                fontFamily = appFont,
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.textPrimary)
                    .clickable { onStay() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(strings.returnToCalm, fontFamily = appFont, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.background)
            }

            if (secondsLeft > 0 || !timeBoxEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .clickable(enabled = secondsLeft == 0) { onProceed(null) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (secondsLeft > 0) strings.waitSecondsFmt(secondsLeft.formatDigits(strings.isArabic)) else strings.openAppFmt(app.label),
                        fontFamily = appFont,
                        fontSize = 14.sp,
                        color = if (secondsLeft > 0) colors.textMuted else colors.textSecondary
                    )
                }
            } else {
                // Time-Boxed Mindful Pause: ask intended duration before launching
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.mindfulChooseDuration,
                        fontFamily = appFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accentWarm
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15).forEach { mins ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentWarmSoft)
                                    .border(1.dp, colors.accentWarm.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .clickable { onProceed(mins) }
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.minutesShortFmt(mins.formatDigits(strings.isArabic)),
                                    fontFamily = appFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.accentWarm
                                )
                            }
                        }
                    }
                    Text(
                        text = strings.mindfulOpenWithoutTimer,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onProceed(null) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// =========================================
// Safe Math Evaluator for Universal Search
// =========================================
private fun evaluateSimpleMath(raw: String): String? {
    val expr = raw.replace("×", "*").replace("x", "*").replace("X", "*").replace("÷", "/").replace(" ", "")
    if (expr.isEmpty() || !expr.matches(Regex("""[\d+\-*/.]+"""))) return null
    if (!expr.any { it in "+-*/" }) return null
    return try {
        val tokens = Regex("""\d+(\.\d+)?|[+\-*/]""").findAll(expr).map { it.value }.toMutableList()
        if (tokens.size < 3) return null
        // Multiply / Divide first
        var i = 1
        while (i < tokens.size - 1) {
            if (tokens[i] == "*" || tokens[i] == "/") {
                val a = tokens[i - 1].toDouble()
                val b = tokens[i + 1].toDouble()
                if (tokens[i] == "/" && b == 0.0) return null
                val r = if (tokens[i] == "*") a * b else a / b
                tokens[i - 1] = r.toString()
                tokens.removeAt(i)
                tokens.removeAt(i)
            } else {
                i += 2
            }
        }
        var acc = tokens[0].toDouble()
        var j = 1
        while (j < tokens.size - 1) {
            val b = tokens[j + 1].toDouble()
            acc = if (tokens[j] == "+") acc + b else acc - b
            j += 2
        }
        if (acc % 1.0 == 0.0) acc.toLong().toString() else String.format("%.3f", acc).trimEnd('0').trimEnd('.')
    } catch (_: Exception) {
        null
    }
}
