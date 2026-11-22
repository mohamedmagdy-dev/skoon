package com.sukoon.launcher.data

import kotlinx.serialization.Serializable

@Serializable
data class PageConfig(
    val id: String,
    val enabled: Boolean = true
)

@Serializable
data class DhikrItem(
    val id: String,
    val text: String,
    val short: String,
    val target: Int = 33,
    val count: Int = 0
)

@Serializable
data class HabitItem(
    val id: Long,
    val text: String,
    val done: Boolean = false,
    val streak: Int = 0
)

@Serializable
data class NoteItem(
    val id: Long,
    val title: String,
    val body: String,
    val pinned: Boolean = false,
    val updatedAt: String = "اليوم"
)

@Serializable
data class LauncherState(
    val onboardingStatus: String = "not_started", // "not_started" | "in_progress" | "completed"
    val onboardingStep: Int = 0,
    val darkTheme: Boolean = true,
    val appLanguage: String = "ar", // "ar" | "en"
    val appFontId: String = "ibm", // "ibm" | "tajawal" | "almarai" | "amiri" | "system"
    val appDisplayStyle: String = "text", // "text" | "monochrome" | "original"
    val prayerCityId: String = "alexandria",
    val useGpsLocation: Boolean = false,
    val customLat: Double? = null,
    val customLng: Double? = null,
    val customLocationLabel: String = "",
    val prayerReminderEnabled: Boolean = false,
    val prayerReminderMinutesBefore: Int = 10,
    val focusMode: Boolean = false,
    val mindfulPauseSeconds: Int = 5,
    val hapticsEnabled: Boolean = true,
    val pageConfig: List<PageConfig> = defaultPageConfig(),
    val favoritePackages: List<String> = emptyList(),
    val distractingPackages: List<String> = defaultDistractingPackages(),
    val habits: List<HabitItem> = defaultHabits(),
    val lastHabitDate: String = "",
    val notes: List<NoteItem> = defaultNotes(),
    val adhkar: List<DhikrItem> = defaultAdhkar(),
    val activeDhikrId: String = "subhan",
    val morningAzkar: List<ZikrCard> = AzkarData.defaultMorningAzkar(),
    val eveningAzkar: List<ZikrCard> = AzkarData.defaultEveningAzkar(),
    val morningAzkarIndex: Int = 0,
    val eveningAzkarIndex: Int = 0,
    val quranPage: Int = 1, // 1..604
    val quranFontSizeSp: Float = 22f,
    val quranLineHeightMultiplier: Float = 2.15f,
    val quranPaperMode: Boolean = false,
    val wirdDoneToday: Boolean = false,
    val lastWirdDate: String = "",
    val wirdStreak: Int = 1,
    val dailyWirdGoalPages: Int = 4,
    val wirdPagesReadToday: Int = 0,
    val pomoAutoFocus: Boolean = true,
    val pomoSessionsToday: Int = 0,
    val pomoTask: String = ""
)

data class AppInfo(
    val label: String,
    val packageName: String
)

@Serializable
data class QuranAyah(
    val n: Int,
    val t: String,
    val sn: String = "",
    val sb: Boolean = false
)

@Serializable
data class QuranPage(
    val p: Int,
    val j: Int,
    val s: String,
    val a: List<QuranAyah>
)

data class PageMeta(
    val id: String,
    val label: String,
    val short: String,
    val labelEn: String,
    val shortEn: String,
    val required: Boolean = false
) {
    fun localizedLabel(isArabic: Boolean): String = if (isArabic) label else labelEn
    fun localizedShort(isArabic: Boolean): String = if (isArabic) short else shortEn
}

val PAGE_META_MAP = mapOf(
    "pomodoro" to PageMeta("pomodoro", "البومودورو", "بومودورو", "Pomodoro", "Pomodoro"),
    "notes" to PageMeta("notes", "الملاحظات", "الملاحظات", "Notes", "Notes"),
    "habits" to PageMeta("habits", "المهام اليومية", "المهام", "Daily Habits", "Habits"),
    "notifications" to PageMeta("notifications", "إشعارات التركيز", "الإشعارات", "Focus Inbox", "Inbox"),
    "home" to PageMeta("home", "الشاشة الرئيسية", "الرئيسية", "Home Screen", "Home", required = true),
    "quran" to PageMeta("quran", "الورد القرآني", "الورد", "Quran", "Quran"),
    "azkar" to PageMeta("azkar", "أذكار الصباح والمساء", "الأذكار", "Morning & Evening Azkar", "Azkar"),
    "dhikr" to PageMeta("dhikr", "المسبحة", "المسبحة", "Tasbih", "Tasbih")
)

fun defaultPageConfig(): List<PageConfig> = listOf(
    PageConfig("pomodoro", true),
    PageConfig("notes", true),
    PageConfig("habits", true),
    PageConfig("notifications", true),
    PageConfig("home", true),
    PageConfig("quran", true),
    PageConfig("azkar", true),
    PageConfig("dhikr", true)
)

fun defaultDistractingPackages(): List<String> = listOf(
    "com.google.android.youtube",
    "com.instagram.android",
    "com.zhiliaoapp.musically",
    "com.ss.android.ugc.trill",
    "com.facebook.katana",
    "com.twitter.android",
    "com.snapchat.android"
)

fun defaultAdhkar(): List<DhikrItem> = listOf(
    DhikrItem("subhan", "سبحان الله", "سبحان الله", 33, 0),
    DhikrItem("hamd", "الحمد لله", "الحمد لله", 33, 0),
    DhikrItem("akbar", "الله أكبر", "الله أكبر", 33, 0),
    DhikrItem("istighfar", "أستغفر الله العظيم وأتوب إليه", "الاستغفار", 100, 0),
    DhikrItem("salawat", "اللهم صلِّ وسلِّم على نبينا محمد", "الصلاة على النبي", 10, 0)
)

fun defaultHabits(): List<HabitItem> = listOf(
    HabitItem(1, "صلاة الفجر في وقتها", false, 0),
    HabitItem(2, "قراءة الورد اليومي من القرآن", false, 0),
    HabitItem(3, "أذكار الصباح والمساء", false, 0),
    HabitItem(4, "مشي أو تمرين ٣٠ دقيقة", false, 0),
    HabitItem(5, "شرب ٢ لتر مياه", false, 0)
)

fun defaultNotes(): List<NoteItem> = listOf(
    NoteItem(
        id = 1,
        title = "مرحباً في سكون",
        body = "• اسحب من اليمين للورد القرآني والأذكار\n• اسحب من الشمال للمهام والملاحظات والبومودورو\n• اسحب لتحت للبحث السريع والتحكم في الصوت",
        pinned = true,
        updatedAt = "اليوم"
    )
)

fun Int.toArabicDigits(): String =
    this.toString().map { ch ->
        if (ch in '0'..'9') "٠١٢٣٤٥٦٧٨٩"[ch - '0'] else ch
    }.joinToString("")

fun String.toArabicDigits(): String =
    this.map { ch ->
        if (ch in '0'..'9') "٠١٢٣٤٥٦٧٨٩"[ch - '0'] else ch
    }.joinToString("")

fun Int.formatDigits(isArabic: Boolean): String =
    if (isArabic) toArabicDigits() else toString()

fun String.formatDigits(isArabic: Boolean): String =
    if (isArabic) toArabicDigits() else this
