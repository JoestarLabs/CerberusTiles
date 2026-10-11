package com.bl4ckswordsman.cerberustiles.navbar

import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.DialogType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationModelsTest {

    @Test
    fun `Screen Home route matches Constants HOME_SCREEN`() {
        assertEquals("Home", Screen.Home.route)
        assertEquals(Constants.HOME_SCREEN, Screen.Home.route)
    }

    @Test
    fun `Screen Settings route matches Constants SETTINGS_SCREEN`() {
        assertEquals("Settings", Screen.Settings.route)
        assertEquals(Constants.SETTINGS_SCREEN, Screen.Settings.route)
    }

    @Test
    fun `Screen Licenses route is Licenses`() {
        assertEquals("Licenses", Screen.Licenses.route)
    }

    @Test
    fun `DialogType entries contain all supported dialog variants`() {
        val types = DialogType.entries.map { it.name }.toSet()
        assertTrue(types.contains("NONE"))
        assertTrue(types.contains("COMPONENT_VISIBILITY"))
        assertTrue(types.contains("APP_VERSION"))
    }

    @Test
    fun `RingerMode contains expected enum constants`() {
        val modes = RingerMode.entries.map { it.name }.toSet()
        assertEquals(setOf("NORMAL", "SILENT", "VIBRATE"), modes)
    }

    @Test
    fun `Constants contains expected intent action strings`() {
        assertEquals(
            "com.bl4ckswordsman.cerberustiles.TOGGLE_ADAPTIVE_BRIGHTNESS",
            Constants.TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION
        )
        assertEquals(
            "com.bl4ckswordsman.cerberustiles.action.TOGGLE_VIBRATION_MODE",
            Constants.TOGGLE_VIBRATION_MODE_ACTION
        )
        assertEquals("Unknown", Constants.UNKNOWN)
    }
}
