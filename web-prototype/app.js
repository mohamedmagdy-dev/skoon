/* =========================================
   Sukoon (سكون) — Minimalist Launcher Logic v2
   ========================================= */

const STORAGE_KEY = 'sukoon_launcher_state_v2';

function loadState() {
  const defaults = {
    theme: 'dark',
    focusMode: false,
    pageConfig: JSON.parse(JSON.stringify(DEFAULT_PAGE_CONFIG)),
    apps: JSON.parse(JSON.stringify(DEFAULT_APPS)),
    habits: JSON.parse(JSON.stringify(DEFAULT_HABITS)),
    notes: JSON.parse(JSON.stringify(DEFAULT_NOTES)),
    adhkar: JSON.parse(JSON.stringify(DEFAULT_ADHKAR)),
    activeDhikrId: 'subhan',
    fpOffset: 14,
    quranPageIndex: 2,
    quranFontSize: 1.38,
    quranPaperMode: false,
    wirdDoneToday: false,
    wirdStreak: 7,
    pomoAutoFocus: true,
    pomoSessionsToday: 2,
    pomoTask: ''
  };

  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return defaults;
    const parsed = JSON.parse(raw);
    return { ...defaults, ...parsed };
  } catch {
    return defaults;
  }
}

let state = loadState();
let visiblePageIds = [];
let currentSlide = 0;
let editingFavorites = false;
let activeNoteId = null;
let toastTimer = null;

// Mindful Pause state
let mindfulInterval = null;
let pendingDistractingApp = null;

// Pomodoro state
let pomoTotalSeconds = 25 * 60;
let pomoSeconds = 25 * 60;
let pomoModeLabel = 'تركيز';
let pomoRunning = false;
let pomoInterval = null;

function saveState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch {
    // Ignore storage quota errors
  }
}

// =========================================
// DOM References
// =========================================
const rootEl = document.documentElement;
const viewport = document.getElementById('viewport');
const pagesTrack = document.getElementById('pagesTrack');
const pageDotsEl = document.getElementById('pageDots');
const desktopNavList = document.getElementById('desktopNavList');

const statusTime = document.getElementById('statusTime');
const homeClock = document.getElementById('homeClock');
const gregorianDate = document.getElementById('gregorianDate');
const hijriDate = document.getElementById('hijriDate');
const statusFocusDot = document.getElementById('statusFocusDot');

const favoritesList = document.getElementById('favoritesList');
const editFavBtn = document.getElementById('editFavBtn');
const openSettingsHomeBtn = document.getElementById('openSettingsHomeBtn');
const focusPill = document.getElementById('focusPill');
const focusPillDot = document.getElementById('focusPillDot');
const focusPillText = document.getElementById('focusPillText');
const prayerPill = document.getElementById('prayerPill');
const hintRightTab = document.getElementById('hintRightTab');
const hintLeftTab = document.getElementById('hintLeftTab');
const openDrawerBtn = document.getElementById('openDrawerBtn');
const homeIndicator = document.getElementById('homeIndicator');

// Quran DOM
const quranSurah = document.getElementById('quranSurah');
const quranJuz = document.getElementById('quranJuz');
const wirdProgressText = document.getElementById('wirdProgressText');
const wirdProgressFill = document.getElementById('wirdProgressFill');
const wirdStreak = document.getElementById('wirdStreak');
const mushafSheet = document.getElementById('mushafSheet');
const mushafContent = document.getElementById('mushafContent');
const quranPrevBtn = document.getElementById('quranPrevBtn');
const quranNextBtn = document.getElementById('quranNextBtn');
const quranDoneBtn = document.getElementById('quranDoneBtn');
const quranFontDec = document.getElementById('quranFontDec');
const quranFontInc = document.getElementById('quranFontInc');
const quranPaperToggle = document.getElementById('quranPaperToggle');

// Dhikr DOM
const toggleAddDhikrBtn = document.getElementById('toggleAddDhikrBtn');
const dhikrAddForm = document.getElementById('dhikrAddForm');
const newDhikrText = document.getElementById('newDhikrText');
const newDhikrTarget = document.getElementById('newDhikrTarget');
const cancelAddDhikrBtn = document.getElementById('cancelAddDhikrBtn');
const dhikrPills = document.getElementById('dhikrPills');
const dhikrPhrase = document.getElementById('dhikrPhrase');
const dhikrCurrent = document.getElementById('dhikrCurrent');
const dhikrTargetVal = document.getElementById('dhikrTargetVal');
const dhikrTargetDec = document.getElementById('dhikrTargetDec');
const dhikrTargetInc = document.getElementById('dhikrTargetInc');
const dhikrResetBtn = document.getElementById('dhikrResetBtn');
const dhikrDeleteBtn = document.getElementById('dhikrDeleteBtn');
const fpZone = document.getElementById('fpZone');
const fpCounterBtn = document.getElementById('fpCounterBtn');
const fpRingFill = document.getElementById('fpRingFill');
const fpRipple = document.getElementById('fpRipple');

// Habits DOM
const habitsList = document.getElementById('habitsList');
const habitsProgressText = document.getElementById('habitsProgressText');
const habitsPercent = document.getElementById('habitsPercent');
const habitsProgressFill = document.getElementById('habitsProgressFill');
const habitAddForm = document.getElementById('habitAddForm');
const habitInput = document.getElementById('habitInput');
const newDayHabitsBtn = document.getElementById('newDayHabitsBtn');

// Notes DOM
const addNoteBtn = document.getElementById('addNoteBtn');
const notesGrid = document.getElementById('notesGrid');
const noteEditorView = document.getElementById('noteEditorView');
const closeNoteEditorBtn = document.getElementById('closeNoteEditorBtn');
const pinNoteBtn = document.getElementById('pinNoteBtn');
const deleteNoteBtn = document.getElementById('deleteNoteBtn');
const noteTitleInput = document.getElementById('noteTitleInput');
const noteBodyInput = document.getElementById('noteBodyInput');
const noteSaveHint = document.getElementById('noteSaveHint');

// Pomodoro DOM
const pomoModes = document.getElementById('pomoModes');
const pomoRingProgress = document.getElementById('pomoRingProgress');
const pomoDisplay = document.getElementById('pomoDisplay');
const pomoStatus = document.getElementById('pomoStatus');
const pomoSessionsCount = document.getElementById('pomoSessionsCount');
const pomoTaskInput = document.getElementById('pomoTaskInput');
const pomoStartBtn = document.getElementById('pomoStartBtn');
const pomoResetBtn = document.getElementById('pomoResetBtn');
const pomoAutoFocusToggle = document.getElementById('pomoAutoFocusToggle');

