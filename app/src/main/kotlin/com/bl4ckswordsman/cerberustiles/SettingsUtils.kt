package com.bl4ckswordsman.cerberustiles

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bl4ckswordsman.cerberustiles.util.AutomaticZenManager
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
                // Show a toast with the new state of adaptive brightness
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
                // Convert the brightness value to a 0-255 range
                val brightnessValue = (255.0.pow(brightness.toDouble())).toInt()
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
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
            fun toggle(): Boolean {
                if (!canWriteSettings(params.context)) {
                    openPermissionSettings(params.context)
                    return false
                }

                return performVibrationToggle()
            }

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
                } catch (e: SecurityException) {
                    handleVibrationSecurityException()
                    false
                }
            }

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
     * Utilities for silent mode settings using AutomaticZenManager.
     */
    object Silent {
        /**
         * Checks if the silent mode is enabled.
         */
        fun isSilentModeEnabled(context: Context): Boolean {
            // Check both audio manager and DND state for complete silent mode
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val isAudioSilent = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
            val isDndActive = AutomaticZenManager.isSilentModeActive(context)

            return isAudioSilent || isDndActive
        }

        /**
         * Toggles the silent mode using AutomaticZenManager for Android 15+ compatibility.
         */
        fun toggleSilentMode(params: SettingsToggleParams): Boolean {
            return SilentModeToggler(params).toggle()
        }

        /**
         * Handles the silent mode toggling logic.
         */
        private class SilentModeToggler(private val params: SettingsToggleParams) {
            fun toggle(): Boolean {
                if (!canWriteSettings(params.context)) {
                    openPermissionSettings(params.context)
                    return false
                }

                return try {
                    val isSilentModeOn = isSilentModeEnabled(params.context)
                    if (isSilentModeOn) {
                        deactivateSilentMode()
                    } else {
                        activateSilentMode()
                    }
                } catch (e: SecurityException) {
                    handleSilentModeSecurityException()
                    false
                }
            }

            private fun activateSilentMode(): Boolean {
                val success = AutomaticZenManager.activateSilentMode(params.context)
                if (success) {
                    val audioManager =
                        params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    showToast(params.context, "Silent mode", true)
                    params.onSettingChanged(true)
                }
                return success
            }

            private fun deactivateSilentMode(): Boolean {
                val success = AutomaticZenManager.deactivateSilentMode(params.context)
                if (success) {
                    val audioManager =
                        params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    showToast(params.context, "Silent mode", false)
                    params.onSettingChanged(false)
                }
                return success
            }

            private fun handleSilentModeSecurityException() {
                Toast.makeText(
                    params.context,
                    "Cannot change silent mode settings. Please check permissions.",
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
         * A guarded read is attempted to verify if the setting is recognized.
         */
        fun isChargingOptimizationSupported(context: Context): Boolean {
            return try {
                // If it doesn't throw a Settings.SettingNotFoundException (or we can at least read a default),
                // we assume it is supported on this device.
                Settings.Secure.getInt(context.contentResolver, CHARGE_OPTIMIZATION_MODE, -1)
                true
            } catch (e: Exception) {
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
            } catch (e: Exception) {
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
            } catch (e: SecurityException) {
                params.onPermissionDenied?.invoke()
            }
        }
    }

    /**
     * Opens the screen to allow the user to write system settings.
     */
    fun openPermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
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

    /**
     * The main view model that holds the state of the settings.
     */
    class MainViewModel : ViewModel() {
        val canWrite = MutableLiveData<Boolean>()
        val isSwitchedOn = mutableStateOf(false)
        val isVibrationModeOn = mutableStateOf(false)
        val isChargingOptimizationOn = mutableStateOf(false)
        val isChargingOptimizationSupported = mutableStateOf(false)
        val showAdbDialog = mutableStateOf(false)

        /**
         * Updates the state of the canWrite setting.
         */
        fun updateCanWrite(context: Context) {
            canWrite.value = canWriteSettings(context)
        }

        /**
         * Updates the state of the adaptive brightness setting.
         */
        fun updateIsSwitchedOn(context: Context) {
            isSwitchedOn.value = Brightness.isAdaptiveBrightnessEnabled(context)
        }

        /**
         * Updates the state of the vibration mode setting.
         */
        fun updateIsVibrationModeOn(context: Context) {
            isVibrationModeOn.value = Vibration.isVibrationModeEnabled(context)
        }

        /**
         * Updates the state of the charging optimization setting.
         */
        fun updateIsChargingOptimizationOn(context: Context) {
            isChargingOptimizationSupported.value = Charging.isChargingOptimizationSupported(context)
            if (isChargingOptimizationSupported.value) {
                isChargingOptimizationOn.value = Charging.isChargingOptimizationEnabled(context)
            }
        }
    }
}


// TODO: Add other settings utilities here
