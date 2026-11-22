// Verified Quranic pages for the Daily Wird preview
const QURAN_PAGES = [
  {
    pageNumber: '١',
    surahTitle: 'سورة الفاتحة',
    juz: 'الجزء ١',
    sections: [
      {
        surahName: 'سورة الفاتحة',
        showBasmala: false,
        ayahs: [
          { n: '١', t: 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ' },
          { n: '٢', t: 'الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ' },
          { n: '٣', t: 'الرَّحْمَٰنِ الرَّحِيمِ' },
          { n: '٤', t: 'مَالِكِ يَوْمِ الدِّينِ' },
          { n: '٥', t: 'إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ' },
          { n: '٦', t: 'اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ' },
          { n: '٧', t: 'صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ' }
        ]
      }
    ]
  },
  {
    pageNumber: '٢',
    surahTitle: 'سورة البقرة',
    juz: 'الجزء ١',
    sections: [
      {
        surahName: 'سورة البقرة',
        showBasmala: true,
        ayahs: [
          { n: '١', t: 'الم' },
          { n: '٢', t: 'ذَٰلِكَ الْكِتَابُ لَا رَيْبَ فِيهِ هُدًى لِّلْمُتَّقِينَ' },
          { n: '٣', t: 'الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ' },
          { n: '٤', t: 'وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ' },
          { n: '٥', t: 'أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ' }
        ]
      }
    ]
  },
  {
    pageNumber: '٥٩٦',
    surahTitle: 'سورة الضحى والشرح',
    juz: 'الجزء ٣٠',
    sections: [
      {
        surahName: 'سورة الضحى',
        showBasmala: true,
        ayahs: [
          { n: '١', t: 'وَالضُّحَىٰ' },
          { n: '٢', t: 'وَاللَّيْلِ إِذَا سَجَىٰ' },
          { n: '٣', t: 'مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ' },
          { n: '٤', t: 'وَلَلْآخِرَةُ خَيْرٌ لَّكَ مِنَ الْأُولَىٰ' },
          { n: '٥', t: 'وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ' },
          { n: '٦', t: 'أَلَمْ يَجِدْكَ يَتِيمًا فَآوَىٰ' },
          { n: '٧', t: 'وَوَجَدَكَ ضَالًّا فَهَدَىٰ' },
          { n: '٨', t: 'وَوَجَدَكَ عَائِلًا فَأَغْنَىٰ' },
          { n: '٩', t: 'فَأَمَّا الْيَتِيمَ فَلَا تَقْهَرْ' },
          { n: '١٠', t: 'وَأَمَّا السَّائِلَ فَلَا تَنْهَرْ' },
          { n: '١١', t: 'وَأَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ' }
        ]
      },
      {
        surahName: 'سورة الشرح',
        showBasmala: true,
        ayahs: [
          { n: '١', t: 'أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ' },
          { n: '٢', t: 'وَوَضَعْنَا عَنكَ وِزْرَكَ' },
          { n: '٣', t: 'الَّذِي أَنقَضَ ظَهْرَكَ' },
          { n: '٤', t: 'وَرَفَعْنَا لَكَ ذِكْرَكَ' },
          { n: '٥', t: 'فَإِنَّ مَعَ الْعُسْرِ يُسْرًا' },
          { n: '٦', t: 'إِنَّ مَعَ الْعُسْرِ يُسْرًا' },
          { n: '٧', t: 'فَإِذَا فَرَغْتَ فَانصَبْ' },
          { n: '٨', t: 'وَإِلَىٰ رَبِّكَ فَارْغَبْ' }
        ]
      }
    ]
  },
  {
    pageNumber: '٦٠٤',
    surahTitle: 'الإخلاص والمعوذتان',
    juz: 'الجزء ٣٠',
    sections: [
      {
        surahName: 'سورة الإخلاص',
        showBasmala: true,
        ayahs: [
          { n: '١', t: 'قُلْ هُوَ اللَّهُ أَحَدٌ' },
          { n: '٢', t: 'اللَّهُ الصَّمَدُ' },
          { n: '٣', t: 'لَمْ يَلِدْ وَلَمْ يُولَدْ' },
          { n: '٤', t: 'وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ' }
        ]
      },
      {
        surahName: 'سورة الفلق',
        showBasmala: true,
        ayahs: [
          { n: '١', t: 'قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ' },
          { n: '٢', t: 'مِن شَرِّ مَا خَلَقَ' },
          { n: '٣', t: 'وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ' },
          { n: '٤', t: 'وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ' },
          { n: '٥', t: 'وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ' }
        ]
      },
      {
        surahName: 'سورة الناس',
        showBasmala: true,
        ayahs: [
          { n: '١', t: 'قُلْ أَعُوذُ بِرَبِّ النَّاسِ' },
          { n: '٢', t: 'مَلِكِ النَّاسِ' },
          { n: '٣', t: 'إِلَٰهِ النَّاسِ' },
          { n: '٤', t: 'مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ' },
          { n: '٥', t: 'الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ' },
          { n: '٦', t: 'مِنَ الْجِنَّةِ وَالنَّاسِ' }
        ]
      }
    ]
  }
];

// Default Adhkar with custom targets
const DEFAULT_ADHKAR = [
  { id: 'subhan', text: 'سبحان الله', short: 'سبحان الله', target: 33, count: 0 },
  { id: 'hamd', text: 'الحمد لله', short: 'الحمد لله', target: 33, count: 0 },
  { id: 'akbar', text: 'الله أكبر', short: 'الله أكبر', target: 33, count: 0 },
  { id: 'istighfar', text: 'أستغفر الله العظيم وأتوب إليه', short: 'الاستغفار', target: 100, count: 0 },
  { id: 'salawat', text: 'اللهم صلِّ وسلِّم على نبينا محمد', short: 'الصلاة على النبي', target: 10, count: 0 }
];

// Default Daily Recurring Tasks (Habits)
const DEFAULT_HABITS = [
  { id: 1, text: 'صلاة الفجر في وقتها', done: true, streak: 12 },
  { id: 2, text: 'قراءة الورد اليومي من القرآن', done: false, streak: 7 },
  { id: 3, text: 'أذكار الصباح والمساء', done: false, streak: 5 },
  { id: 4, text: 'تمرين أو مشي ٣٠ دقيقة', done: false, streak: 4 },
  { id: 5, text: 'شرب ٢ لتر مياه', done: true, streak: 9 }
];

// Default Notes
const DEFAULT_NOTES = [
  {
    id: 1,
    title: 'أفكار لانشر سكون',
    body: '• تثبيت الألوان والخطوط الهادئة\n• دائرة التسبيح في مكان البصمة\n• إعادة ترتيب التابات من الإعدادات',
    pinned: true,
    updatedAt: 'اليوم'
  },
  {
    id: 2,
    title: 'قائمة كتب للقراءة',
    body: '١. العادات الذرية\n٢. العمل العميق (Deep Work)\n٣. فاتتني صلاة',
    pinned: false,
    updatedAt: 'أمس'
  }
];

// Page Metadata & Default Order (LTR track order: left pages -> home -> right pages)
// Swiping from RIGHT edge moves forward (+1) to quran, then dhikr
// Swiping from LEFT edge moves backward (-1) to habits, then notes, then pomodoro
const PAGE_META = {
  pomodoro: { id: 'pomodoro', label: 'البومودورو', short: 'بومودورو', required: false },
  notes:    { id: 'notes',    label: 'الملاحظات', short: 'الملاحظات', required: false },
  habits:   { id: 'habits',   label: 'المهام اليومية', short: 'المهام', required: false },
  home:     { id: 'home',     label: 'الشاشة الرئيسية', short: 'الرئيسية', required: true },
  quran:    { id: 'quran',    label: 'الورد القرآني', short: 'الورد', required: false },
  dhikr:    { id: 'dhikr',    label: 'الأذكار والمسبحة', short: 'الأذكار', required: false }
};

const DEFAULT_PAGE_CONFIG = [
  { id: 'pomodoro', enabled: true },
  { id: 'notes', enabled: true },
  { id: 'habits', enabled: true },
  { id: 'home', enabled: true },
  { id: 'quran', enabled: true },
  { id: 'dhikr', enabled: true }
];

// Text-only minimalist apps catalog (NO logos/icons)
const DEFAULT_APPS = [
  { id: 'phone', name: 'الهاتف', favorite: true, distracting: false, unread: 0 },
  { id: 'whatsapp', name: 'واتساب', favorite: true, distracting: false, unread: 3 },
  { id: 'notes', name: 'الملاحظات', favorite: true, distracting: false, unread: 0 },
  { id: 'camera', name: 'الكاميرا', favorite: true, distracting: false, unread: 0 },
  { id: 'maps', name: 'الخرائط', favorite: true, distracting: false, unread: 0 },
  { id: 'calendar', name: 'التقويم', favorite: true, distracting: false, unread: 0 },
  { id: 'youtube', name: 'يوتيوب', favorite: true, distracting: true, unread: 0 },
  { id: 'settings', name: 'الإعدادات', favorite: false, distracting: false, unread: 0 },
  { id: 'browser', name: 'المتصفح', favorite: false, distracting: false, unread: 0 },
  { id: 'telegram', name: 'تيليجرام', favorite: false, distracting: false, unread: 1 },
  { id: 'gmail', name: 'البريد', favorite: false, distracting: false, unread: 2 },
  { id: 'instagram', name: 'إنستغرام', favorite: false, distracting: true, unread: 0 },
  { id: 'x', name: 'إكس', favorite: false, distracting: true, unread: 0 },
  { id: 'tiktok', name: 'تيك توك', favorite: false, distracting: true, unread: 0 },
  { id: 'bank', name: 'البنك', favorite: false, distracting: false, unread: 0 },
  { id: 'files', name: 'الملفات', favorite: false, distracting: false, unread: 0 },
  { id: 'clock', name: 'المنبه', favorite: false, distracting: false, unread: 0 },
  { id: 'calculator', name: 'الحاسبة', favorite: false, distracting: false, unread: 0 },
  { id: 'podcasts', name: 'البودكاست', favorite: false, distracting: false, unread: 0 },
  { id: 'books', name: 'الكتب', favorite: false, distracting: false, unread: 0 }
];

const MOCK_CONTACTS = [
  { name: 'أمي', detail: 'مكالمة سريعة' },
  { name: 'أحمد طارق', detail: 'واتساب • اتصال' },
  { name: 'محمود (الشغل)', detail: 'اتصال' },
  { name: 'د. كريم', detail: 'اتصال' }
];
