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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * A custom [View] that renders a hardware-accelerated checkered mesh of black pixels
 * across the screen to reduce OLED/AMOLED display power consumption.
 */
class SmartPixelsOverlayView(context: Context) : View(context) {

    private val paint = Paint()
    private var cachedPatternBitmap: Bitmap? = null
    private var lastWidth = 0
    private var lastHeight = 0
    var currentIntensity: Float = 50f
        private set
    var patternOffset: Int = 0
        private set

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    /**
     * Updates the mesh intensity and invalidates the cached pattern bitmap if changed.
     *
     * @param intensity Mesh intensity percentage between 10% and 90%.
     */
    fun setIntensity(intensity: Float) {
        if (currentIntensity != intensity) {
            currentIntensity = intensity
            recycleCachedBitmap()
            invalidate()
        }
    }

    /**
     * Advances the burn-in shift pattern offset (0..3) to prevent uneven OLED subpixel wear.
     */
    fun shiftPattern() {
        patternOffset = (patternOffset + 1) % 4
        recycleCachedBitmap()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != lastWidth || h != lastHeight) {
            lastWidth = w
            lastHeight = h
            recycleCachedBitmap()
        }
    }

    /**
     * Creates a new checkered bitmap of black rectangles based on the current intensity and offset.
     */
    fun createPatternBitmap(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = false
            alpha = calculateAlpha(currentIntensity)
        }

        val step = calculateStep(currentIntensity)
        val offsetX = (patternOffset % 2) * step
        val offsetY = (patternOffset / 2) * step

        var y = 0
        while (y < h) {
            var x = ((y / step % 2) * step + offsetX) % (step * 2)
            while (x < w) {
                canvas.drawRect(
                    x.toFloat(),
                    (y + offsetY).toFloat(),
                    (x + step).toFloat(),
                    (y + offsetY + step).toFloat(),
                    p
                )
                x += step * 2
            }
            y += step
        }
        return bmp
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        var bitmap = cachedPatternBitmap
        if (bitmap == null || bitmap.isRecycled || bitmap.width != w || bitmap.height != h) {
            bitmap = createPatternBitmap(w, h)
            cachedPatternBitmap = bitmap
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleCachedBitmap()
    }

    private fun recycleCachedBitmap() {
        cachedPatternBitmap?.recycle()
        cachedPatternBitmap = null
    }

    companion object {
        /**
         * Calculates grid step size in pixels based on intensity percentage.
         */
        fun calculateStep(intensity: Float): Int = when {
            intensity >= 75f -> 2
            intensity >= 50f -> 3
            intensity >= 30f -> 4
            else -> 6
        }

        /**
         * Calculates paint alpha value (20..230) corresponding to the intensity percentage.
         */
        fun calculateAlpha(intensity: Float): Int =
            (intensity / 100f * 255).toInt().coerceIn(20, 230)
    }
}
