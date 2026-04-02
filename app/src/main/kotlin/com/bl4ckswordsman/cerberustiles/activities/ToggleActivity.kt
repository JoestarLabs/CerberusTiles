package com.bl4ckswordsman.cerberustiles.activities

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * An [Activity] that toggles a setting when the user taps the shortcut.
 */
abstract class BaseToggleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the app has the necessary permissions
        if (hasRequiredPermissions()) {
            performAction()
        }

        // Finish the activity without showing any UI
        finish()
    }

    /** Checks if the app has the necessary permissions. */
    abstract fun hasRequiredPermissions(): Boolean

    /** Performs the action when the user taps the shortcut. */
    abstract fun performAction()
}

/**
 * An [Activity] that toggles adaptive brightness when the user taps the adaptive brightness shortcut.
 * It uses BaseToggleActivity as a base class.
 */
class ToggleAdaptiveBrightnessActivity : BaseToggleActivity() {
    /** Checks if the app has the necessary permissions. */
    override fun hasRequiredPermissions(): Boolean {
        return Settings.System.canWrite(this)
    }

    /** Toggles the adaptive brightness setting. */
    override fun performAction() {
        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { _ -> }
        )
        SettingsUtils.Brightness.toggleAdaptiveBrightness(params)
    }
}

/**
 * An [Activity] that toggles vibration mode when the user taps the vibration mode shortcut.
 * It uses BaseToggleActivity as a base class.
 */
class ToggleVibrationModeActivity : BaseToggleActivity() {
    /** Checks if the app has the necessary permissions. */
    override fun hasRequiredPermissions(): Boolean {
        return SettingsUtils.canWriteSettings(this) && SettingsUtils.canAccessNotificationPolicy(
            this
        )
    }

    /** Toggles the vibration mode setting. */
    override fun performAction() {
        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { _ -> }
        )
        SettingsUtils.Vibration.toggleVibrationMode(params)
    }
}
