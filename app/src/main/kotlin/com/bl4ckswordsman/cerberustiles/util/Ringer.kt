package com.bl4ckswordsman.cerberustiles.util

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.RingerMode

/**
 * Utility object for managing device ringer mode settings.
 * Handles mode changes with proper permission checking and DND integration.
 */
object Ringer {

    /**
     * Gets the current ringer mode from the device's audio manager.
     *
     * @param context The application context
     * @return The current [RingerMode] (NORMAL, SILENT, or VIBRATE)
     */
    fun getCurrentRingerMode(context: Context): RingerMode {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> RingerMode.NORMAL
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            else -> RingerMode.NORMAL
        }
    }

    /**
     * Sets the device ringer mode with proper permission checking and DND integration.
     *
     * @param params The settings toggle parameters containing context and callbacks
     * @param newMode The desired [RingerMode] to set
     */
    fun setRingerMode(params: SettingsUtils.SettingsToggleParams, newMode: RingerMode) {
        if (!SettingsUtils.canWriteSettings(params.context)) {
            SettingsUtils.openPermissionSettings(params.context)
            return
        }

        val currentMode = getCurrentRingerMode(params.context)
        if (currentMode == newMode) return

        val modeChangeResult = RingerModeChanger(params, newMode).execute()
        if (modeChangeResult.success) {
            params.onSettingChanged(true)
        }
    }

    /**
     * Handles the process of changing ringer mode with proper error handling.
     */
    private class RingerModeChanger(
        private val params: SettingsUtils.SettingsToggleParams,
        private val newMode: RingerMode
    ) {
        data class ChangeResult(val success: Boolean, val message: String? = null)

        /**
         * Executes the ringer mode change, applying the new mode and verifying the result.
         * Returns a [ChangeResult] indicating success or failure.
         */
        fun execute(): ChangeResult {
            return try {
                applyRingerModeChange()
                verifyAndNotifyModeChange()
            } catch (e: SecurityException) {
                handleSecurityException(e)
                ChangeResult(false, e.message)
            }
        }

        /**
         * Dispatches to the appropriate mode-specific change method based on [newMode].
         */
        private fun applyRingerModeChange() {
            val audioManager =
                params.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            when (newMode) {
                RingerMode.SILENT -> applySilentMode(audioManager)
                else -> applyNonSilentMode(audioManager)
            }
        }

        /**
         * Activates silent mode via [AutomaticZenManager] and sets the audio manager ringer
         * mode to silent if the DND activation succeeds.
         */
        private fun applySilentMode(audioManager: AudioManager) {
            val success = AutomaticZenManager.activateSilentMode(params.context)
            if (success) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
        }

        /**
         * Deactivates any active silent mode via [AutomaticZenManager] if needed, then sets
         * the audio manager to the requested non-silent [newMode].
         */
        private fun applyNonSilentMode(audioManager: AudioManager) {
            if (AutomaticZenManager.isSilentModeActive(params.context)) {
                AutomaticZenManager.deactivateSilentMode(params.context)
            }

            val systemMode = when (newMode) {
                RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
                RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
            }
            audioManager.ringerMode = systemMode
        }

        /**
         * Reads back the current ringer mode after the change and shows a confirmation toast
         * if the mode matches [newMode]. Returns a [ChangeResult] reflecting the verification outcome.
         */
        private fun verifyAndNotifyModeChange(): ChangeResult {
            val updatedMode = getCurrentRingerMode(params.context)

            return if (updatedMode == newMode) {
                val modeName = getModeDisplayName(newMode)
                SettingsUtils.showToast(params.context, modeName, true)
                ChangeResult(true)
            } else {
                ChangeResult(false, "Mode change verification failed")
            }
        }

        /**
         * Handles a [SecurityException] thrown during mode change by showing an appropriate
         * error message or redirecting to DND permission settings for silent mode.
         */
        private fun handleSecurityException(e: SecurityException) {
            when (newMode) {
                RingerMode.SILENT -> showSilentModeError()
                else -> showGeneralError(e.message)
            }
        }

        /**
         * Shows a long toast prompting the user to grant Do Not Disturb permission and
         * redirects them to the notification policy access settings.
         */
        private fun showSilentModeError() {
            Toast.makeText(
                params.context,
                "Cannot set silent mode. Please grant Do Not Disturb permission in settings.",
                Toast.LENGTH_LONG
            ).show()
            SettingsUtils.openDndPermissionSettings(params.context)
        }

        /**
         * Shows a long toast with a generic error message when ringer mode change fails
         * for reasons other than silent mode DND permission.
         *
         * @param message The error detail from the [SecurityException], or null.
         */
        private fun showGeneralError(message: String?) {
            Toast.makeText(
                params.context,
                "Cannot change ringer mode: $message",
                Toast.LENGTH_LONG
            ).show()
        }

        /**
         * Returns a human-readable display name for the given [RingerMode].
         *
         * @param mode The ringer mode to get the display name for.
         * @return A user-facing string such as "Sound mode", "Silent mode", or "Vibrate mode".
         */
        private fun getModeDisplayName(mode: RingerMode): String {
            return when (mode) {
                RingerMode.NORMAL -> "Sound mode"
                RingerMode.SILENT -> "Silent mode"
                RingerMode.VIBRATE -> "Vibrate mode"
            }
        }
    }
}
