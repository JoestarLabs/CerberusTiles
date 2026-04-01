package com.bl4ckswordsman.cerberustiles

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils.canWriteSettings
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.util.Ringer

/**
 * The main ViewModel that holds the canonical state for all settings screens.
 *
 * Both [com.bl4ckswordsman.cerberustiles.activities.MainActivity] and
 * [com.bl4ckswordsman.cerberustiles.activities.OverlayActivity] use this ViewModel
 * via `by viewModels<MainViewModel>()` for structural consistency. Each activity
 * has its own instance; state is refreshed from device settings in onResume().
 *
 * ## State type rationale
 * Two fields use [MutableLiveData] and the rest use Compose [mutableStateOf].
 * This split is intentional:
 * - [canWrite] and [currentRingerMode] are [MutableLiveData] because
 *   [com.bl4ckswordsman.cerberustiles.ui.MainScreenParams] and
 *   [com.bl4ckswordsman.cerberustiles.ui.OverlayDialogParams] declare those
 *   parameters as `LiveData<T>`, so they must stay as LiveData to satisfy
 *   the shared UI contract.
 * - The remaining five fields are [mutableStateOf] because they are read as
 *   plain `.value` (Boolean) in OverlayDialogParams, which does not accept
 *   LiveData for those slots.
 */
class MainViewModel : ViewModel() {
    /** Whether the app has WRITE_SETTINGS permission. */
    val canWrite = MutableLiveData<Boolean>()

    /** Whether adaptive brightness is currently enabled. */
    val isAdaptiveBrightnessOn = mutableStateOf(false)

    /** Whether vibration mode is currently active. */
    val isVibrationModeOn = mutableStateOf(false)

    /** Whether charging optimization (80% limit) is currently enabled. */
    val isChargingOptimizationOn = mutableStateOf(false)

    /** Whether the device supports charging optimization. */
    val isChargingOptimizationSupported = mutableStateOf(false)

    /** Whether the ADB-instructions dialog should be shown. */
    val showAdbDialog = mutableStateOf(false)

    /** The current ringer mode of the device. */
    val currentRingerMode = MutableLiveData<RingerMode>(RingerMode.NORMAL)

    /**
     * Updates the state of the canWrite setting.
     */
    fun updateCanWrite(context: Context) {
        canWrite.value = canWriteSettings(context)
    }

    /**
     * Updates the state of the adaptive brightness setting.
     */
    fun updateIsAdaptiveBrightnessOn(context: Context) {
        isAdaptiveBrightnessOn.value = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(context)
    }

    /**
     * Updates the state of the vibration mode setting.
     */
    fun updateIsVibrationModeOn(context: Context) {
        isVibrationModeOn.value = SettingsUtils.Vibration.isVibrationModeEnabled(context)
    }

    /**
     * Updates the state of the charging optimization setting.
     */
    fun updateIsChargingOptimizationOn(context: Context) {
        isChargingOptimizationSupported.value =
            SettingsUtils.Charging.isChargingOptimizationSupported(context)
        if (isChargingOptimizationSupported.value) {
            isChargingOptimizationOn.value =
                SettingsUtils.Charging.isChargingOptimizationEnabled(context)
        }
    }

    /**
     * Reads the current ringer mode from the device and updates [currentRingerMode].
     */
    fun updateCurrentRingerMode(context: Context) {
        currentRingerMode.value = Ringer.getCurrentRingerMode(context)
    }
}
