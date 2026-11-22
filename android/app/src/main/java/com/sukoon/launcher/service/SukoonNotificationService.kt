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
 * When Focus Mode is active, intercepts non-critical notifications, cancels them
 * from the status bar so there are zero visual cues/banners, and stores them in
 * Sukoon's dedicated Notifications tab for intentional review.
 */
class SukoonNotificationService : NotificationListenerService() {

    companion object {
        private const val PREFS_NAME = "sukoon_focus_prefs"
        private const val KEY_FOCUS_ACTIVE = "focus_mode_active"
        private const val STORE_FILE = "sukoon_captured_notifs_v1.json"

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _captured = MutableStateFlow<List<CapturedNotification>>(emptyList())
        val captured: StateFlow<List<CapturedNotification>> = _captured.asStateFlow()

        fun setFocusModeFlag(context: Context, enabled: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_FOCUS_ACTIVE, enabled)
                .apply()
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
            try {
                val f = File(context.filesDir, STORE_FILE)
                if (f.exists()) {
                    _captured.value = json.decodeFromString(f.readText())
                }
            } catch (_: Exception) {
            }
        }

        private fun persist(context: Context, list: List<CapturedNotification>) {
            try {
                File(context.filesDir, STORE_FILE).writeText(json.encodeToString(list))
            } catch (_: Exception) {
            }
        }

        fun removeItem(context: Context, id: String) {
            val next = _captured.value.filterNot { it.id == id }
            _captured.value = next
            persist(context, next)
        }

        fun clearAll(context: Context) {
            _captured.value = emptyList()
            persist(context, emptyList())
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isConnected.value = true
        loadPersisted(applicationContext)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isConnected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (!isFocusModeFlagActive(applicationContext)) return

        val pkg = sbn.packageName ?: return
        // Never intercept Sukoon's own notifications (e.g. prayer reminders) or system UI
        if (pkg == applicationContext.packageName || pkg == "android" || pkg == "com.android.systemui") return

        // Never block ongoing notifications (active calls, music, timers, navigation) or calls/alarms
        if (sbn.isOngoing) return
        val notif = sbn.notification ?: return
        if ((notif.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            // Silently cancel group summary too so no orphan icon stays in status bar
            try {
                cancelNotification(sbn.key)
            } catch (_: Exception) {
            }
            return
        }
        val category = notif.category
        if (category == Notification.CATEGORY_CALL || category == Notification.CATEGORY_ALARM) return

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
            persist(applicationContext, updated)
        }

        // Remove from Android status bar so ZERO notification icons or cues appear in Focus Mode
        try {
            cancelNotification(sbn.key)
        } catch (_: Exception) {
        }
    }
}
