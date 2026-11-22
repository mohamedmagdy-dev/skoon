package com.sukoon.launcher.data

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

data class PrayerCity(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val countryEn: String,
    val lat: Double,
    val lng: Double,
    val fajrAngle: Double = 19.5,
    val ishaAngle: Double = 17.5,
    val ishaIntervalMinutes: Int? = null
) {
    fun localizedName(isArabic: Boolean): String = if (isArabic) nameAr else nameEn
    fun localizedCountry(isArabic: Boolean): String = if (isArabic) countryAr else countryEn
    fun localizedFull(isArabic: Boolean): String =
        if (isArabic) "$nameAr، $countryAr" else "$nameEn, $countryEn"
}

val PRAYER_CITIES: List<PrayerCity> = listOf(
    // Egypt (Egyptian General Authority of Survey: Fajr 19.5°, Isha 17.5°)
    PrayerCity("alexandria", "الإسكندرية", "Alexandria", "مصر", "Egypt", 31.2001, 29.9187),
    PrayerCity("cairo", "القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357),
    PrayerCity("giza", "الجيزة", "Giza", "مصر", "Egypt", 30.0131, 31.2089),
    PrayerCity("mansoura", "المنصورة", "Mansoura", "مصر", "Egypt", 31.0409, 31.3785),
    PrayerCity("tanta", "طنطا", "Tanta", "مصر", "Egypt", 30.7865, 31.0004),
    PrayerCity("zagazig", "الزقازيق", "Zagazig", "مصر", "Egypt", 30.5877, 31.5020),
    PrayerCity("damanhur", "دمنهور", "Damanhur", "مصر", "Egypt", 31.0341, 30.4682),
    PrayerCity("kafr_el_sheikh", "كفر الشيخ", "Kafr El-Sheikh", "مصر", "Egypt", 31.1107, 30.9388),
    PrayerCity("damietta", "دمياط", "Damietta", "مصر", "Egypt", 31.4175, 31.8144),
    PrayerCity("port_said", "بورسعيد", "Port Said", "مصر", "Egypt", 31.2653, 32.3019),
    PrayerCity("ismailia", "الإسماعيلية", "Ismailia", "مصر", "Egypt", 30.5965, 32.2715),
    PrayerCity("suez", "السويس", "Suez", "مصر", "Egypt", 29.9668, 32.5498),
    PrayerCity("banha", "بنها", "Banha", "مصر", "Egypt", 30.4660, 31.1848),
    PrayerCity("shibin_el_kom", "شبين الكوم", "Shibin El-Kom", "مصر", "Egypt", 30.5503, 31.0106),
    PrayerCity("fayoum", "الفيوم", "Fayoum", "مصر", "Egypt", 29.3084, 30.8428),
    PrayerCity("beni_suef", "بني سويف", "Beni Suef", "مصر", "Egypt", 29.0661, 31.0994),
    PrayerCity("minya", "المنيا", "Minya", "مصر", "Egypt", 28.0871, 30.7618),
    PrayerCity("assiut", "أسيوط", "Assiut", "مصر", "Egypt", 27.1783, 31.1859),
    PrayerCity("sohag", "سوهاج", "Sohag", "مصر", "Egypt", 26.5569, 31.6948),
    PrayerCity("qena", "قنا", "Qena", "مصر", "Egypt", 26.1551, 32.7160),
    PrayerCity("luxor", "الأقصر", "Luxor", "مصر", "Egypt", 25.6872, 32.6396),
    PrayerCity("aswan", "أسوان", "Aswan", "مصر", "Egypt", 24.0889, 32.8998),
    PrayerCity("hurghada", "الغردقة", "Hurghada", "مصر", "Egypt", 27.2579, 33.8116),
    PrayerCity("sharm", "شرم الشيخ", "Sharm El-Sheikh", "مصر", "Egypt", 27.9158, 34.3300),
    PrayerCity("matruh", "مرسى مطروح", "Marsa Matruh", "مصر", "Egypt", 31.3543, 27.2373),
    PrayerCity("arish", "العريش", "Arish", "مصر", "Egypt", 31.1321, 33.8033),

    // Saudi Arabia (Umm al-Qura: Fajr 18.5°, Isha 90 min after Maghrib)
    PrayerCity("makkah", "مكة المكرمة", "Makkah", "السعودية", "Saudi Arabia", 21.3891, 39.8579, 18.5, 18.5, 90),
    PrayerCity("madinah", "المدينة المنورة", "Madinah", "السعودية", "Saudi Arabia", 24.5247, 39.5692, 18.5, 18.5, 90),
    PrayerCity("riyadh", "الرياض", "Riyadh", "السعودية", "Saudi Arabia", 24.7136, 46.6753, 18.5, 18.5, 90),
    PrayerCity("jeddah", "جدة", "Jeddah", "السعودية", "Saudi Arabia", 21.4858, 39.1925, 18.5, 18.5, 90),
    PrayerCity("dammam", "الدمام", "Dammam", "السعودية", "Saudi Arabia", 26.4207, 50.0888, 18.5, 18.5, 90),

    // Gulf & Levant
    PrayerCity("dubai", "دبي", "Dubai", "الإمارات", "UAE", 25.2048, 55.2708, 18.2, 18.2),
    PrayerCity("abu_dhabi", "أبوظبي", "Abu Dhabi", "الإمارات", "UAE", 24.4539, 54.3773, 18.2, 18.2),
    PrayerCity("kuwait", "الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, 18.0, 17.5),
    PrayerCity("doha", "الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, 18.0, 18.0, 90),
    PrayerCity("manama", "المنامة", "Manama", "البحرين", "Bahrain", 26.2285, 50.5860, 18.5, 18.5, 90),
    PrayerCity("muscat", "مسقط", "Muscat", "عُمان", "Oman", 23.5880, 58.3829, 18.0, 18.0),
    PrayerCity("amman", "عمّان", "Amman", "الأردن", "Jordan", 31.9454, 35.9284, 18.0, 18.0),
    PrayerCity("jerusalem", "القدس", "Jerusalem", "فلسطين", "Palestine", 31.7683, 35.2137, 18.0, 18.0),
    PrayerCity("gaza", "غزة", "Gaza", "فلسطين", "Palestine", 31.5017, 34.4668, 19.5, 17.5),
    PrayerCity("beirut", "بيروت", "Beirut", "لبنان", "Lebanon", 33.8938, 35.5018, 18.0, 17.5),
    PrayerCity("damascus", "دمشق", "Damascus", "سوريا", "Syria", 33.5138, 36.2765, 18.5, 17.5),
    PrayerCity("baghdad", "بغداد", "Baghdad", "العراق", "Iraq", 33.3152, 44.3661, 18.0, 17.0),

    // North Africa & Sudan & Yemen
    PrayerCity("khartoum", "الخرطوم", "Khartoum", "السودان", "Sudan", 15.5007, 32.5599, 19.5, 17.5),
    PrayerCity("tripoli", "طرابلس", "Tripoli", "ليبيا", "Libya", 32.8872, 13.1913, 19.5, 17.5),
    PrayerCity("tunis", "تونس", "Tunis", "تونس", "Tunisia", 36.8065, 10.1815, 18.0, 18.0),
    PrayerCity("algiers", "الجزائر", "Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, 18.0, 17.0),
    PrayerCity("casablanca", "الدار البيضاء", "Casablanca", "المغرب", "Morocco", 33.5731, -7.5898, 19.0, 17.0),
    PrayerCity("rabat", "الرباط", "Rabat", "المغرب", "Morocco", 34.0209, -6.8416, 19.0, 17.0),
    PrayerCity("sanaa", "صنعاء", "Sanaa", "اليمن", "Yemen", 15.3694, 44.1910, 18.5, 18.5),

    // International
    PrayerCity("istanbul", "إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, 18.0, 17.0),
    PrayerCity("london", "لندن", "London", "بريطانيا", "UK", 51.5072, -0.1276, 18.0, 17.0),
    PrayerCity("paris", "باريس", "Paris", "فرنسا", "France", 48.8566, 2.3522, 12.0, 12.0),
    PrayerCity("berlin", "برلين", "Berlin", "ألمانيا", "Germany", 52.5200, 13.4050, 18.0, 17.0),
    PrayerCity("new_york", "نيويورك", "New York", "أمريكا", "USA", 40.7128, -74.0060, 15.0, 15.0),
    PrayerCity("toronto", "تورنتو", "Toronto", "كندا", "Canada", 43.6532, -79.3832, 15.0, 15.0),
    PrayerCity("kuala_lumpur", "كوالالمبور", "Kuala Lumpur", "ماليزيا", "Malaysia", 3.1390, 101.6869, 20.0, 18.0),
    PrayerCity("jakarta", "جاكرتا", "Jakarta", "إندونيسيا", "Indonesia", -6.2088, 106.8456, 20.0, 18.0)
)

data class PrayerEntry(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val hour24: Int,
    val minute: Int,
    val totalMinutes: Int
) {
    fun localizedName(isArabic: Boolean): String = if (isArabic) nameAr else nameEn

    fun format12h(isArabic: Boolean): String {
        val period = if (isArabic) {
            if (hour24 < 12) "ص" else "م"
        } else {
            if (hour24 < 12) "AM" else "PM"
        }
        val h12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val mStr = minute.toString().padStart(2, '0')
        return "${h12.formatDigits(isArabic)}:${mStr.formatDigits(isArabic)} $period"
    }

    fun format12hShort(isArabic: Boolean): String {
        val h12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val mStr = minute.toString().padStart(2, '0')
        return "${h12.formatDigits(isArabic)}:${mStr.formatDigits(isArabic)}"
    }
}

data class NextPrayerInfo(
    val city: PrayerCity,
    val nextPrayer: PrayerEntry,
    val remainingMinutes: Int,
    val todayPrayers: List<PrayerEntry>
) {
    fun formatRemaining(isArabic: Boolean): String {
        val h = remainingMinutes / 60
        val m = remainingMinutes % 60
        return if (isArabic) {
            when {
                h > 0 && m > 0 -> "بعد ${h.toArabicDigits()} س ${m.toArabicDigits()} د"
                h > 0 -> "بعد ${h.toArabicDigits()} س"
                m > 0 -> "بعد ${m.toArabicDigits()} د"
                else -> "الآن"
            }
        } else {
            when {
                h > 0 && m > 0 -> "in ${h}h ${m}m"
                h > 0 -> "in ${h}h"
                m > 0 -> "in ${m}m"
                else -> "now"
            }
        }
    }
}

/**
 * Offline Astronomical Prayer Times Calculator
 * Supports selectable cities with region-appropriate Fajr/Isha calculation angles.
 */
object PrayerTimesCalculator {

    fun findCity(cityId: String): PrayerCity =
        PRAYER_CITIES.firstOrNull { it.id == cityId } ?: PRAYER_CITIES.first()

    fun findNearestCity(lat: Double, lng: Double): PrayerCity {
        var best = PRAYER_CITIES.first()
        var bestDist = Double.MAX_VALUE
        for (c in PRAYER_CITIES) {
            val dLat = c.lat - lat
            val dLng = c.lng - lng
            val d = dLat * dLat + dLng * dLng
            if (d < bestDist) {
                bestDist = d
                best = c
            }
        }
        return best
    }

    fun resolveCityForState(state: LauncherState): PrayerCity {
        val lat = state.customLat
        val lng = state.customLng
        if (state.useGpsLocation && lat != null && lng != null) {
            val nearest = findNearestCity(lat, lng)
            val label = state.customLocationLabel.ifBlank { nearest.nameAr }
            return nearest.copy(
                id = "gps",
                nameAr = label,
                nameEn = state.customLocationLabel.ifBlank { nearest.nameEn },
                lat = lat,
                lng = lng
            )
        }
        return findCity(state.prayerCityId)
    }

    fun getNextPrayerForState(
        state: LauncherState,
        nowMillis: Long = System.currentTimeMillis()
    ): NextPrayerInfo = getNextPrayer(resolveCityForState(state), nowMillis)

    fun getNextPrayer(
        cityId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): NextPrayerInfo = getNextPrayer(findCity(cityId), nowMillis)

    fun getNextPrayer(
        city: PrayerCity,
        nowMillis: Long = System.currentTimeMillis()
    ): NextPrayerInfo {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val tzHours = TimeZone.getDefault().getOffset(nowMillis) / 3_600_000.0
        val todayPrayers = computeDayPrayers(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH),
            city = city,
            tzHours = tzHours
        )

        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val upcomingToday = todayPrayers.firstOrNull { it.totalMinutes > nowMinutes }

        if (upcomingToday != null) {
            return NextPrayerInfo(
                city = city,
                nextPrayer = upcomingToday,
                remainingMinutes = upcomingToday.totalMinutes - nowMinutes,
                todayPrayers = todayPrayers
            )
        }

        // After Isha -> compute tomorrow's Fajr
        val tomorrow = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val tomorrowTz = TimeZone.getDefault().getOffset(tomorrow.timeInMillis) / 3_600_000.0
        val tomorrowFajr = computeDayPrayers(
            year = tomorrow.get(Calendar.YEAR),
            month = tomorrow.get(Calendar.MONTH) + 1,
            day = tomorrow.get(Calendar.DAY_OF_MONTH),
            city = city,
            tzHours = tomorrowTz
        ).first()

        val remaining = (24 * 60 - nowMinutes) + tomorrowFajr.totalMinutes
        return NextPrayerInfo(
            city = city,
            nextPrayer = tomorrowFajr,
            remainingMinutes = remaining,
            todayPrayers = todayPrayers
        )
    }

    private fun computeDayPrayers(
        year: Int,
        month: Int,
        day: Int,
        city: PrayerCity,
        tzHours: Double
    ): List<PrayerEntry> {
        val jd = julianDate(year, month, day) - city.lng / (15.0 * 24.0)

        val fajrH = computeAngleTime(jd, 5.0 / 24.0, city.fajrAngle, city.lat, ccw = true)
        val dhuhrH = computeMidDay(jd, 12.0 / 24.0)
        val asrH = computeAsr(jd, 13.0 / 24.0, city.lat, factor = 1.0)
        val maghribH = computeAngleTime(jd, 18.0 / 24.0, 0.833, city.lat, ccw = false)
        val ishaH = if (city.ishaIntervalMinutes != null) {
            maghribH + city.ishaIntervalMinutes / 60.0
        } else {
            computeAngleTime(jd, 18.0 / 24.0, city.ishaAngle, city.lat, ccw = false)
        }

        val offset = tzHours - city.lng / 15.0

        return listOf(
            toEntry("fajr", "الفجر", "Fajr", fajrH + offset),
            toEntry("dhuhr", "الظهر", "Dhuhr", dhuhrH + offset),
            toEntry("asr", "العصر", "Asr", asrH + offset),
            toEntry("maghrib", "المغرب", "Maghrib", maghribH + offset),
            toEntry("isha", "العشاء", "Isha", ishaH + offset)
        )
    }

    private fun toEntry(id: String, nameAr: String, nameEn: String, rawHours: Double): PrayerEntry {
        val normalized = ((rawHours % 24.0) + 24.0) % 24.0
        val totalMin = (normalized * 60.0 + 0.5).toInt() % (24 * 60)
        return PrayerEntry(
            id = id,
            nameAr = nameAr,
            nameEn = nameEn,
            hour24 = totalMin / 60,
            minute = totalMin % 60,
            totalMinutes = totalMin
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0
        val eqT = q / 15.0 - fixHour(ra)
        val decl = darcsin(dsin(e) * dsin(l))
        return decl to eqT
    }

    private fun computeMidDay(jd: Double, t: Double): Double {
        val (_, eqT) = sunPosition(jd + t)
        return fixHour(12.0 - eqT)
    }

    private fun computeAngleTime(jd: Double, t: Double, angle: Double, lat: Double, ccw: Boolean): Double {
        val (decl, _) = sunPosition(jd + t)
        val noon = computeMidDay(jd, t)
        val cosVal = (-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat))
        val clamped = cosVal.coerceIn(-1.0, 1.0)
        val diff = darccos(clamped) / 15.0
        return noon + if (ccw) -diff else diff
    }

    private fun computeAsr(jd: Double, t: Double, lat: Double, factor: Double): Double {
        val (decl, _) = sunPosition(jd + t)
        val angle = -darccot(factor + dtan(abs(lat - decl)))
        return computeAngleTime(jd, t, angle, lat, ccw = false)
    }

    private fun dsin(d: Double) = sin(Math.toRadians(d))
    private fun dcos(d: Double) = cos(Math.toRadians(d))
    private fun dtan(d: Double) = tan(Math.toRadians(d))
    private fun darcsin(x: Double) = Math.toDegrees(asin(x))
    private fun darccos(x: Double) = Math.toDegrees(acos(x))
    private fun darctan2(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    private fun darccot(x: Double) = Math.toDegrees(atan(1.0 / x))
    private fun fixAngle(a: Double): Double = a - 360.0 * floor(a / 360.0)
    private fun fixHour(h: Double): Double = h - 24.0 * floor(h / 24.0)
}
