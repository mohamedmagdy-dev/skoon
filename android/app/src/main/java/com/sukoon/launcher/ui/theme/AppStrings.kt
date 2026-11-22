package com.sukoon.launcher.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

interface AppStrings {
    val isArabic: Boolean

    // Common
    val done: String
    val close: String
    val cancel: String
    val save: String
    val delete: String
    val add: String
    val reset: String
    val open: String
    val openChevron: String
    val backArrow: String
    val edit: String
    val remove: String
    val settings: String
    val percentSymbol: String

    // Home Screen
    val focusOn: String
    val normalMode: String
    val searchAndVolume: String
    val favorites: String
    val mindfulTag: String
    val hiddenDistractingFmt: (Int) -> String
    val allAppsUp: String
    val changeCityHint: String

    // Quran Screen
    val holyQuran: String
    val juzPageFmt: (Int, Int) -> String
    val fontMinus: String
    val fontPlus: String
    val paperMode: String
    val jumpToPageLabel: String
    val go: String
    val loadingMushaf: String
    val prevPage: String
    val nextPage: String

    // Azkar Screen
    val autoTimeMorning: String
    val autoTimeEvening: String
    val morningAzkar: String
    val eveningAzkar: String
    val morning: String
    val evening: String
    val azkarDoneFmt: (Int, Int) -> String
    val blackScreen: String
    val completedAlhamdulillah: String
    val tapToCount: String
    val prevItem: String
    val nextItem: String

    // Dhikr Screen
    val serenityAndDhikr: String
    val adhkarAndTasbih: String
    val addDhikrBtn: String
    val blackScreenTitle: String
    val blackScreenSubtitle: String
    val launchChevron: String
    val dhikrTextHint: String
    val targetLabel: String
    val ofTargetFmt: (String) -> String
    val dhikrTapHint: String
    val minusTarget: String
    val plusTarget: String
    val zeroCounter: String
    val longPressToExit: String

    // Habits Screen
    val repeatsDaily: String
    val dailyHabits: String
    val newDayBtn: String
    val completedTodayFmt: (String, String) -> String
    val streakDaysFmt: (String) -> String
    val addHabitHint: String

    // Notes Screen
    val quickThoughts: String
    val notesTitle: String
    val newNoteBtn: String
    val pinnedStar: String
    val pin: String
    val noteTitleHint: String
    val noteBodyHint: String
    val autoSaved: String
    val noNotesYet: String
    val untitledNote: String
    val pinnedDot: String
    val tapToWrite: String

    // Pomodoro Screen
    val deepSession: String
    val pomodoroTitle: String
    val sessionsTodayFmt: (String) -> String
    val focusModeName: String
    val shortBreakModeName: String
    val longBreakModeName: String
    val focusChip: String
    val shortBreakChip: String
    val longBreakChip: String
    val runningFmt: (String) -> String
    val readyFmt: (String) -> String
    val pomoTaskHint: String
    val pauseSession: String
    val startSession: String
    val autoFocusToggle: String

    // Overlays: App Drawer & Search & Volume & Mindful
    val searchAppsHint: String
    val drawerLegend: String
    val searchOrCalcHint: String
    val quickJump: String
    val quickJumpQuranSubFmt: (String) -> String
    val quickJumpDhikrSub: String
    val quickJumpHabitsSub: String
    val quickJumpSettingsTitle: String
    val quickJumpSettingsSub: String
    val appsLabel: String
    val quickVolumeControl: String
    val unmute: String
    val quickMute: String
    val mediaVol: String
    val ringVol: String
    val alarmVol: String
    val beforeOpeningFmt: (String) -> String
    val mindfulPromptFmt: (String) -> String
    val returnToCalm: String
    val waitSecondsFmt: (String) -> String
    val openAppFmt: (String) -> String

