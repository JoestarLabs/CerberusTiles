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
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * A hardened [AccessibilityService] that maintains the [SmartPixelsOverlayView]
 * using [android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY].
 *
 * ## Security & Privacy Hardening
 * - Configured with zero event types (`accessibilityEventTypes=""`).
 * - `canRetrieveWindowContent="false"`: Cannot inspect screen content, read text, or observe passwords.
 * - `canPerformGestures="false"`: Cannot simulate user touches or gestures.
 * - `canRequestFilterKeyEvents="false"`: Cannot intercept keystrokes or hardware buttons.
 * - `canRequestTouchExplorationMode="false"`: Cannot explore touch targets.
 * - `canRequestEnhancedWebAccessibility="false"`: Cannot access web view structures.
 * - Used exclusively as a trusted visual layer for OLED battery saving.
 */
class SmartPixelsAccessibilityService : AccessibilityService() {

    private var controller: SmartPixelsOverlayController? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        controller = SmartPixelsOverlayController(this).also {
            it.init()
            it.updateState()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: strictly used for visual overlay, zero event processing
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onUnbind(intent: Intent?): Boolean {
        controller?.destroy()
        controller = null
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        controller?.destroy()
        controller = null
        instance = null
        super.onDestroy()
    }

    companion object {
        var instance: SmartPixelsAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        fun updateState(context: Context) {
            instance?.controller?.updateState()
        }

        fun updateIntensity(context: Context, intensity: Float) {
            instance?.controller?.setIntensity(intensity)
        }
    }
}
