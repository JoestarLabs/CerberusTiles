package com.bl4ckswordsman.cerberustiles.models

/**
 * Represents the ringer mode of the device.
 *
 * Maps to the three states exposed by [android.media.AudioManager]:
 * [android.media.AudioManager.RINGER_MODE_NORMAL],
 * [android.media.AudioManager.RINGER_MODE_SILENT], and
 * [android.media.AudioManager.RINGER_MODE_VIBRATE].
 */
enum class RingerMode {
    /** The device plays sounds for calls and notifications. */
    NORMAL,

    /** The device neither plays sounds nor vibrates (requires DND permission). */
    SILENT,

    /** The device vibrates for calls and notifications without playing sounds. */
    VIBRATE
}