// Overlays & Modals
const drawerOverlay = document.getElementById('drawerOverlay');
const drawerHandle = document.getElementById('drawerHandle');
const drawerSearch = document.getElementById('drawerSearch');
const drawerList = document.getElementById('drawerList');
const closeDrawerBtn = document.getElementById('closeDrawerBtn');

const searchOverlay = document.getElementById('searchOverlay');
const universalSearchInput = document.getElementById('universalSearchInput');
const universalSearchResults = document.getElementById('universalSearchResults');
const closeSearchBtn = document.getElementById('closeSearchBtn');

const settingsOverlay = document.getElementById('settingsOverlay');
const closeSettingsBtn = document.getElementById('closeSettingsBtn');
const reorderList = document.getElementById('reorderList');
const fpOffsetSlider = document.getElementById('fpOffsetSlider');
const settingsThemeDark = document.getElementById('settingsThemeDark');
const settingsThemeLight = document.getElementById('settingsThemeLight');

const mindfulModal = document.getElementById('mindfulModal');
const mindfulCountdown = document.getElementById('mindfulCountdown');
const mindfulTitle = document.getElementById('mindfulTitle');
const mindfulQuestion = document.getElementById('mindfulQuestion');
const mindfulStayBtn = document.getElementById('mindfulStayBtn');
const mindfulProceedBtn = document.getElementById('mindfulProceedBtn');

const appLaunchScreen = document.getElementById('appLaunchScreen');
const launchAppName = document.getElementById('launchAppName');
const launchAppNote = document.getElementById('launchAppNote');
const launchBackBtn = document.getElementById('launchBackBtn');

const toastEl = document.getElementById('toast');

// Desktop Controls
const openSettingsDesktopBtn = document.getElementById('openSettingsDesktopBtn');
const themeToggleBtn = document.getElementById('themeToggleBtn');
const themeLabel = document.getElementById('themeLabel');
const focusModeDesktopBtn = document.getElementById('focusModeDesktopBtn');
const focusLabelDesktop = document.getElementById('focusLabelDesktop');
const resetDemoBtn = document.getElementById('resetDemoBtn');
const guideOpenDrawer = document.getElementById('guideOpenDrawer');
const guideOpenSearch = document.getElementById('guideOpenSearch');

// =========================================
// Helpers
// =========================================
function showToast(message) {
  toastEl.textContent = message;
  toastEl.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastEl.classList.remove('show'), 2200);
}

function toArabicDigits(num) {
  return String(num).replace(/\d/g, d => '٠١٢٣٤٥٦٧٨٩'[d]);
}

function vibrate(pattern) {
  if (navigator.vibrate) {
    try { navigator.vibrate(pattern); } catch { /* ignore */ }
  }
}

// =========================================
// Clock & Dates
// =========================================
function updateClock() {
  const now = new Date();
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  homeClock.textContent = `${hh}:${mm}`;
  statusTime.textContent = toArabicDigits(`${hh}:${mm}`);

  try {
    gregorianDate.textContent = new Intl.DateTimeFormat('ar-EG', {
      weekday: 'long',
      day: 'numeric',
      month: 'long'
    }).format(now);

    hijriDate.textContent = new Intl.DateTimeFormat('ar-SA-u-ca-islamic', {
      day: 'numeric',
      month: 'long'
    }).format(now);
  } catch {
    // fallback in HTML
  }
}

// =========================================
// Dynamic Multi-Page Track & Reordering
// =========================================
function applyPageOrder(keepPageId = null) {
  const prevPageId = keepPageId || visiblePageIds[currentSlide] || 'home';
  visiblePageIds = [];

  const pageMap = {};
  pagesTrack.querySelectorAll('.page').forEach(el => {
    pageMap[el.dataset.pageId] = el;
  });

  state.pageConfig.forEach(cfg => {
    const el = pageMap[cfg.id];
    if (!el) return;
    pagesTrack.appendChild(el);
    if (cfg.enabled || cfg.id === 'home') {
      el.classList.remove('hidden-page');
      visiblePageIds.push(cfg.id);
    } else {
      el.classList.add('hidden-page');
    }
  });

  const count = visiblePageIds.length;
  pagesTrack.style.width = `${count * 100}%`;
  visiblePageIds.forEach(id => {
    pageMap[id].style.width = `${100 / count}%`;
  });

  // Rebuild dots
  pageDotsEl.innerHTML = '';
  visiblePageIds.forEach((id, idx) => {
    const dot = document.createElement('button');
    dot.className = `dot ${(id === 'quran' || id === 'dhikr') ? 'warm-dot' : ''}`;
    dot.title = PAGE_META[id].label;
    dot.addEventListener('click', () => goToSlide(idx));
    pageDotsEl.appendChild(dot);
  });

  // Update Home dynamic edge hints
  const homeIdx = visiblePageIds.indexOf('home');
  const rightId = visiblePageIds[homeIdx + 1];
  const leftId = visiblePageIds[homeIdx - 1];

  hintRightTab.textContent = rightId ? `‹ ${PAGE_META[rightId].short}` : '';
  hintRightTab.onclick = () => rightId && goToPageId(rightId);

  hintLeftTab.textContent = leftId ? `${PAGE_META[leftId].short} ›` : '';
  hintLeftTab.onclick = () => leftId && goToPageId(leftId);

  // Navigate to kept page or home
  const nextIdx = visiblePageIds.indexOf(prevPageId);
  goToSlide(nextIdx !== -1 ? nextIdx : homeIdx);
  renderDesktopNavList();
}

function goToSlide(index) {
  const count = visiblePageIds.length;
  if (!count) return;
  currentSlide = Math.max(0, Math.min(count - 1, index));
  const offset = -currentSlide * (100 / count);
  pagesTrack.classList.remove('dragging');
  pagesTrack.style.transform = `translateX(${offset}%)`;

  pageDotsEl.querySelectorAll('.dot').forEach((dot, idx) => {
    dot.classList.toggle('active', idx === currentSlide);
  });

  renderDesktopNavList();
}

function goToPageId(pageId) {
  const idx = visiblePageIds.indexOf(pageId);
  if (idx !== -1) goToSlide(idx);
}

function getRelativePositionLabel(pageId) {
  const homeIdx = visiblePageIds.indexOf('home');
  const idx = visiblePageIds.indexOf(pageId);
  if (idx === -1) return 'مخفية';
  if (idx === homeIdx) return 'المنتصف';
  if (idx > homeIdx) return `يمين (${toArabicDigits(idx - homeIdx)})`;
  return `شمال (${toArabicDigits(homeIdx - idx)})`;
}