    // Settings Overlay
    val customizeSukoon: String
    val settingsTitle: String
    val catAll: String
    val catDisplay: String
    val catSpiritual: String
    val catFocus: String
    val catSystem: String
    val appDisplaySection: String
    val appDisplaySubtitle: String
    val styleTextOnly: String
    val styleTextOnlySub: String
    val styleMonochrome: String
    val styleMonochromeSub: String
    val styleOriginal: String
    val styleOriginalSub: String
    val focusMindfulSection: String
    val mindfulDurationLabel: String
    val hapticsLabel: String
    val restartSetupTitle: String
    val restartSetupSub: String
    val resetAppTitle: String
    val resetAppSub: String
    val confirmResetAppMsg: String
    val obWelcomeEyebrow: String
    val obWelcomeHeading: String
    val obWelcomeDesc: String
    val obGetStarted: String
    val obLangTitle: String
    val obLangSub: String
    val obExpTitle: String
    val obExpSub: String
    val obProductivity: String
    val obSpiritual: String
    val obHomeCenterNote: String
    val obAppearanceTitle: String
    val obAppearanceSub: String
    val obLocationTitle: String
    val obLocationSub: String
    val obNavTitle: String
    val obNavSub: String
    val obReadyHeading: String
    val obReadyDesc: String
    val obEnterSukoon: String
    val nextLabel: String
    val skipLabel: String
    val backLabel: String
    val languageSection: String
    val languageSubtitle: String
    val fontSection: String
    val fontSubtitle: String
    val fontWeightSection: String
    val weightLight: String
    val weightRegular: String
    val weightBold: String
    val themesSection: String
    val prayerCitySection: String
    val prayerCitySubtitle: String
    val searchCityHint: String
    val changeCityBtn: String
    val detectLocationBtn: String
    val detectingLocation: String
    val gpsActiveLabel: String
    val prayerReminderTitle: String
    val prayerReminderSub: String
    val reminderBeforeLabel: String
    val adhanTitle: String
    val adhanSub: String
    val muezzinVoiceLabel: String
    val previewAudio: String
    val stopAudio: String
    val qiblaTitle: String
    val qiblaSub: String
    val qiblaAligned: String
    val qiblaTurnHint: String
    val qiblaAngleFmt: (String) -> String
    val qiblaDistanceFmt: (String) -> String
    val qiblaNoSensor: String
    val qiblaCalibrateHint: String
    val toastAdhanOnFmt: (String) -> String
    val toastAdhanOff: String
    val prayerAutoFocusTitle: String
    val prayerAutoFocusSub: String
    val prayerAutoFocusDurationLabel: String
    val prayerAutoFocusActivePill: String
    val toastPrayerAutoFocusOnFmt: (String) -> String
    val toastPrayerAutoFocusOff: String
    val prayerTrackerTitle: String
    val prayerTrackerSub: String
    val prayersTrackedFmt: (String, String) -> String
    val prayerTrackerTapHint: String
    val mindfulTimeBoxTitle: String
    val mindfulTimeBoxSub: String
    val mindfulChooseDuration: String
    val mindfulOpenWithoutTimer: String
    val toastTimeBoxScheduledFmt: (String, String) -> String
    val animationsTitle: String
    val animationsSub: String
    val minutesShortFmt: (String) -> String
    val toastLocationUpdatedFmt: (String) -> String
    val toastLocationFailed: String
    val toastReminderOnFmt: (String) -> String
    val toastReminderOff: String
    val toastNotifPermissionDenied: String
    val notifTabEyebrow: String
    val notifTabTitle: String
    val notifClearAll: String
    val notifPermTitle: String
    val notifPermSub: String
    val notifPermGrantBtn: String
    val notifShieldActive: String
    val notifShieldPaused: String
    val notifEmptyTitle: String
    val notifEmptySub: String
    val notifAccessSettingsTitle: String
    val notifAccessActive: String
    val notifAccessPrompt: String
    val quranSpacingSection: String
    val quranSpacingSubtitle: String
    val spacingCompact: String
    val spacingComfortable: String
    val spacingWide: String
    val spacingExtraWide: String
    val tabsOrderSection: String
    val tabsOrderSubtitle: String
    val hiddenLabel: String
    val centerHomeLabel: String
    val rightOfHomeFmt: (String) -> String
    val leftOfHomeFmt: (String) -> String
    val enabledLabel: String
    val appearanceSection: String
    val darkOled: String
    val lightPaper: String
    val systemSettingsSection: String
    val fingerprintTasbihTitle: String
    val accessibilityActive: String
    val accessibilityPrompt: String
    val defaultLauncherTitle: String
    val defaultLauncherSubtitle: String

    // Toasts
    val toastAccessibilityFallback: String
    val toastStayedCalm: String
    val toastRemovedFavFmt: (String) -> String
    val toastAddedFavFmt: (String) -> String
    val toastDisabledMindfulFmt: (String) -> String
    val toastEnabledMindfulFmt: (String) -> String
    val toastFocusEnabled: String
    val toastFocusDisabled: String
    val toastWirdDone: String
    val toastDhikrCompletedFmt: (String) -> String
    val toastDhikrAlreadyDone: String
    val toastCounterReset: String
    val toastDhikrAdded: String
    val toastDhikrDeleted: String
    val toastMorningAzkarDone: String
    val toastEveningAzkarDone: String
    val toastMorningAzkarReset: String
    val toastEveningAzkarReset: String
    val toastNewDayHabits: String
    val toastNoteDeleted: String
    val toastSessionDone: String
    val toastVolumeError: String
    val toastMuted: String
    val toastUnmuted: String
    val toastCityUpdatedFmt: (String) -> String
    val toastFontUpdatedFmt: (String) -> String
    val toastLanguageUpdated: String
}

object ArabicStrings : AppStrings {
    override val isArabic = true
    override val done = "تم"
    override val close = "إغلاق"
    override val cancel = "إلغاء"
    override val save = "حفظ"
    override val delete = "حذف"
    override val add = "إضافة"
    override val reset = "إعادة"
    override val open = "فتح"
    override val openChevron = "فتح ›"
    override val backArrow = "→ رجوع"
    override val edit = "تعديل"
    override val remove = "إزالة"
    override val settings = "الإعدادات"
    override val percentSymbol = "٪"

    override val focusOn = "وضع التركيز مفعل"
    override val normalMode = "وضع عادي"
    override val searchAndVolume = "بحث • الصوت"
    override val favorites = "المفضلة"
    override val mindfulTag = "تأمل"
    override val hiddenDistractingFmt: (Int) -> String = { n -> "تم إخفاء $n تطبيق مشتت أثناء وضع التركيز" }
    override val allAppsUp = "كل التطبيقات ↑"
    override val changeCityHint = "تغيير المدينة"

    override val holyQuran = "القرآن الكريم"
    override val juzPageFmt: (Int, Int) -> String = { j, p -> "الجزء $j • صفحة $p من ٦٠٤" }
    override val fontMinus = "أ-"
    override val fontPlus = "أ+"
    override val paperMode = "ورق"
    override val jumpToPageLabel = "انتقال لصفحة (١ - ٦٠٤):"
    override val go = "انتقال"
    override val loadingMushaf = "جاري تحميل المصحف..."
    override val prevPage = "→ السابقة"
    override val nextPage = "التالية ←"

    override val autoTimeMorning = "تلقائي حسب الوقت • الصباح"
    override val autoTimeEvening = "تلقائي حسب الوقت • المساء"
    override val morningAzkar = "أذكار الصباح"
    override val eveningAzkar = "أذكار المساء"
    override val morning = "الصباح"
    override val evening = "المساء"
    override val azkarDoneFmt: (Int, Int) -> String = { done, total -> "تم $done من $total أذكار" }
    override val blackScreen = "شاشة سوداء"
    override val completedAlhamdulillah = "تم بحمد الله ✓"
    override val tapToCount = "اضغط للعد"
    override val prevItem = "→ السابق"
    override val nextItem = "التالي ←"

