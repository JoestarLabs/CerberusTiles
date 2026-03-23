package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bl4ckswordsman.cerberustiles.Constants.UNKNOWN
import com.bl4ckswordsman.cerberustiles.navbar.Screen
import androidx.core.content.edit
import androidx.core.net.toUri

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
    NONE, COMPONENT_VISIBILITY, APP_VERSION
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

                val dialogParams = DialogParams(
                    showDialog = params.sharedParams.showDialog,
                    titleText = "App Version",
                    content = {
                        Column {
                            Text("Current Version: v$appVersion")
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text("Updates are available via IzzyOnDroid or F-Droid.")
                        }
                    },
                    confirmButtonText = "Close",
                    onConfirmButtonClick = { params.sharedParams.showDialog.value = false },
                    dismissButtonText = "GitHub",
                    onDismissButtonClick = {
                        val intent = Intent(Intent.ACTION_VIEW,
                            "https://github.com/JoestarLabs/CerberusTiles".toUri())
                        context.startActivity(intent)
                    },
                    neutralButtonText = "IzzyOnDroid",
                    onNeutralButtonClick = {
                        val intent = Intent(Intent.ACTION_VIEW,
                            "https://apt.izzysoft.de/fdroid/index/apk/com.bl4ckswordsman.cerberustiles".toUri())
                        context.startActivity(intent)
                    }
                )
                CreateDialog(dialogParams)
            }

            DialogType.COMPONENT_VISIBILITY -> {
                // Call CreateComponentVisibilityDialog here
                CreateComponentVisibilityDialog(params)
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
        headlineContent = { Text(headlineText) },
        supportingContent = { Text(supportingText) })
}

/**
 * Creates a dialog.
 * @param params The dialog parameters.
 */
@Composable
fun CreateDialog(params: DialogParams) {
    if (params.showDialog.value) {
        AlertDialog(
            onDismissRequest = { params.showDialog.value = false },
            title = { Text(params.titleText) },
            text = { params.content() },
            confirmButton = {
                Button(onClick = params.onConfirmButtonClick) {
                    Text(params.confirmButtonText)
                }
            },
            dismissButton = {
                if (params.dismissButtonText != null && params.onDismissButtonClick != null) {
                    Button(onClick = params.onDismissButtonClick) {
                        Text(params.dismissButtonText)
                    }
                }

                if (params.neutralButtonText != null && params.onNeutralButtonClick != null) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(onClick = params.onNeutralButtonClick) {
                        Text(params.neutralButtonText)
                    }
                }
            }
        )
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
