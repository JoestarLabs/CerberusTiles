package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun switchWithLabel_displaysLabelAndTogglesOnClick() {
        val checkedState = mutableStateOf(false)
        var callbackInvocationCount = 0

        composeTestRule.setContent {
            CerberusTilesTheme {
                SwitchWithLabel(
                    isSwitchedOn = checkedState.value,
                    onCheckedChange = {
                        checkedState.value = it
                        callbackInvocationCount++
                    },
                    label = "Test Setting"
                )
            }
        }

        // Verify label is visible
        composeTestRule.onNodeWithText("Test Setting").assertIsDisplayed()

        // Click row/switch to toggle ON
        composeTestRule.onNodeWithText("Test Setting").performClick()

        assertEquals(1, callbackInvocationCount)
        assertTrue(checkedState.value)
    }

    @Test
    fun switchWithLabel_displaysLeadingIconWhenProvided() {
        composeTestRule.setContent {
            CerberusTilesTheme {
                SwitchWithLabel(
                    isSwitchedOn = false,
                    onCheckedChange = {},
                    label = "Icon Setting",
                    icon = {
                        androidx.compose.material3.Text("LeadingIcon")
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Icon Setting").assertIsDisplayed()
        composeTestRule.onNodeWithText("LeadingIcon").assertIsDisplayed()
    }

    @Test
    fun adbPermissionDialog_displaysTitleAndInvokesDismissOnOk() {
        var dismissed = false

        composeTestRule.setContent {
            val context = LocalContext.current
            CerberusTilesTheme {
                AdbPermissionDialog(
                    context = context,
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Verify dialog title, instructional body, and buttons are displayed
        composeTestRule.onNodeWithText("Permission Required").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "To use this feature, please connect your device to a PC and grant the WRITE_SECURE_SETTINGS permission using the following ADB command:\n\n" +
                    "adb shell pm grant com.bl4ckswordsman.cerberustiles android.permission.WRITE_SECURE_SETTINGS\n\n" +
                    "Note: Shizuku support is planned for the future."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Copy").assertIsDisplayed()

        // Click OK to dismiss
        composeTestRule.onNodeWithText("OK").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun brightnessPresetButtonGroup_rendersAllPresets() {
        composeTestRule.setContent {
            val context = LocalContext.current
            CerberusTilesTheme {
                BrightnessPresetButtonGroup(context = context)
            }
        }

        // Verify all preset buttons and content descriptions exist
        composeTestRule.onNodeWithContentDescription("Minimum brightness").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Balanced brightness").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Maximum brightness").assertIsDisplayed()
        composeTestRule.onNodeWithText("Min").assertIsDisplayed()
        composeTestRule.onNodeWithText("Balanced").assertIsDisplayed()
        composeTestRule.onNodeWithText("Max").assertIsDisplayed()
    }
}
