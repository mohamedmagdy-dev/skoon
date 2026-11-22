import { formatDigits } from '../design-system/i18n';

export interface PrayerCity {
  id: string;
  nameAr: string;
  nameEn: string;
  countryAr: string;
  countryEn: string;
  lat: number;
  lng: number;
  fajrAngle: number;
  ishaAngle: number;
  ishaIntervalMinutes?: number;
}

export interface PrayerEntry {
  id: 'fajr' | 'dhuhr' | 'asr' | 'maghrib' | 'isha';
  nameAr: string;
  nameEn: string;
  hour24: number;
  minute: number;
  totalMinutes: number;
}

export interface NextPrayerInfo {
  city: PrayerCity;
  nextPrayer: PrayerEntry;
  remainingMinutes: number;
  todayPrayers: PrayerEntry[];
}

export const PRAYER_CITIES: PrayerCity[] = [
  // Egypt (Egyptian General Authority of Survey: 19.5° / 17.5°)
  { id: 'alexandria', nameAr: 'الإسكندرية', nameEn: 'Alexandria', countryAr: 'مصر', countryEn: 'Egypt', lat: 31.2001, lng: 29.9187, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'cairo', nameAr: 'القاهرة', nameEn: 'Cairo', countryAr: 'مصر', countryEn: 'Egypt', lat: 30.0444, lng: 31.2357, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'giza', nameAr: 'الجيزة', nameEn: 'Giza', countryAr: 'مصر', countryEn: 'Egypt', lat: 30.0131, lng: 31.2089, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'mansoura', nameAr: 'المنصورة', nameEn: 'Mansoura', countryAr: 'مصر', countryEn: 'Egypt', lat: 31.0409, lng: 31.3785, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'tanta', nameAr: 'طنطا', nameEn: 'Tanta', countryAr: 'مصر', countryEn: 'Egypt', lat: 30.7865, lng: 31.0004, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'zagazig', nameAr: 'الزقازيق', nameEn: 'Zagazig', countryAr: 'مصر', countryEn: 'Egypt', lat: 30.5877, lng: 31.502, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'damanhur', nameAr: 'دمنهور', nameEn: 'Damanhur', countryAr: 'مصر', countryEn: 'Egypt', lat: 31.0341, lng: 30.4682, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'kafr_el_sheikh', nameAr: 'كفر الشيخ', nameEn: 'Kafr El-Sheikh', countryAr: 'مصر', countryEn: 'Egypt', lat: 31.1107, lng: 30.9388, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'damietta', nameAr: 'دمياط', nameEn: 'Damietta', countryAr: 'مصر', countryEn: 'Egypt', lat: 31.4175, lng: 31.8144, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'port_said', nameAr: 'بورسعيد', nameEn: 'Port Said', countryAr: 'مصر', countryEn: 'Egypt', lat: 31.2653, lng: 32.3019, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'ismailia', nameAr: 'الإسماعيلية', nameEn: 'Ismailia', countryAr: 'مصر', countryEn: 'Egypt', lat: 30.5965, lng: 32.2715, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'suez', nameAr: 'السويس', nameEn: 'Suez', countryAr: 'مصر', countryEn: 'Egypt', lat: 29.9668, lng: 32.5498, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'fayoum', nameAr: 'الفيوم', nameEn: 'Fayoum', countryAr: 'مصر', countryEn: 'Egypt', lat: 29.3084, lng: 30.8428, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'beni_suef', nameAr: 'بني سويف', nameEn: 'Beni Suef', countryAr: 'مصر', countryEn: 'Egypt', lat: 29.0661, lng: 31.0994, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'minya', nameAr: 'المنيا', nameEn: 'Minya', countryAr: 'مصر', countryEn: 'Egypt', lat: 28.0871, lng: 30.7618, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'assiut', nameAr: 'أسيوط', nameEn: 'Assiut', countryAr: 'مصر', countryEn: 'Egypt', lat: 27.1783, lng: 31.1859, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'sohag', nameAr: 'سوهاج', nameEn: 'Sohag', countryAr: 'مصر', countryEn: 'Egypt', lat: 26.5569, lng: 31.6948, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'qena', nameAr: 'قنا', nameEn: 'Qena', countryAr: 'مصر', countryEn: 'Egypt', lat: 26.1551, lng: 32.716, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'luxor', nameAr: 'الأقصر', nameEn: 'Luxor', countryAr: 'مصر', countryEn: 'Egypt', lat: 25.6872, lng: 32.6396, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'aswan', nameAr: 'أسوان', nameEn: 'Aswan', countryAr: 'مصر', countryEn: 'Egypt', lat: 24.0889, lng: 32.8998, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'hurghada', nameAr: 'الغردقة', nameEn: 'Hurghada', countryAr: 'مصر', countryEn: 'Egypt', lat: 27.2579, lng: 33.8116, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'sharm', nameAr: 'شرم الشيخ', nameEn: 'Sharm El-Sheikh', countryAr: 'مصر', countryEn: 'Egypt', lat: 27.9158, lng: 34.33, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'matruh', nameAr: 'مرسى مطروح', nameEn: 'Marsa Matruh', countryAr: 'مصر', countryEn: 'Egypt', lat: 31.3543, lng: 27.2373, fajrAngle: 19.5, ishaAngle: 17.5 },

  // Saudi Arabia (Umm al-Qura)
  { id: 'makkah', nameAr: 'مكة المكرمة', nameEn: 'Makkah', countryAr: 'السعودية', countryEn: 'Saudi Arabia', lat: 21.3891, lng: 39.8579, fajrAngle: 18.5, ishaAngle: 18.5, ishaIntervalMinutes: 90 },
  { id: 'madinah', nameAr: 'المدينة المنورة', nameEn: 'Madinah', countryAr: 'السعودية', countryEn: 'Saudi Arabia', lat: 24.5247, lng: 39.5692, fajrAngle: 18.5, ishaAngle: 18.5, ishaIntervalMinutes: 90 },
  { id: 'riyadh', nameAr: 'الرياض', nameEn: 'Riyadh', countryAr: 'السعودية', countryEn: 'Saudi Arabia', lat: 24.7136, lng: 46.6753, fajrAngle: 18.5, ishaAngle: 18.5, ishaIntervalMinutes: 90 },
  { id: 'jeddah', nameAr: 'جدة', nameEn: 'Jeddah', countryAr: 'السعودية', countryEn: 'Saudi Arabia', lat: 21.4858, lng: 39.1925, fajrAngle: 18.5, ishaAngle: 18.5, ishaIntervalMinutes: 90 },
  { id: 'dammam', nameAr: 'الدمام', nameEn: 'Dammam', countryAr: 'السعودية', countryEn: 'Saudi Arabia', lat: 26.4207, lng: 50.0888, fajrAngle: 18.5, ishaAngle: 18.5, ishaIntervalMinutes: 90 },

  // Gulf & Levant
  { id: 'dubai', nameAr: 'دبي', nameEn: 'Dubai', countryAr: 'الإمارات', countryEn: 'UAE', lat: 25.2048, lng: 55.2708, fajrAngle: 18.2, ishaAngle: 18.2 },
  { id: 'abu_dhabi', nameAr: 'أبوظبي', nameEn: 'Abu Dhabi', countryAr: 'الإمارات', countryEn: 'UAE', lat: 24.4539, lng: 54.3773, fajrAngle: 18.2, ishaAngle: 18.2 },
  { id: 'kuwait', nameAr: 'الكويت', nameEn: 'Kuwait City', countryAr: 'الكويت', countryEn: 'Kuwait', lat: 29.3759, lng: 47.9774, fajrAngle: 18.0, ishaAngle: 17.5 },
  { id: 'doha', nameAr: 'الدوحة', nameEn: 'Doha', countryAr: 'قطر', countryEn: 'Qatar', lat: 25.2854, lng: 51.531, fajrAngle: 18.0, ishaAngle: 18.0, ishaIntervalMinutes: 90 },
  { id: 'manama', nameAr: 'المنامة', nameEn: 'Manama', countryAr: 'البحرين', countryEn: 'Bahrain', lat: 26.2285, lng: 50.586, fajrAngle: 18.5, ishaAngle: 18.5, ishaIntervalMinutes: 90 },
  { id: 'muscat', nameAr: 'مسقط', nameEn: 'Muscat', countryAr: 'عُمان', countryEn: 'Oman', lat: 23.588, lng: 58.3829, fajrAngle: 18.0, ishaAngle: 18.0 },
  { id: 'amman', nameAr: 'عمّان', nameEn: 'Amman', countryAr: 'الأردن', countryEn: 'Jordan', lat: 31.9454, lng: 35.9284, fajrAngle: 18.0, ishaAngle: 18.0 },
  { id: 'jerusalem', nameAr: 'القدس', nameEn: 'Jerusalem', countryAr: 'فلسطين', countryEn: 'Palestine', lat: 31.7683, lng: 35.2137, fajrAngle: 18.0, ishaAngle: 18.0 },
  { id: 'gaza', nameAr: 'غزة', nameEn: 'Gaza', countryAr: 'فلسطين', countryEn: 'Palestine', lat: 31.5017, lng: 34.4668, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'beirut', nameAr: 'بيروت', nameEn: 'Beirut', countryAr: 'لبنان', countryEn: 'Lebanon', lat: 33.8938, lng: 35.5018, fajrAngle: 18.0, ishaAngle: 17.5 },
  { id: 'damascus', nameAr: 'دمشق', nameEn: 'Damascus', countryAr: 'سوريا', countryEn: 'Syria', lat: 33.5138, lng: 36.2765, fajrAngle: 18.5, ishaAngle: 17.5 },
  { id: 'baghdad', nameAr: 'بغداد', nameEn: 'Baghdad', countryAr: 'العراق', countryEn: 'Iraq', lat: 33.3152, lng: 44.3661, fajrAngle: 18.0, ishaAngle: 17.0 },

  // North Africa & Sudan & Yemen
  { id: 'khartoum', nameAr: 'الخرطوم', nameEn: 'Khartoum', countryAr: 'السودان', countryEn: 'Sudan', lat: 15.5007, lng: 32.5599, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'tripoli', nameAr: 'طرابلس', nameEn: 'Tripoli', countryAr: 'ليبيا', countryEn: 'Libya', lat: 32.8872, lng: 13.1913, fajrAngle: 19.5, ishaAngle: 17.5 },
  { id: 'tunis', nameAr: 'تونس', nameEn: 'Tunis', countryAr: 'تونس', countryEn: 'Tunisia', lat: 36.8065, lng: 10.1815, fajrAngle: 18.0, ishaAngle: 18.0 },
  { id: 'algiers', nameAr: 'الجزائر', nameEn: 'Algiers', countryAr: 'الجزائر', countryEn: 'Algeria', lat: 36.7538, lng: 3.0588, fajrAngle: 18.0, ishaAngle: 17.0 },
  { id: 'casablanca', nameAr: 'الدار البيضاء', nameEn: 'Casablanca', countryAr: 'المغرب', countryEn: 'Morocco', lat: 33.5731, lng: -7.5898, fajrAngle: 19.0, ishaAngle: 17.0 },
  { id: 'sanaa', nameAr: 'صنعاء', nameEn: 'Sanaa', countryAr: 'اليمن', countryEn: 'Yemen', lat: 15.3694, lng: 44.191, fajrAngle: 18.5, ishaAngle: 18.5 },

  // International
  { id: 'istanbul', nameAr: 'إسطنبول', nameEn: 'Istanbul', countryAr: 'تركيا', countryEn: 'Turkey', lat: 41.0082, lng: 28.9784, fajrAngle: 18.0, ishaAngle: 17.0 },
  { id: 'london', nameAr: 'لندن', nameEn: 'London', countryAr: 'بريطانيا', countryEn: 'UK', lat: 51.5072, lng: -0.1276, fajrAngle: 18.0, ishaAngle: 17.0 },
  { id: 'paris', nameAr: 'باريس', nameEn: 'Paris', countryAr: 'فرنسا', countryEn: 'France', lat: 48.8566, lng: 2.3522, fajrAngle: 12.0, ishaAngle: 12.0 },
  { id: 'berlin', nameAr: 'برلين', nameEn: 'Berlin', countryAr: 'ألمانيا', countryEn: 'Germany', lat: 52.52, lng: 13.405, fajrAngle: 18.0, ishaAngle: 17.0 },
  { id: 'new_york', nameAr: 'نيويورك', nameEn: 'New York', countryAr: 'أمريكا', countryEn: 'USA', lat: 40.7128, lng: -74.006, fajrAngle: 15.0, ishaAngle: 15.0 },
  { id: 'toronto', nameAr: 'تورنتو', nameEn: 'Toronto', countryAr: 'كندا', countryEn: 'Canada', lat: 43.6532, lng: -79.3832, fajrAngle: 15.0, ishaAngle: 15.0 },
  { id: 'kuala_lumpur', nameAr: 'كوالالمبور', nameEn: 'Kuala Lumpur', countryAr: 'ماليزيا', countryEn: 'Malaysia', lat: 3.139, lng: 101.6869, fajrAngle: 20.0, ishaAngle: 18.0 },
  { id: 'jakarta', nameAr: 'جاكرتا', nameEn: 'Jakarta', countryAr: 'إندونيسيا', countryEn: 'Indonesia', lat: -6.2088, lng: 106.8456, fajrAngle: 20.0, ishaAngle: 18.0 },
];

