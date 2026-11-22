package com.sukoon.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sukoon.launcher.data.PAGE_META_MAP
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.ui.SukoonViewModel
import com.sukoon.launcher.ui.screens.*
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import com.sukoon.launcher.ui.theme.SukoonTheme
import com.sukoon.launcher.ui.theme.stringsFor
import com.sukoon.launcher.ui.theme.themeOptionFor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SukoonViewModel by viewModels()

    private val isLocatingState = mutableStateOf(false)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentLocation()
        } else {
            isLocatingState.value = false
            viewModel.showToast(stringsFor(viewModel.state.value.appLanguage).toastLocationFailed)
        }
    }

    private var pendingNotifAction: String? = null // "reminder" | "adhan"

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (pendingNotifAction == "adhan") viewModel.setAdhanEnabled(true)
            else viewModel.setPrayerReminderEnabled(true)
        } else {
            viewModel.showToast(stringsFor(viewModel.state.value.appLanguage).toastNotifPermissionDenied)
        }
        pendingNotifAction = null
    }

    private fun requestUserLocation() {
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            fetchCurrentLocation()
        } else {
            isLocatingState.value = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun togglePrayerReminderWithPermission(enable: Boolean) {
        if (!enable) {
            viewModel.setPrayerReminderEnabled(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                pendingNotifAction = "reminder"
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.setPrayerReminderEnabled(true)
    }

    private fun toggleAdhanWithPermission(enable: Boolean) {
        if (!enable) {
            viewModel.setAdhanEnabled(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                pendingNotifAction = "adhan"
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.setAdhanEnabled(true)
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        isLocatingState.value = true
        val lm = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            isLocatingState.value = false
            viewModel.showToast(stringsFor(viewModel.state.value.appLanguage).toastLocationFailed)
            return
        }

        // Try best last known location first for instant response
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (p in providers) {
            try {
                val loc = lm.getLastKnownLocation(p)
                if (loc != null && (best == null || loc.accuracy < best.accuracy)) {
                    best = loc
                }
            } catch (_: Exception) {
            }
        }

        if (best != null) {
            resolveAndApplyLocation(best.latitude, best.longitude)
            return
        }

        // Request fresh single location fix if no cached fix exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val activeProvider = when {
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }
            if (activeProvider != null) {
                try {
                    lm.getCurrentLocation(
                        activeProvider,
                        CancellationSignal(),
                        mainExecutor
                    ) { loc ->
                        if (loc != null) {
                            resolveAndApplyLocation(loc.latitude, loc.longitude)
                        } else {
                            isLocatingState.value = false
                            viewModel.showToast(stringsFor(viewModel.state.value.appLanguage).toastLocationFailed)
                        }
                    }
                    return
                } catch (_: Exception) {
                }
            }
        }

        isLocatingState.value = false
        viewModel.showToast(stringsFor(viewModel.state.value.appLanguage).toastLocationFailed)
    }

    private fun resolveAndApplyLocation(lat: Double, lng: Double) {
        lifecycleScope.launch {
            val isAr = viewModel.state.value.appLanguage != "en"
            val label = withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val list = Geocoder(this@MainActivity, if (isAr) Locale("ar") else Locale.ENGLISH)
                        .getFromLocation(lat, lng, 1)
                    val addr = list?.firstOrNull()
                    addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: ""
                } catch (_: Exception) {
                    ""
                }
            }
            isLocatingState.value = false
            viewModel.setGpsLocation(lat, lng, label)
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.refreshApps()
        }
    }

    private fun setBlackScreenMode(enabled: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        val lp = window.attributes
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            lp.screenBrightness = 0.01f
            window.attributes = lp
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = lp
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            this,
            packageReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        setContent {
            val state by viewModel.state.collectAsState()
            val layoutDir = if (state.appLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

            val activeTheme = themeOptionFor(state.appThemeId, state.darkTheme)

            // Dynamically adapt status & navigation bar icon contrast for Dark / Light themes on all Android versions
            LaunchedEffect(activeTheme.isDark) {
                val style = if (activeTheme.isDark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !activeTheme.isDark
                controller.isAppearanceLightNavigationBars = !activeTheme.isDark
            }

            SukoonTheme(
                themeId = state.appThemeId,
                darkTheme = state.darkTheme,
                appFontId = state.appFontId,
                appFontWeightId = state.appFontWeight,
                appLanguage = state.appLanguage,
                animationsEnabled = state.animationsEnabled
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    if (state.onboardingStatus != "completed") {
                        OnboardingScreen(
                            state = state,
                            isLocating = isLocatingState.value,
                            onSetStep = { viewModel.setOnboardingStep(it) },
                            onSetLanguage = { viewModel.setAppLanguage(it) },
                            onTogglePageEnabled = { viewModel.togglePageEnabled(it) },
                            onMovePage = { from, to -> viewModel.movePageConfig(from, to) },
                            onSetDarkTheme = { viewModel.setDarkTheme(it) },
                            onSetAppTheme = { viewModel.setAppTheme(it) },
                            onSetAppFont = { viewModel.setAppFont(it) },
                            onSetAppFontWeight = { viewModel.setAppFontWeight(it) },
                            onSetAppDisplayStyle = { viewModel.setAppDisplayStyle(it) },
                            onSetPrayerCity = { viewModel.setPrayerCity(it) },
                            onRequestLocation = { requestUserLocation() },
                            onComplete = { viewModel.completeOnboarding() }
                        )
                    } else {
                        SukoonLauncherRoot(
                            viewModel = viewModel,
                            isLocating = isLocatingState.value,
                            onRequestLocation = { requestUserLocation() },
                            onTogglePrayerReminder = { togglePrayerReminderWithPermission(it) },
                            onToggleAdhan = { toggleAdhanWithPermission(it) },
                            onSetBlackScreenWindow = { enabled -> setBlackScreenMode(enabled) },
                            onOpenNotificationAccess = {
                                try {
                                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                } catch (_: Exception) {
                                    startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            },
                            onOpenAccessibility = {
                                try {
                                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                } catch (_: Exception) {
                                    viewModel.showToast(stringsFor(state.appLanguage).toastAccessibilityFallback)
                                }
                            },
                            onOpenDefaultHome = {
                                try {
                                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                                } catch (_: Exception) {
                                    startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshApps()
        viewModel.refreshVolumes()
        viewModel.refreshNotificationAccess()
        viewModel.syncPrayerReminderSchedule()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopMuezzinPreview()
        viewModel.flushPendingSave()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageReceiver)
        } catch (_: Exception) {
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SukoonLauncherRoot(
    viewModel: SukoonViewModel,
    isLocating: Boolean,
    onRequestLocation: () -> Unit,
    onTogglePrayerReminder: (Boolean) -> Unit,
    onToggleAdhan: (Boolean) -> Unit,
    onSetBlackScreenWindow: (Boolean) -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenDefaultHome: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val quranPages by viewModel.quranPages.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val volumes by viewModel.volumes.collectAsState()
    val mindfulApp by viewModel.mindfulApp.collectAsState()
    val mindfulSecondsLeft by viewModel.mindfulSecondsLeft.collectAsState()
    val prayerAutoFocusUntil by viewModel.prayerAutoFocusUntil.collectAsState()
    val isPrayerAutoFocusActive = prayerAutoFocusUntil > System.currentTimeMillis()
    val animOn = state.animationsEnabled
    val pomoTotal by viewModel.pomoTotalSeconds.collectAsState()
    val pomoSeconds by viewModel.pomoSeconds.collectAsState()
    val pomoModeKey by viewModel.pomoModeKey.collectAsState()
    val pomoRunning by viewModel.pomoRunning.collectAsState()
    val isAccConnected by viewModel.isAccessibilityConnected.collectAsState()
    val capturedNotifications by viewModel.capturedNotifications.collectAsState()
    val isNotifAccessGranted by viewModel.isNotificationAccessGranted.collectAsState()
    val previewingMuezzinId by viewModel.previewingMuezzinId.collectAsState()
    val pageLayoutDir = if (strings.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    var showDrawer by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showQibla by remember { mutableStateOf(false) }
    var blackScreenMode by remember { mutableStateOf<String?>(null) } // null | "dhikr" | "azkar"
    var azkarIsMorning by remember { mutableStateOf(isCurrentlyMorning()) }

    LaunchedEffect(blackScreenMode) {
        onSetBlackScreenWindow(blackScreenMode != null)
    }

    val visiblePageIds = remember(state.pageConfig) {
        state.pageConfig.filter { it.enabled || it.id == "home" }.map { it.id }
    }
    val homeIdx = visiblePageIds.indexOf("home").coerceAtLeast(0)

    val pagerState = rememberPagerState(
        initialPage = homeIdx,
        pageCount = { visiblePageIds.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage, visiblePageIds) {
        viewModel.currentVisiblePageId = visiblePageIds.getOrNull(pagerState.currentPage) ?: "home"
    }

    BackHandler {
        when {
            blackScreenMode != null -> blackScreenMode = null
            mindfulApp != null -> viewModel.dismissMindfulPause()
            showQibla -> showQibla = false
            showSettings -> {
                viewModel.stopMuezzinPreview()
                showSettings = false
            }
            showDrawer -> showDrawer = false
            showSearch -> showSearch = false
            pagerState.currentPage != homeIdx -> scope.launch { pagerState.animateScrollToPage(homeIdx) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    val pageId = visiblePageIds.getOrNull(pageIndex) ?: "home"
                    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                    val pageModifier = if (animOn) {
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                alpha = 0.72f + 0.28f * fraction
                                val s = 0.965f + 0.035f * fraction
                                scaleX = s
                                scaleY = s
                            }
                    } else {
                        Modifier.fillMaxSize()
                    }
                    Box(modifier = pageModifier) {
                        CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDir) {
                            when (pageId) {
                                "home" -> {
                                    var dragAcc by remember { mutableFloatStateOf(0f) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(Unit) {
                                                detectVerticalDragGestures(
                                                    onDragStart = { dragAcc = 0f },
                                                    onDragEnd = {
                                                        if (dragAcc < -90f) showDrawer = true
                                                        else if (dragAcc > 90f) showSearch = true
                                                        dragAcc = 0f
                                                    },
                                                    onVerticalDrag = { _, dragAmount ->
                                                        dragAcc += dragAmount
                                                    }
                                                )
                                            }
                                    ) {
                                        val rightId = visiblePageIds.getOrNull(homeIdx + 1)
                                        val leftId = visiblePageIds.getOrNull(homeIdx - 1)
                                        HomeScreen(
                                            state = state,
                                            installedApps = installedApps,
                                            rightTabLabel = rightId?.let { PAGE_META_MAP[it]?.localizedShort(strings.isArabic) },
                                            leftTabLabel = leftId?.let { PAGE_META_MAP[it]?.localizedShort(strings.isArabic) },
                                            onLoadAppIcon = { viewModel.loadAppIcon(it) },
                                            onAppClick = { viewModel.onAppClick(it) },
                                            onRemoveFavorite = { viewModel.toggleFavorite(it.packageName, it.label) },
                                            onToggleFocus = { viewModel.toggleFocusMode() },
                                            onOpenDrawer = { showDrawer = true },
                                            onOpenSearch = { showSearch = true },
                                            onOpenSettings = { showSettings = true },
                                            onOpenQibla = { showQibla = true },
                                            isPrayerAutoFocusActive = isPrayerAutoFocusActive,
                                            onTogglePrayerCompleted = { viewModel.togglePrayerCompleted(it) },
                                            onNavigateRight = {
                                                if (homeIdx + 1 < visiblePageIds.size) {
                                                    scope.launch {
                                                        if (animOn) pagerState.animateScrollToPage(homeIdx + 1)
                                                        else pagerState.scrollToPage(homeIdx + 1)
                                                    }
                                                }
                                            },
                                            onNavigateLeft = {
                                                if (homeIdx - 1 >= 0) {
                                                    scope.launch {
                                                        if (animOn) pagerState.animateScrollToPage(homeIdx - 1)
                                                        else pagerState.scrollToPage(homeIdx - 1)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }

                            "quran" -> QuranScreen(
                                state = state,
                                pages = quranPages,
                                onGoToPage = { viewModel.goToQuranPage(it) },
                                onAdjustFont = { viewModel.adjustQuranFont(it) },
                                onTogglePaper = { viewModel.toggleQuranPaperMode() },
                                onToggleDone = { viewModel.toggleWirdDoneToday() }
                            )

                            "azkar" -> AzkarScreen(
                                state = state,
                                isMorning = azkarIsMorning,
                                onSetMorning = { azkarIsMorning = it },
                                onSelectIndex = { viewModel.selectAzkarIndex(azkarIsMorning, it) },
                                onIncrement = { viewModel.incrementAzkar(azkarIsMorning) },
                                onReset = { viewModel.resetAzkar(azkarIsMorning) },
                                onEnterBlackScreenMode = { blackScreenMode = "azkar" }
                            )

                            "dhikr" -> DhikrScreen(
                                state = state,
                                onSelectDhikr = { viewModel.selectDhikr(it) },
                                onIncrement = { viewModel.incrementDhikr() },
                                onAdjustTarget = { viewModel.adjustActiveDhikrTarget(it) },
                                onReset = { viewModel.resetActiveDhikr() },
                                onAddDhikr = { t, n -> viewModel.addDhikr(t, n) },
                                onDeleteActive = { viewModel.deleteActiveDhikr() },
                                onEnterBlackScreenMode = { blackScreenMode = "dhikr" }
                            )

                            "habits" -> HabitsScreen(
                                state = state,
                                onToggleHabit = { viewModel.toggleHabit(it) },
                                onAddHabit = { viewModel.addHabit(it) },
                                onDeleteHabit = { viewModel.deleteHabit(it) },
                                onNewDay = { viewModel.resetHabitsNewDay() }
                            )

                            "notifications" -> NotificationsScreen(
                                state = state,
                                notifications = capturedNotifications,
                                isNotificationAccessGranted = isNotifAccessGranted,
                                onLoadAppIcon = { viewModel.loadAppIcon(it) },
                                onOpenNotificationAccessSettings = onOpenNotificationAccess,
                                onToggleFocus = { viewModel.toggleFocusMode() },
                                onOpenNotificationApp = { viewModel.openCapturedNotificationApp(it) },
                                onDismissNotification = { viewModel.dismissCapturedNotification(it) },
                                onClearAll = { viewModel.clearCapturedNotifications() }
                            )

                            "notes" -> NotesScreen(
                                state = state,
                                onCreateNote = { viewModel.createNote() },
                                onUpdateNote = { id, t, b -> viewModel.updateNote(id, t, b) },
                                onTogglePin = { viewModel.toggleNotePin(it) },
                                onDeleteNote = { viewModel.deleteNote(it) }
                            )

                            "pomodoro" -> PomodoroScreen(
                                state = state,
                                totalSeconds = pomoTotal,
                                secondsLeft = pomoSeconds,
                                modeKey = pomoModeKey,
                                isRunning = pomoRunning,
                                onSelectMode = { m, k -> viewModel.setPomoMode(m, k) },
                                onToggleStart = { viewModel.togglePomo() },
                                onReset = { viewModel.resetPomo() },
                                onTaskChange = { viewModel.setPomoTask(it) },
                                onAutoFocusChange = { viewModel.setPomoAutoFocus(it) }
                            )
                        }
                        }
                    }
                }
            }

            // Minimal Page Indicator Dots
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    visiblePageIds.forEachIndexed { idx, id ->
                        val active = idx == pagerState.currentPage
                        val targetColor = when {
                            active && (id == "quran" || id == "azkar" || id == "dhikr") -> colors.accentWarm
                            active -> colors.textPrimary
                            else -> colors.borderStrong
                        }
                        val dotColor by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = tween(if (animOn) 200 else 0),
                            label = "dotColor"
                        )
                        val dotWidth by animateDpAsState(
                            targetValue = if (active) 18.dp else 6.dp,
                            animationSpec = tween(if (animOn) 220 else 0),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(dotWidth)
                                .clip(RoundedCornerShape(50))
                                .background(dotColor)
                                .clickable {
                                    scope.launch {
                                        if (animOn) pagerState.animateScrollToPage(idx)
                                        else pagerState.scrollToPage(idx)
                                    }
                                }
                        )
                    }
                }
            }
        }

        // Overlays (smooth animated transitions when animationsEnabled == true, instant when false)
        val sheetEnter = if (animOn) fadeIn(tween(190)) + slideInVertically(tween(240)) { it / 14 } else EnterTransition.None
        val sheetExit = if (animOn) fadeOut(tween(150)) + slideOutVertically(tween(180)) { it / 18 } else ExitTransition.None
        val modalEnter = if (animOn) fadeIn(tween(180)) + scaleIn(tween(220), initialScale = 0.96f) else EnterTransition.None
        val modalExit = if (animOn) fadeOut(tween(140)) + scaleOut(tween(160), targetScale = 0.97f) else ExitTransition.None

        AnimatedVisibility(visible = showDrawer, enter = sheetEnter, exit = sheetExit) {
            AppDrawerOverlay(
                state = state,
                installedApps = installedApps,
                onLoadAppIcon = { viewModel.loadAppIcon(it) },
                onAppClick = { viewModel.onAppClick(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it.packageName, it.label) },
                onToggleDistracting = { viewModel.toggleDistracting(it.packageName, it.label) },
                onClose = { showDrawer = false }
            )
        }

        AnimatedVisibility(visible = showSearch, enter = modalEnter, exit = modalExit) {
            UniversalSearchOverlay(
                state = state,
                installedApps = installedApps,
                volumes = volumes,
                onRefreshVolumes = { viewModel.refreshVolumes() },
                onSetVolume = { s, v -> viewModel.setStreamVolume(s, v) },
                onToggleMute = { viewModel.toggleMuteAll() },
                onAppClick = { viewModel.onAppClick(it) },
                onNavigateToPage = { id ->
                    val idx = visiblePageIds.indexOf(id)
                    if (idx != -1) {
                        scope.launch {
                            if (animOn) pagerState.animateScrollToPage(idx)
                            else pagerState.scrollToPage(idx)
                        }
                    }
                },
                onOpenSettings = { showSettings = true },
                onClose = { showSearch = false }
            )
        }

        AnimatedVisibility(visible = showSettings, enter = sheetEnter, exit = sheetExit) {
            SettingsOverlay(
                state = state,
                volumes = volumes,
                isAccessibilityConnected = isAccConnected,
                isNotificationAccessGranted = isNotifAccessGranted,
                isLocating = isLocating,
                previewingMuezzinId = previewingMuezzinId,
                onSetLanguage = { viewModel.setAppLanguage(it) },
                onSetAppFont = { viewModel.setAppFont(it) },
                onSetAppFontWeight = { viewModel.setAppFontWeight(it) },
                onSetAppDisplayStyle = { viewModel.setAppDisplayStyle(it) },
                onSetAppTheme = { viewModel.setAppTheme(it) },
                onSetPrayerCity = { viewModel.setPrayerCity(it) },
                onRequestLocation = onRequestLocation,
                onTogglePrayerReminder = onTogglePrayerReminder,
                onSetPrayerReminderMinutes = { viewModel.setPrayerReminderMinutes(it) },
                onToggleAdhan = onToggleAdhan,
                onSetAdhanMuezzin = { viewModel.setAdhanMuezzin(it) },
                onToggleMuezzinPreview = { viewModel.toggleMuezzinPreview(it) },
                onTogglePrayerAutoFocus = { viewModel.setPrayerAutoFocusEnabled(it) },
                onSetPrayerAutoFocusMinutes = { viewModel.setPrayerAutoFocusMinutes(it) },
                onTogglePrayerTracker = { viewModel.setPrayerTrackerEnabled(it) },
                onOpenQibla = { showQibla = true },
                onSetQuranLineHeight = { viewModel.setQuranLineHeight(it) },
                onMovePage = { from, to -> viewModel.movePageConfig(from, to) },
                onTogglePageEnabled = { viewModel.togglePageEnabled(it) },
                onSetDarkTheme = { viewModel.setDarkTheme(it) },
                onToggleAnimations = { viewModel.setAnimationsEnabled(it) },
                onToggleFocus = { viewModel.toggleFocusMode(it) },
                onSetMindfulDuration = { viewModel.setMindfulPauseSeconds(it) },
                onToggleMindfulTimeBox = { viewModel.setMindfulTimeBoxEnabled(it) },
                onSetHaptics = { viewModel.setHapticsEnabled(it) },
                onRefreshVolumes = { viewModel.refreshVolumes() },
                onSetVolume = { s, v -> viewModel.setStreamVolume(s, v) },
                onToggleMute = { viewModel.toggleMuteAll() },
                onOpenNotificationAccessSettings = onOpenNotificationAccess,
                onOpenAccessibilitySettings = onOpenAccessibility,
                onOpenDefaultLauncherSettings = onOpenDefaultHome,
                onRestartSetup = { viewModel.restartSetup() },
                onResetApp = { viewModel.resetApp() },
                onClose = {
                    viewModel.stopMuezzinPreview()
                    showSettings = false
                }
            )
        }

        AnimatedVisibility(visible = showQibla, enter = modalEnter, exit = modalExit) {
            QiblaOverlay(
                state = state,
                onAlignedHaptic = { viewModel.vibrateLight() },
                onClose = { showQibla = false }
            )
        }

        AnimatedVisibility(visible = mindfulApp != null, enter = modalEnter, exit = modalExit) {
            mindfulApp?.let { app ->
                MindfulPauseOverlay(
                    app = app,
                    secondsLeft = mindfulSecondsLeft,
                    timeBoxEnabled = state.mindfulTimeBoxEnabled,
                    onStay = {
                        viewModel.dismissMindfulPause()
                        viewModel.showToast(strings.toastStayedCalm)
                    },
                    onProceed = { mins -> viewModel.proceedMindfulApp(mins) }
                )
            }
        }

        // Pure-Black "Screen-Off" Tasbih / Azkar Overlay
        if (blackScreenMode != null) {
            val isAzkarMode = blackScreenMode == "azkar"
            val summaryText = if (isAzkarMode) {
                val list = if (azkarIsMorning) state.morningAzkar else state.eveningAzkar
                val idx = (if (azkarIsMorning) state.morningAzkarIndex else state.eveningAzkarIndex)
                    .coerceIn(0, (list.size - 1).coerceAtLeast(0))
                val cur = list.getOrNull(idx)
                cur?.let { "${it.title} • ${it.count.formatDigits(strings.isArabic)} / ${it.target.formatDigits(strings.isArabic)}" } ?: ""
            } else {
                val activeDhikr = state.adhkar.find { it.id == state.activeDhikrId } ?: state.adhkar.firstOrNull()
                activeDhikr?.let { "${it.short} • ${it.count.formatDigits(strings.isArabic)} / ${it.target.formatDigits(strings.isArabic)}" } ?: ""
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (isAzkarMode) viewModel.incrementAzkar(azkarIsMorning)
                            else viewModel.incrementDhikr()
                        },
                        onLongClick = {
                            viewModel.vibrateLight()
                            blackScreenMode = null
                        }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (summaryText.isNotEmpty()) {
                        Text(
                            text = summaryText,
                            fontFamily = appFont,
                            fontSize = 12.sp,
                            color = Color(0x26FFFFFF)
                        )
                    }
                    Text(
                        text = strings.longPressToExit,
                        fontFamily = appFont,
                        fontSize = 11.sp,
                        color = Color(0x1EFFFFFF)
                    )
                }
            }
        }

        // Subtle Toast
        AnimatedVisibility(
            visible = blackScreenMode == null && toastMsg != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp),
            enter = if (animOn) fadeIn(tween(180)) + slideInVertically(tween(200)) { it / 2 } else EnterTransition.None,
            exit = if (animOn) fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 } else ExitTransition.None
        ) {
            toastMsg?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.textPrimary)
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = msg,
                        fontFamily = appFont,
                        fontSize = 12.sp,
                        color = colors.background
                    )
                }
            }
        }
    }
}
