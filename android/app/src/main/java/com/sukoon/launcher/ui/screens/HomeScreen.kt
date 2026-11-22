package com.sukoon.launcher.ui.screens

import android.icu.text.SimpleDateFormat
import android.icu.util.IslamicCalendar
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.AppInfo
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.PrayerTimesCalculator
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    state: LauncherState,
    installedApps: List<AppInfo>,
    rightTabLabel: String?,
    leftTabLabel: String?,
    onLoadAppIcon: suspend (String) -> ImageBitmap?,
    onAppClick: (AppInfo) -> Unit,
    onRemoveFavorite: (AppInfo) -> Unit,
    onToggleFocus: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateRight: () -> Unit,
    onNavigateLeft: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    var editingFavorites by remember { mutableStateOf(false) }
    var showAllPrayers by remember { mutableStateOf(false) }

    // Minute-aligned clock ticker (minimizes CPU wakeups and recompositions)
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            nowMillis = now
            val delayToNextMinute = (60_000L - (now % 60_000L)).coerceAtLeast(1_000L)
            delay(delayToNextMinute)
        }
    }

    val prayerInfo = remember(nowMillis / 60_000, state.prayerCityId) {
        PrayerTimesCalculator.getNextPrayer(state.prayerCityId, nowMillis)
    }

    val timeStr = remember(nowMillis) {
        SimpleDateFormat("HH:mm", Locale.US).format(Date(nowMillis))
    }
    val gregDate = remember(nowMillis, strings.isArabic) {
        val locale = if (strings.isArabic) Locale("ar", "EG") else Locale.ENGLISH
        val pattern = if (strings.isArabic) "EEEE، d MMMM" else "EEEE, d MMMM"
        SimpleDateFormat(pattern, locale).format(Date(nowMillis))
    }
    val hijriDate = remember(nowMillis, strings.isArabic) {
        try {
            val cal = IslamicCalendar()
            cal.timeInMillis = nowMillis
            val locale = if (strings.isArabic) Locale("ar", "SA") else Locale.ENGLISH
            val fmt = SimpleDateFormat("d MMMM", locale)
            fmt.calendar = cal
            fmt.format(cal.time)
        } catch (_: Exception) {
            ""
        }
    }

    val appMap = remember(installedApps) { installedApps.associateBy { it.packageName } }
    val favoriteApps = remember(state.favoritePackages, appMap) {
        state.favoritePackages.mapNotNull { appMap[it] }
    }
    val visibleFavorites = remember(favoriteApps, state.focusMode, state.distractingPackages, editingFavorites) {
        if (state.focusMode && !editingFavorites) {
            favoriteApps.filterNot { it.packageName in state.distractingPackages }
        } else {
            favoriteApps
        }
    }
    val hiddenCount = favoriteApps.size - visibleFavorites.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Glance Header
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = timeStr,
                    fontFamily = appFont,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    color = colors.textPrimary,
                    letterSpacing = (-2).sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gregDate,
                        fontFamily = appFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = colors.textSecondary
                    )
                    if (hijriDate.isNotEmpty()) {
                        Text("•", color = colors.textMuted, fontSize = 13.sp)
                        Text(
                            text = hijriDate,
                            fontFamily = appFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Next Prayer Card (Dynamic Selected City — Expandable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                    .clickable { showAllPrayers = !showAllPrayers }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.accentWarm)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${prayerInfo.nextPrayer.localizedName(strings.isArabic)}  ${prayerInfo.nextPrayer.format12h(strings.isArabic)}",
                            fontFamily = appFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${prayerInfo.formatRemaining(strings.isArabic)} • ${prayerInfo.city.localizedName(strings.isArabic)}",
                            fontFamily = appFont,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                Text(
                    text = if (showAllPrayers) "▴" else "▾",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            }

            // Expandable All 5 Prayers Strip + City Header
            if (showAllPrayers) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prayerInfo.city.localizedFull(strings.isArabic),
                            fontFamily = appFont,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = strings.changeCityHint,
                            fontFamily = appFont,
                            fontSize = 11.sp,
                            color = colors.accentWarm,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onOpenSettings() }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        prayerInfo.todayPrayers.forEach { p ->
                            val isNext = p.id == prayerInfo.nextPrayer.id
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = p.localizedName(strings.isArabic),
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    fontWeight = if (isNext) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isNext) colors.accentWarm else colors.textMuted
                                )
                                Text(
                                    text = p.format12hShort(strings.isArabic),
                                    fontFamily = appFont,
                                    fontSize = 12.sp,
                                    fontWeight = if (isNext) FontWeight.Medium else FontWeight.Light,
                                    color = if (isNext) colors.textPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Focus Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(50))
                        .clickable { onToggleFocus() }
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (state.focusMode) colors.accentFocus else colors.textMuted)
                    )
                    Text(
                        text = if (state.focusMode) strings.focusOn else strings.normalMode,
                        fontFamily = appFont,
                        fontSize = 11.sp,
                        color = if (state.focusMode) colors.textPrimary else colors.textSecondary
                    )
                }

                // Quick Search + Volume Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(50))
                        .clickable { onOpenSearch() }
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.accentWarm)
                    )
                    Text(
                        text = strings.searchAndVolume,
                        fontFamily = appFont,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                // Quick Settings Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(50))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.settings,
                        fontFamily = appFont,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // Minimalist Text-Only Favorites (NO ICONS)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.favorites,
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (editingFavorites) strings.done else strings.edit,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = if (editingFavorites) colors.accentWarm else colors.textMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { editingFavorites = !editingFavorites }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = strings.settings,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenSettings() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(visibleFavorites, key = { it.packageName }) { app ->
                    val isDistracting = app.packageName in state.distractingPackages
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !editingFavorites) { onAppClick(app) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
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
                                size = 38.dp,
                                loadIcon = onLoadAppIcon
                            )
                            Text(
                                text = app.label,
                                fontFamily = appFont,
                                fontSize = if (state.appDisplayStyle == "text") 25.sp else 19.sp,
                                fontWeight = FontWeight.Light,
                                color = colors.textPrimary
                            )
                            if (isDistracting && !editingFavorites) {
                                Text(
                                    text = strings.mindfulTag,
                                    fontFamily = appFont,
                                    fontSize = 10.sp,
                                    color = colors.textMuted,
                                    modifier = Modifier
                                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(50))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (editingFavorites) {
                            Text(
                                text = strings.remove,
                                fontFamily = appFont,
                                fontSize = 12.sp,
                                color = colors.dangerSoft,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.dangerSoft.copy(alpha = 0.12f))
                                    .clickable { onRemoveFavorite(app) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (hiddenCount > 0) {
                    item {
                        Text(
                            text = strings.hiddenDistractingFmt(hiddenCount).formatDigits(strings.isArabic),
                            fontFamily = appFont,
                            fontSize = 12.sp,
                            color = colors.textMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Dynamic Edge Hints Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, colors.background)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rightTabLabel?.let { if (strings.isArabic) "‹ $it" else "‹ $it" } ?: "",
                fontFamily = appFont,
                fontSize = 13.sp,
                color = colors.accentWarm,
                modifier = Modifier
                    .clickable(enabled = rightTabLabel != null) { onNavigateRight() }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            )
            Text(
                text = strings.allAppsUp,
                fontFamily = appFont,
                fontSize = 13.sp,
                color = colors.textMuted,
                modifier = Modifier
                    .clickable { onOpenDrawer() }
                    .padding(vertical = 6.dp, horizontal = 8.dp)
            )
            Text(
                text = leftTabLabel?.let { "$it ›" } ?: "",
                fontFamily = appFont,
                fontSize = 13.sp,
                color = colors.textMuted,
                modifier = Modifier
                    .clickable(enabled = leftTabLabel != null) { onNavigateLeft() }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            )
        }
    }
}
