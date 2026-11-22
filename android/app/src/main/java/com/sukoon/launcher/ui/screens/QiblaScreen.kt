package com.sukoon.launcher.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.PrayerTimesCalculator
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QiblaOverlay(
    state: LauncherState,
    onAlignedHaptic: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current

    val city = remember(state.prayerCityId, state.useGpsLocation, state.customLat, state.customLng, state.customLocationLabel) {
        PrayerTimesCalculator.resolveCityForState(state)
    }
    val qiblaBearing = remember(city.lat, city.lng) {
        PrayerTimesCalculator.calculateQiblaBearing(city.lat, city.lng)
    }
    val distanceKm = remember(city.lat, city.lng) {
        PrayerTimesCalculator.calculateDistanceToKaabaKm(city.lat, city.lng)
    }

    var azimuthDeg by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotSensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (sm == null || (rotSensor == null && (accelSensor == null || magSensor == null))) {
            hasSensor = false
            onDispose { }
        } else {
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            val rMat = FloatArray(9)
            val orientation = FloatArray(3)
            var smoothAz = 0f
            var initialized = false

            fun updateAzimuth(rawDeg: Float) {
                val norm = (rawDeg + 360f) % 360f
                if (!initialized) {
                    smoothAz = norm
                    initialized = true
                } else {
                    var delta = norm - smoothAz
                    if (delta > 180f) delta -= 360f
                    if (delta < -180f) delta += 360f
                    smoothAz = (smoothAz + delta * 0.14f + 360f) % 360f
                }
                azimuthDeg = smoothAz
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rMat, event.values)
                            SensorManager.getOrientation(rMat, orientation)
                            val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            updateAzimuth(deg)
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, 3)
                            if (SensorManager.getRotationMatrix(rMat, null, gravity, geomagnetic)) {
                                SensorManager.getOrientation(rMat, orientation)
                                updateAzimuth(Math.toDegrees(orientation[0].toDouble()).toFloat())
                            }
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                            if (SensorManager.getRotationMatrix(rMat, null, gravity, geomagnetic)) {
                                SensorManager.getOrientation(rMat, orientation)
                                updateAzimuth(Math.toDegrees(orientation[0].toDouble()).toFloat())
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (rotSensor != null) {
                sm.registerListener(listener, rotSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                sm.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sm.unregisterListener(listener)
            }
        }
    }

    // Relative angle from phone's top to Kaaba
    val relativeQibla = ((qiblaBearing.toFloat() - azimuthDeg) + 360f) % 360f
    val diffFromZero = if (relativeQibla > 180f) 360f - relativeQibla else relativeQibla
    val isAligned = hasSensor && abs(diffFromZero) <= 5f

    LaunchedEffect(isAligned) {
        if (isAligned) onAlignedHaptic()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlayBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(strings.qiblaSub, fontFamily = appFont, fontSize = 12.sp, color = colors.textMuted)
                Text(strings.qiblaTitle, fontFamily = appFont, fontSize = 22.sp, color = colors.textPrimary)
            }
            SmallChip(strings.close) { onClose() }
        }

        // City & Bearing Summary Pill
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = city.localizedFull(strings.isArabic),
                fontFamily = appFont,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Text(
                text = strings.qiblaAngleFmt(qiblaBearing.roundToInt().formatDigits(strings.isArabic)),
                fontFamily = appFont,
                fontSize = 13.sp,
                color = colors.accentWarm
            )
            Text(
                text = strings.qiblaDistanceFmt(distanceKm.formatDigits(strings.isArabic)),
                fontFamily = appFont,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        // Minimalist Rotating Compass Dial
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            val ringBorder = if (isAligned) colors.accentFocus else colors.borderStrong
            val needleColor = if (isAligned) colors.accentFocus else colors.accentWarm
            val mutedCol = colors.textMuted
            val surfaceCol = colors.surface

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 12.dp.toPx()

                // Outer dial circle
                drawCircle(color = surfaceCol, radius = radius, center = center)
                drawCircle(
                    color = ringBorder,
                    radius = radius,
                    center = center,
                    style = Stroke(width = if (isAligned) 3.dp.toPx() else 1.5.dp.toPx())
                )

                // Subtle cardinal tick marks rotated by -azimuthDeg (North rotates)
                rotate(degrees = -azimuthDeg, pivot = center) {
                    for (deg in 0 until 360 step 30) {
                        val isCardinal = deg % 90 == 0
                        val tickLen = if (isCardinal) 12.dp.toPx() else 6.dp.toPx()
                        rotate(degrees = deg.toFloat(), pivot = center) {
                            drawLine(
                                color = if (deg == 0) colors.dangerSoft else mutedCol.copy(alpha = 0.45f),
                                start = Offset(center.x, center.y - radius + 4.dp.toPx()),
                                end = Offset(center.x, center.y - radius + 4.dp.toPx() + tickLen),
                                strokeWidth = if (isCardinal) 2.5.dp.toPx() else 1.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Qibla Pointer Arrow rotated to relativeQibla
                rotate(degrees = if (hasSensor) relativeQibla else qiblaBearing.toFloat(), pivot = center) {
                    val arrowTipY = center.y - radius + 22.dp.toPx()
                    val path = Path().apply {
                        moveTo(center.x, arrowTipY)
                        lineTo(center.x - 12.dp.toPx(), center.y - 8.dp.toPx())
                        lineTo(center.x, center.y - 22.dp.toPx())
                        lineTo(center.x + 12.dp.toPx(), center.y - 8.dp.toPx())
                        close()
                    }
                    drawPath(path = path, color = needleColor)
                    drawLine(
                        color = needleColor.copy(alpha = 0.35f),
                        start = center,
                        end = Offset(center.x, center.y + radius * 0.45f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Center Pivot Dot
                drawCircle(color = needleColor, radius = 6.dp.toPx(), center = center)
            }

            // Static Top Alignment Notch
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isAligned) colors.accentFocus else colors.accentWarm)
            )
        }

        // Status Feedback Pill + Calibration Hint
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isAligned) colors.accentFocus.copy(alpha = 0.16f) else colors.accentWarmSoft)
                    .border(
                        1.dp,
                        if (isAligned) colors.accentFocus else colors.accentWarm.copy(alpha = 0.5f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (!hasSensor) strings.qiblaNoSensor
                    else if (isAligned) strings.qiblaAligned
                    else strings.qiblaTurnHint,
                    fontFamily = appFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAligned) colors.accentFocus else colors.accentWarm,
                    textAlign = TextAlign.Center
                )
            }

            if (hasSensor) {
                Text(
                    text = strings.qiblaCalibrateHint,
                    fontFamily = appFont,
                    fontSize = 11.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
