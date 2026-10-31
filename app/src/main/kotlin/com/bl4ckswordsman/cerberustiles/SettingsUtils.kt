package com.bl4ckswordsman.cerberustiles

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.core.net.toUri
import com.bl4ckswordsman.cerberustiles.SettingsUtils.Charging.CHARGE_OPTIMIZATION_MODE
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import com.bl4ckswordsman.cerberustiles.smartpixels.SmartPixelsAccessibilityService
import com.bl4ckswordsman.cerberustiles.util.NetworkSpeedLimitRepository
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
         * Sets the adaptive brightness mode directly.
         *
         * @return True if the setting was successfully written, false otherwise.
         */
        fun setAdaptiveBrightness(context: Context, enabled: Boolean): Boolean {
            if (Settings.System.canWrite(context)) {
                val success = Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    if (enabled) 1 else 0
                )
                if (success) {
                    showToast(context, "Adaptive brightness", enabled)
                }
                return success
            }
            return false
        }

        /**
         * Toggles the adaptive brightness setting.
         */
        fun toggleAdaptiveBrightness(params: SettingsToggleParams) {
            if (Settings.System.canWrite(params.context)) {
                val isAdaptive = isAdaptiveBrightnessEnabled(params.context)
                val success = setAdaptiveBrightness(params.context, !isAdaptive)
                if (success) {
                    params.onSettingChanged(!isAdaptive)
                }
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
         *
         * @return True if the brightness was successfully written, false otherwise (e.g., missing WRITE_SETTINGS permission).
         */
        fun setScreenBrightness(context: Context, brightness: Float): Boolean {
            if (Settings.System.canWrite(context)) {
                val brightnessValue = (255.0.pow(brightness.toDouble())).toInt()
                return Settings.System.putInt(
                    context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue
                )
            }
            return false
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
         * Checks if charging optimization (80% limit) is supported on this device.
         *
         * Returns false if the [CHARGE_OPTIMIZATION_MODE] secure setting does not exist
         * (default value of -1 is returned) or if reading it throws any exception.
         */
        fun isChargingOptimizationSupported(context: Context): Boolean {
            return try {
                Settings.Secure.getInt(
                    context.contentResolver, CHARGE_OPTIMIZATION_MODE, -1
                ) != -1
            } catch (_: Exception) {
                false
            }
        }

        /**
         * Checks if charging optimization (80% limit) is enabled.
         *
         * Returns false if the setting is 0, absent, or if reading it throws any exception.
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
     * Utilities for Network Download Rate Limit (Developer Options).
     */
    object NetworkSpeed {
        const val INGRESS_RATE_LIMIT_BYTES_PER_SECOND = "ingress_rate_limit_bytes_per_second"

        /**
         * Checks whether the network speed limitation feature is supported on this device.
         * The feature was introduced in Android 13 (API level 33).
         */
        fun isSupported(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
        }

        /**
         * Checks if the app has permission to modify secure settings.
         */
        fun canWriteSecureSettings(context: Context): Boolean {
            return context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
                    PackageManager.PERMISSION_GRANTED
        }

        /**
         * Gets the raw rate limit in bytes per second from [Settings.Global].
         * Returns -1L if absent or if reading throws an exception.
         */
        fun getRateLimitBytes(context: Context): Long {
            return try {
                Settings.Global.getLong(
                    context.contentResolver,
                    INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                    -1L
                )
            } catch (_: Exception) {
                -1L
            }
        }

        /**
         * Gets the current [NetworkSpeedLimit] configured on the device.
         */
        fun getCurrentSpeedLimit(context: Context): NetworkSpeedLimit {
            val bytes = getRateLimitBytes(context)
            val available = NetworkSpeedLimitRepository.getAvailableLimits(context)
            return NetworkSpeedLimit.fromBytesPerSecond(bytes, available)
        }

        /**
         * Sets the network download speed limit.
         *
         * @return True if successfully written, false otherwise.
         */
        fun setSpeedLimit(
            context: Context,
            limit: NetworkSpeedLimit,
            onPermissionDenied: (() -> Unit)? = null
        ): Boolean {
            return try {
                val success = Settings.Global.putLong(
                    context.contentResolver,
                    INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                    limit.bytesPerSecond
                )
                if (success) {
                    val message = if (limit.isUnlimited) {
                        "Network speed limit disabled"
                    } else {
                        "Network speed limit: ${limit.label}"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to change network speed limit. It may be restricted.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                success
            } catch (_: SecurityException) {
                onPermissionDenied?.invoke()
                false
            } catch (_: Exception) {
                false
            }
        }

        /**
         * Cycles to the next available speed limit in the options list.
         * e.g., No limit -> 128 kbps -> 256 kbps -> 1 Mbps -> 5 Mbps -> 15 Mbps -> No limit.
         */
        fun cycleNextSpeedLimit(
            context: Context,
            onPermissionDenied: (() -> Unit)? = null
        ): NetworkSpeedLimit? {
            val available = NetworkSpeedLimitRepository.getAvailableLimits(context)
            if (available.isEmpty()) return null

            val current = getCurrentSpeedLimit(context)
            val currentIndex = available.indexOfFirst {
                if (current.isUnlimited) it.isUnlimited else it.bytesPerSecond == current.bytesPerSecond
            }

            val nextIndex =
                if (currentIndex == -1 || currentIndex >= available.lastIndex) 0 else currentIndex + 1
            val nextLimit = available[nextIndex]

            val success = setSpeedLimit(context, nextLimit, onPermissionDenied)
            return if (success) nextLimit else null
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

    /**
     * Utilities for Smart Pixels settings and accessibility overlay management.
     */
    object SmartPixels {
        const val PREFS_NAME = "settings"
        const val KEY_SMART_PIXELS_ENABLED = "smart_pixels_enabled"
        const val KEY_SMART_PIXELS_INTENSITY = "smart_pixels_intensity"
        const val KEY_SMART_PIXELS_DISABLE_ON_CAST = "smart_pixels_disable_on_cast"
        const val DEFAULT_INTENSITY = 50f

        /**
         * Checks whether the Smart Pixels AccessibilityService is enabled in Android settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            if (SmartPixelsAccessibilityService.isRunning()) return true
            val am =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabledServices =
                am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            if (enabledServices?.any {
                    it.resolveInfo?.serviceInfo?.packageName == context.packageName &&
                            it.resolveInfo?.serviceInfo?.name == SmartPixelsAccessibilityService::class.java.name
                } == true) {
                return true
            }
            val enabledServicesSetting = try {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
            } catch (_: Exception) {
                null
            } ?: return false

            val expectedFull =
                "${context.packageName}/${SmartPixelsAccessibilityService::class.java.name}"
            val expectedShort =
                "${context.packageName}/.${SmartPixelsAccessibilityService::class.java.simpleName}"
            return enabledServicesSetting.split(':').any {
                it.equals(expectedFull, ignoreCase = true) || it.equals(
                    expectedShort,
                    ignoreCase = true
                )
            }
        }

        /**
         * Opens the system accessibility settings screen.
         */
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        /**
         * Checks whether Smart Pixels is currently enabled in settings.
         */
        fun isSmartPixelsEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SMART_PIXELS_ENABLED, false)
        }

        /**
         * Sets whether Smart Pixels is enabled, persists the setting, and updates the service overlay.
         */
        fun setSmartPixelsEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SMART_PIXELS_ENABLED, enabled).apply()
            SmartPixelsAccessibilityService.updateState(context)
        }

        /**
         * Gets the configured Smart Pixels intensity (percentage 10f..90f, default 50f).
         */
        fun getSmartPixelsIntensity(context: Context): Float {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getFloat(KEY_SMART_PIXELS_INTENSITY, DEFAULT_INTENSITY)
        }

        /**
         * Sets the configured Smart Pixels intensity and notifies the service.
         */
        fun setSmartPixelsIntensity(context: Context, intensity: Float) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putFloat(KEY_SMART_PIXELS_INTENSITY, intensity).apply()
            SmartPixelsAccessibilityService.updateIntensity(context, intensity)
        }

        /**
         * Gets whether Smart Pixels automatically pauses during screen recording or casting.
         */
        fun isDisableOnCastEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SMART_PIXELS_DISABLE_ON_CAST, true)
        }

        /**
         * Sets whether Smart Pixels automatically pauses during screen recording or casting.
         */
        fun setDisableOnCastEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SMART_PIXELS_DISABLE_ON_CAST, enabled).apply()
            SmartPixelsAccessibilityService.updateState(context)
        }
    }
}