function renderDesktopNavList() {
  desktopNavList.innerHTML = '';
  // Display in natural Arabic RTL reading order (rightmost first -> home -> leftmost)
  const displayIds = [...visiblePageIds].reverse();
  const activeId = visiblePageIds[currentSlide];

  displayIds.forEach(id => {
    const li = document.createElement('li');
    const btn = document.createElement('button');
    btn.className = `guide-jump ${id === activeId ? 'current' : ''}`;
    btn.innerHTML = `<span class="g-key">${PAGE_META[id].label}</span><span class="g-desc">${getRelativePositionLabel(id)}</span>`;
    btn.addEventListener('click', () => {
      closeOverlays();
      goToPageId(id);
    });
    li.appendChild(btn);
    desktopNavList.appendChild(li);
  });
}

// =========================================
// Swipe & Gesture Engine
// =========================================
let pointerStartX = 0;
let pointerStartY = 0;
let isPointerDown = false;
let swipeAxis = null;

viewport.addEventListener('pointerdown', (e) => {
  if (e.target.closest('input, textarea, button, .dhikr-pills')) return;
  if (document.querySelector('.overlay.open')) return;
  if (noteEditorView.classList.contains('open')) return;

  isPointerDown = true;
  swipeAxis = null;
  pointerStartX = e.clientX;
  pointerStartY = e.clientY;
});

window.addEventListener('pointermove', (e) => {
  if (!isPointerDown) return;
  const dx = e.clientX - pointerStartX;
  const dy = e.clientY - pointerStartY;

  if (!swipeAxis) {
    if (Math.abs(dx) > 10 && Math.abs(dx) > Math.abs(dy) * 1.15) {
      swipeAxis = 'x';
      pagesTrack.classList.add('dragging');
    } else if (Math.abs(dy) > 16 && Math.abs(dy) > Math.abs(dx) * 1.3) {
      swipeAxis = 'y';
    }
  }

  if (swipeAxis === 'x') {
    const count = visiblePageIds.length;
    const width = viewport.clientWidth || 368;
    const step = 100 / count;
    const basePercent = -currentSlide * step;
    const deltaPercent = (dx / width) * step;
    let target = basePercent + deltaPercent;
    const minTarget = -((count - 1) * step) - 3;
    if (target > 3) target = 3;
    if (target < minTarget) target = minTarget;
    pagesTrack.style.transform = `translateX(${target}%)`;
  }
});

window.addEventListener('pointerup', (e) => {
  if (!isPointerDown) return;
  isPointerDown = false;

  const dx = e.clientX - pointerStartX;
  const dy = e.clientY - pointerStartY;

  if (swipeAxis === 'x') {
    const threshold = 55;
    if (dx < -threshold && currentSlide < visiblePageIds.length - 1) {
      goToSlide(currentSlide + 1);
    } else if (dx > threshold && currentSlide > 0) {
      goToSlide(currentSlide - 1);
    } else {
      goToSlide(currentSlide);
    }
  } else if (swipeAxis === 'y' && visiblePageIds[currentSlide] === 'home') {
    if (dy < -55) openDrawer();
    else if (dy > 55) openUniversalSearch();
  }

  swipeAxis = null;
});

// Keyboard shortcuts
window.addEventListener('keydown', (e) => {
  if (document.activeElement && ['INPUT', 'TEXTAREA'].includes(document.activeElement.tagName)) {
    if (e.key === 'Escape') closeOverlays();
    return;
  }
  if (e.key === 'ArrowLeft') goToSlide(currentSlide + 1);
  if (e.key === 'ArrowRight') goToSlide(currentSlide - 1);
  if (e.key === 'ArrowUp' && visiblePageIds[currentSlide] === 'home') openDrawer();
  if (e.key === 'ArrowDown' && visiblePageIds[currentSlide] === 'home') openUniversalSearch();
  if (e.key === ' ' && visiblePageIds[currentSlide] === 'dhikr' && !document.querySelector('.overlay.open')) {
    e.preventDefault();
    incrementDhikr();
  }
  if (e.key === 'Escape') {
    closeOverlays();
    closeMindfulModal();
    closeLaunchScreen();
  }
});

// =========================================
// Overlays (Drawer, Search, Settings)
// =========================================
function closeOverlays() {
  [drawerOverlay, searchOverlay, settingsOverlay].forEach(el => {
    el.classList.remove('open');
    el.setAttribute('aria-hidden', 'true');
  });
  drawerSearch.blur();
  universalSearchInput.blur();
}

function openDrawer() {
  closeOverlays();
  drawerOverlay.classList.add('open');
  drawerOverlay.setAttribute('aria-hidden', 'false');
  drawerSearch.value = '';
  renderDrawer();
}

function openUniversalSearch() {
  closeOverlays();
  searchOverlay.classList.add('open');
  searchOverlay.setAttribute('aria-hidden', 'false');
  universalSearchInput.value = '';
  renderUniversalSearch('');
  setTimeout(() => universalSearchInput.focus(), 180);
}

function openSettings() {
  closeOverlays();
  settingsOverlay.classList.add('open');
  settingsOverlay.setAttribute('aria-hidden', 'false');
  renderReorderList();
}

