package com.sukoon.launcher.ui.screens

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.PAGE_META_MAP
import com.sukoon.launcher.data.PRAYER_CITIES
import com.sukoon.launcher.data.PrayerTimesCalculator
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.service.MUEZZIN_OPTIONS
import com.sukoon.launcher.service.muezzinOptionFor
import com.sukoon.launcher.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    state: LauncherState,
    isLocating: Boolean,
    previewingMuezzinId: String?,
    onSetStep: (Int) -> Unit,
    onSetLanguage: (String) -> Unit,
    onTogglePageEnabled: (String) -> Unit,
    onMovePage: (Int, Int) -> Unit,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetAppTheme: (String) -> Unit,
    onSetAppFont: (String) -> Unit,
    onSetAppFontWeight: (String) -> Unit,
    onSetAppDisplayStyle: (String) -> Unit,
    onSetPrayerCity: (String) -> Unit,
    onRequestLocation: () -> Unit,
    onToggleAdhan: (Boolean) -> Unit,
    onSetAdhanMuezzin: (String) -> Unit,
    onToggleMuezzinPreview: (String) -> Unit,
    onTogglePrayerReminder: (Boolean) -> Unit,
    onSetPrayerReminderMinutes: (Int) -> Unit,
    onTogglePrayerAutoFocus: (Boolean) -> Unit,
    onSetPrayerAutoFocusMinutes: (Int) -> Unit,
    onSetMindfulDuration: (Int) -> Unit,
    onToggleMindfulTimeBox: (Boolean) -> Unit,
    onComplete: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val step = state.onboardingStep

    // Screen 01 — Splash Screen (OLED black, smooth transition to Welcome)
    if (step == 0) {
        LaunchedEffect(Unit) {
            delay(1300)
            onSetStep(1)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09090B))
                .clickable { onSetStep(1) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD9B88C))
                )
                Text(
                    text = "سكون",
                    fontFamily = AmiriFont,
                    fontSize = 44.sp,
                    color = Color(0xFFF4F0E8)
                )
                Text(
                    text = "SUKOON",
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    letterSpacing = 4.sp,
                    color = Color(0xFFA19D95)
                )
            }
        }
        return
    }

    var cityQuery by remember { mutableStateOf("") }
    val selectedCity = remember(state.prayerCityId, state.useGpsLocation, state.customLat, state.customLng, state.customLocationLabel) {
        PrayerTimesCalculator.resolveCityForState(state)
    }
    val nextPrayer = remember(state.prayerCityId, state.useGpsLocation, state.customLat, state.customLng) {
        PrayerTimesCalculator.getNextPrayerForState(state)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Progress Bar & Back/Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    Text(
                        text = strings.backLabel,
                        fontFamily = appFont,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetStep(step - 1) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                } else {
                    Text(
                        text = "سكون",
                        fontFamily = AmiriFont,
                        fontSize = 20.sp,
                        color = colors.accentWarm
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (i in 1..8) {
                        val active = i == step
                        val passed = i < step
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(if (active) 20.dp else 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        active -> colors.accentWarm
                                        passed -> colors.accentWarm.copy(alpha = 0.45f)
                                        else -> colors.borderSubtle
                                    }
                                )
                        )
                    }
                }

                if (step == 5 || step == 6) {
                    Text(
                        text = strings.skipLabel,
                        fontFamily = appFont,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetStep(step + 1) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                } else {
                    Spacer(Modifier.width(36.dp))
                }
            }

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                when (step) {
                    // Screen 02 — Welcome
                    1 -> Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.obWelcomeEyebrow,
                            fontFamily = appFont,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            color = colors.accentWarm
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = strings.obWelcomeHeading,
                            fontFamily = appFont,
                            fontSize = 32.sp,
                            lineHeight = 42.sp,
                            fontWeight = FontWeight.Light,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = strings.obWelcomeDesc,
                            fontFamily = appFont,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = colors.textSecondary
                        )
                    }

                    // Screen 03 — Language
                    2 -> Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.obLangTitle, fontFamily = appFont, fontSize = 26.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                            Text(strings.obLangSub, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                        }
                        listOf(
                            Triple("ar", "العربية", "واجهة عربية كاملة من اليمين إلى اليسار (RTL)"),
                            Triple("en", "English", "Clean left-to-right (LTR) interface")
                        ).forEach { (code, title, sub) ->
                            val sel = state.appLanguage == code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (sel) colors.accentWarmSoft else colors.surface)
                                    .border(1.dp, if (sel) colors.accentWarm else colors.borderSubtle, RoundedCornerShape(14.dp))
                                    .clickable { onSetLanguage(code) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, fontFamily = appFont, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                    Text(sub, fontFamily = appFont, fontSize = 12.sp, color = colors.textSecondary)
                                }
                                Text(
                                    text = if (sel) "✓" else "",
                                    fontSize = 16.sp,
                                    color = colors.accentWarm
                                )
                            }
                        }
                    }

                    // Screen 04 — Choose Your Experience
                    3 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.obExpTitle, fontFamily = appFont, fontSize = 26.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                            Text(strings.obExpSub, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                        }

                        Text(strings.obProductivity, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                        listOf("pomodoro", "notes", "habits").forEach { id ->
                            val cfg = state.pageConfig.find { it.id == id }
                            val meta = PAGE_META_MAP[id] ?: return@forEach
                            val on = cfg?.enabled ?: true
                            OnboardingToggleRow(
                                label = meta.localizedLabel(strings.isArabic),
                                checked = on,
                                onClick = { onTogglePageEnabled(id) }
                            )
                        }

                        Text(strings.obSpiritual, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                        listOf("quran", "azkar", "dhikr").forEach { id ->
                            val cfg = state.pageConfig.find { it.id == id }
                            val meta = PAGE_META_MAP[id] ?: return@forEach
                            val on = cfg?.enabled ?: true
                            OnboardingToggleRow(
                                label = meta.localizedLabel(strings.isArabic),
                                checked = on,
                                onClick = { onTogglePageEnabled(id) }
                            )
                        }

                        Text(
                            text = strings.obHomeCenterNote,
                            fontFamily = appFont,
                            fontSize = 12.sp,
                            color = colors.accentWarm
                        )
                    }

                    // Screen 05 — Appearance & Font
                    4 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.obAppearanceTitle, fontFamily = appFont, fontSize = 26.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                            Text(strings.obAppearanceSub, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                        }

                        // Live Preview Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = fontOptionFor(state.appFontId).localizedName(strings.isArabic),
                                fontFamily = appFont,
                                fontSize = 11.sp,
                                color = colors.accentWarm
                            )
                            Text("أقل ضجيجاً. أكثر معنى.", fontFamily = appFont, fontSize = 21.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                            Text("Less noise. More meaning.", fontFamily = appFont, fontSize = 14.sp, color = colors.textSecondary)
                        }

                        // App Display Style (Text / Monochrome / Original)
                        Text(strings.appDisplaySection, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) colors.accentWarmSoft else colors.surface)
                                        .border(1.dp, if (active) colors.accentWarm else colors.borderSubtle, RoundedCornerShape(12.dp))
                                        .clickable { onSetAppDisplayStyle(id) }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.height(24.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        when (id) {
                                            "text" -> Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(Modifier.width(30.dp).height(4.dp).clip(RoundedCornerShape(50)).background(colors.textPrimary.copy(alpha = 0.8f)))
                                                Box(Modifier.width(20.dp).height(3.dp).clip(RoundedCornerShape(50)).background(colors.textMuted))
                                            }
                                            "monochrome" -> {
                                                Box(Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)).background(colors.surfaceHover).border(1.dp, colors.borderStrong, RoundedCornerShape(5.dp)))
                                                Box(Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)).background(colors.surfaceHover).border(1.dp, colors.borderStrong, RoundedCornerShape(5.dp)))
                                            }
                                            else -> {
                                                Box(Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF25D366)))
                                                Box(Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFEF4444)))
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

                        // Color Themes (6 curated themes with color swatch)
                        Text(strings.themesSection, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                        APP_THEME_OPTIONS.chunked(2).forEach { rowThemes ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowThemes.forEach { tOpt ->
                                    val active = state.appThemeId == tOpt.id
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (active) colors.accentWarmSoft else colors.surface)
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

                        // Font Weight (Light / Regular / Bold)
                        Text(strings.fontWeightSection, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "light" to strings.weightLight,
                                "normal" to strings.weightRegular,
                                "bold" to strings.weightBold
                            ).forEach { (id, label) ->
                                SmallChip(label = label, active = state.appFontWeight == id, modifier = Modifier.weight(1f)) {
                                    onSetAppFontWeight(id)
                                }
                            }
                        }

                        // Fonts
                        Text(strings.fontSection, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                        APP_FONT_OPTIONS.chunked(2).forEach { rowOpts ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowOpts.forEach { opt ->
                                    SmallChip(
                                        label = opt.localizedName(strings.isArabic),
                                        active = state.appFontId == opt.id,
                                        fontOverride = opt.family,
                                        modifier = Modifier.weight(1f)
                                    ) { onSetAppFont(opt.id) }
                                }
                                if (rowOpts.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    // Screen 06 — Location
                    5 -> Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.obLocationTitle, fontFamily = appFont, fontSize = 26.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                            Text(strings.obLocationSub, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                        }

                        // Current City Preview + GPS Detect Button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.accentWarmSoft)
                                .border(1.dp, colors.accentWarm.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (state.useGpsLocation) selectedCity.localizedName(strings.isArabic) else selectedCity.localizedFull(strings.isArabic),
                                        fontFamily = appFont,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${nextPrayer.nextPrayer.localizedName(strings.isArabic)} • ${nextPrayer.nextPrayer.format12h(strings.isArabic)}" +
                                            if (state.useGpsLocation) "  •  GPS" else "",
                                        fontFamily = appFont,
                                        fontSize = 12.sp,
                                        color = colors.accentWarm
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isLocating) { onRequestLocation() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isLocating) strings.detectingLocation else strings.detectLocationBtn,
                                    fontFamily = appFont,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp)),
                            contentPadding = PaddingValues(6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(filteredCities, key = { it.id }) { city ->
                                val active = city.id == state.prayerCityId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) colors.accentWarmSoft else Color.Transparent)
                                        .clickable { onSetPrayerCity(city.id) }
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = city.localizedName(strings.isArabic),
                                        fontFamily = appFont,
                                        fontSize = 14.sp,
                                        color = if (active) colors.accentWarm else colors.textPrimary
                                    )
                                    Text(
                                        text = city.localizedCountry(strings.isArabic) + if (active) " ✓" else "",
                                        fontFamily = appFont,
                                        fontSize = 11.sp,
                                        color = if (active) colors.accentWarm else colors.textMuted
                                    )
                                }
                            }
                        }
                    }

                    // Screen 07 — Adhan Voice, Prayer Reminders & Essential Timings
                    6 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.obPrayerSetupTitle, fontFamily = appFont, fontSize = 24.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                            Text(strings.obPrayerSetupSub, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                        }

                        // Card 1: Adhan & Muezzin Voice
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.adhanTitle, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                    Text(strings.adhanSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Switch(
                                    checked = state.adhanEnabled,
                                    onCheckedChange = onToggleAdhan,
                                    colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                                )
                            }

                            Text(strings.muezzinVoiceLabel, fontFamily = appFont, fontSize = 11.sp, color = colors.textMuted)
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

                        // Card 2: Pre-Prayer Reminder + Auto-Focus at Prayer Times
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.prayerReminderTitle, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                    Text(strings.prayerReminderSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Switch(
                                    checked = state.prayerReminderEnabled,
                                    onCheckedChange = onTogglePrayerReminder,
                                    colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                                )
                            }
                            if (state.prayerReminderEnabled) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(5, 10, 15, 20, 30).forEach { mins ->
                                        SmallChip(
                                            label = strings.minutesShortFmt(mins.formatDigits(strings.isArabic)),
                                            active = state.prayerReminderMinutesBefore == mins,
                                            modifier = Modifier.weight(1f)
                                        ) { onSetPrayerReminderMinutes(mins) }
                                    }
                                }
                            }

                            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.prayerAutoFocusTitle, fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                    Text(strings.prayerAutoFocusSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Switch(
                                    checked = state.prayerAutoFocusEnabled,
                                    onCheckedChange = onTogglePrayerAutoFocus,
                                    colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                                )
                            }
                            if (state.prayerAutoFocusEnabled) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

                        // Card 3: Mindful Pause Duration & Time-Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.mindfulDurationLabel, fontFamily = appFont, fontSize = 13.sp, color = colors.textPrimary)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(3, 5, 10).forEach { sec ->
                                        SmallChip("${sec.formatDigits(strings.isArabic)}s", active = state.mindfulPauseSeconds == sec) {
                                            onSetMindfulDuration(sec)
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.mindfulTimeBoxTitle, fontFamily = appFont, fontSize = 13.sp, color = colors.textPrimary)
                                    Text(strings.mindfulTimeBoxSub, fontFamily = appFont, fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Switch(
                                    checked = state.mindfulTimeBoxEnabled,
                                    onCheckedChange = onToggleMindfulTimeBox,
                                    colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.accentWarm)
                                )
                            }
                        }
                    }

                    // Screen 08 — Customize Navigation
                    7 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.obNavTitle, fontFamily = appFont, fontSize = 26.sp, fontWeight = FontWeight.Light, color = colors.textPrimary)
                            Text(strings.obNavSub, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                        }

                        val total = state.pageConfig.size
                        for (visualIdx in 0 until total) {
                            val configIdx = total - 1 - visualIdx
                            val cfg = state.pageConfig[configIdx]
                            val meta = PAGE_META_MAP[cfg.id] ?: continue
                            val isHome = cfg.id == "home"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isHome) colors.accentWarmSoft else colors.surface)
                                    .border(1.dp, if (isHome) colors.accentWarm else colors.borderSubtle, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(meta.localizedLabel(strings.isArabic), fontFamily = appFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                    if (isHome) {
                                        Text(strings.centerHomeLabel, fontFamily = appFont, fontSize = 11.sp, color = colors.accentWarm)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
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

                    // Screen 08 — Ready
                    else -> Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.accentWarmSoft)
                                .border(1.dp, colors.accentWarm, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 20.sp, color = colors.accentWarm)
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = strings.obReadyHeading,
                            fontFamily = appFont,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Light,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = strings.obReadyDesc,
                            fontFamily = appFont,
                            fontSize = 15.sp,
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.height(20.dp))
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
                                Text(strings.appearanceSection, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                                Text(
                                    "${if (state.darkTheme) strings.darkOled else strings.lightPaper} • ${fontOptionFor(state.appFontId).localizedName(strings.isArabic)}",
                                    fontFamily = appFont,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(strings.prayerCitySection, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                                Text(selectedCity.localizedName(strings.isArabic), fontFamily = appFont, fontSize = 13.sp, color = colors.textPrimary)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(strings.muezzinVoiceLabel, fontFamily = appFont, fontSize = 13.sp, color = colors.textSecondary)
                                Text(muezzinOptionFor(state.adhanMuezzinId).localizedName(strings.isArabic), fontFamily = appFont, fontSize = 13.sp, color = colors.accentWarm)
                            }
                        }
                    }
                }
            }

            // Primary Bottom CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.textPrimary)
                    .clickable {
                        if (step < 8) onSetStep(step + 1) else onComplete()
                    }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (step) {
                        1 -> strings.obGetStarted
                        8 -> strings.obEnterSukoon
                        else -> strings.nextLabel
                    },
                    fontFamily = appFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.background,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OnboardingToggleRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) colors.surface else colors.background)
            .border(
                1.dp,
                if (checked) colors.accentWarm.copy(alpha = 0.55f) else colors.borderSubtle,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = appFont,
            fontSize = 15.sp,
            color = if (checked) colors.textPrimary else colors.textMuted
        )
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) colors.accentWarm else Color.Transparent)
                .border(1.5.dp, if (checked) colors.accentWarm else colors.borderStrong, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Text("✓", fontSize = 13.sp, color = colors.background, fontWeight = FontWeight.Bold)
            }
        }
    }
}