export function findCity(cityId: string): PrayerCity {
  return PRAYER_CITIES.find((c) => c.id === cityId) ?? PRAYER_CITIES[0];
}

export function findNearestCity(lat: number, lng: number): PrayerCity {
  let best = PRAYER_CITIES[0];
  let bestDist = Infinity;
  for (const c of PRAYER_CITIES) {
    const dLat = c.lat - lat;
    const dLng = c.lng - lng;
    const d = dLat * dLat + dLng * dLng;
    if (d < bestDist) {
      bestDist = d;
      best = c;
    }
  }
  return best;
}

const dsin = (d: number) => Math.sin((d * Math.PI) / 180);
const dcos = (d: number) => Math.cos((d * Math.PI) / 180);
const dtan = (d: number) => Math.tan((d * Math.PI) / 180);
const darcsin = (x: number) => (Math.asin(x) * 180) / Math.PI;
const darccos = (x: number) => (Math.acos(x) * 180) / Math.PI;
const darctan2 = (y: number, x: number) => (Math.atan2(y, x) * 180) / Math.PI;
const darccot = (x: number) => (Math.atan(1 / x) * 180) / Math.PI;
const fixAngle = (a: number) => a - 360 * Math.floor(a / 360);
const fixHour = (h: number) => h - 24 * Math.floor(h / 24);

