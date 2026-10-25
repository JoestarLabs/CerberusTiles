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
    fun `4-phase offset calculation cycles correctly`() {
        var offset = 0
        val offsets = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until 4) {
            val step = 3
            val offsetX = (offset % 2) * step
            val offsetY = (offset / 2) * step
            offsets.add(Pair(offsetX, offsetY))
            offset = (offset + 1) % 4
        }

        assertEquals(Pair(0, 0), offsets[0])
        assertEquals(Pair(3, 0), offsets[1])
        assertEquals(Pair(0, 3), offsets[2])
        assertEquals(Pair(3, 3), offsets[3])
        assertEquals(0, offset) // Back to start
    }
}
