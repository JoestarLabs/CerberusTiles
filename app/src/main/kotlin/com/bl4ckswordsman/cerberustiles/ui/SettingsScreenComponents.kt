package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.bl4ckswordsman.cerberustiles.Constants.UNKNOWN
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.navbar.Screen

/**
 * The settings list item parameters.
 */
data class SettingsListItemParams(
    val sharedParams: SharedParams
)

/**
 * The dialog creation parameters.
 */
data class DialogCreationParams(
    val sharedParams: SharedParams
)

/**
 * Enum class representing different types of dialogs in the app.
 *
 * NONE: No dialog is currently active.
 * COMPONENT_VISIBILITY: Dialog for managing component visibility settings.
 * APP_VERSION: Dialog for displaying app version information.
 */
enum class DialogType {
    NONE, COMPONENT_VISIBILITY, APP_VERSION, SMART_PIXELS
}

/**
 * The shared parameters between the settings screen components.
 */
@Composable
fun createSharedParams(navController: NavController? = null): SharedParams {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val showDialog = rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val dialogType = rememberSaveable { mutableStateOf(DialogType.NONE) }

    return SharedParams(
        context = context,
        coroutineScope = coroutineScope,
        showDialog = showDialog,
        dialogType = dialogType,
        sharedPreferences = sharedPreferences,
        navController = navController
    )
}

/**
 * Creates the list items of the settings screen.
 * @param params The parameters of the list items.
 */
@Composable
fun CreateSettingsListItem(params: SettingsListItemParams) {
    CreateSettingsListItem(
        headlineText = "Smart Pixels",
        supportingText = "Dim pixels to save power on OLED screens",
        onClick = {
            params.sharedParams.showDialog.value = true
            params.sharedParams.dialogType.value = DialogType.SMART_PIXELS
        })
    CreateSettingsListItem(
        headlineText = "Component Visibility in Overlay Dialog",
        supportingText = "Select which components should be visible",
        onClick = {
            params.sharedParams.showDialog.value = true
            params.sharedParams.dialogType.value = DialogType.COMPONENT_VISIBILITY
        })
    CreateSettingsListItem(
        headlineText = "App version",
        supportingText = "Click to view app version",
        onClick = {
            params.sharedParams.showDialog.value = true
            params.sharedParams.dialogType.value = DialogType.APP_VERSION
        })
    CreateSettingsListItem(
        headlineText = "Open Source Licenses",
        supportingText = "View licenses of the libraries that made this app possible",
        onClick = {
            params.sharedParams.navController?.navigate(Screen.Licenses.route)
                ?: Log.w("SettingsScreen", "NavController is null, cannot navigate to licenses")
        })
}

/**
 * Creates the dialog for the settings screen.
 * @param params The parameters of the dialog.
 */
@Composable
fun CreateDialog(params: DialogCreationParams) {
    if (params.sharedParams.showDialog.value) {
        when (params.sharedParams.dialogType.value) {

            DialogType.APP_VERSION -> {
                val context = params.sharedParams.context
                val appVersion = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    Log.w("SettingsScreenComponents", "Failed to retrieve app version", e)
                    UNKNOWN
                }

                AlertDialog(
                    onDismissRequest = { params.sharedParams.showDialog.value = false },
                    title = { Text("App Version") },
                    text = {
                        Column {
                            Text("Current Version: v$appVersion")
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text("Updates are available via IzzyOnDroid and GitHub.")
                        }
                    },
                    confirmButton = {
                        fun openExternalLink(url: String) {
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            try {
                                context.startActivity(Intent.createChooser(intent, null))
                            } catch (e: Exception) {
                                Log.w("SettingsScreenComponents", "No app to handle URL: $url", e)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                                    8.dp
                                )
                            ) {
                                androidx.compose.material3.TextButton(onClick = {
                                    openExternalLink("https://apt.izzysoft.de/fdroid/index/apk/com.bl4ckswordsman.cerberustiles")
                                }) {
                                    Text("IzzyOnDroid")
                                }
                                androidx.compose.material3.TextButton(onClick = {
                                    openExternalLink("https://github.com/JoestarLabs/CerberusTiles")
                                }) {
                                    Text("GitHub")
                                }
                            }
                            Button(onClick = { params.sharedParams.showDialog.value = false }) {
                                Text("Close")
                            }
                        }
                    }
                )
            }

            DialogType.COMPONENT_VISIBILITY -> {
                // Call CreateComponentVisibilityDialog here
                CreateComponentVisibilityDialog(params)
            }

            DialogType.SMART_PIXELS -> {
                CreateSmartPixelsDialog(params)
            }

            DialogType.NONE -> {
                // Do nothing
            }


        }

    }
}

