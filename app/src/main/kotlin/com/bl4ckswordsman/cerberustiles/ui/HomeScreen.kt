package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
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
    val isVibrationModeOn: Boolean = false,
    val setVibrationMode: (Boolean) -> Unit = {},
    val toggleVibrationMode: () -> Boolean = { false },
    val isChargingOptimizationOn: Boolean,
    val setChargingOptimization: (Boolean) -> Unit = {},
    val toggleChargingOptimization: (Boolean) -> Unit,
    val currentRingerMode: RingerMode,
    val onRingerModeChange: (RingerMode) -> Unit,
    val isSmartPixelsOn: Boolean = false,
    val toggleSmartPixels: ((Boolean) -> Unit)? = null,
    val currentNetworkSpeedLimit: NetworkSpeedLimit = NetworkSpeedLimit.NO_LIMIT,
    val onNetworkSpeedLimitChange: ((NetworkSpeedLimit) -> Unit)? = null,
    val isNetworkSpeedLimitSupported: Boolean = false,
    val onOpenAdbDialog: (() -> Unit)? = null
)

/**
 * The Home screen composable of Cerberus Tiles.
 * Renders user-configured quick setting tiles, sliders, and ringer mode controls.
 */
@Composable
fun HomeScreen(params: HomeScreenParams) {
    val context = LocalContext.current
    val sharedPreferences = remember(context) {
        context.getSharedPreferences(Constants.SETTINGS_PREFS_NAME, Context.MODE_PRIVATE)
    }
    val componentVisibility = rememberComponentVisibility(sharedPreferences)

    val settingsCompParams = SettingsComponentsParams(
        canWriteState = params.canWriteState,
        isSwitchedOn = params.isSwitchedOn,
        setSwitchedOn = params.setSwitchedOn,
        toggleAdaptiveBrightness = params.toggleAdaptiveBrightness,
        openPermissionSettings = params.openPermissionSettings,
        setVibrationMode = params.setVibrationMode,
        isChargingOptimizationOn = params.isChargingOptimizationOn,
        toggleChargingOptimization = params.toggleChargingOptimization,
        componentVisibilityParams = componentVisibility,
        currentRingerMode = params.currentRingerMode,
        onRingerModeChange = params.onRingerModeChange,
        isSmartPixelsOn = params.isSmartPixelsOn,
        toggleSmartPixels = params.toggleSmartPixels,
        currentNetworkSpeedLimit = params.currentNetworkSpeedLimit,
        onNetworkSpeedLimitChange = params.onNetworkSpeedLimitChange,
        isNetworkSpeedLimitSupported = params.isNetworkSpeedLimitSupported,
        onOpenAdbDialog = params.onOpenAdbDialog
    )

    SettingsComponents(
        params = settingsCompParams,
        modifier = Modifier
            .fillMaxWidth()
            .padding(params.innerPadding)
            .verticalScroll(rememberScrollState())
    )
}
