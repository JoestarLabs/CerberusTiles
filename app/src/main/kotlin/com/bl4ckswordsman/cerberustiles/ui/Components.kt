package com.bl4ckswordsman.cerberustiles.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.BrightnessPreset

/**
 * A switch with a label. The label is clickable and toggles the switch.
 *
 * @param isSwitchedOn Whether the switch is currently in the on (checked) state.
 * @param onCheckedChange Callback invoked with the new checked state when the user
 *   taps the row or the switch directly.
 * @param label The text displayed next to the switch describing the setting.
 */
@Composable
fun SwitchWithLabel(isSwitchedOn: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp), // Outside padding
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isSwitchedOn) }
                .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 14.dp), // Inside padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp) // Space between label and switch
            ) { Text(text = label) }
            Switch(
                checked = isSwitchedOn,
                onCheckedChange = { onCheckedChange(it) },
                colors = SwitchDefaults.colors(
                    checkedIconColor = MaterialTheme.colorScheme.primary
                ),
                thumbContent = if (isSwitchedOn) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                })
        }
    }
}

/**
 * A dialog showing the required ADB command to grant WRITE_SECURE_SETTINGS permission.
 *
 * The dialog displays the full ADB command and provides a "Copy" button that writes
 * the command to the system clipboard for easy transfer to a PC terminal.
 *
 * @param context The application context used to build the ADB command string and
 *   access the [ClipboardManager].
 * @param onDismiss Callback invoked when the dialog is dismissed via the "OK" button
 *   or a tap outside the dialog.
 */
@Composable
fun AdbPermissionDialog(context: Context, onDismiss: () -> Unit) {
    val adbCommand =
        "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Permission Required") }, text = {
        Text(
            text = "To use this feature, please connect your device to a PC and grant the WRITE_SECURE_SETTINGS permission using the following ADB command:\n\n$adbCommand\n\nNote: Shizuku support is planned for the future."
        )
    }, confirmButton = {
        Button(onClick = onDismiss) {
            Text("OK")
        }
    }, dismissButton = {
        Button(onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ADB Command", adbCommand)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }) {
            Text("Copy")
        }
    })
}

/**
 * A segmented button group for selecting screen brightness presets (Min, Balanced, Max).
 *
 * Each button applies the preset brightness level in real-time via
 * [SettingsUtils.Brightness.setScreenBrightness] and updates the selection state.
 *
 * @param context Application context used to read and set screen brightness.
 * @param isOverlayContext Whether this component is rendered inside an overlay
 *   (compact view with smaller icons and no labels).
 * @param initialPreset Optional initial preset to override system brightness lookup.
 * @param onPresetSelected Optional callback invoked when a preset is selected.
 */
@Composable
fun BrightnessPresetButtonGroup(
    context: Context,
    isOverlayContext: Boolean = false,
    initialPreset: BrightnessPreset? = null,
    onPresetSelected: ((BrightnessPreset) -> Unit)? = null
) {
    var selectedPreset by rememberSaveable {
        mutableStateOf(
            initialPreset ?: BrightnessPreset.fromBrightness(
                SettingsUtils.Brightness.getScreenBrightness(context)
            )
        )
    }

    val presets = BrightnessPreset.entries

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        presets.forEachIndexed { index, preset ->
            val isSelected = selectedPreset == preset

            ToggleButton(
                checked = isSelected,
                onCheckedChange = {
                    selectedPreset = preset
                    SettingsUtils.Brightness.setScreenBrightness(context, preset.level)
                    onPresetSelected?.invoke(preset)
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    presets.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = when (preset) {
                            BrightnessPreset.MIN -> R.drawable.rounded_brightness_empty_24
                            BrightnessPreset.BALANCED -> R.drawable.rounded_brightness_medium_24
                            BrightnessPreset.MAX -> R.drawable.rounded_brightness_high_24
                        }
                    ),
                    contentDescription = preset.contentDescription,
                    modifier = if (isOverlayContext)
                        Modifier.size(16.dp)
                    else
                        Modifier.size(ToggleButtonDefaults.IconSize)
                )
                if (!isOverlayContext) {
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(text = preset.label)
                }
            }
        }
    }
}

/**
 * Backwards-compatible composable for adjusting screen brightness using [BrightnessPresetButtonGroup].
 */
@Composable
fun BrightnessSlider(context: Context) {
    BrightnessPresetButtonGroup(context = context)
}
