package com.bl4ckswordsman.cerberustiles

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils.canWriteSettings
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.util.Ringer
import kotlin.math.pow

/** Utilities for different settings. */
object SettingsUtils {
    /**
     * Parameters for toggling settings.
     */
    data class SettingsToggleParams(
        val context: Context,
        val onSettingChanged: (Boolean) -> Unit,
        val onPermissionDenied: (() -> Unit)? = null
    )

    /**
     * Checks if the app can write settings.
     */
    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    /**
     * Checks if the app can access notification policy (DND settings).
     */
    fun canAccessNotificationPolicy(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Shows a toast with the given message.
     */
    fun showToast(context: Context, setting: String, isEnabled: Boolean) {
        val state = if (isEnabled) "enabled" else "disabled"
        Toast.makeText(context, "$setting $state", Toast.LENGTH_SHORT).show()
    }

    /**
     * Utilities for brightness settings.
     */
    object Brightness {
        /**
         * Checks if the adaptive brightness is enabled.
         */
        fun isAdaptiveBrightnessEnabled(context: Context): Boolean {
            return Settings.System.getInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE
            ) == 1
        }

        /**
         * Toggles the adaptive brightness setting.
         */
        fun toggleAdaptiveBrightness(params: SettingsToggleParams) {
            if (Settings.System.canWrite(params.context)) {
                val isAdaptive = isAdaptiveBrightnessEnabled(params.context)
                Settings.System.putInt(
                    params.context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    if (isAdaptive) 0 else 1
                )
                showToast(params.context, "Adaptive brightness", !isAdaptive)
                params.onSettingChanged(!isAdaptive)
            }
        }

        /**
         * Gets the screen brightness.
         */
        fun getScreenBrightness(context: Context): Int {
            return Settings.System.getInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS
            )
        }

        /**
         * Sets the screen brightness.
         */
        fun setScreenBrightness(context: Context, brightness: Float) {
            if (Settings.System.canWrite(context)) {
                val brightnessValue = (255.0.pow(brightness.toDouble())).toInt()
                Settings.System.putInt(
                    context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue
                )
            }
        }
    }

    /**
     * Utilities for vibration settings.
     */
    object Vibration {
        /**
         * Checks if the vibration mode is enabled.
         */
        fun isVibrationModeEnabled(context: Context): Boolean {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
        }

        /**
         * Toggles the vibration mode.
         */
        fun toggleVibrationMode(params: SettingsToggleParams): Boolean {
            return VibrationModeToggler(params).toggle()
        }

        /**
         * Handles the vibration mode toggling logic.
         */
        private class VibrationModeToggler(private val params: SettingsToggleParams) {
            /**
             * Checks for the required WRITE_SETTINGS permission and, if present, delegates to
             * [performVibrationToggle]. Redirects to permission settings and returns false
             * if the permission is missing.
             *
             * @return true if the vibration mode was successfully toggled, false otherwise.
             */
            fun toggle(): Boolean {
                if (!canWriteSettings(params.context)) {
                    openPermissionSettings(params.context)
                    return false
                }
                return performVibrationToggle()
            }

            /**
             * Reads the current vibration mode state and switches it, shows a confirmation toast,
             * and invokes [SettingsToggleParams.onSettingChanged] with the new value.
             * Returns true on success, false if a [SecurityException] is caught.
             */
            private fun performVibrationToggle(): Boolean {
                val audioManager =
                    params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                return try {
                    val isVibrationModeOn =
                        audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
                    val newMode =
                        if (isVibrationModeOn) AudioManager.RINGER_MODE_NORMAL else AudioManager.RINGER_MODE_VIBRATE
                    audioManager.ringerMode = newMode
                    showToast(params.context, "Vibration mode", !isVibrationModeOn)
                    params.onSettingChanged(!isVibrationModeOn)
                    true
                } catch (_: SecurityException) {
                    handleVibrationSecurityException()
                    false
                }
            }

            /**
             * Shows a toast informing the user that vibration settings cannot be changed
             * while Do Not Disturb mode is active.
             */
            private fun handleVibrationSecurityException() {
                Toast.makeText(
                    params.context,
                    "Cannot change vibration settings in Do Not Disturb mode",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Utilities for charging settings.
     */
    object Charging {
        private const val CHARGE_OPTIMIZATION_MODE = "charge_optimization_mode"

        /**
         * Checks if charging optimization (80% limit) is supported.
         */
        fun isChargingOptimizationSupported(context: Context): Boolean {
            return try {
                Settings.Secure.getInt(context.contentResolver, CHARGE_OPTIMIZATION_MODE, -1) != -1
            } catch (_: Exception) {
                false
            }
        }

        /**
         * Checks if charging optimization (80% limit) is enabled.
         */
        fun isChargingOptimizationEnabled(context: Context): Boolean {
            return try {
                Settings.Secure.getInt(
                    context.contentResolver, CHARGE_OPTIMIZATION_MODE, 0
                ) == 1
            } catch (_: Exception) {
                false
            }
        }

        /**
         * Sets the charging optimization (80% limit).
         */
        fun setChargingOptimization(enabled: Boolean, params: SettingsToggleParams) {
            val newState = if (enabled) 1 else 0
            try {
                val success = Settings.Secure.putInt(
                    params.context.contentResolver, CHARGE_OPTIMIZATION_MODE, newState
                )
                if (success) {
                    showToast(params.context, "Charging optimization", enabled)
                    params.onSettingChanged(enabled)
                } else {
                    Toast.makeText(
                        params.context,
                        "Failed to change charging optimization setting. It may be restricted.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: SecurityException) {
                params.onPermissionDenied?.invoke()
            }
        }
    }

    /**
     * Opens the screen to allow the user to write system settings.
     */
    fun openPermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Opens the screen to allow the user to grant DND permission.
     */
    fun openDndPermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

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


// TODO: Add other settings utilities here
