package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme

/**
 * Parameters for the overlay dialog.
 */
data class OverlayDialogParams(
    val showDialog: MutableState<Boolean>,
    val onDismiss: () -> Unit,
    val canWrite: Boolean,
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
    val sharedParams: SharedParams,
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
 * A dialog that shows settings components.
 */
@Composable
fun OverlayDialog(params: OverlayDialogParams) {
    val componentVisibility = rememberComponentVisibility(params.sharedParams.sharedPreferences)

    if (params.showDialog.value) {
        CerberusTilesTheme {
            AlertDialog(
                onDismissRequest = {
                    params.showDialog.value = false
                    params.onDismiss()
                },
                text = {
                    val settingsComponentsParams = SettingsComponentsParams(
                        canWriteState = params.canWrite,
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
                        componentVisibilityParams = componentVisibility,
                        sharedParams = params.sharedParams,
                        currentRingerMode = params.currentRingerMode,
                        onRingerModeChange = params.onRingerModeChange,
                        isOverlayContext = true,
                        isSmartPixelsOn = params.isSmartPixelsOn,
                        toggleSmartPixels = params.toggleSmartPixels,
                        currentNetworkSpeedLimit = params.currentNetworkSpeedLimit,
                        onNetworkSpeedLimitChange = params.onNetworkSpeedLimitChange,
                        isNetworkSpeedLimitSupported = params.isNetworkSpeedLimitSupported,
                        onOpenAdbDialog = params.onOpenAdbDialog
                    )
                    SettingsComponents(
                        params = settingsComponentsParams,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        params.showDialog.value = false
                        params.onDismiss()
                    }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
