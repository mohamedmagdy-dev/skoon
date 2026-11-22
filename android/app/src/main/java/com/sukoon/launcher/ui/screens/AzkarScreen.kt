package com.sukoon.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.data.toArabicDigits
import com.sukoon.launcher.ui.theme.AmiriFont
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import java.util.Calendar

fun isCurrentlyMorning(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour in 4..14 // 4:00 AM to 2:59 PM -> Morning; 3:00 PM to 3:59 AM -> Evening
}

@Composable
fun AzkarScreen(
    state: LauncherState,
    isMorning: Boolean,
    onSetMorning: (Boolean) -> Unit,
    onSelectIndex: (Int) -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onEnterBlackScreenMode: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val list = if (isMorning) state.morningAzkar else state.eveningAzkar
    val idx = (if (isMorning) state.morningAzkarIndex else state.eveningAzkarIndex)
        .coerceIn(0, (list.size - 1).coerceAtLeast(0))
    val current = list.getOrNull(idx)
    val completedCount = list.count { it.count >= it.target }
    val scrollState = rememberScrollState()

    LaunchedEffect(idx, isMorning) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + Morning/Evening Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isMorning) strings.autoTimeMorning else strings.autoTimeEvening,
                    fontFamily = appFont,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
                Text(
                    text = if (isMorning) strings.morningAzkar else strings.eveningAzkar,
                    fontFamily = appFont,
                    fontSize = 21.sp,
                    color = colors.textPrimary
                )
            }

            // Morning | Evening Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (isMorning) colors.accentWarmSoft else colors.background.copy(alpha = 0f))
                        .clickable { onSetMorning(true) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        strings.morning,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = if (isMorning) colors.accentWarm else colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (!isMorning) colors.accentWarmSoft else colors.background.copy(alpha = 0f))
                        .clickable { onSetMorning(false) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        strings.evening,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = if (!isMorning) colors.accentWarm else colors.textSecondary
                    )
                }
            }
        }

        // Progress Bar + Black-Screen Tasbih Trigger
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.azkarDoneFmt(completedCount, list.size).let {
                        if (strings.isArabic) it.toArabicDigits() else it
                    },
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallChip(strings.blackScreen, active = true) { onEnterBlackScreenMode() }
                    SmallChip(strings.reset) { onReset() }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.borderSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (list.isNotEmpty()) completedCount.toFloat() / list.size else 0f)
                        .fillMaxHeight()
                        .background(colors.accentWarm)
                )
            }
        }

        // Main Zikr Card (Tap anywhere on card to count!)
        if (current != null) {
            val isDone = current.count >= current.target
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.mushafBg)
                    .border(
                        1.dp,
                        if (isDone) colors.accentFocus.copy(alpha = 0.45f) else colors.mushafBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onIncrement() }
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title & Index
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = current.title,
                        fontFamily = appFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accentWarm
                    )
                    Text(
                        text = "${(idx + 1).formatDigits(strings.isArabic)} / ${list.size.formatDigits(strings.isArabic)}",
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }

                // Zikr Text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .verticalScroll(scrollState),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = current.text,
                        fontFamily = AmiriFont,
                        fontSize = if (current.text.length > 260) 20.sp else 24.sp,
                        lineHeight = if (current.text.length > 260) 40.sp else 46.sp,
                        color = colors.mushafText,
                        textAlign = TextAlign.Center
                    )
                }

                // Tactile Count Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isDone) colors.accentFocus.copy(alpha = 0.14f) else colors.accentWarmSoft)
                        .border(
                            1.dp,
                            if (isDone) colors.accentFocus else colors.accentWarm,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isDone) colors.accentFocus else colors.accentWarm)
                    )
                    Text(
                        text = if (isDone) strings.completedAlhamdulillah
                        else "${current.count.formatDigits(strings.isArabic)} / ${current.target.formatDigits(strings.isArabic)}  •  ${strings.tapToCount}",
                        fontFamily = appFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDone) colors.accentFocus else colors.accentWarm
                    )
                }
            }
        }

        // Bottom Prev / Next Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .clickable(enabled = idx > 0) { onSelectIndex(idx - 1) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    strings.prevItem,
                    fontFamily = appFont,
                    fontSize = 13.sp,
                    color = if (idx > 0) colors.textPrimary else colors.textMuted
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .clickable(enabled = idx < list.size - 1) { onSelectIndex(idx + 1) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    strings.nextItem,
                    fontFamily = appFont,
                    fontSize = 13.sp,
                    color = if (idx < list.size - 1) colors.textPrimary else colors.textMuted
                )
            }
        }
    }
}