function julianDate(year: number, month: number, day: number): number {
  let y = year;
  let m = month;
  if (m <= 2) {
    y -= 1;
    m += 12;
  }
  const a = Math.floor(y / 100);
  const b = 2 - a + Math.floor(a / 4);
  return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5;
}

function sunPosition(jd: number): [number, number] {
  const d = jd - 2451545.0;
  const g = fixAngle(357.529 + 0.98560028 * d);
  const q = fixAngle(280.459 + 0.98564736 * d);
  const l = fixAngle(q + 1.915 * dsin(g) + 0.02 * dsin(2 * g));
  const e = 23.439 - 0.00000036 * d;
  const ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0;
  const eqT = q / 15.0 - fixHour(ra);
  const decl = darcsin(dsin(e) * dsin(l));
  return [decl, eqT];
}

function computeMidDay(jd: number, t: number): number {
  const [, eqT] = sunPosition(jd + t);
  return fixHour(12.0 - eqT);
}

function computeAngleTime(
  jd: number,
  t: number,
  angle: number,
  lat: number,
  ccw: boolean
): number {
  const [decl] = sunPosition(jd + t);
  const noon = computeMidDay(jd, t);
  const cosVal = (-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat));
  const clamped = Math.max(-1, Math.min(1, cosVal));
  const diff = darccos(clamped) / 15.0;
  return noon + (ccw ? -diff : diff);
}

