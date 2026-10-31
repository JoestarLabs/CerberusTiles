package com.bl4ckswordsman.cerberustiles.models

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.ui.DialogType
import com.bl4ckswordsman.cerberustiles.ui.HomeScreenParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppModelsTest {

    @Test
    fun `DialogType entries contain all supported dialog variants`() {
        val types = DialogType.entries.map { it.name }.toSet()
        assertTrue(types.contains("NONE"))
        assertTrue(types.contains("COMPONENT_VISIBILITY"))
        assertTrue(types.contains("APP_VERSION"))
        assertTrue(types.contains("SMART_PIXELS"))
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
        assertEquals("Home", Constants.HOME_SCREEN)
        assertEquals("Settings", Constants.SETTINGS_SCREEN)
    }

    @Test
    fun `HomeScreenParams constructs correctly with given properties`() {
        var adaptiveToggled = false
        var vibrationToggled = false
        var chargingToggled = false
        var smartPixelsToggled = false

        val params = HomeScreenParams(
            innerPadding = PaddingValues(12.dp),
            canWriteState = true,
            isSwitchedOn = true,
            setSwitchedOn = {},
            toggleAdaptiveBrightness = { adaptiveToggled = true },
            openPermissionSettings = {},
            isVibrationModeOn = false,
            setVibrationMode = {},
            toggleVibrationMode = { vibrationToggled = true; true },
            isChargingOptimizationOn = true,
            setChargingOptimization = {},
            toggleChargingOptimization = { chargingToggled = it },
            currentRingerMode = RingerMode.VIBRATE,
            onRingerModeChange = {},
            isSmartPixelsOn = true,
            toggleSmartPixels = { smartPixelsToggled = it }
        )

        assertEquals(PaddingValues(12.dp), params.innerPadding)
        assertTrue(params.canWriteState)
        assertTrue(params.isSwitchedOn)
        assertFalse(params.isVibrationModeOn)
        assertTrue(params.isChargingOptimizationOn)
        assertTrue(params.isSmartPixelsOn)
        assertEquals(RingerMode.VIBRATE, params.currentRingerMode)

        params.toggleAdaptiveBrightness()
        assertTrue(adaptiveToggled)

        assertTrue(params.toggleVibrationMode())
        assertTrue(vibrationToggled)

        params.toggleChargingOptimization(true)
        assertTrue(chargingToggled)

        params.toggleSmartPixels?.invoke(false)
        assertFalse(smartPixelsToggled)
    }

    @Test
    fun `BrightnessPreset contains expected enum constants and levels`() {
        val presets = BrightnessPreset.entries.map { it.name }.toSet()
        assertEquals(setOf("MIN", "BALANCED", "MAX"), presets)

        assertEquals(0f, BrightnessPreset.MIN.level, 0.001f)
        assertEquals(0.75f, BrightnessPreset.BALANCED.level, 0.001f)
        assertEquals(1f, BrightnessPreset.MAX.level, 0.001f)
    }

    @Test
    fun `BrightnessPreset fromBrightness maps thresholds correctly`() {
        assertEquals(BrightnessPreset.MIN, BrightnessPreset.fromBrightness(0))
        assertEquals(BrightnessPreset.MIN, BrightnessPreset.fromBrightness(1))
        assertEquals(BrightnessPreset.MIN, BrightnessPreset.fromBrightness(30))

        assertEquals(BrightnessPreset.BALANCED, BrightnessPreset.fromBrightness(31))
        assertEquals(BrightnessPreset.BALANCED, BrightnessPreset.fromBrightness(128))
        assertEquals(BrightnessPreset.BALANCED, BrightnessPreset.fromBrightness(199))

        assertEquals(BrightnessPreset.MAX, BrightnessPreset.fromBrightness(200))
        assertEquals(BrightnessPreset.MAX, BrightnessPreset.fromBrightness(255))
    }

    @Test
    fun `NetworkSpeedLimit presets contain expected labels and values`() {
        assertEquals(6, NetworkSpeedLimit.DEFAULT_PRESETS.size)

        assertEquals("No limit", NetworkSpeedLimit.NO_LIMIT.label)
        assertEquals(-1L, NetworkSpeedLimit.NO_LIMIT.bytesPerSecond)
        assertTrue(NetworkSpeedLimit.NO_LIMIT.isUnlimited)

        assertEquals("128 kbps", NetworkSpeedLimit.LIMIT_128K.label)
        assertEquals(16_000L, NetworkSpeedLimit.LIMIT_128K.bytesPerSecond)
        assertFalse(NetworkSpeedLimit.LIMIT_128K.isUnlimited)

        assertEquals("256 kbps", NetworkSpeedLimit.LIMIT_256K.label)
        assertEquals(32_000L, NetworkSpeedLimit.LIMIT_256K.bytesPerSecond)

        assertEquals("1 Mbps", NetworkSpeedLimit.LIMIT_1M.label)
        assertEquals(125_000L, NetworkSpeedLimit.LIMIT_1M.bytesPerSecond)

        assertEquals("5 Mbps", NetworkSpeedLimit.LIMIT_5M.label)
        assertEquals(625_000L, NetworkSpeedLimit.LIMIT_5M.bytesPerSecond)

        assertEquals("15 Mbps", NetworkSpeedLimit.LIMIT_15M.label)
        assertEquals(1_875_000L, NetworkSpeedLimit.LIMIT_15M.bytesPerSecond)
    }

    @Test
    fun `NetworkSpeedLimit formatBytesPerSecond formats rates properly`() {
        assertEquals("No limit", NetworkSpeedLimit.formatBytesPerSecond(-1L))
        assertEquals("No limit", NetworkSpeedLimit.formatBytesPerSecond(0L))
        assertEquals("128 kbps", NetworkSpeedLimit.formatBytesPerSecond(16_000L))
        assertEquals("1 Mbps", NetworkSpeedLimit.formatBytesPerSecond(125_000L))
        assertEquals("15 Mbps", NetworkSpeedLimit.formatBytesPerSecond(1_875_000L))
        assertEquals("1 Gbps", NetworkSpeedLimit.formatBytesPerSecond(125_000_000L))
        assertEquals("800 bps", NetworkSpeedLimit.formatBytesPerSecond(100L))
    }

    @Test
    fun `NetworkSpeedLimit fromBytesPerSecond resolves presets or synthesizes custom limit`() {
        assertEquals(NetworkSpeedLimit.NO_LIMIT, NetworkSpeedLimit.fromBytesPerSecond(-1L))
        assertEquals(NetworkSpeedLimit.NO_LIMIT, NetworkSpeedLimit.fromBytesPerSecond(0L))
        assertEquals(NetworkSpeedLimit.LIMIT_1M, NetworkSpeedLimit.fromBytesPerSecond(125_000L))

        val custom = NetworkSpeedLimit.fromBytesPerSecond(375_000L)
        assertEquals(375_000L, custom.bytesPerSecond)
        assertEquals("3 Mbps", custom.label)
    }
}
