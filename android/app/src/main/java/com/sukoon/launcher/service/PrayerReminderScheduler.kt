package com.sukoon.launcher.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sukoon.launcher.MainActivity
import com.sukoon.launcher.R
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.PrayerTimesCalculator
import com.sukoon.launcher.data.SukoonRepository
import com.sukoon.launcher.data.formatDigits

object PrayerReminderScheduler {
    const val CHANNEL_ID = "sukoon_prayer_reminders"
    private const val REQUEST_CODE = 7701

    const val EXTRA_PRAYER_NAME_AR = "prayer_name_ar"
    const val EXTRA_PRAYER_NAME_EN = "prayer_name_en"
    const val EXTRA_MINUTES_BEFORE = "minutes_before"
    const val EXTRA_LANG = "lang"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مواقيت الصلاة • Prayer Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle reminders before prayer times"
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    fun scheduleNext(context: Context, state: LauncherState) {
        if (!state.prayerReminderEnabled) {
            cancel(context)
            return
        }
        ensureChannel(context)

        val now = System.currentTimeMillis()
        val leadMin = state.prayerReminderMinutesBefore.coerceIn(1, 60)
        val info = PrayerTimesCalculator.getNextPrayerForState(state, now)

        // If the trigger time for the immediate next prayer is already in the past, look past it
        var triggerAt = now + (info.remainingMinutes - leadMin) * 60_000L
        var targetPrayer = info.nextPrayer

        if (triggerAt <= now + 15_000L) {
            val afterNextPrayerMillis = now + (info.remainingMinutes + 2) * 60_000L
            val following = PrayerTimesCalculator.getNextPrayerForState(state, afterNextPrayerMillis)
            targetPrayer = following.nextPrayer
            triggerAt = afterNextPrayerMillis + (following.remainingMinutes - leadMin) * 60_000L
        }

        val intent = Intent(context, PrayerReminderReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME_AR, targetPrayer.nameAr)
            putExtra(EXTRA_PRAYER_NAME_EN, targetPrayer.nameEn)
            putExtra(EXTRA_MINUTES_BEFORE, leadMin)
            putExtra(EXTRA_LANG, state.appLanguage)
        }

        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } catch (_: Exception) {
            // Gracefully ignore on restricted devices
        }
    }
}

class PrayerReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        PrayerReminderScheduler.ensureChannel(context)
        val lang = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_LANG) ?: "ar"
        val isAr = lang != "en"
        val prayerAr = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_AR) ?: "الصلاة"
        val prayerEn = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_EN) ?: "Prayer"
        val mins = intent?.getIntExtra(PrayerReminderScheduler.EXTRA_MINUTES_BEFORE, 10) ?: 10

        val title = if (isAr) "اقتربت صلاة $prayerAr" else "$prayerEn prayer is approaching"
        val body = if (isAr) {
            "متبقي ${mins.formatDigits(true)} دقائق على أذان $prayerAr — استعد بالوضوء والسكينة"
        } else {
            "$mins minutes remaining until $prayerEn — prepare calmly"
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, PrayerReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPi)
            .build()

        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(7702, notif)
        } catch (_: SecurityException) {
            // Notification permission revoked
        }

        // Schedule the subsequent prayer reminder
        try {
            val repo = SukoonRepository(context)
            val state = repo.loadState()
            if (state.prayerReminderEnabled) {
                PrayerReminderScheduler.scheduleNext(context, state)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
