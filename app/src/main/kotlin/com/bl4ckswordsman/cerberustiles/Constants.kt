package com.bl4ckswordsman.cerberustiles

/** Constants used in the app. */
object Constants {
    /** The route name for the Home screen. */
    const val HOME_SCREEN = "Home"

    /** The route name for the Settings screen. */
    const val SETTINGS_SCREEN = "Settings"

    /** The intent action string used to trigger the adaptive brightness toggle shortcut. */
    const val TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION =
        "com.bl4ckswordsman.cerberustiles.TOGGLE_ADAPTIVE_BRIGHTNESS"

    /** The intent action string used to trigger the vibration mode toggle shortcut. */
    const val TOGGLE_VIBRATION_MODE_ACTION =
        "com.bl4ckswordsman.cerberustiles.action.TOGGLE_VIBRATION_MODE"

    /** The intent action string used to open the toolbox overlay shortcut. */
    const val OPEN_OVERLAY_ACTION =
        "com.bl4ckswordsman.cerberustiles.OPEN_OVERLAY"

    /** Name of the SharedPreferences file storing app settings. */
    const val SETTINGS_PREFS_NAME = "settings"

    /** Fallback display string used when a value (e.g. app version) cannot be determined. */
    const val UNKNOWN = "Unknown"
}
