package com.bl4ckswordsman.cerberustiles.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Encapsulates the visibility states of individual settings components.
 *
 * @property adaptBrightnessSwitch Whether the adaptive brightness toggle is visible.
 * @property brightnessSlider Whether the brightness slider is visible.
 * @property ringerModeSelector Whether the ringer mode segmented control is visible.
 * @property chargingOptimizationSwitch Whether the charging optimization switch is visible.
 * @property smartPixelsSwitch Whether the Smart Pixels switch is visible.
 */
data class ComponentVisibility(
    val adaptBrightnessSwitch: Boolean = true,
    val brightnessSlider: Boolean = true,
    val ringerModeSelector: Boolean = true,
    val chargingOptimizationSwitch: Boolean = true,
    val smartPixelsSwitch: Boolean = true
) {
    companion object {
        const val KEY_ADAPTIVE_BRIGHTNESS = "adaptBrightnessSwitch"
        const val KEY_BRIGHTNESS_SLIDER = "brightnessSlider"
        const val KEY_RINGER_MODE = "ringerModeSelector"
        const val KEY_CHARGING_OPTIMIZATION = "chargingOptimizationSwitch"
        const val KEY_SMART_PIXELS = "smartPixelsSwitch"

        /**
         * Reads the current visibility state from the provided [SharedPreferences].
         */
        fun fromPreferences(preferences: SharedPreferences): ComponentVisibility =
            ComponentVisibility(
                adaptBrightnessSwitch = preferences.getBoolean(KEY_ADAPTIVE_BRIGHTNESS, true),
                brightnessSlider = preferences.getBoolean(KEY_BRIGHTNESS_SLIDER, true),
                ringerModeSelector = preferences.getBoolean(KEY_RINGER_MODE, true),
                chargingOptimizationSwitch = preferences.getBoolean(KEY_CHARGING_OPTIMIZATION, true),
                smartPixelsSwitch = preferences.getBoolean(KEY_SMART_PIXELS, true)
            )
    }
}

/** Backward-compatibility alias for [ComponentVisibility]. */
typealias ComponentVisibilityDialogParams = ComponentVisibility

/**
 * Remembers a [ComponentVisibility] instance that automatically updates whenever
 * the corresponding keys in [sharedPreferences] change.
 */
@Composable
fun rememberComponentVisibility(sharedPreferences: SharedPreferences): ComponentVisibility {
    var visibility by remember(sharedPreferences) {
        mutableStateOf(ComponentVisibility.fromPreferences(sharedPreferences))
    }

    DisposableEffect(sharedPreferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key in listOf(
                    ComponentVisibility.KEY_ADAPTIVE_BRIGHTNESS,
                    ComponentVisibility.KEY_BRIGHTNESS_SLIDER,
                    ComponentVisibility.KEY_RINGER_MODE,
                    ComponentVisibility.KEY_CHARGING_OPTIMIZATION,
                    ComponentVisibility.KEY_SMART_PIXELS
                )
            ) {
                visibility = ComponentVisibility.fromPreferences(prefs)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return visibility
}
