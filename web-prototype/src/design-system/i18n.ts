import type { AppLanguage, TabId } from '../types';

export function formatDigits(value: number | string, isArabic: boolean): string {
  const s = String(value);
  if (!isArabic) return s;
  return s.replace(/\d/g, (d) => '٠١٢٣٤٥٦٧٨٩'[Number(d)]);
}

export interface TabMeta {
  id: TabId;
  category: 'productivity' | 'spiritual' | 'core';
  labelAr: string;
  shortAr: string;
  labelEn: string;
  shortEn: string;
  descAr: string;
  descEn: string;
  required?: boolean;
}

export const TAB_META: Record<TabId, TabMeta> = {
  pomodoro: {
    id: 'pomodoro',
    category: 'productivity',
    labelAr: 'البومودورو',
    shortAr: 'بومودورو',
    labelEn: 'Pomodoro',
    shortEn: 'Pomodoro',
    descAr: 'جلسات تركيز عميقة بلا تشتت',
    descEn: 'Deep-work timer linked with Focus Mode',
  },
  notes: {
    id: 'notes',
    category: 'productivity',
    labelAr: 'الملاحظات',
    shortAr: 'الملاحظات',
    labelEn: 'Notes',
    shortEn: 'Notes',
    descAr: 'أفكار سريعة تُحفظ تلقائياً',
    descEn: 'Minimal scratchpad with instant auto-save',
  },
  habits: {
    id: 'habits',
    category: 'productivity',
    labelAr: 'المهام اليومية',
    shortAr: 'المهام',
    labelEn: 'Daily Habits',
    shortEn: 'Habits',
    descAr: 'عادات متكررة يومياً مع عدّاد استمرارية',
    descEn: 'Recurring daily checklist with streaks',
  },
  home: {
    id: 'home',
    category: 'core',
    labelAr: 'الشاشة الرئيسية',
    shortAr: 'الرئيسية',
    labelEn: 'Home',
    shortEn: 'Home',
    descAr: 'مركز يومك الهادئ (أساسية)',
    descEn: 'Your calm daily command center (required)',
    required: true,
  },
  quran: {
    id: 'quran',
    category: 'spiritual',
    labelAr: 'القرآن الكريم',
    shortAr: 'القرآن',
    labelEn: 'Quran',
    shortEn: 'Quran',
    descAr: 'المصحف الكامل ٦٠٤ صفحة بدون إنترنت',
    descEn: 'Full 604-page offline Mushaf reader',
  },
  azkar: {
    id: 'azkar',
    category: 'spiritual',
    labelAr: 'الأذكار',
    shortAr: 'الأذكار',
    labelEn: 'Azkar',
    shortEn: 'Azkar',
    descAr: 'أذكار الصباح والمساء تلقائياً حسب الوقت',
    descEn: 'Morning & Evening remembrance cards',
  },
  tasbih: {
    id: 'tasbih',
    category: 'spiritual',
    labelAr: 'المسبحة',
    shortAr: 'المسبحة',
    labelEn: 'Tasbih',
    shortEn: 'Tasbih',
    descAr: 'عدّاد تسبيح هادئ مع وضع الشاشة السوداء',
    descEn: 'Tactile digital Dhikr counter & black mode',
  },
};