/**
 * Creates the component visibility dialog that allows the user to select which components should
 * be visible.
 * @param params The parameters of the dialog.
 */
@Composable
fun CreateComponentVisibilityDialog(params: DialogCreationParams) {
    if (params.sharedParams.showDialog.value) {
        AlertDialog(
            onDismissRequest = { params.sharedParams.showDialog.value = false },
            title = { Text("Component Visibility") },
            text = {
                Column {
                    SettingsCheckbox(
                        initialValue = params.sharedParams.sharedPreferences.getBoolean(
                            "adaptBrightnessSwitch", true
                        ), text = "1. Adaptive Brightness Switch", onCheckedChange = { newValue ->
                            params.sharedParams.sharedPreferences.edit {
                                putBoolean("adaptBrightnessSwitch", newValue)
                            }
                        })
                    SettingsCheckbox(
                        initialValue = params.sharedParams.sharedPreferences.getBoolean(
                            "brightnessSlider", true
                        ), text = "2. Brightness Slider", onCheckedChange = { newValue ->
                            params.sharedParams.sharedPreferences.edit {
                                putBoolean("brightnessSlider", newValue)
                            }
                        })
                    SettingsCheckbox(
                        initialValue = params.sharedParams.sharedPreferences.getBoolean(
                            "ringerModeSelector", true
                        ), text = "3. Ringer Mode Selector", onCheckedChange = { newValue ->
                            params.sharedParams.sharedPreferences.edit {
                                putBoolean("ringerModeSelector", newValue)
                            }
                        })
                    SettingsCheckbox(
                        initialValue = params.sharedParams.sharedPreferences.getBoolean(
                            "chargingOptimizationSwitch", true
                        ), text = "4. Charging Optimization", onCheckedChange = { newValue ->
                            params.sharedParams.sharedPreferences.edit {
                                putBoolean("chargingOptimizationSwitch", newValue)
                            }
                        })
                    SettingsCheckbox(
                        initialValue = params.sharedParams.sharedPreferences.getBoolean(
                            "smartPixelsSwitch", true
                        ), text = "5. Smart Pixels", onCheckedChange = { newValue ->
                            params.sharedParams.sharedPreferences.edit {
                                putBoolean("smartPixelsSwitch", newValue)
                            }
                        })
                }
            },
            confirmButton = {
                Button(onClick = { params.sharedParams.showDialog.value = false }) {
                    Text("Confirm")
                }
            })
    }
}

/**
 * Creates the Smart Pixels settings dialog.
 */
@Composable
fun CreateSmartPixelsDialog(params: DialogCreationParams) {
    if (params.sharedParams.showDialog.value) {
        val context = params.sharedParams.context
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
            onDismissRequest = { params.sharedParams.showDialog.value = false },
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
                            val currentPerm = SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context)
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
                Button(onClick = { params.sharedParams.showDialog.value = false }) {
                    Text("Done")
                }
            }
        )
    }
}

/**
 * Creates a settings list item.
 * @param headlineText The headline text of the item.
 * @param supportingText The supporting text of the item.
 * @param onClick The action to perform when the item is clicked.
 */
@Composable
fun CreateSettingsListItem(
    headlineText: String, supportingText: String, onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        supportingContent = { Text(supportingText) }
    ) {
        Text(headlineText)
    }
}

/**
 * A settings checkbox.
 * @param initialValue The initial value of the checkbox.
 * @param text The text of the checkbox.
 * @param onCheckedChange The action to perform when the checkbox is checked.
 */
@Composable
fun SettingsCheckbox(
    initialValue: Boolean, text: String, onCheckedChange: (Boolean) -> Unit
) {
    val checkboxValue = rememberSaveable { mutableStateOf(initialValue) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checkboxValue.value, onCheckedChange = {
            checkboxValue.value = it
            onCheckedChange(it)
        })
        Text(text)
    }
}
