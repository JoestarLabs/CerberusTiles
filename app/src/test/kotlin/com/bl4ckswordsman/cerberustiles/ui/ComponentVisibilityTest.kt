package com.bl4ckswordsman.cerberustiles.ui

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentVisibilityTest {

    @Test
    fun `default ComponentVisibility has all components visible`() {
        val visibility = ComponentVisibility()
        assertTrue(visibility.adaptBrightnessSwitch)
        assertTrue(visibility.brightnessSlider)
        assertTrue(visibility.ringerModeSelector)
        assertTrue(visibility.chargingOptimizationSwitch)
        assertTrue(visibility.smartPixelsSwitch)
    }

    @Test
    fun `fromPreferences maps all keys from SharedPreferences`() {
        val prefs = mockk<SharedPreferences>()
        every { prefs.getBoolean(ComponentVisibility.KEY_ADAPTIVE_BRIGHTNESS, true) } returns true
        every { prefs.getBoolean(ComponentVisibility.KEY_BRIGHTNESS_SLIDER, true) } returns false
        every { prefs.getBoolean(ComponentVisibility.KEY_RINGER_MODE, true) } returns true
        every { prefs.getBoolean(ComponentVisibility.KEY_CHARGING_OPTIMIZATION, true) } returns false
        every { prefs.getBoolean(ComponentVisibility.KEY_SMART_PIXELS, true) } returns true

        val visibility = ComponentVisibility.fromPreferences(prefs)

        assertTrue(visibility.adaptBrightnessSwitch)
        assertFalse(visibility.brightnessSlider)
        assertTrue(visibility.ringerModeSelector)
        assertFalse(visibility.chargingOptimizationSwitch)
        assertTrue(visibility.smartPixelsSwitch)
    }

    @Test
    fun `constant keys match expected SharedPreferences keys`() {
        assertEquals("adaptBrightnessSwitch", ComponentVisibility.KEY_ADAPTIVE_BRIGHTNESS)
        assertEquals("brightnessSlider", ComponentVisibility.KEY_BRIGHTNESS_SLIDER)
        assertEquals("ringerModeSelector", ComponentVisibility.KEY_RINGER_MODE)
        assertEquals("chargingOptimizationSwitch", ComponentVisibility.KEY_CHARGING_OPTIMIZATION)
        assertEquals("smartPixelsSwitch", ComponentVisibility.KEY_SMART_PIXELS)
    }
}
