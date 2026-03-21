package com.bl4ckswordsman.cerberustiles.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.bl4ckswordsman.cerberustiles.VersionManager
import com.bl4ckswordsman.cerberustiles.navbar.Screen
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

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
    val releaseInfo = remember { mutableStateOf(ReleaseInfo("", "", "")) }
    val downloadId = rememberSaveable { mutableLongStateOf(-1L) }
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val isUpdateAvailable = rememberSaveable { mutableStateOf(false) }
    val versionManager = remember { VersionManager() }
    val coroutineScope = rememberCoroutineScope()
    val dialogType = rememberSaveable { mutableStateOf(DialogType.NONE) }
    val showLicensesDialog = rememberSaveable { mutableStateOf(false) }


    return SharedParams(
        context = context,
        coroutineScope = coroutineScope,
        showDialog = showDialog,
        dialogType = dialogType,
        isUpdateAvailable = isUpdateAvailable,
        releaseInfo = releaseInfo,
        downloadId = downloadId,
        downloadManager = downloadManager,
        versionManager = versionManager,
        sharedPreferences = sharedPreferences,
        showLicensesDialog = showLicensesDialog,
        navController = navController
    )
}

/**
 * Creates the list items of the settings screen.
 * @param params The parameters of the list items.
 */
@Composable
fun CreateSettingsListItem(params: SettingsListItemParams) {
    CreateSettingsListItem(headlineText = "Component Visibility in Overlay Dialog",
        supportingText = "Select which components should be visible",
        onClick = {
            params.sharedParams.showDialog.value = true
            params.sharedParams.dialogType.value = DialogType.COMPONENT_VISIBILITY
        })
    CreateSettingsListItem(
        headlineText = "App version",
        supportingText = "Click to view release notes",
        onClick = {
            params.sharedParams.coroutineScope.launch {
                val (updateAvailable, info) = params.sharedParams.versionManager.fetchAndParseVersionInfo(
                    params.sharedParams.context, params.sharedParams.versionManager
                )
                params.sharedParams.isUpdateAvailable.value = updateAvailable
                params.sharedParams.releaseInfo.value = info
                params.sharedParams.showDialog.value = true
                params.sharedParams.dialogType.value = DialogType.APP_VERSION
            }
        })
    CreateSettingsListItem(headlineText = "Open Source Licenses",
        supportingText = "View licenses of the libraries that made this app possible",
        onClick = {
            params.sharedParams.navController?.navigate(Screen.Licenses.route)
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
                val dialogParams = DialogParams(showDialog = params.sharedParams.showDialog,
                    titleText = if (params.sharedParams.isUpdateAvailable.value) "New update available" else "Release Information",
                    content = {
                        Column {
                            HorizontalDivider()
                            Text("Current Version: v${params.sharedParams.releaseInfo.value.currentVersion}")
                            Text("Latest Version: ${params.sharedParams.releaseInfo.value.latestVersion}")
                            HorizontalDivider()
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text("Latest Release Notes:")
                            MarkdownText(params.sharedParams.releaseInfo.value.releaseNotes)
                        }
                    },
                    confirmButtonText = "Close",
                    onConfirmButtonClick = { params.sharedParams.showDialog.value = false },
                    dismissButtonText = if (params.sharedParams.isUpdateAvailable.value) "Download update" else null,
                    onDismissButtonClick = if (params.sharedParams.isUpdateAvailable.value) {
                        {
                            params.sharedParams.coroutineScope.launch {
                                val url =
                                    params.sharedParams.versionManager.getLatestReleaseApkUrl()
                                if (url.startsWith("http://") || url.startsWith("https://")) {
                                    val request = DownloadManager.Request(Uri.parse(url))
                                    request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS, "CerberusTiles-update.apk"
                                    )
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    params.sharedParams.downloadId.value =
                                        params.sharedParams.downloadManager.enqueue(request)
                                } else {
                                    Log.d("Download Error", "Invalid URL: $url")
                                }
                            }
                        }
                    } else null)
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
        AlertDialog(onDismissRequest = { params.sharedParams.showDialog.value = false },
            title = { Text("Component Visibility") },
            text = {
                Column {
                    SettingsCheckbox(initialValue = params.sharedParams.sharedPreferences.getBoolean(
                        "adaptBrightnessSwitch", true
                    ), text = "1. Adaptive Brightness Switch", onCheckedChange = { newValue ->
                        params.sharedParams.sharedPreferences.edit()
                            .putBoolean("adaptBrightnessSwitch", newValue).apply()
                    })
                    SettingsCheckbox(initialValue = params.sharedParams.sharedPreferences.getBoolean(
                        "brightnessSlider", true
                    ), text = "2. Brightness Slider", onCheckedChange = { newValue ->
                        params.sharedParams.sharedPreferences.edit()
                            .putBoolean("brightnessSlider", newValue).apply()
                    })
                    SettingsCheckbox(initialValue = params.sharedParams.sharedPreferences.getBoolean(
                        "ringerModeSelector", true
                    ), text = "3. Ringer Mode Selector", onCheckedChange = { newValue ->
                        params.sharedParams.sharedPreferences.edit()
                            .putBoolean("ringerModeSelector", newValue).apply()
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
 * Pads the list with the specified value to the specified size.
 */
fun List<Int>.padEnd(size: Int, value: Int = 0): List<Int> {
    return if (size > this.size) this + List(size - this.size) { value } else this
}

/**
 * A composable that displays markdown text using Markwon.
 * @param markdown The markdown text to display.
 */
@Composable
fun MarkdownText(markdown: String) {
    val markdownContext = LocalContext.current
    val markwon = remember { Markwon.create(markdownContext) }

    AndroidView(factory = { context ->
        TextView(context).apply {
            movementMethod = LinkMovementMethod.getInstance()
        }
    }, update = { view ->
        markwon.setMarkdown(view, markdown)
    })
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
    ListItem(modifier = Modifier.clickable { onClick() },
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
        AlertDialog(onDismissRequest = { params.showDialog.value = false },
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
            })
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
