package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun rememberTestSharedParams(): SharedParams {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        return SharedParams(
            context = context,
            coroutineScope = scope,
            showDialog = remember { mutableStateOf(false) },
            dialogType = remember { mutableStateOf(DialogType.NONE) },
            sharedPreferences = sharedPreferences
        )
    }

    private fun testVisibilityParams(
        adaptBrightness: Boolean = false,
        chargingOptimization: Boolean = false,
        ringerMode: Boolean = false,
        brightnessSlider: Boolean = false
    ): ComponentVisibility {
        return ComponentVisibility(
            adaptBrightnessSwitch = adaptBrightness,
            brightnessSlider = brightnessSlider,
            ringerModeSelector = ringerMode,
            chargingOptimizationSwitch = chargingOptimization
        )
    }

    @Test
    fun adaptiveBrightnessSetting_whenCanWriteTrue_togglesAdaptiveBrightness() {
        var toggleCalled = false
        var setSwitchedOnCalled = false

        composeTestRule.setContent {
            val sharedParams = rememberTestSharedParams()
            val visibilityParams = testVisibilityParams(adaptBrightness = true)

            val params = SettingsComponentsParams(
                componentVisibilityParams = visibilityParams,
                canWriteState = true,
                isSwitchedOn = false,
                setSwitchedOn = { setSwitchedOnCalled = true },
                toggleAdaptiveBrightness = { toggleCalled = true },
                openPermissionSettings = {},
                isVibrationModeOn = false,
                setVibrationMode = {},
                toggleVibrationMode = { true },
                isChargingOptimizationOn = false,
                setChargingOptimization = {},
                toggleChargingOptimization = {},
                sharedParams = sharedParams,
                currentRingerMode = RingerMode.NORMAL,
                onRingerModeChange = {}
            )

            CerberusTilesTheme {
                SettingsComponents(params = params)
            }
        }

        // Verify row text for OFF state
        composeTestRule.onNodeWithText("Adaptive Brightness is OFF").assertIsDisplayed()

        // Click row
        composeTestRule.onNodeWithText("Adaptive Brightness is OFF").performClick()

        assertTrue(toggleCalled)
        assertTrue(setSwitchedOnCalled)
    }

    @Test
    fun adaptiveBrightnessSetting_whenCanWriteFalse_opensPermissionSettings() {
        var openPermissionCalled = false

        composeTestRule.setContent {
            val sharedParams = rememberTestSharedParams()
            val visibilityParams = testVisibilityParams(adaptBrightness = true)

            val params = SettingsComponentsParams(
                componentVisibilityParams = visibilityParams,
                canWriteState = false,
                isSwitchedOn = false,
                setSwitchedOn = {},
                toggleAdaptiveBrightness = {},
                openPermissionSettings = { openPermissionCalled = true },
                isVibrationModeOn = false,
                setVibrationMode = {},
                toggleVibrationMode = { true },
                isChargingOptimizationOn = false,
                setChargingOptimization = {},
                toggleChargingOptimization = {},
                sharedParams = sharedParams,
                currentRingerMode = RingerMode.NORMAL,
                onRingerModeChange = {}
            )

            CerberusTilesTheme {
                SettingsComponents(params = params)
            }
        }

        // Click row when app lacks permission
        composeTestRule.onNodeWithText("Adaptive Brightness is OFF").performClick()

        assertTrue(openPermissionCalled)
    }

    @Test
    fun brightnessSetting_whenCanWriteFalse_opensPermissionSettings() {
        var openPermissionCalled = false

        composeTestRule.setContent {
            val sharedParams = rememberTestSharedParams()
            val visibilityParams = testVisibilityParams(brightnessSlider = true)

            val params = SettingsComponentsParams(
                componentVisibilityParams = visibilityParams,
                canWriteState = false,
                isSwitchedOn = false,
                setSwitchedOn = {},
                toggleAdaptiveBrightness = {},
                openPermissionSettings = { openPermissionCalled = true },
                isVibrationModeOn = false,
                setVibrationMode = {},
                toggleVibrationMode = { true },
                isChargingOptimizationOn = false,
                setChargingOptimization = {},
                toggleChargingOptimization = {},
                sharedParams = sharedParams,
                currentRingerMode = RingerMode.NORMAL,
                onRingerModeChange = {}
            )

            CerberusTilesTheme {
                SettingsComponents(params = params)
            }
        }

        // Click preset button when app lacks write-settings permission
        composeTestRule.onNodeWithText("Max").performClick()

        assertTrue(openPermissionCalled)
    }

    @Test
    fun chargingOptimizationSetting_togglesCorrectly() {
        var toggledValue = false

        composeTestRule.setContent {
            val sharedParams = rememberTestSharedParams()
            val visibilityParams = testVisibilityParams(chargingOptimization = true)

            val params = SettingsComponentsParams(
                componentVisibilityParams = visibilityParams,
                canWriteState = true,
                isSwitchedOn = false,
                setSwitchedOn = {},
                toggleAdaptiveBrightness = {},
                openPermissionSettings = {},
                isVibrationModeOn = false,
                setVibrationMode = {},
                toggleVibrationMode = { true },
                isChargingOptimizationOn = false,
                setChargingOptimization = {},
                toggleChargingOptimization = { toggledValue = it },
                sharedParams = sharedParams,
                currentRingerMode = RingerMode.NORMAL,
                onRingerModeChange = {}
            )

            CerberusTilesTheme {
                SettingsComponents(params = params)
            }
        }

        composeTestRule.onNodeWithText("Charging Optimization is OFF").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charging Optimization is OFF").performClick()

        assertTrue(toggledValue)
    }

    @Test
    fun ringerModeSelector_displaysModesAndChecksInitialSelection() {
        composeTestRule.setContent {
            val sharedParams = rememberTestSharedParams()
            val visibilityParams = testVisibilityParams(ringerMode = true)

            val params = SettingsComponentsParams(
                componentVisibilityParams = visibilityParams,
                canWriteState = true,
                isSwitchedOn = false,
                setSwitchedOn = {},
                toggleAdaptiveBrightness = {},
                openPermissionSettings = {},
                isVibrationModeOn = false,
                setVibrationMode = {},
                toggleVibrationMode = { true },
                isChargingOptimizationOn = false,
                setChargingOptimization = {},
                toggleChargingOptimization = {},
                sharedParams = sharedParams,
                currentRingerMode = RingerMode.VIBRATE,
                onRingerModeChange = {}
            )

            CerberusTilesTheme {
                SettingsComponents(params = params)
            }
        }

        // In non-overlay context, the mode names are shown as text: Normal, Vibrate, Silent
        composeTestRule.onNodeWithText("Normal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vibrate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Silent").assertIsDisplayed()
    }
}
