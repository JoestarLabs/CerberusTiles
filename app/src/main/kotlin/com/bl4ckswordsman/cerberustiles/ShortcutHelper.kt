package com.bl4ckswordsman.cerberustiles

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.bl4ckswordsman.cerberustiles.activities.OverlayActivity
import com.bl4ckswordsman.cerberustiles.activities.ToggleAdaptiveBrightnessActivity
import com.bl4ckswordsman.cerberustiles.activities.ToggleVibrationModeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data class to store the information of a shortcut.
 */
data class CustomShortcutInfo(
    val id: String,
    val shortLabel: String,
    val longLabel: String,
    val icon: Int,
    val action: String,
    val activityClass: Class<*>
)

/**
 * Helper class to create shortcuts.
 */
class ShortcutHelper(private val context: Context) {
    /**
     * Creates all the shortcuts for the app.
     */
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    suspend fun createAllShortcuts() {
        withContext(Dispatchers.IO) {
            val shortcutManager =
                context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager?

            val adaptiveBrightnessShortcut = createShortcut(
                CustomShortcutInfo(
                    id = ID_ADAPTIVE_BRIGHTNESS,
                    shortLabel = "Toggle Adaptive Brightness",
                    longLabel = "Toggle Adaptive Brightness",
                    icon = R.drawable.baseline_brightness_auto_24,
                    action = Constants.TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION,
                    activityClass = ToggleAdaptiveBrightnessActivity::class.java
                )
            )

            val vibrationRingerModeShortcut = createShortcut(
                CustomShortcutInfo(
                    id = ID_VIBRATION_RINGER_MODE,
                    shortLabel = "Toggle Vibration Ringer Mode",
                    longLabel = "Toggle Vibration Ringer Mode",
                    icon = R.drawable.baseline_vibration_24,
                    action = Constants.TOGGLE_VIBRATION_MODE_ACTION,
                    activityClass = ToggleVibrationModeActivity::class.java
                )
            )

            val overlayShortcut = createShortcut(
                CustomShortcutInfo(
                    id = ID_OVERLAY,
                    shortLabel = "Toolbox Overlay",
                    longLabel = "Toolbox Overlay",
                    icon = R.drawable.round_open_in_new_24,
                    action = "com.bl4ckswordsman.cerberustiles.OPEN_OVERLAY",
                    activityClass = OverlayActivity::class.java
                )
            )

            shortcutManager?.dynamicShortcuts =
                listOf(adaptiveBrightnessShortcut, vibrationRingerModeShortcut, overlayShortcut)
        }
    }

    companion object {
        const val ID_ADAPTIVE_BRIGHTNESS = "toggle_adaptive_brightness"
        const val ID_VIBRATION_RINGER_MODE = "toggle_vibration_ringer_mode"
        const val ID_OVERLAY = "overlay_shortcut"

        /**
         * Reports shortcut usage to [ShortcutManager] on API 25+ (Android 7.1+).
         */
        @SuppressLint("NewApi")
        fun reportUsage(
            context: Context,
            shortcutId: String,
            shortcutManager: ShortcutManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context.getSystemService(Context.SHORTCUT_SERVICE) as? ShortcutManager
            } else {
                null
            }
        ) {
            shortcutManager?.reportShortcutUsed(shortcutId)
        }
    }

    /**
     * Creates a shortcut with the given parameters.
     * @param shortcutInfo The CustomShortcutInfo object.
     */
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    suspend fun createShortcut(shortcutInfo: CustomShortcutInfo): ShortcutInfo {
        return withContext(Dispatchers.IO) {
            val shortcut = ShortcutInfo.Builder(context, shortcutInfo.id)
                .setShortLabel(shortcutInfo.shortLabel).setLongLabel(shortcutInfo.longLabel)
                .setIcon(Icon.createWithResource(context, shortcutInfo.icon))
                .setIntent(Intent(context, shortcutInfo.activityClass).apply {
                    this.action = shortcutInfo.action
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }).build()

            shortcut
        }
    }
}