// =========================================
// Settings: Reorder Tabs & Fingerprint Offset
// =========================================
function renderReorderList() {
  reorderList.innerHTML = '';
  // Display in natural RTL order: top = rightmost (highest index in state.pageConfig)
  const total = state.pageConfig.length;

  for (let visualIdx = 0; visualIdx < total; visualIdx++) {
    const configIdx = total - 1 - visualIdx;
    const cfg = state.pageConfig[configIdx];
    const meta = PAGE_META[cfg.id];
    const isHome = cfg.id === 'home';

    const li = document.createElement('li');
    li.className = `reorder-item ${isHome ? 'is-home' : ''} ${!cfg.enabled ? 'is-disabled' : ''}`;

    const info = document.createElement('div');
    info.className = 'reorder-info';
    info.innerHTML = `
      <span class="reorder-name">${meta.label}</span>
      <span class="reorder-pos">${getRelativePositionLabel(cfg.id)}</span>
    `;

    const actions = document.createElement('div');
    actions.className = 'reorder-actions';

    // Move Up (visually up = further to the right of Home = configIdx + 1)
    const upBtn = document.createElement('button');
    upBtn.className = 'reorder-arrow';
    upBtn.textContent = '↑';
    upBtn.title = 'تحريك لليمين';
    upBtn.disabled = visualIdx === 0;
    upBtn.addEventListener('click', () => {
      const tmp = state.pageConfig[configIdx];
      state.pageConfig[configIdx] = state.pageConfig[configIdx + 1];
      state.pageConfig[configIdx + 1] = tmp;
      saveState();
      applyPageOrder();
      renderReorderList();
    });

    // Move Down (visually down = further to the left of Home = configIdx - 1)
    const downBtn = document.createElement('button');
    downBtn.className = 'reorder-arrow';
    downBtn.textContent = '↓';
    downBtn.title = 'تحريك للشمال';
    downBtn.disabled = visualIdx === total - 1;
    downBtn.addEventListener('click', () => {
      const tmp = state.pageConfig[configIdx];
      state.pageConfig[configIdx] = state.pageConfig[configIdx - 1];
      state.pageConfig[configIdx - 1] = tmp;
      saveState();
      applyPageOrder();
      renderReorderList();
    });

    actions.appendChild(upBtn);
    actions.appendChild(downBtn);

    if (!meta.required) {
      const toggleBtn = document.createElement('button');
      toggleBtn.className = `q-icon-btn ${cfg.enabled ? 'active' : ''}`;
      toggleBtn.textContent = cfg.enabled ? 'مفعلة' : 'مخفية';
      toggleBtn.addEventListener('click', () => {
        cfg.enabled = !cfg.enabled;
        saveState();
        applyPageOrder();
        renderReorderList();
      });
      actions.appendChild(toggleBtn);
    }

    li.appendChild(info);
    li.appendChild(actions);
    reorderList.appendChild(li);
  }
}

function applyFpOffset() {
  fpZone.style.paddingBottom = `${state.fpOffset}px`;
  fpOffsetSlider.value = state.fpOffset;
}

// =========================================
// Theme & Focus Mode
// =========================================
function applyTheme() {
  rootEl.setAttribute('data-theme', state.theme);
  themeLabel.textContent = state.theme === 'dark' ? 'داكن OLED' : 'فاتح ورقي';
  settingsThemeDark.classList.toggle('active', state.theme === 'dark');
  settingsThemeLight.classList.toggle('active', state.theme === 'light');
}

function applyFocusMode() {
  statusFocusDot.classList.toggle('active', state.focusMode);
  focusPillDot.classList.toggle('focus-on', state.focusMode);
  focusPillText.textContent = state.focusMode ? 'وضع التركيز مفعل' : 'وضع عادي';
  focusLabelDesktop.textContent = state.focusMode ? 'مفعل' : 'متوقف';
  renderFavorites();
  renderDrawer();
}

function toggleFocusMode(forceVal = null) {
  state.focusMode = forceVal !== null ? forceVal : !state.focusMode;
  saveState();
  applyFocusMode();
  showToast(state.focusMode ? 'تم تفعيل وضع التركيز — إخفاء المشتتات' : 'تم إيقاف وضع التركيز');
}

// =========================================
// Favorites (Text-only) & App Launch
// =========================================
function renderFavorites() {
  favoritesList.innerHTML = '';
  const favs = state.apps.filter(a => a.favorite);
  let hiddenCount = 0;

  favs.forEach(app => {
    if (state.focusMode && app.distracting && !editingFavorites) {
      hiddenCount++;
      return;
    }

    const li = document.createElement('li');
    li.className = 'fav-item';

    const btn = document.createElement('button');
    btn.className = 'fav-item-btn';
    btn.innerHTML = `<span>${app.name}</span>`;

    if (app.unread > 0 && !state.focusMode) {
      const dot = document.createElement('span');
      dot.className = 'fav-unread-dot';
      btn.appendChild(dot);
    }

    if (app.distracting && !editingFavorites) {
      const tag = document.createElement('span');
      tag.className = 'fav-pause-tag';
      tag.textContent = 'وقفة تأمل';
      btn.appendChild(tag);
    }

    btn.addEventListener('click', () => {
      if (editingFavorites) return;
      handleAppClick(app);
    });

    li.appendChild(btn);

    if (editingFavorites) {
      const rmBtn = document.createElement('button');
      rmBtn.className = 'fav-remove-btn';
      rmBtn.textContent = 'إزالة';
      rmBtn.addEventListener('click', () => {
        app.favorite = false;
        saveState();
        renderFavorites();
        renderDrawer();
        showToast(`تمت إزالة ${app.name} من المفضلة`);
      });
      li.appendChild(rmBtn);
    }

    favoritesList.appendChild(li);
  });

  if (hiddenCount > 0) {
    const note = document.createElement('li');
    note.className = 'fav-hidden-note';
    note.textContent = `تم إخفاء ${toArabicDigits(hiddenCount)} تطبيق مشتت أثناء وضع التركيز`;
    favoritesList.appendChild(note);
  }
}

function handleAppClick(app) {
  if (app.id === 'settings') {
    openSettings();
    return;
  }
  if (app.id === 'notes' && visiblePageIds.includes('notes')) {
    goToPageId('notes');
    return;
  }
  if (app.distracting) {
    openMindfulModal(app);
  } else {
    openLaunchScreen(app.name, 'تطبيق أساسي • تم الفتح مباشرة بدون تشتيت');
  }
}

function openMindfulModal(app) {
  pendingDistractingApp = app;
  mindfulTitle.textContent = `قبل فتح ${app.name}`;
  mindfulQuestion.textContent = `خد نفس عميق.. هل فعلاً محتاج تفتح ${app.name} دلوقتي؟`;
  let sec = 5;
  mindfulCountdown.textContent = toArabicDigits(sec);
  mindfulProceedBtn.disabled = true;
  mindfulProceedBtn.textContent = `انتظر ${toArabicDigits(sec)} ثواني...`;

  mindfulModal.classList.add('open');
  mindfulModal.setAttribute('aria-hidden', 'false');

  clearInterval(mindfulInterval);
  mindfulInterval = setInterval(() => {
    sec--;
    if (sec <= 0) {
      clearInterval(mindfulInterval);
      mindfulCountdown.textContent = '✓';
      mindfulProceedBtn.disabled = false;
      mindfulProceedBtn.textContent = `فتح ${app.name} لمدة ٥ دقائق`;
    } else {
      mindfulCountdown.textContent = toArabicDigits(sec);
      mindfulProceedBtn.textContent = `انتظر ${toArabicDigits(sec)} ثواني...`;
    }
  }, 1000);
}

function closeMindfulModal() {
  clearInterval(mindfulInterval);
  mindfulModal.classList.remove('open');
  mindfulModal.setAttribute('aria-hidden', 'true');
  pendingDistractingApp = null;
}