export interface Dictionary {
  isArabic: boolean;
  // Common
  done: string;
  close: string;
  cancel: string;
  save: string;
  delete: string;
  confirm: string;
  add: string;
  reset: string;
  open: string;
  back: string;
  next: string;
  skip: string;
  edit: string;
  remove: string;
  settings: string;
  search: string;
  percent: string;
  // Onboarding
  obWelcomeEyebrow: string;
  obWelcomeHeading: string;
  obWelcomeDesc: string;
  obGetStarted: string;
  obLangTitle: string;
  obLangSubtitle: string;
  obExpTitle: string;
  obExpSubtitle: string;
  obProductivity: string;
  obSpiritual: string;
  obHomeAlwaysOn: string;
  obSelectedPreview: string;
  obAppearanceTitle: string;
  obAppearanceSubtitle: string;
  obThemeLabel: string;
  obFontLabel: string;
  obLocationTitle: string;
  obLocationSubtitle: string;
  obUseCurrentLocation: string;
  obLocating: string;
  obLocationFailed: string;
  obNavTitle: string;
  obNavSubtitle: string;
  obDragHint: string;
  obReadyHeading: string;
  obReadyDesc: string;
  obEnterSukoon: string;
  obStepOf: (cur: number, total: number) => string;
  // Home
  focusActive: string;
  focusInactive: string;
  searchAction: string;
  favorites: string;
  mindfulBadge: string;
  hiddenAppsNote: (count: number) => string;
  allApps: string;
  changeCity: string;
  // Quran
  holyQuran: string;
  juzPage: (juz: number, page: number) => string;
  paperMode: string;
  jumpToPage: string;
  go: string;
  loadingMushaf: string;
  prevPage: string;
  nextPage: string;
  lineSpacing: string;
  spacingCompact: string;
  spacingComfortable: string;
  spacingWide: string;
  spacingExtraWide: string;
  // Azkar
  morningAzkar: string;
  eveningAzkar: string;
  autoMorning: string;
  autoEvening: string;
  morning: string;
  evening: string;
  azkarCompleted: (done: number, total: number) => string;
  blackScreen: string;
  completedCheck: string;
  tapToCount: string;
  prevZikr: string;
  nextZikr: string;
  // Tasbih
  tasbihEyebrow: string;
  tasbihTitle: string;
  addDhikr: string;
  dhikrPlaceholder: string;
  target: string;
  ofTarget: (n: string) => string;
  tapAnywhereHint: string;
  confirmResetTitle: string;
  confirmResetDesc: string;
  confirmDeleteDhikrTitle: string;
  confirmDeleteDhikrDesc: string;
  blackScreenBannerTitle: string;
  blackScreenBannerSub: string;
  longPressToExit: string;
  blackScreenWebNote: string;
  // Habits
  habitsEyebrow: string;
  habitsTitle: string;
  newDay: string;
  habitsProgress: (done: number, total: number) => string;
  streakDays: (days: number) => string;
  addHabitPlaceholder: string;
  noHabitsEmpty: string;
  // Notes
  notesEyebrow: string;
  notesTitle: string;
  newNote: string;
  searchNotesPlaceholder: string;
  pinned: string;
  pin: string;
  noteTitlePlaceholder: string;
  noteBodyPlaceholder: string;
  autoSaved: string;
  saving: string;
  noNotesEmpty: string;
  untitledNote: string;
  tapToWrite: string;
  confirmDeleteNoteTitle: string;
  confirmDeleteNoteDesc: string;
  // Pomodoro
  pomoEyebrow: string;
  pomoTitle: string;
  sessionsToday: (n: number) => string;
  pomoFocus: string;
  pomoShort: string;
  pomoLong: string;
  pomoFocusChip: string;
  pomoShortChip: string;
  pomoLongChip: string;
  pomoRunning: (mode: string) => string;
  pomoPaused: (mode: string) => string;
  pomoReady: (mode: string) => string;
  pomoTaskPlaceholder: string;
  start: string;
  pause: string;
  resume: string;
  autoFocusLabel: string;
  // Overlays
  appDrawerTitle: string;
  searchAppsPlaceholder: string;
  appDrawerWebNote: string;
  drawerLegend: string;
  universalSearchPlaceholder: string;
  quickShortcuts: string;
  appsSection: string;
  notesResults: string;
  quranJumpResult: (page: number) => string;
  volumePreviewTitle: string;
  volumeWebNote: string;
  mute: string;
  unmute: string;
  media: string;
  ring: string;
  alarm: string;
  // Mindful Pause
  beforeOpening: (app: string) => string;
  mindfulPrompt: (app: string) => string;
  returnToCalm: string;
  waitSeconds: (s: number) => string;
  openApp: (app: string) => string;
  // Settings
  settingsEyebrow: string;
  settingsTitle: string;
  catAll: string;
  catDisplay: string;
  catSpiritual: string;
  catFocus: string;
  catSystem: string;
  secAppDisplay: string;
  secAppDisplaySub: string;
  styleTextOnly: string;
  styleTextOnlySub: string;
  styleMonochrome: string;
  styleMonochromeSub: string;
  styleOriginal: string;
  styleOriginalSub: string;
  secLanguage: string;
  secFonts: string;
  secLocation: string;
  secQuran: string;
  secAppearance: string;
  secNavigation: string;
  secVolume: string;
  secFocusMindful: string;
  secSystem: string;
  darkOled: string;
  lightPaper: string;
  searchCityPlaceholder: string;
  centerAnchor: string;
  hidden: string;
  visible: string;
  rightOfHome: (n: number) => string;
  leftOfHome: (n: number) => string;
  mindfulDurationLabel: string;
  hapticsLabel: string;
  restartSetup: string;
  restartSetupDesc: string;
  resetApp: string;
  resetAppDesc: string;
  confirmResetAppTitle: string;
  confirmResetAppDesc: string;
  webPlatformNote: string;
  // Toasts
  toastOpenedApp: (name: string) => string;
  toastStayedCalm: string;
  toastFocusOn: string;
  toastFocusOff: string;
  toastCityUpdated: (city: string) => string;
  toastNewDay: string;
  toastSessionDone: string;
  toastDhikrDone: (short: string) => string;
  toastAzkarDone: string;
  toastResetDone: string;
}