function computeAsr(jd: number, t: number, lat: number, factor = 1): number {
  const [decl] = sunPosition(jd + t);
  const angle = -darccot(factor + dtan(Math.abs(lat - decl)));
  return computeAngleTime(jd, t, angle, lat, false);
}

function toEntry(
  id: PrayerEntry['id'],
  nameAr: string,
  nameEn: string,
  rawHours: number
): PrayerEntry {
  const normalized = ((rawHours % 24) + 24) % 24;
  const totalMinutes = Math.floor(normalized * 60 + 0.5) % (24 * 60);
  return {
    id,
    nameAr,
    nameEn,
    hour24: Math.floor(totalMinutes / 60),
    minute: totalMinutes % 60,
    totalMinutes,
  };
}

function computeDayPrayers(
  year: number,
  month: number,
  day: number,
  city: PrayerCity,
  tzHours: number
): PrayerEntry[] {
  const jd = julianDate(year, month, day) - city.lng / (15 * 24);
  const fajrH = computeAngleTime(jd, 5 / 24, city.fajrAngle, city.lat, true);
  const dhuhrH = computeMidDay(jd, 12 / 24);
  const asrH = computeAsr(jd, 13 / 24, city.lat, 1);
  const maghribH = computeAngleTime(jd, 18 / 24, 0.833, city.lat, false);
  const ishaH =
    city.ishaIntervalMinutes != null
      ? maghribH + city.ishaIntervalMinutes / 60
      : computeAngleTime(jd, 18 / 24, city.ishaAngle, city.lat, false);

  const offset = tzHours - city.lng / 15;
  return [
    toEntry('fajr', 'الفجر', 'Fajr', fajrH + offset),
    toEntry('dhuhr', 'الظهر', 'Dhuhr', dhuhrH + offset),
    toEntry('asr', 'العصر', 'Asr', asrH + offset),
    toEntry('maghrib', 'المغرب', 'Maghrib', maghribH + offset),
    toEntry('isha', 'العشاء', 'Isha', ishaH + offset),
  ];
}

