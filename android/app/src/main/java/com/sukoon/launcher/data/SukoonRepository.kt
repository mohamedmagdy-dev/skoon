package com.sukoon.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.Collator
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class SukoonRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val saveMutex = Mutex()

    private val stateFile: File
        get() = File(context.filesDir, "sukoon_state_v1.json")

    private val tempStateFile: File
        get() = File(context.filesDir, "sukoon_state_v1.json.tmp")

    @Volatile
    private var cachedQuranPages: List<QuranPage>? = null

    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()

    suspend fun loadAppIcon(packageName: String): ImageBitmap? {
        iconCache[packageName]?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                val size = 96
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, size, size)
                drawable.draw(canvas)
                val img = bitmap.asImageBitmap()
                iconCache[packageName] = img
                img
            } catch (_: Exception) {
                null
            }
        }
    }

    fun loadState(): LauncherState {
        val loaded = try {
            if (stateFile.exists()) {
                json.decodeFromString<LauncherState>(stateFile.readText())
            } else {
                LauncherState()
            }
        } catch (_: Exception) {
            LauncherState()
        }
        return applyDailyRollover(loaded)
    }

    suspend fun saveStateAsync(state: LauncherState) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            try {
                val encoded = json.encodeToString(state)
                tempStateFile.writeText(encoded)
                if (!tempStateFile.renameTo(stateFile)) {
                    stateFile.writeText(encoded)
                    tempStateFile.delete()
                }
            } catch (_: Exception) {
                // Ignore write error gracefully
            }
        }
    }

    private fun applyDailyRollover(state: LauncherState): LauncherState {
        val today = LocalDate.now().toString()
        var updated = state

        if (state.lastHabitDate.isNotEmpty() && state.lastHabitDate != today) {
            val rolledHabits = state.habits.map { h ->
                h.copy(
                    streak = if (h.done) h.streak + 1 else h.streak,
                    done = false
                )
            }
            updated = updated.copy(
                habits = rolledHabits,
                lastHabitDate = today,
                morningAzkar = AzkarData.defaultMorningAzkar(),
                eveningAzkar = AzkarData.defaultEveningAzkar(),
                morningAzkarIndex = 0,
                eveningAzkarIndex = 0
            )
        } else if (state.lastHabitDate.isEmpty()) {
            updated = updated.copy(lastHabitDate = today)
        }

        // Ensure "azkar" tab is present if migrating from older saved state
        if (updated.pageConfig.none { it.id == "azkar" }) {
            val list = updated.pageConfig.toMutableList()
            val qIdx = list.indexOfFirst { it.id == "quran" }
            val insertAt = if (qIdx != -1) qIdx + 1 else list.size
            list.add(insertAt, PageConfig("azkar", true))
            updated = updated.copy(pageConfig = list)
        }

        if (state.lastWirdDate.isNotEmpty() && state.lastWirdDate != today) {
            updated = updated.copy(
                wirdDoneToday = false,
                wirdPagesReadToday = 0,
                lastWirdDate = today,
                pomoSessionsToday = 0
            )
        } else if (state.lastWirdDate.isEmpty()) {
            updated = updated.copy(lastWirdDate = today)
        }

        return updated
    }

    suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
        val selfPkg = context.packageName
        val collator = Collator.getInstance(Locale.getDefault())

        resolved
            .mapNotNull { ri ->
                val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == selfPkg) return@mapNotNull null
                val label = ri.loadLabel(pm)?.toString()?.trim().orEmpty()
                if (label.isEmpty()) return@mapNotNull null
                AppInfo(label = label, packageName = pkg)
            }
            .distinctBy { it.packageName }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
    }

    fun pickInitialFavorites(apps: List<AppInfo>): List<String> {
        val preferredHints = listOf(
            "dialer", "phone", "contacts", "whatsapp", "messaging", "mms",
            "camera", "maps", "calendar", "chrome", "youtube"
        )
        val picked = mutableListOf<String>()
        for (hint in preferredHints) {
            val match = apps.firstOrNull {
                it.packageName.lowercase().contains(hint) && it.packageName !in picked
            }
            if (match != null) picked.add(match.packageName)
            if (picked.size >= 7) break
        }
        if (picked.size < 5) {
            apps.take(6).forEach {
                if (it.packageName !in picked) picked.add(it.packageName)
            }
        }
        return picked
    }

    suspend fun loadQuranPages(): List<QuranPage> = withContext(Dispatchers.IO) {
        cachedQuranPages?.let { return@withContext it }
        val text = context.assets.open("quran_pages.json").bufferedReader().use { it.readText() }
        val pages = json.decodeFromString<List<QuranPage>>(text)
        cachedQuranPages = pages
        pages
    }

    fun launchApp(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(launchIntent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
