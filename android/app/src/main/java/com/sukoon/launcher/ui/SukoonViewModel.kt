package com.sukoon.launcher.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sukoon.launcher.data.*
import com.sukoon.launcher.service.CapturedNotification
import com.sukoon.launcher.service.PrayerReminderScheduler
import com.sukoon.launcher.service.SukoonAccessibilityService
import com.sukoon.launcher.service.SukoonNotificationService
import com.sukoon.launcher.ui.theme.fontOptionFor
import com.sukoon.launcher.ui.theme.stringsFor
import com.sukoon.launcher.ui.theme.themeOptionFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VolumeLevels(
    val media: Int = 5,
    val mediaMax: Int = 15,
    val ring: Int = 5,
    val ringMax: Int = 15,
    val alarm: Int = 5,
    val alarmMax: Int = 15
)

class SukoonViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SukoonRepository(app)
    private val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _state = MutableStateFlow(repo.loadState())
    val state: StateFlow<LauncherState> = _state.asStateFlow()

    private var saveJob: Job? = null

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _quranPages = MutableStateFlow<List<QuranPage>>(emptyList())
    val quranPages: StateFlow<List<QuranPage>> = _quranPages.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Volume state (especially helpful since user's physical volume buttons are broken!)
    private val _volumes = MutableStateFlow(readVolumes())
    val volumes: StateFlow<VolumeLevels> = _volumes.asStateFlow()

    // Mindful Pause state
    private val _mindfulApp = MutableStateFlow<AppInfo?>(null)
    val mindfulApp: StateFlow<AppInfo?> = _mindfulApp.asStateFlow()

    private val _mindfulSecondsLeft = MutableStateFlow(5)
    val mindfulSecondsLeft: StateFlow<Int> = _mindfulSecondsLeft.asStateFlow()
    private var mindfulJob: Job? = null

    // Pomodoro state
    private val _pomoTotalSeconds = MutableStateFlow(25 * 60)
    val pomoTotalSeconds: StateFlow<Int> = _pomoTotalSeconds.asStateFlow()

    private val _pomoSeconds = MutableStateFlow(25 * 60)
    val pomoSeconds: StateFlow<Int> = _pomoSeconds.asStateFlow()

    private val _pomoModeKey = MutableStateFlow("focus") // "focus" | "short" | "long"
    val pomoModeKey: StateFlow<String> = _pomoModeKey.asStateFlow()

    private val _pomoRunning = MutableStateFlow(false)
    val pomoRunning: StateFlow<Boolean> = _pomoRunning.asStateFlow()
    private var pomoJob: Job? = null

    private val strings get() = stringsFor(_state.value.appLanguage)

    // Current visible page ID (for fingerprint routing)
    var currentVisiblePageId: String = "home"

    // Accessibility / Fingerprint service status
    val isAccessibilityConnected = SukoonAccessibilityService.isServiceConnected
    val isFingerprintGestureAvailable = SukoonAccessibilityService.isFingerprintGestureAvailable

    // Focus Mode Notification Shield
    val capturedNotifications = SukoonNotificationService.captured
    private val _isNotificationAccessGranted = MutableStateFlow(SukoonNotificationService.isPermissionGranted(app))
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    private var toastJob: Job? = null

    init {
        SukoonNotificationService.setFocusModeFlag(app, _state.value.focusMode)
        SukoonNotificationService.loadPersisted(app)
        refreshApps()
        viewModelScope.launch {
            _quranPages.value = repo.loadQuranPages()
        }
        viewModelScope.launch {
            SukoonAccessibilityService.fingerprintEvents.collect {
                if (currentVisiblePageId == "dhikr") {
                    incrementDhikr()
                }
            }
        }
    }

    private fun updateState(transform: (LauncherState) -> LauncherState) {
        val next = transform(_state.value)
        _state.value = next
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(180)
            repo.saveStateAsync(next)
        }
    }

    fun flushPendingSave() {
        val snapshot = _state.value
        saveJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            withContext(NonCancellable) {
                repo.saveStateAsync(snapshot)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        flushPendingSave()
    }

    fun showToast(msg: String) {
        toastJob?.cancel()
        _toastMessage.value = msg
        toastJob = viewModelScope.launch {
            delay(2200)
            _toastMessage.value = null
        }
    }

    fun vibrateLight() {
        if (!_state.value.hapticsEnabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(25L)
            }
        } catch (_: Exception) {
        }
    }

    fun vibrateSuccess() {
        if (!_state.value.hapticsEnabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 45, 65, 75), -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 45, 65, 75), -1)
            }
        } catch (_: Exception) {
        }
    }

    suspend fun loadAppIcon(packageName: String): ImageBitmap? = repo.loadAppIcon(packageName)

    // ===== Apps =====
    fun refreshApps() {
        viewModelScope.launch {
            val apps = repo.loadInstalledApps()
            _installedApps.value = apps
            if (_state.value.favoritePackages.isEmpty() && apps.isNotEmpty()) {
                updateState { it.copy(favoritePackages = repo.pickInitialFavorites(apps)) }
            }
        }
    }

    fun onAppClick(app: AppInfo) {
        if (app.packageName in _state.value.distractingPackages) {
            startMindfulPause(app)
        } else {
            repo.launchApp(app.packageName)
        }
    }

    fun toggleFavorite(packageName: String, label: String) {
        val exists = packageName in _state.value.favoritePackages
        updateState { s ->
            val list = if (exists) s.favoritePackages - packageName else s.favoritePackages + packageName
            s.copy(favoritePackages = list)
        }
        showToast(if (exists) strings.toastRemovedFavFmt(label) else strings.toastAddedFavFmt(label))
    }

    fun toggleDistracting(packageName: String, label: String) {
        val exists = packageName in _state.value.distractingPackages
        updateState { s ->
            val list = if (exists) s.distractingPackages - packageName else s.distractingPackages + packageName
            s.copy(distractingPackages = list)
        }
        showToast(if (exists) strings.toastDisabledMindfulFmt(label) else strings.toastEnabledMindfulFmt(label))
    }

    private fun startMindfulPause(app: AppInfo) {
        mindfulJob?.cancel()
        val duration = _state.value.mindfulPauseSeconds.coerceIn(3, 15)
        _mindfulApp.value = app
        _mindfulSecondsLeft.value = duration
        mindfulJob = viewModelScope.launch {
            for (sec in (duration - 1) downTo 0) {
                delay(1000)
                _mindfulSecondsLeft.value = sec
            }
        }
    }

    fun dismissMindfulPause() {
        mindfulJob?.cancel()
        _mindfulApp.value = null
    }

    fun proceedMindfulApp() {
        val app = _mindfulApp.value ?: return
        dismissMindfulPause()
        repo.launchApp(app.packageName)
    }

    // ===== Theme, Language, Font, Prayer City & Focus =====
    fun setDarkTheme(dark: Boolean) {
        updateState { it.copy(darkTheme = dark, appThemeId = if (dark) "dark_oled" else "light_paper") }
    }

    fun setAppTheme(themeId: String) {
        val opt = themeOptionFor(themeId)
        updateState { it.copy(appThemeId = opt.id, darkTheme = opt.isDark) }
    }

    fun setAppFontWeight(weightId: String) {
        val normalized = when (weightId) {
            "light", "bold" -> weightId
            else -> "normal"
        }
        updateState { it.copy(appFontWeight = normalized) }
    }

    fun setAppLanguage(lang: String) {
        val normalized = if (lang == "en") "en" else "ar"
        if (_state.value.appLanguage == normalized) return
        updateState { it.copy(appLanguage = normalized) }
        showToast(stringsFor(normalized).toastLanguageUpdated)
    }

    fun setAppFont(fontId: String) {
        val option = fontOptionFor(fontId)
        updateState { it.copy(appFontId = option.id) }
        showToast(strings.toastFontUpdatedFmt(option.localizedName(strings.isArabic)))
    }

    fun setPrayerCity(cityId: String) {
        val city = PrayerTimesCalculator.findCity(cityId)
        updateState {
            it.copy(
                prayerCityId = city.id,
                useGpsLocation = false
            )
        }
        PrayerReminderScheduler.scheduleNext(getApplication(), _state.value)
        showToast(strings.toastCityUpdatedFmt(city.localizedName(strings.isArabic)))
    }

    fun setGpsLocation(lat: Double, lng: Double, label: String) {
        val nearest = PrayerTimesCalculator.findNearestCity(lat, lng)
        val cleanLabel = label.trim().ifEmpty { nearest.localizedName(strings.isArabic) }
        updateState {
            it.copy(
                prayerCityId = nearest.id,
                useGpsLocation = true,
                customLat = lat,
                customLng = lng,
                customLocationLabel = cleanLabel
            )
        }
        PrayerReminderScheduler.scheduleNext(getApplication(), _state.value)
        showToast(strings.toastLocationUpdatedFmt(cleanLabel))
    }

    fun setPrayerReminderEnabled(enabled: Boolean) {
        updateState { it.copy(prayerReminderEnabled = enabled) }
        val cur = _state.value
        PrayerReminderScheduler.scheduleNext(getApplication(), cur)
        showToast(
            if (enabled) strings.toastReminderOnFmt(cur.prayerReminderMinutesBefore.formatDigits(strings.isArabic))
            else strings.toastReminderOff
        )
    }

    fun setPrayerReminderMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(5, 60)
        updateState { it.copy(prayerReminderMinutesBefore = clamped) }
        val cur = _state.value
        if (cur.prayerReminderEnabled) {
            PrayerReminderScheduler.scheduleNext(getApplication(), cur)
            showToast(strings.toastReminderOnFmt(clamped.formatDigits(strings.isArabic)))
        }
    }

    fun syncPrayerReminderSchedule() {
        val cur = _state.value
        if (cur.prayerReminderEnabled) {
            PrayerReminderScheduler.scheduleNext(getApplication(), cur)
        }
    }

    fun setQuranLineHeight(multiplier: Float) {
        val clamped = multiplier.coerceIn(1.6f, 2.9f)
        updateState { it.copy(quranLineHeightMultiplier = clamped) }
    }

    fun setAppDisplayStyle(style: String) {
        val normalized = when (style) {
            "monochrome", "original" -> style
            else -> "text"
        }
        updateState { it.copy(appDisplayStyle = normalized) }
    }

    fun setMindfulPauseSeconds(seconds: Int) {
        updateState { it.copy(mindfulPauseSeconds = seconds.coerceIn(3, 15)) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        updateState { it.copy(hapticsEnabled = enabled) }
    }

    // ===== Onboarding & Reset =====
    fun setOnboardingStep(step: Int) {
        updateState { s ->
            s.copy(
                onboardingStep = step.coerceIn(0, 7),
                onboardingStatus = if (s.onboardingStatus == "not_started" && step > 0) "in_progress" else s.onboardingStatus
            )
        }
    }

    fun completeOnboarding() {
        updateState { it.copy(onboardingStatus = "completed", onboardingStep = 0) }
    }

    fun restartSetup() {
        updateState { it.copy(onboardingStatus = "in_progress", onboardingStep = 1) }
    }

    fun resetApp() {
        val apps = _installedApps.value
        val fresh = LauncherState(
            favoritePackages = if (apps.isNotEmpty()) repo.pickInitialFavorites(apps) else emptyList()
        )
        _state.value = fresh
        flushPendingSave()
    }

    fun toggleFocusMode(force: Boolean? = null) {
        val target = force ?: !_state.value.focusMode
        updateState { it.copy(focusMode = target) }
        SukoonNotificationService.setFocusModeFlag(getApplication(), target)
        showToast(if (target) strings.toastFocusEnabled else strings.toastFocusDisabled)
    }

    fun refreshNotificationAccess() {
        _isNotificationAccessGranted.value = SukoonNotificationService.isPermissionGranted(getApplication())
        SukoonNotificationService.setFocusModeFlag(getApplication(), _state.value.focusMode)
    }

    fun dismissCapturedNotification(id: String) {
        SukoonNotificationService.removeItem(getApplication(), id)
    }

    fun clearCapturedNotifications() {
        SukoonNotificationService.clearAll(getApplication())
    }

    fun openCapturedNotificationApp(item: CapturedNotification) {
        SukoonNotificationService.removeItem(getApplication(), item.id)
        repo.launchApp(item.packageName)
    }

    // ===== Page Ordering =====
    fun movePageConfig(fromIndex: Int, toIndex: Int) {
        val list = _state.value.pageConfig.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        updateState { it.copy(pageConfig = list) }
    }

    fun togglePageEnabled(pageId: String) {
        if (pageId == "home") return
        updateState { s ->
            s.copy(pageConfig = s.pageConfig.map {
                if (it.id == pageId) it.copy(enabled = !it.enabled) else it
            })
        }
    }

    // ===== Quran Wird =====
    fun goToQuranPage(pageNum: Int) {
        val clamped = pageNum.coerceIn(1, 604)
        val prev = _state.value.quranPage
        updateState { s ->
            val readInc = if (clamped > prev) (s.wirdPagesReadToday + 1).coerceAtMost(s.dailyWirdGoalPages) else s.wirdPagesReadToday
            s.copy(quranPage = clamped, wirdPagesReadToday = readInc)
        }
    }

    fun adjustQuranFont(delta: Float) {
        updateState { it.copy(quranFontSizeSp = (it.quranFontSizeSp + delta).coerceIn(16f, 34f)) }
    }

    fun toggleQuranPaperMode() {
        updateState { it.copy(quranPaperMode = !it.quranPaperMode) }
    }

    fun toggleWirdDoneToday() {
        updateState { s ->
            val nextDone = !s.wirdDoneToday
            val nextStreak = if (nextDone) s.wirdStreak + 1 else (s.wirdStreak - 1).coerceAtLeast(0)
            s.copy(
                wirdDoneToday = nextDone,
                wirdStreak = nextStreak,
                wirdPagesReadToday = if (nextDone) s.dailyWirdGoalPages else s.wirdPagesReadToday
            )
        }
        if (_state.value.wirdDoneToday) {
            vibrateSuccess()
            showToast(strings.toastWirdDone)
        }
    }

    // ===== Dhikr & Rear Fingerprint Counter =====
    fun selectDhikr(id: String) {
        updateState { it.copy(activeDhikrId = id) }
    }

    fun incrementDhikr() {
        val currentState = _state.value
        val active = currentState.adhkar.find { it.id == currentState.activeDhikrId }
            ?: currentState.adhkar.firstOrNull() ?: return

        if (active.count < active.target) {
            val newCount = active.count + 1
            val updatedList = currentState.adhkar.map {
                if (it.id == active.id) it.copy(count = newCount) else it
            }
            updateState { it.copy(adhkar = updatedList) }

            if (newCount >= active.target) {
                vibrateSuccess()
                showToast(strings.toastDhikrCompletedFmt(active.short))
                // Auto-advance to next unfinished dhikr
                val idx = updatedList.indexOfFirst { it.id == active.id }
                val next = updatedList.drop(idx + 1).firstOrNull { it.count < it.target }
                    ?: updatedList.firstOrNull { it.count < it.target }
                if (next != null && next.id != active.id) {
                    viewModelScope.launch {
                        delay(550)
                        updateState { it.copy(activeDhikrId = next.id) }
                    }
                }
            } else {
                vibrateLight()
            }
        } else {
            vibrateLight()
            showToast(strings.toastDhikrAlreadyDone)
        }
    }

    fun adjustActiveDhikrTarget(delta: Int) {
        val activeId = _state.value.activeDhikrId
        updateState { s ->
            s.copy(adhkar = s.adhkar.map { d ->
                if (d.id == activeId) {
                    val step = if (d.target >= 100 && delta > 0) 10 else if (d.target > 100 && delta < 0) -10 else delta
                    val newTarget = (d.target + step).coerceIn(1, 10000)
                    d.copy(target = newTarget, count = d.count.coerceAtMost(newTarget))
                } else d
            })
        }
    }

    fun resetActiveDhikr() {
        val activeId = _state.value.activeDhikrId
        updateState { s ->
            s.copy(adhkar = s.adhkar.map { if (it.id == activeId) it.copy(count = 0) else it })
        }
        showToast(strings.toastCounterReset)
    }

    fun addDhikr(text: String, target: Int) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val newItem = DhikrItem(
            id = "dhikr_${System.currentTimeMillis()}",
            text = clean,
            short = clean.split(" ").take(2).joinToString(" "),
            target = target.coerceIn(1, 10000),
            count = 0
        )
        updateState { it.copy(adhkar = it.adhkar + newItem, activeDhikrId = newItem.id) }
        showToast(strings.toastDhikrAdded)
    }

    fun deleteActiveDhikr() {
        val list = _state.value.adhkar
        if (list.size <= 1) return
        val remaining = list.filterNot { it.id == _state.value.activeDhikrId }
        updateState { it.copy(adhkar = remaining, activeDhikrId = remaining.first().id) }
        showToast(strings.toastDhikrDeleted)
    }

    // ===== Morning & Evening Azkar Tab =====
    fun selectAzkarIndex(isMorning: Boolean, index: Int) {
        updateState { s ->
            if (isMorning) s.copy(morningAzkarIndex = index.coerceIn(0, (s.morningAzkar.size - 1).coerceAtLeast(0)))
            else s.copy(eveningAzkarIndex = index.coerceIn(0, (s.eveningAzkar.size - 1).coerceAtLeast(0)))
        }
    }

    fun incrementAzkar(isMorning: Boolean) {
        val s = _state.value
        val list = if (isMorning) s.morningAzkar else s.eveningAzkar
        val idx = (if (isMorning) s.morningAzkarIndex else s.eveningAzkarIndex).coerceIn(0, (list.size - 1).coerceAtLeast(0))
        val item = list.getOrNull(idx) ?: return

        if (item.count < item.target) {
            val newCount = item.count + 1
            val updated = list.mapIndexed { i, z -> if (i == idx) z.copy(count = newCount) else z }
            updateState { cur ->
                if (isMorning) cur.copy(morningAzkar = updated) else cur.copy(eveningAzkar = updated)
            }
            if (newCount >= item.target) {
                vibrateSuccess()
                val nextIdx = (idx + 1 until updated.size).firstOrNull { updated[it].count < updated[it].target }
                    ?: updated.indexOfFirst { it.count < it.target }
                if (nextIdx != -1 && nextIdx != idx) {
                    viewModelScope.launch {
                        delay(450)
                        selectAzkarIndex(isMorning, nextIdx)
                    }
                } else if (updated.all { it.count >= it.target }) {
                    showToast(if (isMorning) strings.toastMorningAzkarDone else strings.toastEveningAzkarDone)
                }
            } else {
                vibrateLight()
            }
        } else {
            // Already finished this card -> jump to next unfinished
            val nextIdx = (idx + 1 until list.size).firstOrNull { list[it].count < list[it].target }
            if (nextIdx != null) {
                vibrateLight()
                selectAzkarIndex(isMorning, nextIdx)
            }
        }
    }

    fun resetAzkar(isMorning: Boolean) {
        updateState { s ->
            if (isMorning) s.copy(morningAzkar = AzkarData.defaultMorningAzkar(), morningAzkarIndex = 0)
            else s.copy(eveningAzkar = AzkarData.defaultEveningAzkar(), eveningAzkarIndex = 0)
        }
        showToast(if (isMorning) strings.toastMorningAzkarReset else strings.toastEveningAzkarReset)
    }

    // ===== Daily Recurring Habits =====
    fun toggleHabit(id: Long) {
        vibrateLight()
        updateState { s ->
            s.copy(habits = s.habits.map { if (it.id == id) it.copy(done = !it.done) else it })
        }
    }

    fun addHabit(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        updateState { s ->
            s.copy(habits = s.habits + HabitItem(System.currentTimeMillis(), clean, false, 0))
        }
    }

    fun deleteHabit(id: Long) {
        updateState { s -> s.copy(habits = s.habits.filterNot { it.id == id }) }
    }

    fun resetHabitsNewDay() {
        updateState { s ->
            s.copy(habits = s.habits.map {
                it.copy(streak = if (it.done) it.streak + 1 else it.streak, done = false)
            })
        }
        showToast(strings.toastNewDayHabits)
    }

    // ===== Notes =====
    fun createNote(): Long {
        val id = System.currentTimeMillis()
        val nowLabel = if (strings.isArabic) "الآن" else "Now"
        val note = NoteItem(id = id, title = "", body = "", pinned = false, updatedAt = nowLabel)
        updateState { s -> s.copy(notes = listOf(note) + s.notes) }
        return id
    }

    fun updateNote(id: Long, title: String, body: String) {
        val nowLabel = if (strings.isArabic) "الآن" else "Now"
        updateState { s ->
            s.copy(notes = s.notes.map {
                if (it.id == id) it.copy(title = title, body = body, updatedAt = nowLabel) else it
            })
        }
    }

    fun toggleNotePin(id: Long) {
        updateState { s ->
            s.copy(notes = s.notes.map { if (it.id == id) it.copy(pinned = !it.pinned) else it })
        }
    }

    fun deleteNote(id: Long) {
        updateState { s -> s.copy(notes = s.notes.filterNot { it.id == id }) }
        showToast(strings.toastNoteDeleted)
    }

    // ===== Pomodoro =====
    fun setPomoMode(minutes: Int, modeKey: String) {
        pomoJob?.cancel()
        _pomoRunning.value = false
        _pomoModeKey.value = modeKey
        _pomoTotalSeconds.value = minutes * 60
        _pomoSeconds.value = minutes * 60
    }

    fun togglePomo() {
        if (_pomoRunning.value) {
            pomoJob?.cancel()
            _pomoRunning.value = false
        } else {
            _pomoRunning.value = true
            if (_state.value.pomoAutoFocus && _pomoModeKey.value == "focus" && !_state.value.focusMode) {
                toggleFocusMode(true)
            }
            pomoJob = viewModelScope.launch {
                while (_pomoSeconds.value > 0) {
                    delay(1000)
                    _pomoSeconds.value -= 1
                }
                _pomoRunning.value = false
                if (_pomoModeKey.value == "focus") {
                    updateState { it.copy(pomoSessionsToday = it.pomoSessionsToday + 1) }
                }
                _pomoSeconds.value = _pomoTotalSeconds.value
                vibrateSuccess()
                showToast(strings.toastSessionDone)
            }
        }
    }

    fun resetPomo() {
        pomoJob?.cancel()
        _pomoRunning.value = false
        _pomoSeconds.value = _pomoTotalSeconds.value
    }

    fun setPomoTask(task: String) {
        updateState { it.copy(pomoTask = task) }
    }

    fun setPomoAutoFocus(enabled: Boolean) {
        updateState { it.copy(pomoAutoFocus = enabled) }
    }

    // ===== Quick Volume Control (for broken hardware volume buttons!) =====
    fun refreshVolumes() {
        _volumes.value = readVolumes()
    }

    private fun readVolumes(): VolumeLevels = VolumeLevels(
        media = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
        mediaMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1),
        ring = audioManager.getStreamVolume(AudioManager.STREAM_RING),
        ringMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING).coerceAtLeast(1),
        alarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM),
        alarmMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM).coerceAtLeast(1)
    )

    fun setStreamVolume(streamType: Int, value: Int) {
        try {
            val max = audioManager.getStreamMaxVolume(streamType)
            audioManager.setStreamVolume(streamType, value.coerceIn(0, max), 0)
            _volumes.value = readVolumes()
        } catch (_: Exception) {
            showToast(strings.toastVolumeError)
        }
    }

    fun toggleMuteAll() {
        val current = readVolumes()
        val mute = current.media > 0 || current.ring > 0
        setStreamVolume(AudioManager.STREAM_MUSIC, if (mute) 0 else (current.mediaMax / 2))
        setStreamVolume(AudioManager.STREAM_RING, if (mute) 0 else (current.ringMax / 2))
        showToast(if (mute) strings.toastMuted else strings.toastUnmuted)
    }
}