export function getNextPrayer(cityId: string, now: Date = new Date()): NextPrayerInfo {
  const city = findCity(cityId);
  const tzHours = -now.getTimezoneOffset() / 60;
  const todayPrayers = computeDayPrayers(
    now.getFullYear(),
    now.getMonth() + 1,
    now.getDate(),
    city,
    tzHours
  );
  const nowMinutes = now.getHours() * 60 + now.getMinutes();
  const upcoming = todayPrayers.find((p) => p.totalMinutes > nowMinutes);
  if (upcoming) {
    return {
      city,
      nextPrayer: upcoming,
      remainingMinutes: upcoming.totalMinutes - nowMinutes,
      todayPrayers,
    };
  }
  const tomorrow = new Date(now.getTime() + 86400000);
  const tomorrowTz = -tomorrow.getTimezoneOffset() / 60;
  const tomorrowFajr = computeDayPrayers(
    tomorrow.getFullYear(),
    tomorrow.getMonth() + 1,
    tomorrow.getDate(),
    city,
    tomorrowTz
  )[0];
  return {
    city,
    nextPrayer: tomorrowFajr,
    remainingMinutes: 24 * 60 - nowMinutes + tomorrowFajr.totalMinutes,
    todayPrayers,
  };
}

export function formatPrayerTime12h(entry: PrayerEntry, isArabic: boolean): string {
  const period = isArabic
    ? entry.hour24 < 12
      ? 'ص'
      : 'م'
    : entry.hour24 < 12
      ? 'AM'
      : 'PM';
  const h12 = entry.hour24 === 0 ? 12 : entry.hour24 > 12 ? entry.hour24 - 12 : entry.hour24;
  const mStr = String(entry.minute).padStart(2, '0');
  return `${formatDigits(h12, isArabic)}:${formatDigits(mStr, isArabic)} ${period}`;
}

export function formatPrayerTimeShort(entry: PrayerEntry, isArabic: boolean): string {
  const h12 = entry.hour24 === 0 ? 12 : entry.hour24 > 12 ? entry.hour24 - 12 : entry.hour24;
  const mStr = String(entry.minute).padStart(2, '0');
  return `${formatDigits(h12, isArabic)}:${formatDigits(mStr, isArabic)}`;
}

export function formatRemainingPrayer(remainingMinutes: number, isArabic: boolean): string {
  const h = Math.floor(remainingMinutes / 60);
  const m = remainingMinutes % 60;
  if (isArabic) {
    if (h > 0 && m > 0) return `بعد ${formatDigits(h, true)} س ${formatDigits(m, true)} د`;
    if (h > 0) return `بعد ${formatDigits(h, true)} س`;
    if (m > 0) return `بعد ${formatDigits(m, true)} د`;
    return 'الآن';
  }
  if (h > 0 && m > 0) return `in ${h}h ${m}m`;
  if (h > 0) return `in ${h}h`;
  if (m > 0) return `in ${m}m`;
  return 'now';
}
