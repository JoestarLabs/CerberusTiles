package com.bl4ckswordsman.cerberustiles.ui

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.core.content.edit

/**
 * Dialog allowing the user to select which components should be visible in the overlay dialog and home screen.
 *
 * @param onDismiss Invoked when the dialog is dismissed or confirmed.
 * @param sharedPreferences [SharedPreferences] used to persist component visibility preferences.
 */
@Composable
fun ComponentVisibilityDialog(
    onDismiss: () -> Unit,
    sharedPreferences: SharedPreferences
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Component Visibility") },
        text = {
            Column {
                SettingsCheckbox(
                    initialValue = sharedPreferences.getBoolean(
                        ComponentVisibility.KEY_ADAPTIVE_BRIGHTNESS,
                        true
                    ),
                    text = "1. Adaptive Brightness Switch",
                    onCheckedChange = { newValue ->
                        sharedPreferences.edit {
                            putBoolean(ComponentVisibility.KEY_ADAPTIVE_BRIGHTNESS, newValue)
                        }
                    }
                )
                SettingsCheckbox(
                    initialValue = sharedPreferences.getBoolean(
                        ComponentVisibility.KEY_BRIGHTNESS_SLIDER,
                        true
                    ),
                    text = "2. Brightness Presets (Min, Balanced, Max)",
                    onCheckedChange = { newValue ->
                        sharedPreferences.edit {
                            putBoolean(ComponentVisibility.KEY_BRIGHTNESS_SLIDER, newValue)
                        }
                    }
                )
                SettingsCheckbox(
                    initialValue = sharedPreferences.getBoolean(
                        ComponentVisibility.KEY_RINGER_MODE,
                        true
                    ),
                    text = "3. Ringer Mode Selector",
                    onCheckedChange = { newValue ->
                        sharedPreferences.edit {
                            putBoolean(ComponentVisibility.KEY_RINGER_MODE, newValue)
                        }
                    }
                )
                SettingsCheckbox(
                    initialValue = sharedPreferences.getBoolean(
                        ComponentVisibility.KEY_CHARGING_OPTIMIZATION,
                        true
                    ),
                    text = "4. Charging Optimization",
                    onCheckedChange = { newValue ->
                        sharedPreferences.edit {
                            putBoolean(ComponentVisibility.KEY_CHARGING_OPTIMIZATION, newValue)
                        }
                    }
                )
                SettingsCheckbox(
                    initialValue = sharedPreferences.getBoolean(
                        ComponentVisibility.KEY_SMART_PIXELS,
                        true
                    ),
                    text = "5. Smart Pixels",
                    onCheckedChange = { newValue ->
                        sharedPreferences.edit {
                            putBoolean(ComponentVisibility.KEY_SMART_PIXELS, newValue)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Confirm")
            }
        }
    )
}

/**
 * A reusable checkbox row for settings screens.
 *
 * @param initialValue The initial checked state.
 * @param text The label text to display next to the checkbox.
 * @param onCheckedChange Callback invoked when the checked state changes.
 */
@Composable
fun SettingsCheckbox(
    initialValue: Boolean,
    text: String,
    onCheckedChange: (Boolean) -> Unit
) {
    val checkboxValue = rememberSaveable { mutableStateOf(initialValue) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checkboxValue.value,
            onCheckedChange = {
                checkboxValue.value = it
                onCheckedChange(it)
            }
        )
        Text(text)
    }
}
