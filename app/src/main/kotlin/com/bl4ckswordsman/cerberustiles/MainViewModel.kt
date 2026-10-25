package com.bl4ckswordsman.cerberustiles

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils.canWriteSettings
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.util.Ringer

/**
 * Shared ViewModel holding settings state for Cerberus Tiles activities.
 *
 * All mutable settings state is managed here, refreshed on lifecycle resume,
 * and consumed reactively by the Compose UI layer.
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
    val currentRingerMode = MutableLiveData(RingerMode.NORMAL)

    /** Whether the Smart Pixels AccessibilityService is enabled. */
    val isAccessibilityEnabled = MutableLiveData<Boolean>()

    /** Whether Smart Pixels is currently enabled. */
    val isSmartPixelsOn = mutableStateOf(false)

    /** Current intensity for Smart Pixels (10% to 90%, default 50%). */
    val smartPixelsIntensity = mutableStateOf(SettingsUtils.SmartPixels.DEFAULT_INTENSITY)

    /** Whether Smart Pixels should pause during screen recording / casting. */
    val isSmartPixelsDisableOnCast = mutableStateOf(true)

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
        isChargingOptimizationOn.value =
            isChargingOptimizationSupported.value && SettingsUtils.Charging.isChargingOptimizationEnabled(
                context
            )
    }

    /**
     * Reads the current ringer mode from the device and updates [currentRingerMode].
     */
    fun updateCurrentRingerMode(context: Context) {
        currentRingerMode.value = Ringer.getCurrentRingerMode(context)
    }

    /**
     * Updates the state of the Smart Pixels settings and accessibility status.
     */
    fun updateSmartPixelsState(context: Context) {
        isAccessibilityEnabled.value =
            SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context)
        isSmartPixelsOn.value = SettingsUtils.SmartPixels.isSmartPixelsEnabled(context)
        smartPixelsIntensity.value = SettingsUtils.SmartPixels.getSmartPixelsIntensity(context)
        isSmartPixelsDisableOnCast.value = SettingsUtils.SmartPixels.isDisableOnCastEnabled(context)
    }
}