export const AR_DICT: Dictionary = {
  isArabic: true,
  done: 'تم',
  close: 'إغلاق',
  cancel: 'إلغاء',
  save: 'حفظ',
  delete: 'حذف',
  confirm: 'تأكيد',
  add: 'إضافة',
  reset: 'إعادة',
  open: 'فتح',
  back: 'رجوع',
  next: 'التالي',
  skip: 'تخطي',
  edit: 'تعديل',
  remove: 'إزالة',
  settings: 'الإعدادات',
  search: 'بحث',
  percent: '٪',

  obWelcomeEyebrow: 'مرحباً بك في سكون',
  obWelcomeHeading: 'مزيد من الهدوء. مزيد من القصد.',
  obWelcomeDesc: 'مساحتك الشخصية للتركيز اليومي، العادات الروحية، واللحظات ذات المعنى — بلا ضجيج أو تشتت.',
  obGetStarted: 'ابدأ الإعداد',
  obLangTitle: 'اختر لغتك',
  obLangSubtitle: 'تتكيف الواجهة والاتجاه فوراً حسب اختيارك',
  obExpTitle: 'صمّم تجربتك',
  obExpSubtitle: 'اختر الأقسام التي تحتاجها في يومك. يمكنك تعديلها في أي وقت.',
  obProductivity: 'الإنتاجية والتركيز',
  obSpiritual: 'السكينة والعبادة',
  obHomeAlwaysOn: 'الشاشة الرئيسية مفعّلة دائماً في المنتصف',
  obSelectedPreview: 'الأقسام المختارة',
  obAppearanceTitle: 'المظهر والخط',
  obAppearanceSubtitle: 'معاينة فورية للمظهر والخط قبل الدخول',
  obThemeLabel: 'المظهر',
  obFontLabel: 'الخط',
  obLocationTitle: 'موقع مواقيت الصلاة',
  obLocationSubtitle: 'اختر مدينتك لحساب مواقيت الصلاة بدقة بدون إنترنت',
  obUseCurrentLocation: 'تحديد أقرب مدينة تلقائياً',
  obLocating: 'جاري تحديد الموقع...',
  obLocationFailed: 'تعذر الوصول للموقع — اختر مدينتك يدوياً',
  obNavTitle: 'تخصيص التنقل',
  obNavSubtitle: 'رتّب التابات بالسحب أو الأسهم حول الشاشة الرئيسية',
  obDragHint: 'اسحب لترتيب الصفحات أو استخدم الأسهم',
  obReadyHeading: 'مساحتك. على إيقاعك.',
  obReadyDesc: 'كل شيء جاهز الآن. أهلاً بك في سكون.',
  obEnterSukoon: 'ادخل إلى سكون',
  obStepOf: (cur, total) => `خطوة ${formatDigits(cur, true)} من ${formatDigits(total, true)}`,

  focusActive: 'وضع التركيز مفعل',
  focusInactive: 'وضع التركيز',
  searchAction: 'بحث',
  favorites: 'المفضلة',
  mindfulBadge: 'تأمل',
  hiddenAppsNote: (n) => `تم إخفاء ${formatDigits(n, true)} تطبيق مشتت أثناء وضع التركيز`,
  allApps: 'كل التطبيقات',
  changeCity: 'تغيير المدينة',

  holyQuran: 'القرآن الكريم',
  juzPage: (j, p) => `الجزء ${formatDigits(j, true)} • صفحة ${formatDigits(p, true)} من ٦٠٤`,
  paperMode: 'ورقي',
  jumpToPage: 'انتقال لصفحة (١ - ٦٠٤)',
  go: 'انتقال',
  loadingMushaf: 'جاري تحميل المصحف الشريف...',
  prevPage: 'السابقة',
  nextPage: 'التالية',
  lineSpacing: 'تباعد السطور',
  spacingCompact: 'متقارب',
  spacingComfortable: 'مريح',
  spacingWide: 'واسع',
  spacingExtraWide: 'واسع جداً',

  morningAzkar: 'أذكار الصباح',
  eveningAzkar: 'أذكار المساء',
  autoMorning: 'تلقائي حسب الوقت • الصباح',
  autoEvening: 'تلقائي حسب الوقت • المساء',
  morning: 'الصباح',
  evening: 'المساء',
  azkarCompleted: (d, t) => `تم ${formatDigits(d, true)} من ${formatDigits(t, true)} أذكار`,
  blackScreen: 'شاشة سوداء',
  completedCheck: 'تم بحمد الله ✓',
  tapToCount: 'المس للعد',
  prevZikr: 'السابق',
  nextZikr: 'التالي',

  tasbihEyebrow: 'طمأنينة وذكر',
  tasbihTitle: 'المسبحة',
  addDhikr: 'ذكر جديد',
  dhikrPlaceholder: 'نص الذكر (مثلاً: لا حول ولا قوة إلا بالله)',
  target: 'العدد المستهدف',
  ofTarget: (n) => `من ${n}`,
  tapAnywhereHint: 'المس الدائرة أو أي مكان في المساحة للعد',
  confirmResetTitle: 'تصفير العدّاد؟',
  confirmResetDesc: 'سيتم إعادة عدّاد هذا الذكر إلى الصفر.',
  confirmDeleteDhikrTitle: 'حذف الذكر؟',
  confirmDeleteDhikrDesc: 'هل أنت متأكد من حذف هذا الذكر من القائمة؟',
  blackScreenBannerTitle: 'وضع الشاشة السوداء (AMOLED)',
  blackScreenBannerSub: 'تعتيم كامل — المس أي مكان للعد بهزة خفيفة',
  longPressToExit: 'اضغط مطولاً للخروج (أو Esc)',
  blackScreenWebNote: 'معاينة ويب: التحكم الفعلي في السطوع وإخفاء شريط النظام يعمل في نسخة أندرويد.',

  habitsEyebrow: 'تتكرر يومياً',
  habitsTitle: 'المهام اليومية',
  newDay: 'يوم جديد',
  habitsProgress: (d, t) => `أنجزت ${formatDigits(d, true)} من ${formatDigits(t, true)} اليوم`,
  streakDays: (s) => `استمرارية ${formatDigits(s, true)} يوم`,
  addHabitPlaceholder: 'أضف عادة أو مهمة يومية...',
  noHabitsEmpty: 'لا توجد مهام يومية بعد. أضف أول عادة لك بالأسفل.',

  notesEyebrow: 'أفكار سريعة',
  notesTitle: 'الملاحظات',
  newNote: 'جديد',
  searchNotesPlaceholder: 'ابحث في الملاحظات...',
  pinned: 'مثبتة',
  pin: 'تثبيت',
  noteTitlePlaceholder: 'عنوان الملاحظة...',
  noteBodyPlaceholder: 'اكتب أفكارك هنا... تُحفظ تلقائياً',
  autoSaved: 'محفوظة تلقائياً',
  saving: 'جاري الحفظ...',
  noNotesEmpty: 'لا توجد ملاحظات بعد — ابدأ بإنشاء ملاحظة جديدة.',
  untitledNote: 'بدون عنوان',
  tapToWrite: 'اضغط للكتابة...',
  confirmDeleteNoteTitle: 'حذف الملاحظة؟',
  confirmDeleteNoteDesc: 'لا يمكن التراجع عن حذف هذه الملاحظة.',

  pomoEyebrow: 'جلسة عميقة',
  pomoTitle: 'البومودورو',
  sessionsToday: (n) => `جلسات اليوم: ${formatDigits(n, true)}`,
  pomoFocus: 'تركيز',
  pomoShort: 'راحة قصيرة',
  pomoLong: 'راحة طويلة',
  pomoFocusChip: 'تركيز ٢٥',
  pomoShortChip: 'راحة ٥',
  pomoLongChip: 'راحة ١٥',
  pomoRunning: (m) => `${m} — جاري الآن`,
  pomoPaused: (m) => `${m} — متوقف مؤقتاً`,
  pomoReady: (m) => `جاهز لـ ${m}`,
  pomoTaskPlaceholder: 'بتركز على إيه في الجلسة دي؟',
  start: 'ابدأ الجلسة',
  pause: 'إيقاف مؤقت',
  resume: 'استئناف',
  autoFocusLabel: 'تفعيل وضع التركيز تلقائياً مع جلسة العمل',

  appDrawerTitle: 'كل التطبيقات',
  searchAppsPlaceholder: 'ابحث في التطبيقات...',
  appDrawerWebNote: 'تطبيقات محاكاة لمعاينة الويب — في نسخة أندرويد تظهر تطبيقات جهازك الفعلية.',
  drawerLegend: '★ للمفضلة  •  تأمل: وقفة قبل الفتح',
  universalSearchPlaceholder: 'ابحث في التطبيقات أو احسب (25*4)...',
  quickShortcuts: 'انتقال سريع',
  appsSection: 'التطبيقات',
  notesResults: 'الملاحظات',
  quranJumpResult: (p) => `الانتقال إلى صفحة ${formatDigits(p, true)} في المصحف`,
  volumePreviewTitle: 'التحكم السريع في الصوت',
  volumeWebNote: 'معاينة ويب — التحكم الفعلي بصوت النظام يعمل في نسخة أندرويد.',
  mute: 'كتم سريع',
  unmute: 'إلغاء الكتم',
  media: 'الوسائط',
  ring: 'النغمة',
  alarm: 'المنبه',

  beforeOpening: (a) => `قبل فتح ${a}`,
  mindfulPrompt: (a) => `خذ نفساً عميقاً... هل تحتاج حقاً لفتح ${a} الآن؟`,
  returnToCalm: 'رجوع إلى الهدوء',
  waitSeconds: (s) => `انتظر ${formatDigits(s, true)} ثوانٍ...`,
  openApp: (a) => `فتح ${a}`,

  settingsEyebrow: 'تخصيص سكون',
  settingsTitle: 'الإعدادات',
  catAll: 'الكل',
  catDisplay: 'المظهر والعرض',
  catSpiritual: 'الروحانيات',
  catFocus: 'التركيز والتنقل',
  catSystem: 'النظام',
  secAppDisplay: 'طريقة عرض التطبيقات',
  secAppDisplaySub: 'اختر كيف تظهر التطبيقات في الرئيسية والدرج',
  styleTextOnly: 'نص فقط',
  styleTextOnlySub: 'مينيماليست هادئ',
  styleMonochrome: 'أيقونات موحدة',
  styleMonochromeSub: 'أحادي متناسق',
  styleOriginal: 'أيقونات أصلية',
  styleOriginalSub: 'بألوانها الطبيعية',
  secLanguage: 'اللغة',
  secFonts: 'خط التطبيق',
  secLocation: 'الموقع (مواقيت الصلاة)',
  secQuran: 'قراءة القرآن',
  secAppearance: 'المظهر',
  secNavigation: 'التنقل وترتيب التابات',
  secVolume: 'الصوت (معاينة)',
  secFocusMindful: 'التركيز ووقفة التأمل',
  secSystem: 'النظام وإعادة الإعداد',
  darkOled: 'داكن OLED',
  lightPaper: 'فاتح ورقي',
  searchCityPlaceholder: 'ابحث عن مدينتك أو دولتك...',
  centerAnchor: 'المنتصف (أساسية)',
  hidden: 'مخفية',
  visible: 'مفعلة',
  rightOfHome: (n) => `يمين الرئيسية (${formatDigits(n, true)})`,
  leftOfHome: (n) => `شمال الرئيسية (${formatDigits(n, true)})`,
  mindfulDurationLabel: 'مدة وقفة التأمل',
  hapticsLabel: 'الاهتزاز اللمسي (عند الدعم)',
  restartSetup: 'إعادة جولة الإعداد الأولي',
  restartSetupDesc: 'فتح شاشات الترحيب والإعداد من جديد',
  resetApp: 'إعادة ضبط التطبيق بالكامل',
  resetAppDesc: 'مسح جميع البيانات والعودة للإعدادات الافتراضية',
  confirmResetAppTitle: 'إعادة ضبط سكون؟',
  confirmResetAppDesc: 'سيتم مسح جميع ملاحظاتك ومهامك وتفضيلاتك وإعادة الإعداد من البداية.',
  webPlatformNote: 'نسخة الويب التفاعلية — جميع الإعدادات والبيانات تُحفظ محلياً في المتصفح ومفصولة عبر Platform Adapter للتحويل السلس إلى أندرويد.',

  toastOpenedApp: (name) => `فتح ${name} (محاكاة ويب)`,
  toastStayedCalm: 'قرار هادئ — حافظ على تركيزك',
  toastFocusOn: 'تم تفعيل وضع التركيز',
  toastFocusOff: 'تم إيقاف وضع التركيز',
  toastCityUpdated: (c) => `تم تحديث مواقيت الصلاة لـ ${c}`,
  toastNewDay: 'يوم جديد — تم تحديث الاستمرارية',
  toastSessionDone: 'أحسنت! انتهت الجلسة بنجاح',
  toastDhikrDone: (s) => `ما شاء الله — أتممت «${s}»`,
  toastAzkarDone: 'ما شاء الله — أتممت الأذكار',
  toastResetDone: 'تم التصفير',
};

