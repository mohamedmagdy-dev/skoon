package com.sukoon.launcher.ui.screens

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.sukoon.launcher.ui.VolumeLevels
import com.sukoon.launcher.ui.theme.APP_FONT_OPTIONS
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import kotlin.math.abs

// =========================================
// 1. App Drawer Overlay (Text-Only, NO Icons)
// =========================================
@Composable
fun AppDrawerOverlay(
    state: LauncherState,
    installedApps: List<AppInfo>,
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
                    Text(
                        text = app.label,
                        fontFamily = appFont,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Light,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
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
// 3. Settings & Customization Overlay
// =========================================
@Composable
fun SettingsOverlay(
    state: LauncherState,
    volumes: VolumeLevels,
    isAccessibilityConnected: Boolean,
    onSetLanguage: (String) -> Unit,
    onSetAppFont: (String) -> Unit,
    onSetPrayerCity: (String) -> Unit,
    onSetQuranLineHeight: (Float) -> Unit,
    onMovePage: (Int, Int) -> Unit,
    onTogglePageEnabled: (String) -> Unit,
    onSetDarkTheme: (Boolean) -> Unit,
    onRefreshVolumes: () -> Unit,
    onSetVolume: (Int, Int) -> Unit,
    onToggleMute: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenDefaultLauncherSettings: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    LaunchedEffect(Unit) { onRefreshVolumes() }

    var showCityPicker by remember { mutableStateOf(false) }
    var cityQuery by remember { mutableStateOf("") }

    val selectedCity = remember(state.prayerCityId) {
        PrayerTimesCalculator.findCity(state.prayerCityId)
    }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlayBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(strings.customizeSukoon, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                Text(strings.settingsTitle, fontFamily = appFont, fontSize = 20.sp, color = colors.textPrimary)
            }
            SmallChip(strings.done, active = true) { onClose() }
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Language
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.languageSection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(strings.languageSubtitle, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallChip(
                        label = "العربية",
                        active = state.appLanguage == "ar",
                        modifier = Modifier.weight(1f)
                    ) { onSetLanguage("ar") }
                    SmallChip(
                        label = "English",
                        active = state.appLanguage == "en",
                        modifier = Modifier.weight(1f)
                    ) { onSetLanguage("en") }
                }
            }

            // 2. App Font
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.fontSection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(strings.fontSubtitle, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                APP_FONT_OPTIONS.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { opt ->
                            SmallChip(
                                label = opt.localizedName(strings.isArabic),
                                active = state.appFontId == opt.id,
                                fontOverride = opt.family,
                                modifier = Modifier.weight(1f)
                            ) { onSetAppFont(opt.id) }
                        }
                        if (rowOptions.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // 3. Prayer Times City / Location
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.prayerCitySection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(strings.prayerCitySubtitle, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(
                            1.dp,
                            if (showCityPicker) colors.accentWarm else colors.borderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showCityPicker = !showCityPicker }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCity.localizedName(strings.isArabic),
                            fontFamily = appFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = selectedCity.localizedCountry(strings.isArabic),
                            fontFamily = appFont,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    SmallChip(if (showCityPicker) strings.close else strings.changeCityBtn, active = !showCityPicker) {
                        showCityPicker = !showCityPicker
                    }
                }

                if (showCityPicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredCities, key = { it.id }) { city ->
                                val isSelected = city.id == state.prayerCityId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.accentWarmSoft else colors.background.copy(alpha = 0f))
                                        .clickable {
                                            onSetPrayerCity(city.id)
                                            cityQuery = ""
                                            showCityPicker = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = city.localizedName(strings.isArabic),
                                        fontFamily = appFont,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSelected) colors.accentWarm else colors.textPrimary
                                    )
                                    Text(
                                        text = city.localizedCountry(strings.isArabic) + if (isSelected) " ✓" else "",
                                        fontFamily = appFont,
                                        fontSize = 11.sp,
                                        color = if (isSelected) colors.accentWarm else colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Quran Line Spacing
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.quranSpacingSection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(strings.quranSpacingSubtitle, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
                val presets = listOf(
                    1.85f to strings.spacingCompact,
                    2.15f to strings.spacingComfortable,
                    2.45f to strings.spacingWide,
                    2.75f to strings.spacingExtraWide
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (value, label) ->
                        val active = abs(state.quranLineHeightMultiplier - value) < 0.12f
                        SmallChip(
                            label = label,
                            active = active,
                            modifier = Modifier.weight(1f)
                        ) { onSetQuranLineHeight(value) }
                    }
                }
            }

            // 5. Appearance
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.appearanceSection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallChip(strings.darkOled, active = state.darkTheme, modifier = Modifier.weight(1f)) { onSetDarkTheme(true) }
                    SmallChip(strings.lightPaper, active = !state.darkTheme, modifier = Modifier.weight(1f)) { onSetDarkTheme(false) }
                }
            }

            // 6. Reorder Tabs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.tabsOrderSection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(strings.tabsOrderSubtitle, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)

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
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isHome) colors.accentWarmSoft else colors.surface)
                            .border(
                                1.dp,
                                if (isHome) colors.accentWarm else colors.borderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meta.localizedLabel(strings.isArabic), fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                            Text(posLabel, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
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

            // 7. Quick Volume Control
            VolumeControlCard(
                volumes = volumes,
                onSetVolume = onSetVolume,
                onToggleMute = onToggleMute
            )

            // 8. System Permissions (Fingerprint Accessibility & Default Home)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.systemSettingsSection, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
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
            }

            Spacer(Modifier.height(12.dp))
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
    onStay: () -> Unit,
    onProceed: () -> Unit
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    .clickable(enabled = secondsLeft == 0) { onProceed() }
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
