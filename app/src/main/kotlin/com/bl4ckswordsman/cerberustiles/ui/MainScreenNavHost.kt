package com.bl4ckswordsman.cerberustiles.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.predictiveBackEnterTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.predictiveBackExitTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.settingsExitTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.settingsPopEnterTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.subScreenEnterTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.subScreenExitTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.subScreenPopEnterTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.subScreenPopExitTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.tabEnterTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.tabExitTransition
import com.bl4ckswordsman.cerberustiles.navbar.Screen
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

/**
 * Parameters for the main screen nav host.
 */
data class MainScreenNavHostParams(
    val navController: NavHostController,
    val innerPadding: PaddingValues,
    val canWriteState: Boolean,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean,
    val isChargingOptimizationOn: Boolean,
    val isChargingOptimizationSupported: Boolean,
    val setChargingOptimization: (Boolean) -> Unit,
    val toggleChargingOptimization: (Boolean) -> Unit,
    val currentRingerMode: RingerMode,
    val onRingerModeChange: (RingerMode) -> Unit,
    val isSmartPixelsOn: Boolean = false,
    val toggleSmartPixels: ((Boolean) -> Unit)? = null
)

/**
 * Converts [MainScreenNavHostParams] to [HomeScreenParams] for the Home destination.
 */
fun MainScreenNavHostParams.toHomeScreenParams(): HomeScreenParams =
    HomeScreenParams(
        innerPadding = innerPadding,
        canWriteState = canWriteState,
        isSwitchedOn = isSwitchedOn,
        setSwitchedOn = setSwitchedOn,
        toggleAdaptiveBrightness = toggleAdaptiveBrightness,
        openPermissionSettings = openPermissionSettings,
        isVibrationModeOn = isVibrationModeOn,
        setVibrationMode = setVibrationMode,
        toggleVibrationMode = toggleVibrationMode,
        isChargingOptimizationOn = isChargingOptimizationOn,
        setChargingOptimization = setChargingOptimization,
        toggleChargingOptimization = toggleChargingOptimization,
        currentRingerMode = currentRingerMode,
        onRingerModeChange = onRingerModeChange,
        isSmartPixelsOn = isSmartPixelsOn,
        toggleSmartPixels = toggleSmartPixels,
        navController = navController
    )

/**
 * The main screen navigation host that handles declarative route definitions and transitions.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreenNavHost(params: MainScreenNavHostParams) {
    NavHost(
        navController = params.navController,
        startDestination = Screen.Home.route,
        predictivePopEnterTransition = { predictiveBackEnterTransition() },
        predictivePopExitTransition = { predictiveBackExitTransition() }
    ) {
        composable(
            Screen.Home.route,
            enterTransition = { tabEnterTransition() },
            exitTransition = { tabExitTransition() },
            popEnterTransition = { tabEnterTransition() },
            popExitTransition = { tabExitTransition() }
        ) {
            HomeScreen(params = params.toHomeScreenParams())
        }

        composable(
            Screen.Settings.route,
            enterTransition = { tabEnterTransition() },
            exitTransition = { settingsExitTransition() },
            popEnterTransition = { settingsPopEnterTransition() },
            popExitTransition = { tabExitTransition() }
        ) {
            val sharedParams = createSharedParams(params.navController)
            val settingsScreenParams = SettingsScreenParams(
                paddingValues = params.innerPadding,
                sharedParams = sharedParams
            )
            SettingsScreen(settingsScreenParams)
        }

        composable(
            Screen.Licenses.route,
            enterTransition = { subScreenEnterTransition() },
            exitTransition = { subScreenExitTransition() },
            popEnterTransition = { subScreenPopEnterTransition() },
            popExitTransition = { subScreenPopExitTransition() }
        ) {
            val libraries by produceLibraries(R.raw.aboutlibraries)
            Column(
                modifier = Modifier.padding(params.innerPadding)
            ) {
                LibrariesContainer(
                    libraries = libraries,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composable(
            Screen.Permissions.route,
            enterTransition = { subScreenEnterTransition() },
            exitTransition = { subScreenExitTransition() },
            popEnterTransition = { subScreenPopEnterTransition() },
            popExitTransition = { subScreenPopExitTransition() }
        ) {
            PermissionsScreen(paddingValues = params.innerPadding)
        }
    }
}