    override val serenityAndDhikr = "طمأنينة وذكر"
    override val adhkarAndTasbih = "الأذكار والمسبحة"
    override val addDhikrBtn = "+ ذكر"
    override val blackScreenTitle = "وضع الشاشة المقفولة (تعتيم كامل)"
    override val blackScreenSubtitle = "الشاشة تسود تماماً — المس أي مكان للعد بهزة خفيفة"
    override val launchChevron = "تشغيل ›"
    override val dhikrTextHint = "نص الذكر (مثلاً: لا حول ولا قوة إلا بالله)"
    override val targetLabel = "التارجت:"
    override val ofTargetFmt: (String) -> String = { t -> "من $t" }
    override val dhikrTapHint = "المس أي مكان في الشاشة للعد • ينقل تلقائياً عند الإتمام"
    override val minusTarget = "- التارجت"
    override val plusTarget = "+ التارجت"
    override val zeroCounter = "تصفير"
    override val longPressToExit = "ضغطة مطولة للخروج"

    override val repeatsDaily = "تتكرر يومياً"
    override val dailyHabits = "المهام اليومية"
    override val newDayBtn = "يوم جديد ↻"
    override val completedTodayFmt: (String, String) -> String = { d, t -> "تم إنجاز $d من $t اليوم" }
    override val streakDaysFmt: (String) -> String = { s -> "استمرارية: $s أيام" }
    override val addHabitHint = "أضف مهمة تتكرر كل يوم..."

    override val quickThoughts = "أفكار سريعة"
    override val notesTitle = "الملاحظات"
    override val newNoteBtn = "+ جديد"
    override val pinnedStar = "مثبتة ★"
    override val pin = "تثبيت"
    override val noteTitleHint = "عنوان الملاحظة..."
    override val noteBodyHint = "اكتب ملاحظتك هنا... تُحفظ تلقائياً"
    override val autoSaved = "محفوظة تلقائياً"
    override val noNotesYet = "لا توجد ملاحظات بعد — اضغط «+ جديد»"
    override val untitledNote = "ملاحظة بدون عنوان"
    override val pinnedDot = "مثبتة • "
    override val tapToWrite = "اضغط للكتابة..."

    override val deepSession = "جلسة عميقة"
    override val pomodoroTitle = "البومودورو"
    override val sessionsTodayFmt: (String) -> String = { n -> "جلسات اليوم: $n" }
    override val focusModeName = "تركيز"
    override val shortBreakModeName = "راحة قصيرة"
    override val longBreakModeName = "راحة طويلة"
    override val focusChip = "تركيز ٢٥"
    override val shortBreakChip = "راحة ٥"
    override val longBreakChip = "راحة ١٥"
    override val runningFmt: (String) -> String = { m -> "$m — جاري الآن..." }
    override val readyFmt: (String) -> String = { m -> "جاهز لـ $m" }
    override val pomoTaskHint = "بتركز على إيه في الجلسة دي؟"
    override val pauseSession = "إيقاف مؤقت"
    override val startSession = "ابدأ الجلسة"
    override val autoFocusToggle = "تفعيل وضع التركيز تلقائياً مع الجلسة"

    override val searchAppsHint = "ابحث في التطبيقات..."
    override val drawerLegend = "★ لإضافة للمفضلة • «تأمل» لتفعيل وقفة ٥ ثواني قبل الفتح"
    override val searchOrCalcHint = "ابحث عن تطبيق أو احسب (مثل 25*4)..."
    override val quickJump = "انتقال سريع"
    override val quickJumpQuranSubFmt: (String) -> String = { p -> "صفحة $p" }
    override val quickJumpDhikrSub = "المسبحة والأذكار"
    override val quickJumpHabitsSub = "تتكرر يومياً"
    override val quickJumpSettingsTitle = "الإعدادات والتخصيص"
    override val quickJumpSettingsSub = "اللغة، الخط، المواقيت"
    override val appsLabel = "التطبيقات"
    override val quickVolumeControl = "التحكم السريع في الصوت"
    override val unmute = "إلغاء الكتم"
    override val quickMute = "كتم سريع"
    override val mediaVol = "الوسائط"
    override val ringVol = "النغمة"
    override val alarmVol = "المنبه"
    override val beforeOpeningFmt: (String) -> String = { a -> "قبل فتح $a" }
    override val mindfulPromptFmt: (String) -> String = { a -> "خد نفس عميق.. هل فعلاً محتاج تفتح $a دلوقتي؟" }
    override val returnToCalm = "رجوع للهدوء"
    override val waitSecondsFmt: (String) -> String = { s -> "انتظر $s ثواني..." }
    override val openAppFmt: (String) -> String = { a -> "فتح $a" }

