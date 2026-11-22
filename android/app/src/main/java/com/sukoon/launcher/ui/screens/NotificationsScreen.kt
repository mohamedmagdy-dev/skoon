package com.sukoon.launcher.ui.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.service.CapturedNotification
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    state: LauncherState,
    notifications: List<CapturedNotification>,
    isNotificationAccessGranted: Boolean,
    onLoadAppIcon: suspend (String) -> ImageBitmap?,
    onOpenNotificationAccessSettings: () -> Unit,
    onToggleFocus: () -> Unit,
    onOpenNotificationApp: (CapturedNotification) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.US) }

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
                Text(strings.notifTabEyebrow, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                Text(strings.notifTabTitle, fontFamily = appFont, fontSize = 22.sp, color = colors.textPrimary)
            }
            if (notifications.isNotEmpty()) {
                SmallChip(strings.notifClearAll) { onClearAll() }
            }
        }

        // Permission Prompt Card (only if Notification Access is not yet granted)
        if (!isNotificationAccessGranted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accentWarmSoft)
                    .border(1.dp, colors.accentWarm.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = strings.notifPermTitle,
                        fontFamily = appFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = strings.notifPermSub,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.textPrimary)
                        .clickable { onOpenNotificationAccessSettings() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.notifPermGrantBtn,
                        fontFamily = appFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.background
                    )
                }
            }
        } else {
            // Focus Shield Live Status Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(
                        1.dp,
                        if (state.focusMode) colors.accentFocus.copy(alpha = 0.45f) else colors.borderSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleFocus() }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (state.focusMode) colors.accentFocus else colors.textMuted)
                )
                Text(
                    text = if (state.focusMode) strings.notifShieldActive else strings.notifShieldPaused,
                    fontFamily = appFont,
                    fontSize = 12.sp,
                    color = if (state.focusMode) colors.textPrimary else colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Captured Notifications List
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.notifEmptyTitle,
                        fontFamily = appFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        color = colors.textPrimary
                    )
                    Text(
                        text = strings.notifEmptySub,
                        fontFamily = appFont,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    val timeText = remember(item.timestampMillis, strings.isArabic) {
                        timeFormatter.format(Date(item.timestampMillis)).formatDigits(strings.isArabic)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                            .clickable { onOpenNotificationApp(item) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppIconView(
                            packageName = item.packageName,
                            displayStyle = state.appDisplayStyle,
                            size = 34.dp,
                            loadIcon = onLoadAppIcon
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.appLabel,
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    color = colors.accentWarm,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = timeText,
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                            if (item.title.isNotEmpty() && item.title != item.appLabel) {
                                Text(
                                    text = item.title,
                                    fontFamily = appFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (item.text.isNotEmpty()) {
                                Text(
                                    text = item.text,
                                    fontFamily = appFont,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = colors.textSecondary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Text(
                            text = "×",
                            fontSize = 18.sp,
                            color = colors.textMuted,
                            modifier = Modifier
                                .clickable { onDismissNotification(item.id) }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
