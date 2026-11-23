package com.bl4ckswordsman.cerberustiles.smartpixels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartPixelsOverlayViewTest {

    @Test
    fun `calculateStep returns 2 for intensity 75 percent and above`() {
        assertEquals(2, SmartPixelsOverlayView.calculateStep(75f))
        assertEquals(2, SmartPixelsOverlayView.calculateStep(85f))
        assertEquals(2, SmartPixelsOverlayView.calculateStep(90f))
    }

    @Test
    fun `calculateStep returns 3 for intensity between 50 and 74 percent`() {
        assertEquals(3, SmartPixelsOverlayView.calculateStep(50f))
        assertEquals(3, SmartPixelsOverlayView.calculateStep(60f))
        assertEquals(3, SmartPixelsOverlayView.calculateStep(74.9f))
    }

    @Test
    fun `calculateStep returns 4 for intensity between 30 and 49 percent`() {
        assertEquals(4, SmartPixelsOverlayView.calculateStep(30f))
        assertEquals(4, SmartPixelsOverlayView.calculateStep(40f))
        assertEquals(4, SmartPixelsOverlayView.calculateStep(49.9f))
    }

    @Test
    fun `calculateStep returns 6 for intensity below 30 percent`() {
        assertEquals(6, SmartPixelsOverlayView.calculateStep(10f))
        assertEquals(6, SmartPixelsOverlayView.calculateStep(20f))
        assertEquals(6, SmartPixelsOverlayView.calculateStep(29.9f))
    }

    @Test
    fun `calculateAlpha returns expected alpha values coerced in range 20 to 230`() {
        val alpha10 = SmartPixelsOverlayView.calculateAlpha(10f)
        val alpha50 = SmartPixelsOverlayView.calculateAlpha(50f)
        val alpha90 = SmartPixelsOverlayView.calculateAlpha(90f)

        assertEquals(25, alpha10)
        assertEquals(127, alpha50)
        assertEquals(229, alpha90)

        // Extreme bounds check
        val alphaMin = SmartPixelsOverlayView.calculateAlpha(0f)
        val alphaMax = SmartPixelsOverlayView.calculateAlpha(100f)
        assertTrue(alphaMin >= 20)
        assertTrue(alphaMax <= 230)
    }

    @Test
    fun `shiftPattern cycles patternOffset through 0 to 3 and wraps around`() {
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        val view = SmartPixelsOverlayView(context)

        assertEquals(0, view.patternOffset)
        view.shiftPattern()
        assertEquals(1, view.patternOffset)
        view.shiftPattern()
        assertEquals(2, view.patternOffset)
        view.shiftPattern()
        assertEquals(3, view.patternOffset)
        view.shiftPattern()
        assertEquals(0, view.patternOffset)
    }

    @Test
    fun `setIntensity updates currentIntensity`() {
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        val view = SmartPixelsOverlayView(context)

        assertEquals(50f, view.currentIntensity, 0.01f)
        view.setIntensity(75f)
        assertEquals(75f, view.currentIntensity, 0.01f)
    }

    @Test
    fun `calculateAlpha returns 255 when pitchBlack is true regardless of intensity`() {
        assertEquals(255, SmartPixelsOverlayView.calculateAlpha(10f, pitchBlack = true))
        assertEquals(255, SmartPixelsOverlayView.calculateAlpha(50f, pitchBlack = true))
        assertEquals(255, SmartPixelsOverlayView.calculateAlpha(90f, pitchBlack = true))
    }

    @Test
    fun `setPitchBlack updates isPitchBlack`() {
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        val view = SmartPixelsOverlayView(context)

        assertEquals(false, view.isPitchBlack)
        view.setPitchBlack(true)
        assertEquals(true, view.isPitchBlack)
    }
}