function openLaunchScreen(appName, note) {
  launchAppName.textContent = appName;
  launchAppNote.textContent = note;
  appLaunchScreen.classList.add('open');
  appLaunchScreen.setAttribute('aria-hidden', 'false');
}

function closeLaunchScreen() {
  appLaunchScreen.classList.remove('open');
  appLaunchScreen.setAttribute('aria-hidden', 'true');
}

// =========================================
// App Drawer & Universal Search
// =========================================
function renderDrawer() {
  const query = drawerSearch.value.trim();
  drawerList.innerHTML = '';

  const filtered = state.apps
    .filter(a => a.name.includes(query))
    .sort((a, b) => a.name.localeCompare(b.name, 'ar'));

  if (!filtered.length) {
    drawerList.innerHTML = `<div class="empty-state">لا توجد تطبيقات مطابقة</div>`;
    return;
  }

  filtered.forEach(app => {
    const row = document.createElement('div');
    row.className = 'drawer-row';

    const btn = document.createElement('button');
    btn.className = 'drawer-app-btn';
    btn.innerHTML = `<span>${app.name}</span>${app.distracting ? '<span class="fav-pause-tag">تأمل</span>' : ''}`;
    btn.addEventListener('click', () => {
      closeOverlays();
      handleAppClick(app);
    });

    const star = document.createElement('button');
    star.className = `star-btn ${app.favorite ? 'starred' : ''}`;
    star.textContent = app.favorite ? '★' : '☆';
    star.addEventListener('click', () => {
      app.favorite = !app.favorite;
      saveState();
      renderFavorites();
      renderDrawer();
      showToast(app.favorite ? `تمت إضافة ${app.name} للمفضلة` : `تمت إزالة ${app.name} من المفضلة`);
    });

    row.appendChild(btn);
    row.appendChild(star);
    drawerList.appendChild(row);
  });
}

function evaluateMath(expr) {
  const cleaned = expr.replace(/[×x]/gi, '*').replace(/[÷]/g, '/').replace(/\s+/g, '');
  if (!/^[\d+\-*/().]+$/.test(cleaned) || !/[+\-*/]/.test(cleaned)) return null;
  try {
    const res = Function(`"use strict"; return (${cleaned})`)();
    if (typeof res === 'number' && Number.isFinite(res)) {
      return Number.isInteger(res) ? res : Number(res.toFixed(4));
    }
  } catch { return null; }
  return null;
}

function renderUniversalSearch(query) {
  const q = query.trim();
  universalSearchResults.innerHTML = '';

  if (!q) {
    universalSearchResults.innerHTML = `
      <div class="search-group-title">انتقال سريع</div>
      <button class="search-row" data-quick="quran"><span>الورد القرآني</span><span class="search-row-sub">صفحة ${QURAN_PAGES[state.quranPageIndex].pageNumber}</span></button>
      <button class="search-row" data-quick="dhikr"><span>الأذكار والمسبحة</span><span class="search-row-sub">دائرة البصمة</span></button>
      <button class="search-row" data-quick="habits"><span>المهام اليومية</span><span class="search-row-sub">تتكرر يومياً</span></button>
      <button class="search-row" data-quick="settings"><span>الإعدادات وترتيب التابات</span><span class="search-row-sub">تخصيص</span></button>
    `;
    universalSearchResults.querySelectorAll('[data-quick]').forEach(btn => {
      btn.addEventListener('click', () => {
        const a = btn.dataset.quick;
        closeOverlays();
        if (a === 'settings') openSettings();
        else goToPageId(a);
      });
    });
    return;
  }

  const calc = evaluateMath(q);
  if (calc !== null) {
    const card = document.createElement('div');
    card.className = 'search-calc-card';
    card.innerHTML = `<span class="calc-expr">${q} =</span><span class="calc-result">${calc}</span>`;
    universalSearchResults.appendChild(card);
  }

  const matchedApps = state.apps.filter(a => a.name.includes(q));
  if (matchedApps.length) {
    const h = document.createElement('div');
    h.className = 'search-group-title';
    h.textContent = 'التطبيقات';
    universalSearchResults.appendChild(h);

    matchedApps.forEach(app => {
      const btn = document.createElement('button');
      btn.className = 'search-row';
      btn.innerHTML = `<span>${app.name}</span><span class="search-row-sub">تطبيق</span>`;
      btn.addEventListener('click', () => { closeOverlays(); handleAppClick(app); });
      universalSearchResults.appendChild(btn);
    });
  }

  const matchedContacts = MOCK_CONTACTS.filter(c => c.name.includes(q));
  if (matchedContacts.length) {
    const h = document.createElement('div');
    h.className = 'search-group-title';
    h.textContent = 'جهات الاتصال';
    universalSearchResults.appendChild(h);

    matchedContacts.forEach(c => {
      const btn = document.createElement('button');
      btn.className = 'search-row';
      btn.innerHTML = `<span>${c.name}</span><span class="search-row-sub">${c.detail}</span>`;
      btn.addEventListener('click', () => { closeOverlays(); openLaunchScreen(`اتصال بـ ${c.name}`, c.detail); });
      universalSearchResults.appendChild(btn);
    });
  }

  if (!matchedApps.length && !matchedContacts.length && calc === null) {
    const webBtn = document.createElement('button');
    webBtn.className = 'search-row';
    webBtn.innerHTML = `<span>بحث على الويب عن "${q}"</span><span class="search-row-sub">المتصفح</span>`;
    webBtn.addEventListener('click', () => { closeOverlays(); openLaunchScreen('المتصفح', `نتائج البحث عن: ${q}`); });
    universalSearchResults.appendChild(webBtn);
  }
}

