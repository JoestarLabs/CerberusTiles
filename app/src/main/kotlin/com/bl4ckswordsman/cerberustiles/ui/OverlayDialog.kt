package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
    val toggleSmartPixels: ((Boolean) -> Unit)? = null
)

/**
 * A dialog that shows settings components.
 */
@Composable
fun OverlayDialog(params: OverlayDialogParams) {
    val adaptBrightnessSwitch =
        params.sharedParams.sharedPreferences.getBoolean("adaptBrightnessSwitch", true)
    val brightnessSlider =
        params.sharedParams.sharedPreferences.getBoolean("brightnessSlider", true)
    val ringerModeSelector =
        params.sharedParams.sharedPreferences.getBoolean("ringerModeSelector", true)
    val chargingOptimizationSwitch =
        params.sharedParams.sharedPreferences.getBoolean("chargingOptimizationSwitch", true)
    val smartPixelsSwitch =
        params.sharedParams.sharedPreferences.getBoolean("smartPixelsSwitch", true)

    if (params.showDialog.value) {
        CerberusTilesTheme {
            AlertDialog(
                onDismissRequest = {
                    params.showDialog.value = false
                    params.onDismiss()
                },
                text = {
                    Column {
                        val componentVisibilityDialogParams = ComponentVisibilityDialogParams(
                            adaptBrightnessSwitch = rememberSaveable {
                                mutableStateOf(adaptBrightnessSwitch)
                            },
                            brightnessSlider = rememberSaveable {
                                mutableStateOf(brightnessSlider)
                            },
                            ringerModeSelector = rememberSaveable {
                                mutableStateOf(ringerModeSelector)
                            },
                            chargingOptimizationSwitch = rememberSaveable {
                                mutableStateOf(chargingOptimizationSwitch)
                            },
                            smartPixelsSwitch = rememberSaveable {
                                mutableStateOf(smartPixelsSwitch)
                            }
                        )
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
                            componentVisibilityParams = componentVisibilityDialogParams,
                            sharedParams = params.sharedParams,
                            currentRingerMode = params.currentRingerMode,
                            onRingerModeChange = params.onRingerModeChange,
                            isOverlayContext = true,
                            isSmartPixelsOn = params.isSmartPixelsOn,
                            toggleSmartPixels = params.toggleSmartPixels
                        )
                        SettingsComponents(settingsComponentsParams)
                    }
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
