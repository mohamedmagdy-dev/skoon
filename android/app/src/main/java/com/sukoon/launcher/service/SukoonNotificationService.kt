package com.sukoon.launcher.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.Executors

@Serializable
data class CapturedNotification(
    val id: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val timestampMillis: Long
)

/**
 * Official Android NotificationListenerService for Sukoon Focus Mode.
 * When Focus Mode is active:
 * 1) Switches interruption filter to INTERRUPTION_FILTER_ALARMS so ZERO heads-up banners,
 *    sounds, vibrations, or status-bar cues appear for incoming messages (WhatsApp, social, etc.).
 * 2) Immediately cancels incoming non-critical notifications from the status bar & shade
 *    before saving them asynchronously into Sukoon's dedicated Focus Inbox tab.
 * 3) Restores the previous interruption filter the instant Focus Mode is turned off.
 */
class SukoonNotificationService : NotificationListenerService() {

    companion object {
        private const val PREFS_NAME = "sukoon_focus_prefs"
        private const val KEY_FOCUS_ACTIVE = "focus_mode_active"
        private const val STORE_FILE = "sukoon_captured_notifs_v1.json"

        private val ioExecutor = Executors.newSingleThreadExecutor()
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        @Volatile
        private var activeService: SukoonNotificationService? = null

        @Volatile
        private var savedPreviousFilter: Int? = null

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _captured = MutableStateFlow<List<CapturedNotification>>(emptyList())
        val captured: StateFlow<List<CapturedNotification>> = _captured.asStateFlow()

        fun setFocusModeFlag(context: Context, enabled: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_FOCUS_ACTIVE, enabled)
                .apply()
            activeService?.applyFocusFilter(enabled)
        }

        fun isFocusModeFlagActive(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_ACTIVE, false)

        fun isPermissionGranted(context: Context): Boolean {
            return try {
                val enabled = Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                ) ?: return false
                val cn = ComponentName(context, SukoonNotificationService::class.java)
                enabled.contains(cn.flattenToString()) || enabled.contains(context.packageName)
            } catch (_: Exception) {
                false
            }
        }

        fun loadPersisted(context: Context) {
            ioExecutor.execute {
                try {
                    val f = File(context.filesDir, STORE_FILE)
                    if (f.exists()) {
                        _captured.value = json.decodeFromString(f.readText())
                    }
                } catch (_: Exception) {
                }
            }
        }

        private fun persistAsync(context: Context, list: List<CapturedNotification>) {
            val appCtx = context.applicationContext
            ioExecutor.execute {
                try {
                    File(appCtx.filesDir, STORE_FILE).writeText(json.encodeToString(list))
                } catch (_: Exception) {
                }
            }
        }

        fun removeItem(context: Context, id: String) {
            val next = _captured.value.filterNot { it.id == id }
            _captured.value = next
            persistAsync(context, next)
        }

        fun clearAll(context: Context) {
            _captured.value = emptyList()
            persistAsync(context, emptyList())
        }
    }

    private fun applyFocusFilter(enabled: Boolean) {
        try {
            if (enabled) {
                if (savedPreviousFilter == null) {
                    val cur = currentInterruptionFilter
                    savedPreviousFilter = if (cur == INTERRUPTION_FILTER_UNKNOWN) INTERRUPTION_FILTER_ALL else cur
                }
                // Silences all heads-up popups, sounds, vibrations & status-bar alerts while allowing alarms
                requestInterruptionFilter(INTERRUPTION_FILTER_ALARMS)
            } else {
                val restore = savedPreviousFilter ?: INTERRUPTION_FILTER_ALL
                savedPreviousFilter = null
                requestInterruptionFilter(restore)
            }
        } catch (_: Exception) {
            // Gracefully ignore if OEM restricts interruption filter
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeService = this
        _isConnected.value = true
        loadPersisted(applicationContext)
        if (isFocusModeFlagActive(applicationContext)) {
            applyFocusFilter(true)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (savedPreviousFilter != null) {
            applyFocusFilter(false)
        }
        activeService = null
        _isConnected.value = false
    }

    override fun onDestroy() {
        if (savedPreviousFilter != null) {
            applyFocusFilter(false)
        }
        activeService = null
        _isConnected.value = false
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (!isFocusModeFlagActive(applicationContext)) return

        val pkg = sbn.packageName ?: return
        // Never intercept Sukoon's own notifications (e.g. prayer reminders) or core system UI
        if (pkg == applicationContext.packageName || pkg == "android" || pkg == "com.android.systemui") return

        // Never block ongoing notifications (active calls, music, timers, navigation) or calls/alarms
        if (sbn.isOngoing) return
        val notif = sbn.notification ?: return
        val category = notif.category
        if (category == Notification.CATEGORY_CALL || category == Notification.CATEGORY_ALARM) return

        // 1. Cancel from status bar & shade IMMEDIATELY before any slow work!
        try {
            cancelNotification(sbn.key)
            // For grouped apps like WhatsApp, also clear any companion summary notification for this package
            activeNotifications?.forEach { active ->
                if (active.packageName == pkg && !active.isOngoing && active.key != sbn.key) {
                    val isSummary = (active.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
                    if (isSummary) {
                        cancelNotification(active.key)
                    }
                }
            }
        } catch (_: Exception) {
        }

        // Skip storing empty group summary rows in our inbox
        if ((notif.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val extras = notif.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = (extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras?.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.trim().orEmpty()

        if (title.isEmpty() && text.isEmpty()) return

        val appLabel = try {
            val pm = applicationContext.packageManager
            val ai = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(ai)?.toString()?.trim().orEmpty().ifEmpty { pkg }
        } catch (_: Exception) {
            pkg
        }

        val now = sbn.postTime.takeIf { it > 0L } ?: System.currentTimeMillis()
        val existing = _captured.value

        // Deduplicate rapid identical updates within 5 seconds
        val isDuplicate = existing.firstOrNull()?.let { last ->
            last.packageName == pkg &&
                last.title == title &&
                last.text == text &&
                kotlin.math.abs(now - last.timestampMillis) < 5_000L
        } == true

        if (!isDuplicate) {
            val item = CapturedNotification(
                id = "${sbn.key}_$now",
                packageName = pkg,
                appLabel = appLabel,
                title = title.ifEmpty { appLabel },
                text = text,
                timestampMillis = now
            )
            val updated = (listOf(item) + existing).take(150)
            _captured.value = updated
            persistAsync(applicationContext, updated)
        }
    }
}