    override val customizeSukoon = "تخصيص سكون"
    override val settingsTitle = "الإعدادات"
    override val catAll = "الكل"
    override val catDisplay = "المظهر والعرض"
    override val catSpiritual = "الروحانيات"
    override val catFocus = "التركيز والتنقل"
    override val catSystem = "النظام"
    override val appDisplaySection = "طريقة عرض التطبيقات"
    override val appDisplaySubtitle = "اختر كيف تظهر التطبيقات في الرئيسية والدرج"
    override val styleTextOnly = "نص فقط"
    override val styleTextOnlySub = "مينيماليست هادئ"
    override val styleMonochrome = "أيقونات موحدة"
    override val styleMonochromeSub = "أحادي متناسق"
    override val styleOriginal = "أيقونات أصلية"
    override val styleOriginalSub = "بألوانها الطبيعية"
    override val focusMindfulSection = "التركيز ووقفة التأمل"
    override val mindfulDurationLabel = "مدة وقفة التأمل"
    override val hapticsLabel = "الاهتزاز اللمسي"
    override val restartSetupTitle = "إعادة جولة الإعداد الأولي"
    override val restartSetupSub = "فتح شاشات الترحيب والإعداد من جديد"
    override val resetAppTitle = "إعادة ضبط التطبيق بالكامل"
    override val resetAppSub = "مسح البيانات والعودة للإعدادات الافتراضية"
    override val confirmResetAppMsg = "هل تريد مسح التفضيلات وإعادة ضبط سكون؟"
    override val obWelcomeEyebrow = "مرحباً بك في سكون"
    override val obWelcomeHeading = "مزيد من الهدوء.\nمزيد من القصد."
    override val obWelcomeDesc = "مساحتك الشخصية للتركيز اليومي، العادات الروحية، واللحظات ذات المعنى — بلا ضجيج أو تشتت."
    override val obGetStarted = "ابدأ الإعداد"
    override val obLangTitle = "اختر لغتك"
    override val obLangSub = "تتكيف الواجهة والاتجاه فوراً حسب اختيارك"
    override val obExpTitle = "صمّم تجربتك"
    override val obExpSub = "اختر الأقسام التي تحتاجها في يومك. يمكنك تعديلها لاحقاً."
    override val obProductivity = "الإنتاجية والتركيز"
    override val obSpiritual = "السكينة والعبادة"
    override val obHomeCenterNote = "الشاشة الرئيسية ثابتة دائماً في المنتصف"
    override val obAppearanceTitle = "المظهر والخط"
    override val obAppearanceSub = "معاينة فورية للمظهر والخط قبل الدخول"
    override val obLocationTitle = "موقع مواقيت الصلاة"
    override val obLocationSub = "اختر مدينتك لحساب مواقيت الصلاة بدقة بدون إنترنت"
    override val obNavTitle = "تخصيص التنقل"
    override val obNavSub = "رتّب التابات يمين وشمال الشاشة الرئيسية"
    override val obReadyHeading = "مساحتك. على إيقاعك."
    override val obReadyDesc = "كل شيء جاهز الآن. أهلاً بك في سكون."
    override val obEnterSukoon = "ادخل إلى سكون"
    override val nextLabel = "التالي"
    override val skipLabel = "تخطي"
    override val backLabel = "رجوع"
    override val languageSection = "اللغة"
    override val languageSubtitle = "اختر لغة واجهة التطبيق"
    override val fontSection = "خط التطبيق"
    override val fontSubtitle = "تغيير الخط المستخدم في شاشات التطبيق"
    override val fontWeightSection = "سُمك الخط"
    override val weightLight = "خفيف"
    override val weightRegular = "عادي"
    override val weightBold = "عريض"
    override val themesSection = "ثيمات الألوان"
    override val prayerCitySection = "المدينة (مواقيت الصلاة)"
    override val prayerCitySubtitle = "اختر مدينتك لحساب مواقيت الصلاة بدقة"
    override val searchCityHint = "ابحث عن مدينتك أو دولتك..."
    override val changeCityBtn = "تغيير"
    override val detectLocationBtn = "معرفة موقعي تلقائياً (GPS)"
    override val detectingLocation = "جاري تحديد الموقع..."
    override val gpsActiveLabel = "حسب موقعك الجغرافي (GPS)"
    override val prayerReminderTitle = "تنبيه قبل اقتراب الصلاة"
    override val prayerReminderSub = "إشعار هادئ يذكّرك قبل دخول وقت الصلاة"
    override val reminderBeforeLabel = "مدة التنبيه قبل الصلاة"
    override val adhanTitle = "الأذان عند دخول الوقت"
    override val adhanSub = "تشغيل صوت الأذان المختار تلقائياً عند كل صلاة"
    override val muezzinVoiceLabel = "صوت المؤذن"
    override val previewAudio = "▶ استماع"
    override val stopAudio = "■ إيقاف"
    override val qiblaTitle = "اتجاه القبلة"
    override val qiblaSub = "بوصلة القبلة الدقيقة بدون إنترنت"
    override val qiblaAligned = "أنت الآن باتجاه القبلة ✓"
    override val qiblaTurnHint = "وجّه أعلى الهاتف نحو السهم الذهبي"
    override val qiblaAngleFmt: (String) -> String = { deg -> "زاوية القبلة: $deg° من الشمال" }
    override val qiblaDistanceFmt: (String) -> String = { km -> "المسافة إلى الكعبة: $km كم" }
    override val qiblaNoSensor = "جهازك لا يحتوي على حساس بوصلة مغناطيسية — يمكنك الاسترشاد بالزاوية الدقيقة أعلاه."
    override val qiblaCalibrateHint = "لأعلى دقة: حرّك الهاتف على شكل رقم ٨ في الهواء"
    override val toastAdhanOnFmt: (String) -> String = { name -> "تم تفعيل الأذان بصوت $name" }
    override val toastAdhanOff = "تم إيقاف صوت الأذان"
    override val prayerAutoFocusTitle = "وضع السكينة التلقائي وقت الصلاة"
    override val prayerAutoFocusSub = "تفعيل وضع التركيز وحجب الإشعارات تلقائياً عند كل صلاة ثم العودة للوضع الطبيعي"
    override val prayerAutoFocusDurationLabel = "مدة السكينة عند الصلاة"
    override val prayerAutoFocusActivePill = "سكينة الصلاة مفعّلة"
    override val toastPrayerAutoFocusOnFmt: (String) -> String = { m -> "تم تفعيل سكينة الصلاة تلقائياً لمدة $m دقيقة" }
    override val toastPrayerAutoFocusOff = "تم إيقاف سكينة الصلاة التلقائية"
    override val prayerTrackerTitle = "متتبع الصلوات الخمس اليومي"
    override val prayerTrackerSub = "اضغط على أي صلاة في الرئيسية لتعليمها ✓ مع نسبة إنجاز يومية هادئة"
    override val prayersTrackedFmt: (String, String) -> String = { d, t -> "$d / $t ✓" }
    override val prayerTrackerTapHint = "اضغط على أي صلاة لتعليمها ✓"
    override val mindfulTimeBoxTitle = "سؤال المدة قبل فتح التطبيقات"
    override val mindfulTimeBoxSub = "اختيار مدة الاستخدام (٥ / ١٠ / ١٥ د) مع تذكير هادئ عند انتهاء الوقت"
    override val mindfulChooseDuration = "كم دقيقة تنوي قضاءها؟"
    override val mindfulOpenWithoutTimer = "فتح بدون مؤقت"
    override val toastTimeBoxScheduledFmt: (String, String) -> String = { m, app -> "سيتم تذكيرك بهدوء بعد $m دقائق في $app" }
    override val animationsTitle = "السلاسة والرسوم المتحركة"
    override val animationsSub = "حركات انتقال وفتح هادئة — أطفئها لتجربة فورية تماماً بدون حركة"
    override val minutesShortFmt: (String) -> String = { m -> "$m د" }
    override val toastLocationUpdatedFmt: (String) -> String = { loc -> "تم تحديد موقعك: $loc" }
    override val toastLocationFailed = "تعذر تحديد الموقع — تأكد من تفعيل GPS أو اختر مدينتك يدوياً"
    override val toastReminderOnFmt: (String) -> String = { m -> "تم تفعيل التنبيه قبل الصلاة بـ $m دقيقة" }
    override val toastReminderOff = "تم إيقاف تنبيه قبل الصلاة"
    override val toastNotifPermissionDenied = "يرجى السماح بالإشعارات لتفعيل تنبيهات الصلاة"
    override val notifTabEyebrow = "صندوق التركيز"
    override val notifTabTitle = "الإشعارات"
    override val notifClearAll = "مسح الكل"
    override val notifPermTitle = "تفعيل حجب الإشعارات في وضع التركيز"
    override val notifPermSub = "اسمح لسكون بالوصول للإشعارات لإخفائها تماماً وتجميعها هنا أثناء وضع التركيز"
    override val notifPermGrantBtn = "تفعيل الصلاحية"
    override val notifShieldActive = "الحجب نشط — كل الإشعارات الواردة تُجمع هنا بهدوء"
    override val notifShieldPaused = "وضع التركيز متوقف — فعّله لحجب الإشعارات وتجميعها هنا"
    override val notifEmptyTitle = "هدوء تام"
    override val notifEmptySub = "لا توجد إشعارات محجوزة حالياً. عند تفعيل وضع التركيز ستصل الإشعارات هنا بدون إزعاج."
    override val notifAccessSettingsTitle = "حجب الإشعارات في وضع التركيز"
    override val notifAccessActive = "صلاحية الوصول للإشعارات مفعلة ✓"
    override val notifAccessPrompt = "اضغط لتفعيل «سكون» من الوصول للإشعارات"
    override val quranSpacingSection = "تباعد سطور القرآن"
    override val quranSpacingSubtitle = "التحكم في المسافة الرأسية بين سطور المصحف"
    override val spacingCompact = "متقارب"
    override val spacingComfortable = "مريح"
    override val spacingWide = "واسع"
    override val spacingExtraWide = "واسع جداً"
    override val tabsOrderSection = "ترتيب التابات (يمين وشمال الرئيسية)"
    override val tabsOrderSubtitle = "حرّك التابة لأعلى لتكون على يمين الرئيسية، أو لأسفل لتكون على شمالها."
    override val hiddenLabel = "مخفية"
    override val centerHomeLabel = "المنتصف (الرئيسية)"
    override val rightOfHomeFmt: (String) -> String = { n -> "يمين الرئيسية ($n)" }
    override val leftOfHomeFmt: (String) -> String = { n -> "شمال الرئيسية ($n)" }
    override val enabledLabel = "مفعلة"
    override val appearanceSection = "المظهر"
    override val darkOled = "داكن OLED"
    override val lightPaper = "فاتح ورقي"
    override val systemSettingsSection = "إعدادات النظام"
    override val fingerprintTasbihTitle = "التسبيح بحساس البصمة الخلفية (اختياري)"
    override val accessibilityActive = "الخدمة مفعلة ونشطة ✓"
    override val accessibilityPrompt = "للأجهزة المدعومة — اضغط لتفعيل «سكون» من إمكانية الوصول"
    override val defaultLauncherTitle = "تعيين «سكون» كلانشر افتراضي"
    override val defaultLauncherSubtitle = "اختيار سكون كشاشة رئيسية دائمة للتلفون"

