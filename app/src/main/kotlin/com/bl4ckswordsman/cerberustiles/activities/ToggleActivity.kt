package com.bl4ckswordsman.cerberustiles.activities

import android.app.Activity
import android.os.Bundle
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.ShortcutHelper

/**
 * An [Activity] that toggles a setting when the user taps the shortcut.
 */
abstract class BaseToggleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the app has the necessary permissions
        if (hasRequiredPermissions()) {
            performAction()
        } else {
            onPermissionsMissing()
        }

        // Finish the activity without showing any UI
        finish()
    }

    /** Checks if the app has the necessary permissions. */
    abstract fun hasRequiredPermissions(): Boolean

    /** Performs the action when the user taps the shortcut. */
    abstract fun performAction()

    /** Called when [hasRequiredPermissions] returns false. */
    open fun onPermissionsMissing() {}
}

/**
 * An [Activity] that toggles adaptive brightness when the user taps the adaptive brightness shortcut.
 * It uses BaseToggleActivity as a base class.
 */
class ToggleAdaptiveBrightnessActivity : BaseToggleActivity() {
    /** Checks if the app has the necessary permissions. */
    override fun hasRequiredPermissions(): Boolean {
        return SettingsUtils.canWriteSettings(this)
    }

    /** Toggles the adaptive brightness setting. */
    override fun performAction() {
        ShortcutHelper.reportUsage(this, ShortcutHelper.ID_ADAPTIVE_BRIGHTNESS)
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
        return SettingsUtils.canWriteSettings(this)
    }

    /** Opens system write-settings permission screen when permission is missing. */
    override fun onPermissionsMissing() {
        SettingsUtils.openPermissionSettings(this)
    }

    /** Toggles the vibration mode setting. */
    override fun performAction() {
        ShortcutHelper.reportUsage(this, ShortcutHelper.ID_VIBRATION_RINGER_MODE)
        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { _ -> }
        )
        SettingsUtils.Vibration.toggleVibrationMode(params)
    }
}
