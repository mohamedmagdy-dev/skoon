package com.sukoon.launcher.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sukoon.launcher.MainActivity
import com.sukoon.launcher.R
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.PrayerTimesCalculator
import com.sukoon.launcher.data.SukoonRepository
import com.sukoon.launcher.data.formatDigits

object PrayerReminderScheduler {
    // v2 channel guarantees fresh audible sound & vibration configuration on Android 8+
    const val CHANNEL_ID = "sukoon_prayer_reminders_v2"
    private const val REQUEST_CODE = 7701

    const val EXTRA_PRAYER_NAME_AR = "prayer_name_ar"
    const val EXTRA_PRAYER_NAME_EN = "prayer_name_en"
    const val EXTRA_MINUTES_BEFORE = "minutes_before"
    const val EXTRA_LANG = "lang"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "مواقيت الصلاة • Prayer Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Audible reminders before prayer times"
                setSound(soundUri, audioAttrs)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
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

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notif = NotificationCompat.Builder(context, PrayerReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(openPi)
            .build()

        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(7702, notif)
        } catch (_: SecurityException) {
        }

        try {
            val repo = SukoonRepository(context)
            val state = repo.loadState()
            if (state.prayerReminderEnabled) {
                PrayerReminderScheduler.scheduleNext(context, state)
            }
        } catch (_: Exception) {
        }
    }
}

data class MuezzinOption(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val rawResId: Int?
) {
    fun localizedName(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
}

val MUEZZIN_OPTIONS: List<MuezzinOption> = listOf(
    MuezzinOption("makkah", "أذان مكة المكرمة", "Makkah Adhan", R.raw.adhan_makkah),
    MuezzinOption("madinah", "أذان المدينة المنورة", "Madinah Adhan", R.raw.adhan_madinah),
    MuezzinOption("alafasy", "مشاري العفاسي", "Mishary Alafasy", R.raw.adhan_alafasy),
    MuezzinOption("egypt", "الأذان المصري", "Egyptian Adhan", R.raw.adhan_egypt),
    MuezzinOption("chime", "نغمة تنبيه هادئة", "Gentle Chime", null)
)

fun muezzinOptionFor(id: String): MuezzinOption =
    MUEZZIN_OPTIONS.firstOrNull { it.id == id } ?: MUEZZIN_OPTIONS.first()

object AdhanScheduler {
    private const val REQUEST_CODE = 7711
    const val EXTRA_MUEZZIN_ID = "muezzin_id"

    fun channelIdFor(muezzinId: String): String = "sukoon_adhan_${muezzinId}_v1"

    fun soundUriFor(context: Context, muezzinId: String): Uri {
        val opt = muezzinOptionFor(muezzinId)
        return if (opt.rawResId != null) {
            Uri.parse("android.resource://${context.packageName}/${opt.rawResId}")
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    fun ensureChannel(context: Context, muezzinId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val opt = muezzinOptionFor(muezzinId)
            val soundUri = soundUriFor(context, muezzinId)
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                channelIdFor(muezzinId),
                "الأذان • ${opt.nameAr}",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Adhan alert at exact prayer time"
                setSound(soundUri, audioAttrs)
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AdhanReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    fun scheduleNext(context: Context, state: LauncherState) {
        if (!state.adhanEnabled) {
            cancel(context)
            return
        }
        val muezzinId = muezzinOptionFor(state.adhanMuezzinId).id
        ensureChannel(context, muezzinId)

        val now = System.currentTimeMillis()
        val info = PrayerTimesCalculator.getNextPrayerForState(state, now)
        val triggerAt = now + info.remainingMinutes.coerceAtLeast(1) * 60_000L

        val intent = Intent(context, AdhanReceiver::class.java).apply {
            putExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_AR, info.nextPrayer.nameAr)
            putExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_EN, info.nextPrayer.nameEn)
            putExtra(PrayerReminderScheduler.EXTRA_LANG, state.appLanguage)
            putExtra(EXTRA_MUEZZIN_ID, muezzinId)
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
        }
    }
}

class AdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val lang = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_LANG) ?: "ar"
        val isAr = lang != "en"
        val prayerAr = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_AR) ?: "الصلاة"
        val prayerEn = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_EN) ?: "Prayer"
        val muezzinId = intent?.getStringExtra(AdhanScheduler.EXTRA_MUEZZIN_ID) ?: "makkah"

        AdhanScheduler.ensureChannel(context, muezzinId)
        val soundUri = AdhanScheduler.soundUriFor(context, muezzinId)

        val title = if (isAr) "حان الآن موعد أذان $prayerAr" else "It is time for $prayerEn"
        val body = if (isAr) "حيّ على الصلاة — حيّ على الفلاح" else "Hayya 'alas-Salah — Come to prayer"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, AdhanScheduler.channelIdFor(muezzinId))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(openPi)
            .build()

        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(7712, notif)
        } catch (_: SecurityException) {
        }

        try {
            val repo = SukoonRepository(context)
            val state = repo.loadState()
            if (state.adhanEnabled) {
                AdhanScheduler.scheduleNext(context, state)
            }
        } catch (_: Exception) {
        }
    }
}
