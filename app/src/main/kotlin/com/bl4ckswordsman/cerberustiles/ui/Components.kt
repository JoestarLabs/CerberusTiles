package com.bl4ckswordsman.cerberustiles.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import kotlin.math.ln

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
                }
            )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = {
            Text(
                text = "To use this feature, please connect your device to a PC and grant the WRITE_SECURE_SETTINGS permission using the following ADB command:\n\n" +
                        "$adbCommand\n\n" +
                        "Note: Shizuku support is planned for the future."
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ADB Command", adbCommand)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }) {
                Text("Copy")
            }
        }
    )
}

/**
 * A slider for adjusting screen brightness.
 *
 * The slider is initialised from the current system screen brightness and applies
 * changes in real time via [SettingsUtils.Brightness.setScreenBrightness]. The
 * brightness value is mapped on a logarithmic scale so that the perceived brightness
 * change feels linear to the user.
 *
 * @param context The application context used to read and write the system brightness setting.
 */
@Composable
fun BrightnessSlider(context: Context) {
    var sliderPosition by rememberSaveable {
        mutableFloatStateOf(
            (ln(
                SettingsUtils.Brightness.getScreenBrightness(context).toDouble()
            ) / ln(255.0)).toFloat()
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = {
            sliderPosition = 0f
            SettingsUtils.Brightness.setScreenBrightness(context, sliderPosition)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_brightness_empty_24),
                contentDescription = "Minimize brightness"
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Slider(
                value = sliderPosition, onValueChange = {
                    sliderPosition = it
                    SettingsUtils.Brightness.setScreenBrightness(context, it)
                }, valueRange = 0f..1f
            )
        }

        IconButton(onClick = {
            sliderPosition = 1f
            SettingsUtils.Brightness.setScreenBrightness(context, sliderPosition)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_brightness_high_24),
                contentDescription = "Maximize brightness"
            )
        }
    }
}
