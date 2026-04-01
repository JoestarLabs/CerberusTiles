package com.bl4ckswordsman.cerberustiles.util

import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * Manages Do Not Disturb functionality using direct interruption filter approach.
 * This approach avoids ConditionProvider requirements and works on all supported versions.
 */
object AutomaticZenManager {

    /**
     * Checks if we can manage DND rules (requires notification policy access).
     */
    fun canManageDndRules(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Activates silent mode using direct interruption filter for better compatibility.
     * AutomaticZenRule approach has limitations with manual control.
     */
    fun activateSilentMode(context: Context): Boolean {
        return SilentModeController(context).activate()
    }

    /**
     * Deactivates silent mode by restoring normal interruption filter.
     */
    fun deactivateSilentMode(context: Context): Boolean {
        return SilentModeController(context).deactivate()
    }

    /**
     * Checks if silent mode is currently active via interruption filter.
     */
    fun isSilentModeActive(context: Context): Boolean {
        return SilentModeController(context).isActive()
    }

    /**
     * Controller class to handle silent mode operations and reduce complexity.
     */
    private class SilentModeController(private val context: Context) {
        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /**
         * Activates silent mode by setting the interruption filter to NONE.
         * Returns true on success, false if permission is missing or an error occurs.
         */
        fun activate(): Boolean {
            if (!canManageDndRules(context)) {
                showPermissionRequiredMessage()
                return false
            }

            return try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                true
            } catch (_: Exception) {
                showActivationErrorMessage()
                false
            }
        }

        /**
         * Deactivates silent mode by restoring the interruption filter to ALL.
         * Returns true on success, false if permission is missing or an error occurs.
         */
        fun deactivate(): Boolean {
            if (!canManageDndRules(context)) {
                return false
            }

            return try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                true
            } catch (_: Exception) {
                showDeactivationErrorMessage()
                false
            }
        }

        /**
         * Returns true if the current interruption filter is set to NONE (silent mode active).
         * Returns false if permission is missing or an error occurs.
         */
        fun isActive(): Boolean {
            if (!canManageDndRules(context)) {
                return false
            }

            return try {
                notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
            } catch (_: Exception) {
                false
            }
        }

        /**
         * Shows a toast informing the user that DND permission is required and redirects
         * them to the notification policy access settings screen.
         */
        private fun showPermissionRequiredMessage() {
            Toast.makeText(
                context,
                "Silent mode requires Do Not Disturb permission. Redirecting to settings...",
                Toast.LENGTH_SHORT
            ).show()
            SettingsUtils.openDndPermissionSettings(context)
        }

        /**
         * Shows a toast informing the user that activating silent mode failed.
         */
        private fun showActivationErrorMessage() {
            Toast.makeText(
                context,
                "Failed to activate silent mode",
                Toast.LENGTH_SHORT
            ).show()
        }

        /**
         * Shows a toast informing the user that deactivating silent mode failed.
         */
        private fun showDeactivationErrorMessage() {
            Toast.makeText(
                context,
                "Failed to deactivate silent mode",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
