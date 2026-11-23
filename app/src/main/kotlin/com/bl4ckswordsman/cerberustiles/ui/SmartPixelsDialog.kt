package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.smartpixels.SmartPixelsAccessibilityService

/**
 * Dialog for configuring Smart Pixels power-saving overlay settings.
 *
 * @param onDismiss Invoked when the dialog is dismissed or completed.
 * @param context The [Context] used to query and apply Smart Pixels settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPixelsDialog(
    onDismiss: () -> Unit,
    context: Context = LocalContext.current
) {
    val isEnabled = rememberSaveable {
        mutableStateOf(SettingsUtils.SmartPixels.isSmartPixelsEnabled(context))
    }
    val initialIntensity = remember {
        SettingsUtils.SmartPixels.getSmartPixelsIntensity(context)
    }
    val sliderState = rememberSliderState(
        initialIntensity,
        7,
        10f..90f
    )
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
                    label = if (isEnabled.value) "Smart Pixels is ON" else "Smart Pixels is OFF",
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.rounded_grain_24),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                )

                Text("Intensity: ${sliderState.value.toInt()}%")

                Slider(
                    state = sliderState,
                    onValueChange = { newVal ->
                        sliderState.value = newVal
                        SmartPixelsAccessibilityService.updateIntensity(context, newVal)
                    },
                    onValueChangeFinished = {
                        SettingsUtils.SmartPixels.setSmartPixelsIntensity(context, sliderState.value)
                    },
                    enabled = isEnabled.value && hasAccessPerm.value
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = disableOnCast.value,
                            role = Role.Checkbox,
                            onValueChange = { checked ->
                                disableOnCast.value = checked
                                SettingsUtils.SmartPixels.setDisableOnCastEnabled(context, checked)
                            }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = disableOnCast.value,
                        onCheckedChange = null
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
