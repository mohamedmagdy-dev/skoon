package com.sukoon.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SukoonViewModel by viewModels()

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

            // Dynamically adapt status & navigation bar icon contrast for Dark / Light themes on all Android versions
            LaunchedEffect(state.darkTheme) {
                val style = if (state.darkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !state.darkTheme
                controller.isAppearanceLightNavigationBars = !state.darkTheme
            }

            SukoonTheme(
                darkTheme = state.darkTheme,
                appFontId = state.appFontId,
                appLanguage = state.appLanguage
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    SukoonLauncherRoot(
                        viewModel = viewModel,
                        onSetBlackScreenWindow = { enabled -> setBlackScreenMode(enabled) },
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

    override fun onResume() {
        super.onResume()
        viewModel.refreshApps()
        viewModel.refreshVolumes()
    }

    override fun onPause() {
        super.onPause()
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
    onSetBlackScreenWindow: (Boolean) -> Unit,
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
    val pomoTotal by viewModel.pomoTotalSeconds.collectAsState()
    val pomoSeconds by viewModel.pomoSeconds.collectAsState()
    val pomoModeKey by viewModel.pomoModeKey.collectAsState()
    val pomoRunning by viewModel.pomoRunning.collectAsState()
    val isAccConnected by viewModel.isAccessibilityConnected.collectAsState()
    val pageLayoutDir = if (strings.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    var showDrawer by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
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
            showSettings -> showSettings = false
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
                                        onAppClick = { viewModel.onAppClick(it) },
                                        onRemoveFavorite = { viewModel.toggleFavorite(it.packageName, it.label) },
                                        onToggleFocus = { viewModel.toggleFocusMode() },
                                        onOpenDrawer = { showDrawer = true },
                                        onOpenSearch = { showSearch = true },
                                        onOpenSettings = { showSettings = true },
                                        onNavigateRight = {
                                            if (homeIdx + 1 < visiblePageIds.size) {
                                                scope.launch { pagerState.animateScrollToPage(homeIdx + 1) }
                                            }
                                        },
                                        onNavigateLeft = {
                                            if (homeIdx - 1 >= 0) {
                                                scope.launch { pagerState.animateScrollToPage(homeIdx - 1) }
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
                        val dotColor = when {
                            active && (id == "quran" || id == "azkar" || id == "dhikr") -> colors.accentWarm
                            active -> colors.textPrimary
                            else -> colors.borderStrong
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (active) 18.dp else 6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(dotColor)
                                .clickable { scope.launch { pagerState.animateScrollToPage(idx) } }
                        )
                    }
                }
            }
        }

        // Overlays
        if (showDrawer) {
            AppDrawerOverlay(
                state = state,
                installedApps = installedApps,
                onAppClick = { viewModel.onAppClick(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it.packageName, it.label) },
                onToggleDistracting = { viewModel.toggleDistracting(it.packageName, it.label) },
                onClose = { showDrawer = false }
            )
        }

        if (showSearch) {
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
                    if (idx != -1) scope.launch { pagerState.animateScrollToPage(idx) }
                },
                onOpenSettings = { showSettings = true },
                onClose = { showSearch = false }
            )
        }

        if (showSettings) {
            SettingsOverlay(
                state = state,
                volumes = volumes,
                isAccessibilityConnected = isAccConnected,
                onSetLanguage = { viewModel.setAppLanguage(it) },
                onSetAppFont = { viewModel.setAppFont(it) },
                onSetPrayerCity = { viewModel.setPrayerCity(it) },
                onSetQuranLineHeight = { viewModel.setQuranLineHeight(it) },
                onMovePage = { from, to -> viewModel.movePageConfig(from, to) },
                onTogglePageEnabled = { viewModel.togglePageEnabled(it) },
                onSetDarkTheme = { viewModel.setDarkTheme(it) },
                onRefreshVolumes = { viewModel.refreshVolumes() },
                onSetVolume = { s, v -> viewModel.setStreamVolume(s, v) },
                onToggleMute = { viewModel.toggleMuteAll() },
                onOpenAccessibilitySettings = onOpenAccessibility,
                onOpenDefaultLauncherSettings = onOpenDefaultHome,
                onClose = { showSettings = false }
            )
        }

        mindfulApp?.let { app ->
            MindfulPauseOverlay(
                app = app,
                secondsLeft = mindfulSecondsLeft,
                onStay = {
                    viewModel.dismissMindfulPause()
                    viewModel.showToast(strings.toastStayedCalm)
                },
                onProceed = { viewModel.proceedMindfulApp() }
            )
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
        if (blackScreenMode == null) {
            toastMsg?.let { msg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 36.dp)
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
