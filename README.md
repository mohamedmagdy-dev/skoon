# Sukoon (سكون) — Minimalist Islamic Android Launcher

A calm, distraction-free Android home screen launcher built with **Kotlin & Jetpack Compose**, paired with an interactive **Web Prototype** for rapid UI/UX iteration.

## Features
- **Zero-Icon Minimalist Home**: Text-only favorites, Focus Mode (hides distracting apps), and a 5-second Mindful Pause for distracting apps.
- **Offline Prayer Times**: Astronomical prayer calculation for 55+ cities (Egypt, Arab world & international) with countdown pill and expandable 5-prayer schedule.
- **Full 604-Page Quran Reader**: Offline Mushaf in Amiri script with smart auto-fit typography, adjustable font size, Paper mode, and customizable line spacing.
- **Morning & Evening Azkar**: Auto time-of-day switching, progress tracking, tactile counting cards, and AMOLED Black-Screen mode.
- **Tasbih / Dhikr Counter**: Custom Adhkar, circular progress ring, haptic feedback, AMOLED Black-Screen mode, and optional rear fingerprint sensor gesture support.
- **Daily Habits, Notes & Pomodoro**: Recurring daily habits with streaks, instant auto-saving notes with pinning, and a Pomodoro timer integrated with Focus Mode.
- **Full Customization**: Bilingual UI (**Arabic RTL** & **English LTR**), 5 switchable app fonts (`IBM Plex`, `Tajawal`, `Almarai`, `Amiri`, `System Sans`), Dark OLED / Light Paper themes, and reorderable tabs.

## Repository Structure
```text
├── android/          # Native Android Launcher (Kotlin, Jetpack Compose, Material 3)
└── web-prototype/    # Interactive HTML/CSS/JS prototype for UI/UX design & iteration
```

## Building the Android App
1. Open `android/` in Android Studio (JDK 17, Compile SDK 35, Min SDK 26).
2. Run `./gradlew assembleDebug` (or `./gradlew assembleRelease` with your signing keystore).

## Running the Web Prototype
Open `web-prototype/index.html` directly in any modern browser or serve it via a local static server.
