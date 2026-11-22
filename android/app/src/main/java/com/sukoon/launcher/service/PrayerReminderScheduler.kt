package com.sukoon.launcher.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sukoon.launcher.MainActivity
import com.sukoon.launcher.R
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.PrayerTimesCalculator
import com.sukoon.launcher.data.SukoonRepository
import com.sukoon.launcher.data.formatDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val rawResId: Int
) {
    fun localizedName(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
}

val MUEZZIN_OPTIONS: List<MuezzinOption> = listOf(
    MuezzinOption("makkah", "أذان مكة المكرمة", "Makkah Adhan", R.raw.adhan_makkah),
    MuezzinOption("madinah", "أذان المدينة المنورة", "Madinah Adhan", R.raw.adhan_madinah),
    MuezzinOption("alafasy", "مشاري العفاسي", "Mishary Alafasy", R.raw.adhan_alafasy),
    MuezzinOption("egypt", "الأذان المصري", "Egyptian Adhan", R.raw.adhan_egypt)
)

fun muezzinOptionFor(id: String): MuezzinOption =
    MUEZZIN_OPTIONS.firstOrNull { it.id == id } ?: MUEZZIN_OPTIONS.first()

object AdhanPlaybackManager {
    private const val ADHAN_NOTIF_ID = 7712

    @Volatile
    private var player: MediaPlayer? = null

    @Volatile
    private var screenOffReceiver: BroadcastReceiver? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playingPrayerName = MutableStateFlow("")
    val playingPrayerName: StateFlow<String> = _playingPrayerName.asStateFlow()

    fun startAdhan(context: Context, muezzinId: String, prayerName: String) {
        val appCtx = context.applicationContext
        stopAdhan(appCtx)
        val opt = muezzinOptionFor(muezzinId)
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                val afd = appCtx.resources.openRawResourceFd(opt.rawResId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setOnCompletionListener { stopAdhan(appCtx) }
                setOnErrorListener { _, _, _ ->
                    stopAdhan(appCtx)
                    true
                }
                prepare()
                start()
            }
            player = mp
            _playingPrayerName.value = prayerName
            _isPlaying.value = true

            // Pressing the Power button (ACTION_SCREEN_OFF) stops the Adhan immediately
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                        stopAdhan(appCtx)
                    }
                }
            }
            screenOffReceiver = receiver
            ContextCompat.registerReceiver(
                appCtx,
                receiver,
                IntentFilter(Intent.ACTION_SCREEN_OFF),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {
            stopAdhan(appCtx)
        }
    }

    fun stopAdhan(context: Context) {
        val appCtx = context.applicationContext
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) {
        }
        player = null

        screenOffReceiver?.let { r ->
            try {
                appCtx.unregisterReceiver(r)
            } catch (_: Exception) {
            }
        }
        screenOffReceiver = null

        _isPlaying.value = false
        _playingPrayerName.value = ""

        try {
            val nm = appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(ADHAN_NOTIF_ID)
        } catch (_: Exception) {
        }
    }
}

object AdhanScheduler {
    private const val REQUEST_CODE = 7711
    const val CHANNEL_ID = "sukoon_adhan_alert_v2"
    const val EXTRA_MUEZZIN_ID = "muezzin_id"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "الأذان • Adhan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Adhan alert at exact prayer time"
                // Audio is played directly by AdhanPlaybackManager so Power button & Stop button silence it instantly
                setSound(null, null)
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
        ensureChannel(context)

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

class AdhanStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        AdhanPlaybackManager.stopAdhan(context)
    }
}

class AdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val lang = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_LANG) ?: "ar"
        val isAr = lang != "en"
        val prayerAr = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_AR) ?: "الصلاة"
        val prayerEn = intent?.getStringExtra(PrayerReminderScheduler.EXTRA_PRAYER_NAME_EN) ?: "Prayer"
        val muezzinId = intent?.getStringExtra(AdhanScheduler.EXTRA_MUEZZIN_ID) ?: "makkah"

        AdhanScheduler.ensureChannel(context)
        AdhanPlaybackManager.startAdhan(context, muezzinId, if (isAr) prayerAr else prayerEn)

        val title = if (isAr) "حان الآن موعد أذان $prayerAr" else "It is time for $prayerEn"
        val body = if (isAr) "حيّ على الصلاة — اضغط لإيقاف الأذان أو اضغط زر الباور" else "Come to prayer — tap Stop or press Power to silence"
        val stopLabel = if (isAr) "■ إيقاف الأذان" else "■ Stop Adhan"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AdhanStopReceiver::class.java)
        val stopPi = PendingIntent.getBroadcast(
            context,
            7713,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, AdhanScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentIntent(openPi)
            .setDeleteIntent(stopPi)
            .addAction(0, stopLabel, stopPi)
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

object PrayerAutoFocusScheduler {
    private const val START_REQUEST_CODE = 7721
    private const val END_REQUEST_CODE = 7722
    const val EXTRA_DURATION_MINUTES = "duration_minutes"

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val startPi = PendingIntent.getBroadcast(
            context,
            START_REQUEST_CODE,
            Intent(context, PrayerAutoFocusStartReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val endPi = PendingIntent.getBroadcast(
            context,
            END_REQUEST_CODE,
            Intent(context, PrayerAutoFocusEndReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(startPi)
        am.cancel(endPi)
    }

    fun scheduleNext(context: Context, state: LauncherState) {
        if (!state.prayerAutoFocusEnabled) {
            cancel(context)
            return
        }
        val now = System.currentTimeMillis()
        val info = PrayerTimesCalculator.getNextPrayerForState(state, now)
        val triggerAt = now + info.remainingMinutes.coerceAtLeast(1) * 60_000L

        val intent = Intent(context, PrayerAutoFocusStartReceiver::class.java).apply {
            putExtra(EXTRA_DURATION_MINUTES, state.prayerAutoFocusMinutes.coerceIn(10, 60))
        }
        val pi = PendingIntent.getBroadcast(
            context,
            START_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } catch (_: Exception) {
        }
    }

    fun scheduleEnd(context: Context, durationMinutes: Int) {
        val endAt = System.currentTimeMillis() + durationMinutes.coerceIn(5, 60) * 60_000L
        val pi = PendingIntent.getBroadcast(
            context,
            END_REQUEST_CODE,
            Intent(context, PrayerAutoFocusEndReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
        } catch (_: Exception) {
        }
    }
}

class PrayerAutoFocusStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val duration = intent?.getIntExtra(PrayerAutoFocusScheduler.EXTRA_DURATION_MINUTES, 20) ?: 20
        SukoonNotificationService.startPrayerAutoFocus(context, duration)
        PrayerAutoFocusScheduler.scheduleEnd(context, duration)

        try {
            val repo = SukoonRepository(context)
            val state = repo.loadState()
            if (state.prayerAutoFocusEnabled) {
                PrayerAutoFocusScheduler.scheduleNext(context, state)
            }
        } catch (_: Exception) {
        }
    }
}

class PrayerAutoFocusEndReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        SukoonNotificationService.endPrayerAutoFocus(context)
    }
}

object TimeBoxReminderScheduler {
    const val CHANNEL_ID = "sukoon_timebox_reminders_v1"
    private const val REQUEST_CODE = 7731
    const val EXTRA_APP_LABEL = "app_label"
    const val EXTRA_MINUTES = "minutes"
    const val EXTRA_LANG = "lang"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تذكير وقت الاستخدام • Mindful Time-Box",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle reminders when your chosen app duration ends"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, appLabel: String, minutes: Int, lang: String) {
        ensureChannel(context)
        val triggerAt = System.currentTimeMillis() + minutes.coerceIn(1, 60) * 60_000L
        val intent = Intent(context, TimeBoxReminderReceiver::class.java).apply {
            putExtra(EXTRA_APP_LABEL, appLabel)
            putExtra(EXTRA_MINUTES, minutes)
            putExtra(EXTRA_LANG, lang)
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

class TimeBoxReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        TimeBoxReminderScheduler.ensureChannel(context)
        val appLabel = intent?.getStringExtra(TimeBoxReminderScheduler.EXTRA_APP_LABEL) ?: "App"
        val mins = intent?.getIntExtra(TimeBoxReminderScheduler.EXTRA_MINUTES, 5) ?: 5
        val isAr = (intent?.getStringExtra(TimeBoxReminderScheduler.EXTRA_LANG) ?: "ar") != "en"

        val title = if (isAr) "انتهت مدة الـ ${mins.formatDigits(true)} دقائق" else "${mins}m Mindful Timer Finished"
        val body = if (isAr) {
            "قضيت وقتك المحدد في $appLabel — اضغط للعودة إلى سكونك"
        } else {
            "Your planned time in $appLabel is up — tap to return to Sukoon"
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context,
            2,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, TimeBoxReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPi)
            .build()

        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(7732, notif)
        } catch (_: SecurityException) {
        }
    }
}
