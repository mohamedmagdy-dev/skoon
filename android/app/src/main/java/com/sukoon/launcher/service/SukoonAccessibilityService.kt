package com.sukoon.launcher.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.FingerprintGestureController
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AccessibilityService that captures rear fingerprint sensor swipe/brush gestures
 * on devices like Samsung Galaxy A20s (Android 11) and emits events to the Dhikr counter.
 */
class SukoonAccessibilityService : AccessibilityService() {

    companion object {
        private val _fingerprintEvents = MutableSharedFlow<Int>(extraBufferCapacity = 16)
        val fingerprintEvents = _fingerprintEvents.asSharedFlow()

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected = _isServiceConnected.asStateFlow()

        private val _isFingerprintGestureAvailable = MutableStateFlow(false)
        val isFingerprintGestureAvailable = _isFingerprintGestureAvailable.asStateFlow()
    }

    private var gestureCallback: FingerprintGestureController.FingerprintGestureCallback? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceConnected.value = true

        try {
            val controller = fingerprintGestureController
            _isFingerprintGestureAvailable.value = controller.isGestureDetectionAvailable

            val cb = object : FingerprintGestureController.FingerprintGestureCallback() {
                override fun onGestureDetectionAvailabilityChanged(available: Boolean) {
                    _isFingerprintGestureAvailable.value = available
                }

                override fun onGestureDetected(gesture: Int) {
                    _fingerprintEvents.tryEmit(gesture)
                }
            }
            gestureCallback = cb
            controller.registerFingerprintGestureCallback(cb, Handler(Looper.getMainLooper()))
        } catch (_: Exception) {
            _isFingerprintGestureAvailable.value = false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed — we only listen for fingerprint gestures
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceConnected.value = false
        _isFingerprintGestureAvailable.value = false
        try {
            gestureCallback?.let { fingerprintGestureController.unregisterFingerprintGestureCallback(it) }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
