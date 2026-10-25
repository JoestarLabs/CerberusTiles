/*
 * Copyright (c) 2025 Sameera Sandakelum (sameerasw/essentials)
 * Portions Copyright (c) 2025-2026 JoestarLabs / CerberusTiles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bl4ckswordsman.cerberustiles.smartpixels

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import java.util.function.Consumer

/**
 * Manages the overlay view, display casting detection, screen state transitions,
 * and periodic burn-in pattern shifting for [SmartPixelsAccessibilityService].
 *
 * Uses [WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY] which:
 * 1. Overlays status bar, navigation bar, and display cutout seamlessly.
 * 2. Overlays Android Settings and system dialogs.
 * 3. Bypasses tapjacking touch blocks (e.g. in the GitHub app) as an OS-trusted system overlay.
 * 4. Enables 100% pitch-black OLED pixel shutoff (`alpha = 1.0f`).
 */
class SmartPixelsOverlayController(
    private val service: AccessibilityService
) {
    private var windowManager: WindowManager? = null
    private var displayManager: DisplayManager? = null
    private var overlayView: SmartPixelsOverlayView? = null
    private var isOverlayAdded = false

    private val handler = Handler(Looper.getMainLooper())
    private val shiftPatternRunnable = object : Runnable {
        override fun run() {
            overlayView?.shiftPattern()
            handler.postDelayed(this, BURN_IN_SHIFT_INTERVAL_MS)
        }
    }

    private var isScreenRecordingActive = false
    private var screenRecordingCallback: Consumer<Int>? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = updateState()
        override fun onDisplayRemoved(displayId: Int) = updateState()
        override fun onDisplayChanged(displayId: Int) = updateState()
    }

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    handler.removeCallbacks(shiftPatternRunnable)
                }
                Intent.ACTION_SCREEN_ON -> {
                    if (isOverlayAdded) {
                        handler.removeCallbacks(shiftPatternRunnable)
                        handler.postDelayed(shiftPatternRunnable, BURN_IN_SHIFT_INTERVAL_MS)
                    }
                }
            }
        }
    }

    fun init() {
        windowManager = service.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        displayManager = service.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        displayManager?.registerDisplayListener(displayListener, handler)

        registerScreenStateReceiver()
        registerScreenRecordingCallback()
    }

    private fun registerScreenStateReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        ContextCompat.registerReceiver(
            service,
            screenStateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun registerScreenRecordingCallback() {
        if (Build.VERSION.SDK_INT >= 35 && windowManager != null) {
            try {
                val callback = Consumer<Int> { state ->
                    isScreenRecordingActive = (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE)
                    updateState()
                }
                screenRecordingCallback = callback
                windowManager?.addScreenRecordingCallback({ command -> handler.post(command) }, callback)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register screen recording callback", e)
            }
        }
    }

    private fun unregisterScreenRecordingCallback() {
        if (Build.VERSION.SDK_INT >= 35 && screenRecordingCallback != null) {
            try {
                windowManager?.removeScreenRecordingCallback(screenRecordingCallback!!)
            } catch (_: Exception) {
            }
            screenRecordingCallback = null
        }
    }

    fun updateState() {
        val isEnabled = SettingsUtils.SmartPixels.isSmartPixelsEnabled(service)
        val disableOnCast = SettingsUtils.SmartPixels.isDisableOnCastEnabled(service)
        val intensity = SettingsUtils.SmartPixels.getSmartPixelsIntensity(service)

        if (isEnabled && !(disableOnCast && isScreenCastingOrMirroring())) {
            showOverlay(intensity)
        } else {
            hideOverlay()
        }
    }

    private fun isScreenCastingOrMirroring(): Boolean {
        if (isScreenRecordingActive) return true
        val displays = displayManager?.displays ?: return false
        for (display in displays) {
            if (display.displayId == Display.DEFAULT_DISPLAY) continue
            val flags = display.flags
            val isPresentation = (flags and Display.FLAG_PRESENTATION) != 0
            val isPrivate = (flags and Display.FLAG_PRIVATE) != 0
            if (isPresentation || isPrivate || display.displayId != Display.DEFAULT_DISPLAY) {
                return true
            }
        }
        return false
    }

    fun setIntensity(intensity: Float) {
        overlayView?.setIntensity(intensity)
    }

    private fun showOverlay(intensity: Float) {
        if (overlayView == null) {
            overlayView = SmartPixelsOverlayView(service)
        }
        overlayView?.setIntensity(intensity)

        if (!isOverlayAdded && windowManager != null && overlayView != null) {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            ).apply {
                alpha = 1.0f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            try {
                overlayView?.visibility = View.VISIBLE
                overlayView?.alpha = 0f
                windowManager?.addView(overlayView, params)
                isOverlayAdded = true

                android.animation.ObjectAnimator.ofFloat(overlayView, "alpha", 0f, 1f).apply {
                    duration = 200
                    start()
                }

                handler.removeCallbacks(shiftPatternRunnable)
                handler.postDelayed(shiftPatternRunnable, BURN_IN_SHIFT_INTERVAL_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to attach Smart Pixels overlay view", e)
            }
        } else if (isOverlayAdded && overlayView != null) {
            overlayView?.visibility = View.VISIBLE
            overlayView?.alpha = 1f
            overlayView?.invalidate()
        }
    }

    fun hideOverlay() {
        if (isOverlayAdded && overlayView != null) {
            handler.removeCallbacks(shiftPatternRunnable)
            val currentView = overlayView ?: return
            currentView.visibility = View.GONE
            try {
                windowManager?.removeView(currentView)
            } catch (_: Exception) {
            }
            isOverlayAdded = false
        }
    }

    fun destroy() {
        unregisterScreenRecordingCallback()
        displayManager?.unregisterDisplayListener(displayListener)
        try {
            service.unregisterReceiver(screenStateReceiver)
        } catch (_: Exception) {
        }
        handler.removeCallbacks(shiftPatternRunnable)
        hideOverlay()
        overlayView = null
        displayManager = null
        windowManager = null
    }

    companion object {
        private const val TAG = "SmartPixelsController"
        private const val BURN_IN_SHIFT_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
    }
}
