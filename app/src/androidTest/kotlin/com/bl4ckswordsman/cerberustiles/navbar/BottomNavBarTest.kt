package com.bl4ckswordsman.cerberustiles.navbar

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomNavBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavBar_displaysHomeAndSettingsTabs() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            CerberusTilesTheme {
                BottomNavBar(navController = navController)
            }
        }

        composeTestRule.onNodeWithText(Constants.HOME_SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithText(Constants.SETTINGS_SCREEN).assertIsDisplayed()
    }

    @Test
    fun bottomNavBar_clickingSettingsTab_navigatesToSettings() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            CerberusTilesTheme {
                NavHost(
                    navController = navController,
                    startDestination = Constants.HOME_SCREEN
                ) {
                    composable(Constants.HOME_SCREEN) {
                        Text("Current: Home")
                    }
                    composable(Constants.SETTINGS_SCREEN) {
                        Text("Current: Settings")
                    }
                }
                BottomNavBar(navController = navController)
            }
        }

        // Verify initial screen
        composeTestRule.onNodeWithText("Current: Home").assertIsDisplayed()

        // Tap Settings tab in bottom nav
        composeTestRule.onNodeWithText(Constants.SETTINGS_SCREEN).performClick()

        // Verify destination screen changed
        composeTestRule.onNodeWithText("Current: Settings").assertIsDisplayed()
    }
}