    override val toastAccessibilityFallback = "افتح الإعدادات ← إمكانية الوصول ← التطبيقات المثبتة ← سكون"
    override val toastStayedCalm = "قرار ممتاز — حافظ على تركيزك"
    override val toastRemovedFavFmt: (String) -> String = { l -> "تمت إزالة $l من المفضلة" }
    override val toastAddedFavFmt: (String) -> String = { l -> "تمت إضافة $l للمفضلة" }
    override val toastDisabledMindfulFmt: (String) -> String = { l -> "تم إلغاء وقفة التأمل عن $l" }
    override val toastEnabledMindfulFmt: (String) -> String = { l -> "تم تفعيل وقفة التأمل لـ $l" }
    override val toastFocusEnabled = "تم تفعيل وضع التركيز — إخفاء المشتتات"
    override val toastFocusDisabled = "تم إيقاف وضع التركيز"
    override val toastWirdDone = "تقبل الله! تم تسجيل إتمام ورد اليوم"
    override val toastDhikrCompletedFmt: (String) -> String = { s -> "ما شاء الله! أتممت «$s»" }
    override val toastDhikrAlreadyDone = "أتممت هذا الذكر — اضغط تصفير للإعادة"
    override val toastCounterReset = "تم تصفير العداد"
    override val toastDhikrAdded = "تمت إضافة الذكر"
    override val toastDhikrDeleted = "تم حذف الذكر"
    override val toastMorningAzkarDone = "ما شاء الله! أتممت أذكار الصباح"
    override val toastEveningAzkarDone = "ما شاء الله! أتممت أذكار المساء"
    override val toastMorningAzkarReset = "تمت إعادة أذكار الصباح"
    override val toastEveningAzkarReset = "تمت إعادة أذكار المساء"
    override val toastNewDayHabits = "يوم جديد! تم تحديث الاستمرارية وتصفير المهام"
    override val toastNoteDeleted = "تم حذف الملاحظة"
    override val toastSessionDone = "أحسنت! انتهت الجلسة بنجاح"
    override val toastVolumeError = "تعذر تعديل الصوت (تحقق من وضع عدم الإزعاج)"
    override val toastMuted = "تم كتم الصوت"
    override val toastUnmuted = "تم استعادة الصوت"
    override val toastCityUpdatedFmt: (String) -> String = { c -> "تم تحديث مواقيت الصلاة لـ $c" }
    override val toastFontUpdatedFmt: (String) -> String = { f -> "تم تغيير الخط إلى $f" }
    override val toastLanguageUpdated = "تم تغيير اللغة إلى العربية"
}

