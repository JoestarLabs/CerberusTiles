package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverlayDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun overlayDialog_whenShowDialogTrue_displaysDialogAndHandlesClose() {
        val showDialogState = mutableStateOf(true)
        var dismissed = false

        composeTestRule.setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)

            val sharedParams = SharedParams(
                context = context,
                coroutineScope = scope,
                showDialog = showDialogState,
                dialogType = androidx.compose.runtime.remember { mutableStateOf(DialogType.NONE) },
                sharedPreferences = sharedPreferences
            )

            val params = OverlayDialogParams(
                showDialog = showDialogState,
                onDismiss = { dismissed = true },
                canWrite = true,
                isSwitchedOn = false,
                setSwitchedOn = {},
                toggleAdaptiveBrightness = {},
                openPermissionSettings = {},
                isVibrationModeOn = false,
                setVibrationMode = {},
                toggleVibrationMode = { true },
                isChargingOptimizationOn = false,
                isChargingOptimizationSupported = true,
                setChargingOptimization = {},
                toggleChargingOptimization = {},
                sharedParams = sharedParams,
                currentRingerMode = RingerMode.NORMAL,
                onRingerModeChange = {}
            )

            CerberusTilesTheme {
                OverlayDialog(params = params)
            }
        }

        // Verify "Close" button is displayed in the dialog
        composeTestRule.onNodeWithText("Close").assertIsDisplayed()

        // Click Close button
        composeTestRule.onNodeWithText("Close").performClick()

        // Assert dialog dismissed and state set to false
        assertTrue(dismissed)
        assertFalse(showDialogState.value)
    }

    @Test
    fun overlayDialog_whenShowDialogFalse_doesNotDisplayDialog() {
        val showDialogState = mutableStateOf(false)

        composeTestRule.setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)

            val sharedParams = SharedParams(
                context = context,
                coroutineScope = scope,
                showDialog = showDialogState,
                dialogType = androidx.compose.runtime.remember { mutableStateOf(DialogType.NONE) },
                sharedPreferences = sharedPreferences
            )

            val params = OverlayDialogParams(
                showDialog = showDialogState,
                onDismiss = {},
                canWrite = true,
                isSwitchedOn = false,
                setSwitchedOn = {},
                toggleAdaptiveBrightness = {},
                openPermissionSettings = {},
                isVibrationModeOn = false,
                setVibrationMode = {},
                toggleVibrationMode = { true },
                isChargingOptimizationOn = false,
                isChargingOptimizationSupported = true,
                setChargingOptimization = {},
                toggleChargingOptimization = {},
                sharedParams = sharedParams,
                currentRingerMode = RingerMode.NORMAL,
                onRingerModeChange = {}
            )

            CerberusTilesTheme {
                OverlayDialog(params = params)
            }
        }

        // Close button should not exist
        composeTestRule.onNodeWithText("Close").assertDoesNotExist()
    }
}