// =========================================
// Quran Wird Page
// =========================================
function renderQuranPage() {
  const page = QURAN_PAGES[state.quranPageIndex];
  quranSurah.textContent = page.surahTitle;
  quranJuz.textContent = `${page.juz} • صفحة ${page.pageNumber}`;

  const total = QURAN_PAGES.length;
  const currentNum = state.quranPageIndex + 1;
  wirdProgressText.textContent = state.wirdDoneToday
    ? 'ما شاء الله! أتممت ورد اليوم'
    : `الورد اليومي: صفحة ${toArabicDigits(currentNum)} من ${toArabicDigits(total)}`;
  wirdProgressFill.style.width = `${state.wirdDoneToday ? 100 : (currentNum / total) * 100}%`;
  wirdStreak.textContent = `${toArabicDigits(state.wirdStreak)} أيام متتالية`;

  quranDoneBtn.classList.toggle('completed', state.wirdDoneToday);
  quranDoneBtn.textContent = state.wirdDoneToday ? 'تم إتمام الورد ✓' : 'أتممت الورد ✓';
  quranPrevBtn.disabled = state.quranPageIndex === 0;
  quranNextBtn.disabled = state.quranPageIndex === total - 1;

  mushafSheet.classList.toggle('paper-mode', state.quranPaperMode);
  quranPaperToggle.classList.toggle('active', state.quranPaperMode);
  mushafContent.style.fontSize = `${state.quranFontSize}rem`;

  let html = '';
  page.sections.forEach(sec => {
    html += `<div class="surah-banner">${sec.surahName}</div>`;
    if (sec.showBasmala) html += `<div class="basmala">بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ</div>`;
    html += `<p class="ayah-paragraph">`;
    sec.ayahs.forEach(a => { html += `${a.t} <span class="ayah-num">﴿${a.n}﴾</span> `; });
    html += `</p>`;
  });

  mushafContent.innerHTML = html;
  mushafSheet.scrollTop = 0;
}

// =========================================
// Dhikr & Fingerprint-Zone Counter
// =========================================
function getActiveDhikr() {
  let d = state.adhkar.find(x => x.id === state.activeDhikrId);
  if (!d && state.adhkar.length) {
    d = state.adhkar[0];
    state.activeDhikrId = d.id;
  }
  return d;
}

function renderDhikr() {
  const active = getActiveDhikr();
  dhikrPills.innerHTML = '';

  state.adhkar.forEach(d => {
    const isDone = d.count >= d.target;
    const btn = document.createElement('button');
    btn.className = `dhikr-pill ${d.id === active.id ? 'active' : ''} ${isDone ? 'done' : ''}`;
    btn.textContent = `${d.short || d.text.slice(0, 14)}${isDone ? ' ✓' : ''}`;
    btn.addEventListener('click', () => {
      state.activeDhikrId = d.id;
      saveState();
      renderDhikr();
    });
    dhikrPills.appendChild(btn);
  });

  if (!active) return;

  dhikrPhrase.textContent = active.text;
  dhikrCurrent.textContent = toArabicDigits(active.count);
  dhikrTargetVal.textContent = toArabicDigits(active.target);
  dhikrDeleteBtn.hidden = state.adhkar.length <= 1;

  // Update SVG progress ring (circumference = 2 * PI * 54 ≈ 339.29)
  const circumference = 339.29;
  const ratio = Math.min(1, active.count / Math.max(1, active.target));
  fpRingFill.style.strokeDashoffset = circumference * (1 - ratio);
}

function incrementDhikr() {
  const active = getActiveDhikr();
  if (!active) return;

  // Tactile visual ripple
  fpCounterBtn.classList.add('tapped');
  fpRipple.classList.remove('pulse');
  void fpRipple.offsetWidth;
  fpRipple.classList.add('pulse');
  setTimeout(() => fpCounterBtn.classList.remove('tapped'), 110);

  if (active.count < active.target) {
    active.count++;
    saveState();
    renderDhikr();

    if (active.count >= active.target) {
      vibrate([30, 50, 40]);
      showToast(`ما شاء الله! أتممت «${active.short || active.text}»`);
      // Auto-advance to next unfinished dhikr
      const currIdx = state.adhkar.findIndex(x => x.id === active.id);
      const next = state.adhkar.slice(currIdx + 1).find(x => x.count < x.target)
        || state.adhkar.find(x => x.count < x.target);
      if (next && next.id !== active.id) {
        setTimeout(() => {
          state.activeDhikrId = next.id;
          saveState();
          renderDhikr();
        }, 550);
      }
    } else {
      vibrate(15);
    }
  } else {
    // Already completed — gently move to next or notify
    showToast('أتممت هذا الذكر — اضغط تصفير للإعادة');
  }
}

// =========================================
// Daily Recurring Habits Page
// =========================================
function renderHabits() {
  habitsList.innerHTML = '';
  const total = state.habits.length;
  const doneCount = state.habits.filter(h => h.done).length;
  const pct = total ? Math.round((doneCount / total) * 100) : 0;

  habitsProgressText.textContent = `تم إنجاز ${toArabicDigits(doneCount)} من ${toArabicDigits(total)} اليوم`;
  habitsPercent.textContent = `${toArabicDigits(pct)}٪`;
  habitsProgressFill.style.width = `${pct}%`;

  if (!total) {
    habitsList.innerHTML = `<li class="empty-state">أضف مهامك اليومية المتكررة هنا</li>`;
    return;
  }

  state.habits.forEach(habit => {
    const li = document.createElement('li');
    li.className = `habit-card ${habit.done ? 'done' : ''}`;

    const toggleBtn = document.createElement('button');
    toggleBtn.className = 'habit-toggle-btn';
    toggleBtn.innerHTML = `
      <span class="habit-check">${habit.done ? '✓' : ''}</span>
      <span class="habit-text-wrap">
        <span class="habit-title">${habit.text}</span>
        <span class="habit-streak">استمرارية: ${toArabicDigits(habit.streak)} أيام</span>
      </span>
    `;
    toggleBtn.addEventListener('click', () => {
      habit.done = !habit.done;
      vibrate(14);
      saveState();
      renderHabits();
    });

    const delBtn = document.createElement('button');
    delBtn.className = 'habit-del';
    delBtn.textContent = '×';
    delBtn.title = 'حذف';
    delBtn.addEventListener('click', () => {
      state.habits = state.habits.filter(h => h.id !== habit.id);
      saveState();
      renderHabits();
    });

    li.appendChild(toggleBtn);
    li.appendChild(delBtn);
    habitsList.appendChild(li);
  });
}

// =========================================
// Notes Page
// =========================================
function renderNotesList() {
  notesGrid.innerHTML = '';
  const sorted = [...state.notes].sort((a, b) => Number(b.pinned) - Number(a.pinned) || b.id - a.id);

  if (!sorted.length) {
    notesGrid.innerHTML = `<div class="empty-state">لا توجد ملاحظات بعد — اضغط «+ جديد»</div>`;
    return;
  }

  sorted.forEach(note => {
    const card = document.createElement('button');
    card.className = `note-card ${note.pinned ? 'pinned' : ''}`;
    card.innerHTML = `
      <div class="note-card-head">
        <span class="note-card-title">${note.title || 'ملاحظة بدون عنوان'}</span>
        <span class="note-card-meta">${note.pinned ? 'مثبتة • ' : ''}${note.updatedAt || 'اليوم'}</span>
      </div>
      <p class="note-card-preview">${note.body || 'اضغط للكتابة...'}</p>
    `;
    card.addEventListener('click', () => openNoteEditor(note.id));
    notesGrid.appendChild(card);
  });
}