object EnglishStrings : AppStrings {
    override val isArabic = false
    override val done = "Done"
    override val close = "Close"
    override val cancel = "Cancel"
    override val save = "Save"
    override val delete = "Delete"
    override val add = "Add"
    override val reset = "Reset"
    override val open = "Open"
    override val openChevron = "Open ›"
    override val backArrow = "← Back"
    override val edit = "Edit"
    override val remove = "Remove"
    override val settings = "Settings"
    override val percentSymbol = "%"

    override val focusOn = "Focus Mode On"
    override val normalMode = "Normal Mode"
    override val searchAndVolume = "Search • Volume"
    override val favorites = "Favorites"
    override val mindfulTag = "Mindful"
    override val hiddenDistractingFmt: (Int) -> String = { n -> "$n distracting app(s) hidden in Focus Mode" }
    override val allAppsUp = "All Apps ↑"
    override val changeCityHint = "Change city"

    override val holyQuran = "The Holy Quran"
    override val juzPageFmt: (Int, Int) -> String = { j, p -> "Juz $j • Page $p of 604" }
    override val fontMinus = "A-"
    override val fontPlus = "A+"
    override val paperMode = "Paper"
    override val jumpToPageLabel = "Go to page (1 - 604):"
    override val go = "Go"
    override val loadingMushaf = "Loading Mushaf..."
    override val prevPage = "← Previous"
    override val nextPage = "Next →"

    override val autoTimeMorning = "Auto by time • Morning"
    override val autoTimeEvening = "Auto by time • Evening"
    override val morningAzkar = "Morning Azkar"
    override val eveningAzkar = "Evening Azkar"
    override val morning = "Morning"
    override val evening = "Evening"
    override val azkarDoneFmt: (Int, Int) -> String = { done, total -> "Completed $done of $total" }
    override val blackScreen = "Black Screen"
    override val completedAlhamdulillah = "Completed ✓"
    override val tapToCount = "Tap to count"
    override val prevItem = "← Prev"
    override val nextItem = "Next →"

    override val serenityAndDhikr = "Serenity & Remembrance"
    override val adhkarAndTasbih = "Adhkar & Tasbih"
    override val addDhikrBtn = "+ Dhikr"
    override val blackScreenTitle = "Black-Screen Mode (AMOLED Off)"
    override val blackScreenSubtitle = "Pure black screen — tap anywhere to count with gentle haptic"
    override val launchChevron = "Start ›"
    override val dhikrTextHint = "Dhikr text (e.g. SubhanAllah)"
    override val targetLabel = "Target:"
    override val ofTargetFmt: (String) -> String = { t -> "of $t" }
    override val dhikrTapHint = "Tap anywhere to count • Auto-advances when done"
    override val minusTarget = "- Target"
    override val plusTarget = "+ Target"
    override val zeroCounter = "Reset"
    override val longPressToExit = "Long-press to exit"

    override val repeatsDaily = "Repeats daily"
    override val dailyHabits = "Daily Habits"
    override val newDayBtn = "New Day ↻"
    override val completedTodayFmt: (String, String) -> String = { d, t -> "Completed $d of $t today" }
    override val streakDaysFmt: (String) -> String = { s -> "Streak: $s days" }
    override val addHabitHint = "Add a daily recurring habit..."

    override val quickThoughts = "Quick Thoughts"
    override val notesTitle = "Notes"
    override val newNoteBtn = "+ New"
    override val pinnedStar = "Pinned ★"
    override val pin = "Pin"
    override val noteTitleHint = "Note title..."
    override val noteBodyHint = "Write your note here... auto-saved"
    override val autoSaved = "Auto-saved"
    override val noNotesYet = "No notes yet — tap «+ New»"
    override val untitledNote = "Untitled Note"
    override val pinnedDot = "Pinned • "
    override val tapToWrite = "Tap to write..."

    override val deepSession = "Deep Session"
    override val pomodoroTitle = "Pomodoro"
    override val sessionsTodayFmt: (String) -> String = { n -> "Sessions today: $n" }
    override val focusModeName = "Focus"
    override val shortBreakModeName = "Short Break"
    override val longBreakModeName = "Long Break"
    override val focusChip = "Focus 25"
    override val shortBreakChip = "Break 5"
    override val longBreakChip = "Break 15"
    override val runningFmt: (String) -> String = { m -> "$m — Running..." }
    override val readyFmt: (String) -> String = { m -> "Ready for $m" }
    override val pomoTaskHint = "What are you focusing on?"
    override val pauseSession = "Pause"
    override val startSession = "Start Session"
    override val autoFocusToggle = "Auto-enable Focus Mode during session"

