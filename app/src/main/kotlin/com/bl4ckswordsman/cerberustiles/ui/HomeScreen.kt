package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.bl4ckswordsman.cerberustiles.models.RingerMode

/**
 * Encapsulated parameter state for the [HomeScreen].
 */
data class HomeScreenParams(
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
    val setChargingOptimization: (Boolean) -> Unit,
    val toggleChargingOptimization: (Boolean) -> Unit,
    val currentRingerMode: RingerMode,
    val onRingerModeChange: (RingerMode) -> Unit,
    val isSmartPixelsOn: Boolean = false,
    val toggleSmartPixels: ((Boolean) -> Unit)? = null,
    val navController: NavController? = null
)

/**
 * The Home screen composable of Cerberus Tiles.
 * Renders user-configured quick setting tiles, sliders, and ringer mode controls.
 */
@Composable
fun HomeScreen(params: HomeScreenParams) {
    Column(
        modifier = Modifier.padding(params.innerPadding)
    ) {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val componentVisibility = rememberComponentVisibility(sharedPreferences)

        val settingsCompParams = SettingsComponentsParams(
            canWriteState = params.canWriteState,
            isSwitchedOn = params.isSwitchedOn,
            setSwitchedOn = params.setSwitchedOn,
            toggleAdaptiveBrightness = params.toggleAdaptiveBrightness,
            openPermissionSettings = params.openPermissionSettings,
            isVibrationModeOn = params.isVibrationModeOn,
            setVibrationMode = params.setVibrationMode,
            toggleVibrationMode = params.toggleVibrationMode,
            isChargingOptimizationOn = params.isChargingOptimizationOn,
            setChargingOptimization = params.setChargingOptimization,
            toggleChargingOptimization = params.toggleChargingOptimization,
            sharedParams = createSharedParams(params.navController),
            componentVisibilityParams = componentVisibility,
            currentRingerMode = params.currentRingerMode,
            onRingerModeChange = params.onRingerModeChange,
            isSmartPixelsOn = params.isSmartPixelsOn,
            toggleSmartPixels = params.toggleSmartPixels
        )

        SettingsComponents(settingsCompParams)
    }
}