function openNoteEditor(noteId) {
  const note = state.notes.find(n => n.id === noteId);
  if (!note) return;
  activeNoteId = note.id;
  noteTitleInput.value = note.title;
  noteBodyInput.value = note.body;
  pinNoteBtn.classList.toggle('active', note.pinned);
  pinNoteBtn.textContent = note.pinned ? 'مثبتة ★' : 'تثبيت';
  noteEditorView.classList.add('open');
  noteEditorView.setAttribute('aria-hidden', 'false');
}

function closeNoteEditor() {
  activeNoteId = null;
  noteEditorView.classList.remove('open');
  noteEditorView.setAttribute('aria-hidden', 'true');
  noteTitleInput.blur();
  noteBodyInput.blur();
  renderNotesList();
}

function saveActiveNote() {
  const note = state.notes.find(n => n.id === activeNoteId);
  if (!note) return;
  note.title = noteTitleInput.value;
  note.body = noteBodyInput.value;
  note.updatedAt = 'الآن';
  noteSaveHint.textContent = 'جاري الحفظ...';
  saveState();
  setTimeout(() => { noteSaveHint.textContent = 'محفوظة تلقائياً'; }, 300);
}

// =========================================
// Dedicated Pomodoro Page
// =========================================
function updatePomoUI() {
  const m = String(Math.floor(pomoSeconds / 60)).padStart(2, '0');
  const s = String(pomoSeconds % 60).padStart(2, '0');
  pomoDisplay.textContent = `${m}:${s}`;
  pomoStartBtn.textContent = pomoRunning ? 'إيقاف مؤقت' : 'ابدأ الجلسة';
  pomoStatus.textContent = pomoRunning ? `${pomoModeLabel} — جاري الآن...` : `جاهز لـ ${pomoModeLabel}`;
  pomoSessionsCount.textContent = `جلسات اليوم: ${toArabicDigits(state.pomoSessionsToday)}`;

  // Ring circumference = 2 * PI * 96 ≈ 603.19
  const circ = 603.19;
  const elapsedRatio = 1 - (pomoSeconds / Math.max(1, pomoTotalSeconds));
  pomoRingProgress.style.strokeDashoffset = circ * elapsedRatio;
}

function togglePomo() {
  if (pomoRunning) {
    clearInterval(pomoInterval);
    pomoRunning = false;
  } else {
    pomoRunning = true;
    if (state.pomoAutoFocus && pomoModeLabel === 'تركيز' && !state.focusMode) {
      toggleFocusMode(true);
    }
    pomoInterval = setInterval(() => {
      pomoSeconds--;
      if (pomoSeconds <= 0) {
        clearInterval(pomoInterval);
        pomoRunning = false;
        if (pomoModeLabel === 'تركيز') {
          state.pomoSessionsToday++;
          saveState();
        }
        pomoSeconds = pomoTotalSeconds;
        vibrate([40, 60, 40]);
        showToast('أحسنت! انتهت الجلسة بنجاح');
      }
      updatePomoUI();
    }, 1000);
  }
  updatePomoUI();
}

// =========================================
// Event Listeners
// =========================================
prayerPill.addEventListener('click', () => goToPageId('quran'));
openDrawerBtn.addEventListener('click', openDrawer);
guideOpenDrawer.addEventListener('click', openDrawer);
guideOpenSearch.addEventListener('click', openUniversalSearch);
openSettingsHomeBtn.addEventListener('click', openSettings);
openSettingsDesktopBtn.addEventListener('click', openSettings);
closeDrawerBtn.addEventListener('click', closeOverlays);
drawerHandle.addEventListener('click', closeOverlays);
closeSearchBtn.addEventListener('click', closeOverlays);
closeSettingsBtn.addEventListener('click', closeOverlays);

homeIndicator.addEventListener('click', () => {
  closeOverlays();
  closeMindfulModal();
  closeLaunchScreen();
  closeNoteEditor();
  goToPageId('home');
});

editFavBtn.addEventListener('click', () => {
  editingFavorites = !editingFavorites;
  editFavBtn.textContent = editingFavorites ? 'تم' : 'تعديل';
  editFavBtn.classList.toggle('active', editingFavorites);
  renderFavorites();
});

focusPill.addEventListener('click', () => toggleFocusMode());
focusModeDesktopBtn.addEventListener('click', () => toggleFocusMode());

themeToggleBtn.addEventListener('click', () => {
  state.theme = state.theme === 'dark' ? 'light' : 'dark';
  saveState();
  applyTheme();
});

settingsThemeDark.addEventListener('click', () => { state.theme = 'dark'; saveState(); applyTheme(); });
settingsThemeLight.addEventListener('click', () => { state.theme = 'light'; saveState(); applyTheme(); });

fpOffsetSlider.addEventListener('input', (e) => {
  state.fpOffset = Number(e.target.value);
  saveState();
  applyFpOffset();
});

resetDemoBtn.addEventListener('click', () => {
  localStorage.removeItem(STORAGE_KEY);
  state = loadState();
  editingFavorites = false;
  closeNoteEditor();
  initUI();
  goToPageId('home');
  showToast('تمت إعادة ضبط النموذج');
});

// Quran events
quranPrevBtn.addEventListener('click', () => {
  if (state.quranPageIndex > 0) {
    state.quranPageIndex--;
    saveState();
    renderQuranPage();
    showToast(`تم حفظ العلامة عند صفحة ${QURAN_PAGES[state.quranPageIndex].pageNumber}`);
  }
});

quranNextBtn.addEventListener('click', () => {
  if (state.quranPageIndex < QURAN_PAGES.length - 1) {
    state.quranPageIndex++;
    saveState();
    renderQuranPage();
    showToast(`تم حفظ العلامة عند صفحة ${QURAN_PAGES[state.quranPageIndex].pageNumber}`);
  }
});

quranDoneBtn.addEventListener('click', () => {
  state.wirdDoneToday = !state.wirdDoneToday;
  state.wirdStreak = state.wirdDoneToday ? state.wirdStreak + 1 : Math.max(0, state.wirdStreak - 1);
  saveState();
  renderQuranPage();
  showToast(state.wirdDoneToday ? 'تقبل الله! تم تسجيل إتمام ورد اليوم' : 'تم التراجع');
});