    override val searchAppsHint = "Search apps..."
    override val drawerLegend = "★ Add to Favorites • «Mindful» adds a 5-second pause before opening"
    override val searchOrCalcHint = "Search apps or calculate (e.g. 25*4)..."
    override val quickJump = "Quick Jump"
    override val quickJumpQuranSubFmt: (String) -> String = { p -> "Page $p" }
    override val quickJumpDhikrSub = "Tasbih & Adhkar"
    override val quickJumpHabitsSub = "Repeats daily"
    override val quickJumpSettingsTitle = "Settings & Customization"
    override val quickJumpSettingsSub = "Language, font, prayer city"
    override val appsLabel = "Apps"
    override val quickVolumeControl = "Quick Volume Control"
    override val unmute = "Unmute"
    override val quickMute = "Quick Mute"
    override val mediaVol = "Media"
    override val ringVol = "Ring"
    override val alarmVol = "Alarm"
    override val beforeOpeningFmt: (String) -> String = { a -> "Before opening $a" }
    override val mindfulPromptFmt: (String) -> String = { a -> "Take a deep breath... do you really need to open $a right now?" }
    override val returnToCalm = "Return to Calm"
    override val waitSecondsFmt: (String) -> String = { s -> "Wait $s seconds..." }
    override val openAppFmt: (String) -> String = { a -> "Open $a" }

    override val customizeSukoon = "Customize Sukoon"
    override val settingsTitle = "Settings"
    override val catAll = "All"
    override val catDisplay = "Display"
    override val catSpiritual = "Spiritual"
    override val catFocus = "Focus & Nav"
    override val catSystem = "System"
    override val appDisplaySection = "App Display Style"
    override val appDisplaySubtitle = "Choose how apps appear on Home & App Drawer"
    override val styleTextOnly = "Text Only"
    override val styleTextOnlySub = "Pure minimalist"
    override val styleMonochrome = "Unified Icons"
    override val styleMonochromeSub = "Calm monochrome"
    override val styleOriginal = "Original Icons"
    override val styleOriginalSub = "Native colors"
    override val focusMindfulSection = "Focus & Mindful Pause"
    override val mindfulDurationLabel = "Mindful pause duration"
    override val hapticsLabel = "Haptic feedback"
    override val restartSetupTitle = "Restart Guided Setup"
    override val restartSetupSub = "Revisit the first-launch onboarding flow"
    override val resetAppTitle = "Reset App Data"
    override val resetAppSub = "Clear local data and restore defaults"
    override val confirmResetAppMsg = "Reset all preferences and restart Sukoon setup?"
    override val obWelcomeEyebrow = "WELCOME TO SUKOON"
    override val obWelcomeHeading = "A little more calm.\nA little more intention."
    override val obWelcomeDesc = "A personal space for your daily focus, spiritual habits, and meaningful moments."
    override val obGetStarted = "Get Started"
    override val obLangTitle = "Choose your language"
    override val obLangSub = "Interface text and layout direction adapt immediately"
    override val obExpTitle = "Choose your experience"
    override val obExpSub = "Select the spaces that serve your day. Change anytime."
    override val obProductivity = "Productivity"
    override val obSpiritual = "Spiritual"
    override val obHomeCenterNote = "Home stays anchored in the center"
    override val obAppearanceTitle = "Appearance & Type"
    override val obAppearanceSub = "Preview theme and typography in real time"
    override val obLocationTitle = "Prayer-time location"
    override val obLocationSub = "Select your city for accurate offline prayer calculations"
    override val obNavTitle = "Customize navigation"
    override val obNavSub = "Arrange tabs to the left and right of Home"
    override val obReadyHeading = "Your space. Your pace."
    override val obReadyDesc = "Everything is ready. Welcome to your Sukoon."
    override val obEnterSukoon = "Enter Sukoon"
    override val nextLabel = "Next"
    override val skipLabel = "Skip"
    override val backLabel = "Back"
    override val languageSection = "Language"
    override val languageSubtitle = "Choose the app interface language"
    override val fontSection = "App Font"
    override val fontSubtitle = "Change the font used across the app"
    override val fontWeightSection = "Font Weight"
    override val weightLight = "Light"
    override val weightRegular = "Regular"
    override val weightBold = "Bold"
    override val themesSection = "Color Themes"
    override val prayerCitySection = "Location (Prayer Times)"
    override val prayerCitySubtitle = "Choose your city for accurate prayer times"
    override val searchCityHint = "Search city or country..."
    override val changeCityBtn = "Change"
    override val detectLocationBtn = "Detect my location (GPS)"
    override val detectingLocation = "Detecting location..."
    override val gpsActiveLabel = "Using your GPS location"
    override val prayerReminderTitle = "Pre-Prayer Reminder"
    override val prayerReminderSub = "Gentle notification before each prayer time"
    override val reminderBeforeLabel = "Remind me before prayer"
    override val adhanTitle = "Adhan at Prayer Time"
    override val adhanSub = "Play your chosen Muezzin voice at every prayer"
    override val muezzinVoiceLabel = "Muezzin Voice"
    override val previewAudio = "▶ Preview"
    override val stopAudio = "■ Stop"
    override val qiblaTitle = "Qibla Compass"
    override val qiblaSub = "Accurate offline Qibla direction"
    override val qiblaAligned = "Aligned with Qibla ✓"
    override val qiblaTurnHint = "Point the top of your phone toward the golden marker"
    override val qiblaAngleFmt: (String) -> String = { deg -> "Qibla bearing: $deg° from North" }
    override val qiblaDistanceFmt: (String) -> String = { km -> "Distance to Kaaba: $km km" }
    override val qiblaNoSensor = "No magnetometer sensor detected — use the exact bearing from North above."
    override val qiblaCalibrateHint = "For best accuracy, move your phone in a figure-8 motion"
    override val toastAdhanOnFmt: (String) -> String = { name -> "Adhan enabled ($name)" }
    override val toastAdhanOff = "Adhan disabled"
    override val prayerAutoFocusTitle = "Auto-Focus at Prayer Times"
    override val prayerAutoFocusSub = "Auto-enable Focus Mode & Notification Shield at each prayer, then restore automatically"
    override val prayerAutoFocusDurationLabel = "Calm duration at prayer"
    override val prayerAutoFocusActivePill = "Prayer Calm Active"
    override val toastPrayerAutoFocusOnFmt: (String) -> String = { m -> "Prayer Auto-Focus enabled (${m}m)" }
    override val toastPrayerAutoFocusOff = "Prayer Auto-Focus disabled"
    override val prayerTrackerTitle = "Daily Prayer Tracker"
    override val prayerTrackerSub = "Tap any prayer on Home to mark ✓ with a calm daily progress indicator"
    override val prayersTrackedFmt: (String, String) -> String = { d, t -> "$d / $t ✓" }
    override val prayerTrackerTapHint = "Tap any prayer to mark ✓"
    override val mindfulTimeBoxTitle = "Time-Boxed Mindful Pause"
    override val mindfulTimeBoxSub = "Choose intended duration (5 / 10 / 15m) with a gentle reminder when time is up"
    override val mindfulChooseDuration = "How long do you intend to stay?"
    override val mindfulOpenWithoutTimer = "Open without timer"
    override val toastTimeBoxScheduledFmt: (String, String) -> String = { m, app -> "Gentle reminder set for ${m}m in $app" }
    override val animationsTitle = "Smooth Animations & Motion"
    override val animationsSub = "Gentle transitions & motion — turn off for instant zero-motion mode"
    override val minutesShortFmt: (String) -> String = { m -> "${m}m" }
    override val toastLocationUpdatedFmt: (String) -> String = { loc -> "Location set: $loc" }
    override val toastLocationFailed = "Could not detect location — enable GPS or pick manually"
    override val toastReminderOnFmt: (String) -> String = { m -> "Prayer reminder set ($m min before)" }
    override val toastReminderOff = "Pre-prayer reminders disabled"
    override val toastNotifPermissionDenied = "Please allow notifications to enable prayer reminders"
    override val notifTabEyebrow = "Focus Inbox"
    override val notifTabTitle = "Notifications"
    override val notifClearAll = "Clear All"
    override val notifPermTitle = "Enable Focus Notification Shield"
    override val notifPermSub = "Allow Sukoon notification access to silence and collect notifications here during Focus Mode"
    override val notifPermGrantBtn = "Enable Access"
    override val notifShieldActive = "Shield active — incoming notifications are quietly held here"
    override val notifShieldPaused = "Focus Mode is off — turn it on to hold notifications here"
    override val notifEmptyTitle = "Complete calm"
    override val notifEmptySub = "No held notifications yet. When Focus Mode is on, notifications are collected here without interruption."
    override val notifAccessSettingsTitle = "Focus Notification Shield"
    override val notifAccessActive = "Notification access active ✓"
    override val notifAccessPrompt = "Tap to enable «Sukoon» in Notification Access"
    override val quranSpacingSection = "Quran Line Spacing"
    override val quranSpacingSubtitle = "Adjust vertical line-height between Quran lines"
    override val spacingCompact = "Compact"
    override val spacingComfortable = "Comfortable"
    override val spacingWide = "Wide"
    override val spacingExtraWide = "Extra Wide"
    override val tabsOrderSection = "Tabs Order (Left & Right of Home)"
    override val tabsOrderSubtitle = "Move a tab up or down to arrange pages around Home."
    override val hiddenLabel = "Hidden"
    override val centerHomeLabel = "Center (Home)"
    override val rightOfHomeFmt: (String) -> String = { n -> "Right of Home ($n)" }
    override val leftOfHomeFmt: (String) -> String = { n -> "Left of Home ($n)" }
    override val enabledLabel = "Visible"
    override val appearanceSection = "Appearance"
    override val darkOled = "Dark OLED"
    override val lightPaper = "Light Paper"
    override val systemSettingsSection = "System Settings"
    override val fingerprintTasbihTitle = "Rear Fingerprint Tasbih (Optional)"
    override val accessibilityActive = "Service active ✓"
    override val accessibilityPrompt = "For supported devices — tap to enable «Sukoon» in Accessibility"
    override val defaultLauncherTitle = "Set «Sukoon» as Default Launcher"
    override val defaultLauncherSubtitle = "Make Sukoon your permanent home screen"