export const EN_DICT: Dictionary = {
  isArabic: false,
  done: 'Done',
  close: 'Close',
  cancel: 'Cancel',
  save: 'Save',
  delete: 'Delete',
  confirm: 'Confirm',
  add: 'Add',
  reset: 'Reset',
  open: 'Open',
  back: 'Back',
  next: 'Next',
  skip: 'Skip',
  edit: 'Edit',
  remove: 'Remove',
  settings: 'Settings',
  search: 'Search',
  percent: '%',

  obWelcomeEyebrow: 'WELCOME TO SUKOON',
  obWelcomeHeading: 'A little more calm. A little more intention.',
  obWelcomeDesc: 'A personal space for your daily focus, spiritual habits, and meaningful moments.',
  obGetStarted: 'Get Started',
  obLangTitle: 'Choose your language',
  obLangSubtitle: 'Interface text and layout direction adapt immediately',
  obExpTitle: 'Choose your experience',
  obExpSubtitle: 'Select the spaces that serve your day. You can change this anytime.',
  obProductivity: 'Productivity',
  obSpiritual: 'Spiritual',
  obHomeAlwaysOn: 'Home stays anchored in the center',
  obSelectedPreview: 'Selected spaces',
  obAppearanceTitle: 'Appearance & Type',
  obAppearanceSubtitle: 'Preview theme and typography in real time',
  obThemeLabel: 'Theme',
  obFontLabel: 'Typography',
  obLocationTitle: 'Prayer-time location',
  obLocationSubtitle: 'Select your city for accurate offline prayer calculations',
  obUseCurrentLocation: 'Use nearest city automatically',
  obLocating: 'Finding nearest city...',
  obLocationFailed: 'Location unavailable — please pick a city manually',
  obNavTitle: 'Customize navigation',
  obNavSubtitle: 'Drag or use arrows to arrange tabs around Home',
  obDragHint: 'Drag rows to reorder or toggle visibility',
  obReadyHeading: 'Your space. Your pace.',
  obReadyDesc: 'Everything is ready. Welcome to your Sukoon.',
  obEnterSukoon: 'Enter Sukoon',
  obStepOf: (cur, total) => `Step ${cur} of ${total}`,

  focusActive: 'Focus On',
  focusInactive: 'Focus',
  searchAction: 'Search',
  favorites: 'Favorites',
  mindfulBadge: 'Mindful',
  hiddenAppsNote: (n) => `${n} distracting app${n > 1 ? 's' : ''} hidden in Focus Mode`,
  allApps: 'All Apps',
  changeCity: 'Change city',

  holyQuran: 'The Holy Quran',
  juzPage: (j, p) => `Juz ${j} • Page ${p} of 604`,
  paperMode: 'Paper',
  jumpToPage: 'Jump to page (1 – 604)',
  go: 'Go',
  loadingMushaf: 'Loading Mushaf...',
  prevPage: 'Previous',
  nextPage: 'Next',
  lineSpacing: 'Line spacing',
  spacingCompact: 'Compact',
  spacingComfortable: 'Comfortable',
  spacingWide: 'Wide',
  spacingExtraWide: 'Extra Wide',

  morningAzkar: 'Morning Azkar',
  eveningAzkar: 'Evening Azkar',
  autoMorning: 'Auto by time • Morning',
  autoEvening: 'Auto by time • Evening',
  morning: 'Morning',
  evening: 'Evening',
  azkarCompleted: (d, t) => `${d} of ${t} completed`,
  blackScreen: 'Black Screen',
  completedCheck: 'Completed ✓',
  tapToCount: 'Tap to count',
  prevZikr: 'Previous',
  nextZikr: 'Next',

  tasbihEyebrow: 'Remembrance',
  tasbihTitle: 'Tasbih',
  addDhikr: 'New Dhikr',
  dhikrPlaceholder: 'Dhikr text (e.g., SubhanAllah)',
  target: 'Target',
  ofTarget: (n) => `of ${n}`,
  tapAnywhereHint: 'Tap anywhere in this area to count',
  confirmResetTitle: 'Reset counter?',
  confirmResetDesc: 'This will reset the current Dhikr count back to zero.',
  confirmDeleteDhikrTitle: 'Delete Dhikr?',
  confirmDeleteDhikrDesc: 'Remove this custom Dhikr from your list?',
  blackScreenBannerTitle: 'AMOLED Black Screen',
  blackScreenBannerSub: 'Pure black canvas — tap anywhere to count',
  longPressToExit: 'Long press to exit (or press Esc)',
  blackScreenWebNote: 'Web preview: true system-bar hiding and minimum brightness require Android native.',

  habitsEyebrow: 'Repeats daily',
  habitsTitle: 'Daily Habits',
  newDay: 'New Day',
  habitsProgress: (d, t) => `${d} of ${t} completed today`,
  streakDays: (s) => `${s}d streak`,
  addHabitPlaceholder: 'Add a daily recurring habit...',
  noHabitsEmpty: 'No habits yet. Add your first daily intention below.',

  notesEyebrow: 'Scratchpad',
  notesTitle: 'Notes',
  newNote: 'New',
  searchNotesPlaceholder: 'Search notes...',
  pinned: 'Pinned',
  pin: 'Pin',
  noteTitlePlaceholder: 'Note title...',
  noteBodyPlaceholder: 'Write freely... saved automatically',
  autoSaved: 'Auto-saved',
  saving: 'Saving...',
  noNotesEmpty: 'No notes yet — tap New to capture a thought.',
  untitledNote: 'Untitled',
  tapToWrite: 'Tap to write...',
  confirmDeleteNoteTitle: 'Delete note?',
  confirmDeleteNoteDesc: 'This action cannot be undone.',

  pomoEyebrow: 'Deep work',
  pomoTitle: 'Pomodoro',
  sessionsToday: (n) => `Today: ${n}`,
  pomoFocus: 'Focus',
  pomoShort: 'Short Break',
  pomoLong: 'Long Break',
  pomoFocusChip: 'Focus 25',
  pomoShortChip: 'Break 5',
  pomoLongChip: 'Break 15',
  pomoRunning: (m) => `${m} — Running`,
  pomoPaused: (m) => `${m} — Paused`,
  pomoReady: (m) => `Ready for ${m}`,
  pomoTaskPlaceholder: 'What is your intention for this session?',
  start: 'Start Session',
  pause: 'Pause',
  resume: 'Resume',
  autoFocusLabel: 'Enable Focus Mode automatically during focus sessions',

  appDrawerTitle: 'All Apps',
  searchAppsPlaceholder: 'Search apps...',
  appDrawerWebNote: 'Simulated app list for web preview — native Android enumerates installed apps.',
  drawerLegend: '★ Favorite  •  Mindful: 5s pause before opening',
  universalSearchPlaceholder: 'Search apps, notes, or 25*4...',
  quickShortcuts: 'Quick Shortcuts',
  appsSection: 'Apps',
  notesResults: 'Notes',
  quranJumpResult: (p) => `Jump to Quran Page ${p}`,
  volumePreviewTitle: 'Quick Volume',
  volumeWebNote: 'Web preview — controlling device system volume requires Android native.',
  mute: 'Mute',
  unmute: 'Unmute',
  media: 'Media',
  ring: 'Ring',
  alarm: 'Alarm',

  beforeOpening: (a) => `Before opening ${a}`,
  mindfulPrompt: (a) => `Take a calm breath. Do you really need to open ${a} right now?`,
  returnToCalm: 'Return to Calm',
  waitSeconds: (s) => `Wait ${s}s...`,
  openApp: (a) => `Open ${a}`,

  settingsEyebrow: 'Preferences',
  settingsTitle: 'Settings',
  catAll: 'All',
  catDisplay: 'Display',
  catSpiritual: 'Spiritual',
  catFocus: 'Focus & Nav',
  catSystem: 'System',
  secAppDisplay: 'App Display Style',
  secAppDisplaySub: 'Choose how apps appear on Home & App Drawer',
  styleTextOnly: 'Text Only',
  styleTextOnlySub: 'Pure minimalist',
  styleMonochrome: 'Unified Icons',
  styleMonochromeSub: 'Calm monochrome',
  styleOriginal: 'Original Icons',
  styleOriginalSub: 'Native colors',
  secLanguage: 'Language',
  secFonts: 'Typography',
  secLocation: 'Location (Prayer Times)',
  secQuran: 'Quran Reading',
  secAppearance: 'Appearance',
  secNavigation: 'Navigation & Tabs',
  secVolume: 'Volume (Preview)',
  secFocusMindful: 'Focus & Mindful Pause',
  secSystem: 'System & Setup',
  darkOled: 'Dark OLED',
  lightPaper: 'Warm Paper',
  searchCityPlaceholder: 'Search city or country...',
  centerAnchor: 'Center (Anchor)',
  hidden: 'Hidden',
  visible: 'Visible',
  rightOfHome: (n) => `Right of Home (${n})`,
  leftOfHome: (n) => `Left of Home (${n})`,
  mindfulDurationLabel: 'Mindful pause duration',
  hapticsLabel: 'Haptic feedback (where supported)',
  restartSetup: 'Restart Guided Setup',
  restartSetupDesc: 'Revisit the first-launch onboarding flow',
  resetApp: 'Reset App Data',
  resetAppDesc: 'Clear all local data and restore defaults',
  confirmResetAppTitle: 'Reset Sukoon?',
  confirmResetAppDesc: 'All notes, habits, and preferences will be erased and setup will restart.',
  webPlatformNote: 'Web prototype — all data persists offline in localStorage/IndexedDB via an isolated Platform Adapter ready for Android conversion.',

  toastOpenedApp: (name) => `Opened ${name} (web preview)`,
  toastStayedCalm: 'Calm choice — stay present',
  toastFocusOn: 'Focus Mode enabled',
  toastFocusOff: 'Focus Mode disabled',
  toastCityUpdated: (c) => `Prayer times updated for ${c}`,
  toastNewDay: 'New day — streaks updated',
  toastSessionDone: 'Session complete — well done',
  toastDhikrDone: (s) => `Completed «${s}»`,
  toastAzkarDone: 'Azkar completed',
  toastResetDone: 'Counter reset',
};

export function getDict(lang: AppLanguage): Dictionary {
  return lang === 'en' ? EN_DICT : AR_DICT;
}
