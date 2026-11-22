package com.sukoon.launcher.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.ui.theme.AmiriFont
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors

@Composable
fun DhikrScreen(
    state: LauncherState,
    onSelectDhikr: (String) -> Unit,
    onIncrement: () -> Unit,
    onAdjustTarget: (Int) -> Unit,
    onReset: () -> Unit,
    onAddDhikr: (String, Int) -> Unit,
    onDeleteActive: () -> Unit,
    onEnterBlackScreenMode: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val active = state.adhkar.find { it.id == state.activeDhikrId } ?: state.adhkar.firstOrNull()
    var showAddForm by remember { mutableStateOf(false) }
    var newText by remember { mutableStateOf("") }
    var newTarget by remember { mutableStateOf("33") }

    val progressTarget = if (active != null && active.target > 0) {
        (active.count.toFloat() / active.target.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressTarget, label = "dhikrProgress")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(strings.serenityAndDhikr, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                Text(strings.adhkarAndTasbih, fontFamily = appFont, fontSize = 22.sp, color = colors.textPrimary)
            }
            SmallChip(strings.addDhikrBtn, active = true) { showAddForm = !showAddForm }
        }

        // Pure-Black "Screen-Off" Tasbih Mode Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.accentWarmSoft)
                .border(1.dp, colors.accentWarm.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .clickable { onEnterBlackScreenMode() }
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
                Column {
                    Text(
                        text = strings.blackScreenTitle,
                        fontFamily = appFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = strings.blackScreenSubtitle,
                        fontFamily = appFont,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
            Text(
                text = strings.launchChevron,
                fontFamily = appFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.accentWarm
            )
        }

        // Add Custom Dhikr Form
        if (showAddForm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    textStyle = TextStyle(fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accentWarm),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.background)
                                .padding(horizontal = 10.dp, vertical = 9.dp)
                        ) {
                            if (newText.isEmpty()) {
                                Text(strings.dhikrTextHint, fontFamily = appFont, fontSize = 13.sp, color = colors.textMuted)
                            }
                            inner()
                        }
                    }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(strings.targetLabel, fontFamily = appFont, fontSize = 12.sp, color = colors.textSecondary)
                    BasicTextField(
                        value = newTarget,
                        onValueChange = { if (it.length <= 5 && it.all(Char::isDigit)) newTarget = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontFamily = appFont,
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(colors.accentWarm),
                        modifier = Modifier
                            .width(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.background)
                            .padding(vertical = 7.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    SmallChip(strings.cancel) { showAddForm = false }
                    SmallChip(strings.save, active = true) {
                        val t = newTarget.toIntOrNull() ?: 33
                        if (newText.isNotBlank()) {
                            onAddDhikr(newText, t)
                            newText = ""
                            newTarget = "33"
                            showAddForm = false
                        }
                    }
                }
            }
        }

        // Dhikr Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.adhkar.forEach { d ->
                val isSel = d.id == active?.id
                val isDone = d.count >= d.target
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSel) colors.accentWarmSoft else colors.surface)
                        .border(
                            1.dp,
                            when {
                                isSel -> colors.accentWarm
                                isDone -> colors.accentFocus.copy(alpha = 0.4f)
                                else -> colors.borderSubtle
                            },
                            RoundedCornerShape(50)
                        )
                        .clickable { onSelectDhikr(d.id) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "${d.short}${if (isDone) " ✓" else ""}",
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = when {
                            isSel -> colors.accentWarm
                            isDone -> colors.accentFocus
                            else -> colors.textSecondary
                        }
                    )
                }
            }
        }

        // Main Dhikr Stage + Full-Area Tap Counter
        if (active != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onIncrement() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = active.text,
                    fontFamily = AmiriFont,
                    fontSize = 30.sp,
                    lineHeight = 48.sp,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Tactile Ring Counter
                Box(
                    modifier = Modifier.size(168.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackCol = colors.borderSubtle
                    val progCol = if (active.count >= active.target) colors.accentFocus else colors.accentWarm
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = trackCol,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawArc(
                            color = progCol,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = active.count.formatDigits(strings.isArabic),
                            fontFamily = appFont,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Light,
                            color = progCol
                        )
                        Text(
                            text = strings.ofTargetFmt(active.target.formatDigits(strings.isArabic)),
                            fontFamily = appFont,
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Text(
                    text = strings.dhikrTapHint,
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Target Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallChip(strings.minusTarget) { onAdjustTarget(-1) }
                    SmallChip(strings.plusTarget) { onAdjustTarget(1) }
                    SmallChip(strings.zeroCounter) { onReset() }
                    if (state.adhkar.size > 1) {
                        SmallChip(strings.delete, danger = true) { onDeleteActive() }
                    }
                }
            }
        }
    }
}