quranFontInc.addEventListener('click', () => {
  state.quranFontSize = Math.min(1.85, Number((state.quranFontSize + 0.12).toFixed(2)));
  saveState();
  renderQuranPage();
});

quranFontDec.addEventListener('click', () => {
  state.quranFontSize = Math.max(1.05, Number((state.quranFontSize - 0.12).toFixed(2)));
  saveState();
  renderQuranPage();
});

quranPaperToggle.addEventListener('click', () => {
  state.quranPaperMode = !state.quranPaperMode;
  saveState();
  renderQuranPage();
});

// Dhikr events
fpCounterBtn.addEventListener('click', incrementDhikr);

dhikrTargetInc.addEventListener('click', () => {
  const d = getActiveDhikr();
  if (!d) return;
  d.target = Math.min(10000, d.target + (d.target >= 100 ? 10 : 1));
  saveState();
  renderDhikr();
});

dhikrTargetDec.addEventListener('click', () => {
  const d = getActiveDhikr();
  if (!d) return;
  d.target = Math.max(1, d.target - (d.target > 100 ? 10 : 1));
  if (d.count > d.target) d.count = d.target;
  saveState();
  renderDhikr();
});

dhikrResetBtn.addEventListener('click', () => {
  const d = getActiveDhikr();
  if (!d) return;
  d.count = 0;
  saveState();
  renderDhikr();
  showToast('تم تصفير العداد');
});

dhikrDeleteBtn.addEventListener('click', () => {
  if (state.adhkar.length <= 1) return;
  state.adhkar = state.adhkar.filter(x => x.id !== state.activeDhikrId);
  state.activeDhikrId = state.adhkar[0].id;
  saveState();
  renderDhikr();
  showToast('تم حذف الذكر');
});

toggleAddDhikrBtn.addEventListener('click', () => {
  dhikrAddForm.hidden = !dhikrAddForm.hidden;
  if (!dhikrAddForm.hidden) newDhikrText.focus();
});

cancelAddDhikrBtn.addEventListener('click', () => { dhikrAddForm.hidden = true; });

dhikrAddForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const text = newDhikrText.value.trim();
  const target = Math.max(1, parseInt(newDhikrTarget.value, 10) || 33);
  if (!text) return;
  const newItem = {
    id: `dhikr_${Date.now()}`,
    text,
    short: text.split(' ').slice(0, 2).join(' '),
    target,
    count: 0
  };
  state.adhkar.push(newItem);
  state.activeDhikrId = newItem.id;
  newDhikrText.value = '';
  newDhikrTarget.value = 33;
  dhikrAddForm.hidden = true;
  saveState();
  renderDhikr();
  showToast('تمت إضافة الذكر');
});

// Habits events
habitAddForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const text = habitInput.value.trim();
  if (!text) return;
  state.habits.push({ id: Date.now(), text, done: false, streak: 0 });
  habitInput.value = '';
  saveState();
  renderHabits();
  showToast('تمت إضافة مهمة يومية متكررة');
});

newDayHabitsBtn.addEventListener('click', () => {
  state.habits.forEach(h => {
    if (h.done) h.streak += 1;
    h.done = false;
  });
  saveState();
  renderHabits();
  showToast('يوم جديد! تم تحديث الاستمرارية وتصفير المهام');
});

// Notes events
addNoteBtn.addEventListener('click', () => {
  const newNote = {
    id: Date.now(),
    title: '',
    body: '',
    pinned: false,
    updatedAt: 'الآن'
  };
  state.notes.unshift(newNote);
  saveState();
  renderNotesList();
  openNoteEditor(newNote.id);
  noteTitleInput.focus();
});

closeNoteEditorBtn.addEventListener('click', closeNoteEditor);
noteTitleInput.addEventListener('input', saveActiveNote);
noteBodyInput.addEventListener('input', saveActiveNote);

pinNoteBtn.addEventListener('click', () => {
  const note = state.notes.find(n => n.id === activeNoteId);
  if (!note) return;
  note.pinned = !note.pinned;
  pinNoteBtn.classList.toggle('active', note.pinned);
  pinNoteBtn.textContent = note.pinned ? 'مثبتة ★' : 'تثبيت';
  saveState();
  showToast(note.pinned ? 'تم تثبيت الملاحظة في الأعلى' : 'تم إلغاء التثبيت');
});

deleteNoteBtn.addEventListener('click', () => {
  state.notes = state.notes.filter(n => n.id !== activeNoteId);
  saveState();
  closeNoteEditor();
  showToast('تم حذف الملاحظة');
});

// Pomodoro events
pomoModes.querySelectorAll('.mode-chip').forEach(btn => {
  btn.addEventListener('click', () => {
    clearInterval(pomoInterval);
    pomoRunning = false;
    pomoModes.querySelectorAll('.mode-chip').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    pomoTotalSeconds = Number(btn.dataset.minutes) * 60;
    pomoSeconds = pomoTotalSeconds;
    pomoModeLabel = btn.dataset.label;
    updatePomoUI();
  });
});

pomoStartBtn.addEventListener('click', togglePomo);
pomoResetBtn.addEventListener('click', () => {
  clearInterval(pomoInterval);
  pomoRunning = false;
  pomoSeconds = pomoTotalSeconds;
  updatePomoUI();
});

pomoAutoFocusToggle.addEventListener('change', (e) => {
  state.pomoAutoFocus = e.target.checked;
  saveState();
});

pomoTaskInput.addEventListener('input', (e) => {
  state.pomoTask = e.target.value;
  saveState();
});

// Drawer & Search & Modals
drawerSearch.addEventListener('input', renderDrawer);
universalSearchInput.addEventListener('input', (e) => renderUniversalSearch(e.target.value));

mindfulStayBtn.addEventListener('click', () => {
  closeMindfulModal();
  showToast('قرار ممتاز — حافظ على تركيزك');
});

mindfulProceedBtn.addEventListener('click', () => {
  const app = pendingDistractingApp;
  closeMindfulModal();
  if (app) openLaunchScreen(app.name, 'جلسة محددة بـ ٥ دقائق ثم تنبيه لطيف');
});

launchBackBtn.addEventListener('click', closeLaunchScreen);

// =========================================
// Initialize
// =========================================
function initUI() {
  applyTheme();
  applyFocusMode();
  applyFpOffset();
  updateClock();
  renderQuranPage();
  renderDhikr();
  renderHabits();
  renderNotesList();
  pomoAutoFocusToggle.checked = state.pomoAutoFocus;
  pomoTaskInput.value = state.pomoTask;
  updatePomoUI();
  applyPageOrder('home');
}

initUI();
setInterval(updateClock, 15000);
