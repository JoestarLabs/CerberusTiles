package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * Dialog for configuring Smart Pixels power-saving overlay settings.
 *
 * @param onDismiss Invoked when the dialog is dismissed or completed.
 * @param context The [Context] used to query and apply Smart Pixels settings.
 */
@Composable
fun SmartPixelsDialog(
    onDismiss: () -> Unit,
    context: Context = LocalContext.current
) {
    val isEnabled = rememberSaveable {
        mutableStateOf(SettingsUtils.SmartPixels.isSmartPixelsEnabled(context))
    }
    val intensity = rememberSaveable {
        mutableFloatStateOf(SettingsUtils.SmartPixels.getSmartPixelsIntensity(context))
    }
    val disableOnCast = rememberSaveable {
        mutableStateOf(SettingsUtils.SmartPixels.isDisableOnCastEnabled(context))
    }
    val hasAccessPerm = remember {
        mutableStateOf(SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentPerm = SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context)
                hasAccessPerm.value = currentPerm
                isEnabled.value = SettingsUtils.SmartPixels.isSmartPixelsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.smart_pixels_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.smart_pixels_desc))

                if (!hasAccessPerm.value) {
                    Button(
                        onClick = { SettingsUtils.SmartPixels.openAccessibilitySettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.grant_accessibility_permission))
                    }
                }

                SwitchWithLabel(
                    isSwitchedOn = isEnabled.value,
                    onCheckedChange = { checked ->
                        val currentPerm =
                            SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context)
                        hasAccessPerm.value = currentPerm
                        if (!currentPerm) {
                            SettingsUtils.SmartPixels.openAccessibilitySettings(context)
                        } else {
                            isEnabled.value = checked
                            SettingsUtils.SmartPixels.setSmartPixelsEnabled(context, checked)
                        }
                    },
                    label = if (isEnabled.value) "Smart Pixels is ON" else "Smart Pixels is OFF"
                )

                Text("Intensity: ${intensity.floatValue.toInt()}%")

                Slider(
                    value = intensity.floatValue,
                    onValueChange = { newVal ->
                        intensity.floatValue = newVal
                        SettingsUtils.SmartPixels.setSmartPixelsIntensity(context, newVal)
                    },
                    valueRange = 10f..90f,
                    steps = 7,
                    enabled = isEnabled.value && hasAccessPerm.value
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = disableOnCast.value,
                        onCheckedChange = { checked ->
                            disableOnCast.value = checked
                            SettingsUtils.SmartPixels.setDisableOnCastEnabled(context, checked)
                        }
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(R.string.smart_pixels_disable_on_cast))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
