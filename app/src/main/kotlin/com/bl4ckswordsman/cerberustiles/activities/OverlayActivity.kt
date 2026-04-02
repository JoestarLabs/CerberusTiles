package com.bl4ckswordsman.cerberustiles.activities

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.SettingsUtils.Brightness
import com.bl4ckswordsman.cerberustiles.SettingsUtils.Vibration.toggleVibrationMode
import com.bl4ckswordsman.cerberustiles.SettingsUtils.openPermissionSettings
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialog
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialogParams
import com.bl4ckswordsman.cerberustiles.ui.createSharedParams

/**
 * A [ComponentActivity] that shows an overlay dialog with settings components.
 *
 * State is owned by [MainViewModel], following the same pattern as [MainActivity].
 * The ringer mode is now stored in [MainViewModel.currentRingerMode] instead of a
 * local [androidx.compose.runtime.MutableState] field.
 */
class OverlayActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    /**
     * Refreshes the ViewModel state from current device settings each time the overlay resumes.
     */
    override fun onResume() {
        super.onResume()
        updateViewModelState()
    }

    /**
     * Reads all relevant settings values from the device and updates the corresponding
     * ViewModel fields.
     */
    private fun updateViewModelState() {
        viewModel.updateCanWrite(this)
        viewModel.updateIsAdaptiveBrightnessOn(this)
        viewModel.updateIsVibrationModeOn(this)
        viewModel.updateIsChargingOptimizationOn(this)
        viewModel.updateCurrentRingerMode(this)
    }

    /**
     * Initialises the overlay activity with a transparent window background and sets up the
     * Compose content tree with [OverlayDialog], wiring all settings toggles to the ViewModel.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setContent {
            val showOverlayDialog = rememberSaveable { mutableStateOf(true) }
            val currentRingerMode by viewModel.currentRingerMode.observeAsState(RingerMode.NORMAL)

            val params = OverlayDialogParams(
                showDialog = showOverlayDialog,
                onDismiss = { finish() },
                canWrite = viewModel.canWrite,
                isSwitchedOn = viewModel.isAdaptiveBrightnessOn.value,
                setSwitchedOn = { viewModel.isAdaptiveBrightnessOn.value = it },
                toggleAdaptiveBrightness = {
                    val brightnessParams = SettingsUtils.SettingsToggleParams(
                        context = this,
                        onSettingChanged = { newValue ->
                            viewModel.isAdaptiveBrightnessOn.value = newValue
                        }
                    )
                    Brightness.toggleAdaptiveBrightness(brightnessParams)
                },
                openPermissionSettings = { openPermissionSettings(this) },
                isVibrationModeOn = viewModel.isVibrationModeOn.value,
                setVibrationMode = { viewModel.isVibrationModeOn.value = it },
                toggleVibrationMode = {
                    val vibrationParams = SettingsUtils.SettingsToggleParams(
                        context = this,
                        onSettingChanged = { newValue ->
                            viewModel.isVibrationModeOn.value = newValue
                            viewModel.currentRingerMode.value =
                                if (newValue) RingerMode.VIBRATE else RingerMode.NORMAL
                        }
                    )
                    toggleVibrationMode(vibrationParams)
                },
                isChargingOptimizationOn = viewModel.isChargingOptimizationOn.value,
                isChargingOptimizationSupported = viewModel.isChargingOptimizationSupported.value,
                setChargingOptimization = { viewModel.isChargingOptimizationOn.value = it },
                toggleChargingOptimization = { enabled ->
                    val chargingParams = SettingsUtils.SettingsToggleParams(
                        context = this,
                        onSettingChanged = { newValue ->
                            viewModel.isChargingOptimizationOn.value = newValue
                        },
                        onPermissionDenied = { viewModel.showAdbDialog.value = true }
                    )
                    SettingsUtils.Charging.setChargingOptimization(enabled, chargingParams)
                },
                showAdbDialog = viewModel.showAdbDialog.value,
                onAdbDialogDismiss = { viewModel.showAdbDialog.value = false },
                sharedParams = createSharedParams(),
                currentRingerMode = currentRingerMode,
                onRingerModeChange = { newMode ->
                    viewModel.currentRingerMode.value = newMode
                    viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                }
            )
            OverlayDialog(params)
        }
    }
}
