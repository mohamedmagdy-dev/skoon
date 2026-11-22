package com.sukoon.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors

@Composable
fun HabitsScreen(
    state: LauncherState,
    onToggleHabit: (Long) -> Unit,
    onAddHabit: (String) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onNewDay: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    var newHabitText by remember { mutableStateOf("") }

    val total = state.habits.size
    val doneCount = state.habits.count { it.done }
    val pct = if (total > 0) (doneCount * 100) / total else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(strings.repeatsDaily, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                Text(strings.dailyHabits, fontFamily = appFont, fontSize = 22.sp, color = colors.textPrimary)
            }
            SmallChip(strings.newDayBtn) { onNewDay() }
        }

        // Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.completedTodayFmt(
                        doneCount.formatDigits(strings.isArabic),
                        total.formatDigits(strings.isArabic)
                    ),
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "${pct.formatDigits(strings.isArabic)}${strings.percentSymbol}",
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.accentWarm
                )
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
                        .fillMaxWidth(if (total > 0) doneCount.toFloat() / total else 0f)
                        .fillMaxHeight()
                        .background(colors.accentWarm)
                )
            }
        }

        // Habits List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.habits, key = { it.id }) { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (habit.done) colors.accentWarmSoft else colors.surface)
                        .border(
                            1.dp,
                            if (habit.done) colors.accentWarm.copy(alpha = 0.35f) else colors.borderSubtle,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onToggleHabit(habit.id) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (habit.done) colors.accentWarm else colors.background)
                                .border(
                                    1.5.dp,
                                    if (habit.done) colors.accentWarm else colors.borderStrong,
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (habit.done) {
                                Text("✓", fontSize = 13.sp, color = colors.background, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = habit.text,
                                fontFamily = appFont,
                                fontSize = 15.sp,
                                color = if (habit.done) colors.textSecondary else colors.textPrimary,
                                textDecoration = if (habit.done) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Text(
                                text = strings.streakDaysFmt(habit.streak.formatDigits(strings.isArabic)),
                                fontFamily = appFont,
                                fontSize = 11.sp,
                                color = colors.accentWarm
                            )
                        }
                    }

                    Text(
                        text = "×",
                        fontSize = 18.sp,
                        color = colors.textMuted,
                        modifier = Modifier
                            .clickable { onDeleteHabit(habit.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Add Recurring Habit Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = newHabitText,
                onValueChange = { newHabitText = it },
                textStyle = TextStyle(fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accentWarm),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (newHabitText.isEmpty()) {
                            Text(strings.addHabitHint, fontFamily = appFont, fontSize = 13.sp, color = colors.textMuted)
                        }
                        inner()
                    }
                }
            )
            SmallChip(strings.add, active = true) {
                if (newHabitText.isNotBlank()) {
                    onAddHabit(newHabitText)
                    newHabitText = ""
                }
            }
        }
    }
}