    override val toastAccessibilityFallback = "Open Settings → Accessibility → Installed Apps → Sukoon"
    override val toastStayedCalm = "Great choice — stay focused"
    override val toastRemovedFavFmt: (String) -> String = { l -> "Removed $l from favorites" }
    override val toastAddedFavFmt: (String) -> String = { l -> "Added $l to favorites" }
    override val toastDisabledMindfulFmt: (String) -> String = { l -> "Disabled mindful pause for $l" }
    override val toastEnabledMindfulFmt: (String) -> String = { l -> "Enabled mindful pause for $l" }
    override val toastFocusEnabled = "Focus Mode enabled — distractions hidden"
    override val toastFocusDisabled = "Focus Mode disabled"
    override val toastWirdDone = "May Allah accept it! Daily Wird completed"
    override val toastDhikrCompletedFmt: (String) -> String = { s -> "MashaAllah! Completed «$s»" }
    override val toastDhikrAlreadyDone = "Completed — tap Reset to recount"
    override val toastCounterReset = "Counter reset"
    override val toastDhikrAdded = "Dhikr added"
    override val toastDhikrDeleted = "Dhikr deleted"
    override val toastMorningAzkarDone = "MashaAllah! Morning Azkar completed"
    override val toastEveningAzkarDone = "MashaAllah! Evening Azkar completed"
    override val toastMorningAzkarReset = "Morning Azkar reset"
    override val toastEveningAzkarReset = "Evening Azkar reset"
    override val toastNewDayHabits = "New day! Streaks updated and habits reset"
    override val toastNoteDeleted = "Note deleted"
    override val toastSessionDone = "Well done! Session finished"
    override val toastVolumeError = "Unable to change volume (check Do Not Disturb)"
    override val toastMuted = "Sound muted"
    override val toastUnmuted = "Sound restored"
    override val toastCityUpdatedFmt: (String) -> String = { c -> "Prayer times updated for $c" }
    override val toastFontUpdatedFmt: (String) -> String = { f -> "App font changed to $f" }
    override val toastLanguageUpdated = "Language changed to English"
}

fun stringsFor(lang: String): AppStrings =
    if (lang == "en") EnglishStrings else ArabicStrings

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { ArabicStrings }
