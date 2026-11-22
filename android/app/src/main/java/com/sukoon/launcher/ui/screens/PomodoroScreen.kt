package com.sukoon.launcher.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import java.util.Locale

@Composable
fun PomodoroScreen(
    state: LauncherState,
    totalSeconds: Int,
    secondsLeft: Int,
    modeKey: String,
    isRunning: Boolean,
    onSelectMode: (Int, String) -> Unit,
    onToggleStart: () -> Unit,
    onReset: () -> Unit,
    onTaskChange: (String) -> Unit,
    onAutoFocusChange: (Boolean) -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val mins = secondsLeft / 60
    val secs = secondsLeft % 60
    val formatted = String.format(Locale.US, "%02d:%02d", mins, secs)
    val progress = (secondsLeft.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val modeLabel = when (modeKey) {
        "short" -> strings.shortBreakModeName
        "long" -> strings.longBreakModeName
        else -> strings.focusModeName
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(strings.deepSession, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                Text(strings.pomodoroTitle, fontFamily = appFont, fontSize = 22.sp, color = colors.textPrimary)
            }
            Text(
                text = strings.sessionsTodayFmt(state.pomoSessionsToday.formatDigits(strings.isArabic)),
                fontFamily = appFont,
                fontSize = 12.sp,
                color = colors.accentWarm
            )
        }

        // Mode Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                Triple(25, "focus", strings.focusChip),
                Triple(5, "short", strings.shortBreakChip),
                Triple(15, "long", strings.longBreakChip)
            ).forEach { (m, key, chipText) ->
                val active = modeKey == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (active) colors.accentWarmSoft else colors.background.copy(alpha = 0f))
                        .clickable { onSelectMode(m, key) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chipText,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                        color = if (active) colors.accentWarm else colors.textSecondary
                    )
                }
            }
        }

        // Circular Timer Ring
        Box(
            modifier = Modifier.size(216.dp),
            contentAlignment = Alignment.Center
        ) {
            val bgCol = colors.borderSubtle
            val fgCol = colors.accentWarm
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = bgCol,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawArc(
                    color = fgCol,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatted,
                    fontFamily = appFont,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = colors.textPrimary
                )
                Text(
                    text = if (isRunning) strings.runningFmt(modeLabel) else strings.readyFmt(modeLabel),
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Intention Input
        BasicTextField(
            value = state.pomoTask,
            onValueChange = onTaskChange,
            textStyle = TextStyle(
                fontFamily = appFont,
                fontSize = 14.sp,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(colors.accentWarm),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.pomoTask.isEmpty()) {
                        Text(strings.pomoTaskHint, fontFamily = appFont, fontSize = 13.sp, color = colors.textMuted)
                    }
                    inner()
                }
            }
        )

        // Start / Reset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.textPrimary)
                    .clickable { onToggleStart() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) strings.pauseSession else strings.startSession,
                    fontFamily = appFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.background
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    .clickable { onReset() }
                    .padding(horizontal = 20.dp, vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.reset,
                    fontFamily = appFont,
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Auto-Focus Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.autoFocusToggle,
                fontFamily = appFont,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Switch(
                checked = state.pomoAutoFocus,
                onCheckedChange = onAutoFocusChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.background,
                    checkedTrackColor = colors.accentWarm
                )
            )
        }
    }
}
