package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.R

/**
 * Enum class representing different types of dialogs in the app.
 */
enum class DialogType {
    NONE, COMPONENT_VISIBILITY, APP_VERSION, SMART_PIXELS
}

/**
 * Retrieves the app version name safely, or `null` if unavailable.
 */
internal fun getAppVersion(context: Context): String? = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
} catch (e: Exception) {
    Log.w("SettingsScreen", "Failed to retrieve app version", e)
    null
}

/**
 * The shared parameters between the settings screen components.
 */
@Composable
fun createSharedParams(
    onNavigateToPermissions: (() -> Unit)? = null,
    onNavigateToLicenses: (() -> Unit)? = null
): SharedParams {
    val context = LocalContext.current
    val sharedPreferences = remember(context) {
        context.getSharedPreferences(Constants.SETTINGS_PREFS_NAME, Context.MODE_PRIVATE)
    }
    val showDialog = rememberSaveable { mutableStateOf(false) }
    val dialogType = rememberSaveable { mutableStateOf(DialogType.NONE) }

    return SharedParams(
        context = context,
        showDialog = showDialog,
        dialogType = dialogType,
        sharedPreferences = sharedPreferences,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToLicenses = onNavigateToLicenses
    )
}

/**
 * A rounded container for grouping related settings list items with Material 3 styling.
 */
@Composable
fun RoundedCardContainer(
    modifier: Modifier = Modifier,
    spacing: Dp = 2.dp,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius)),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content
    )
}

/**
 * A section header for grouping settings categories with primary-tinted typography.
 */
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/**
 * Creates the list items of the settings screen.
 * @param params The parameters of the list items.
 */
@Composable
fun SettingsListContent(sharedParams: SharedParams) {
    SettingsSectionHeader("Features & Overlay")
    RoundedCardContainer {
        SettingsListRow(
            headlineText = "Smart Pixels",
            supportingText = "Dim pixels to save power on OLED screens",
            onClick = {
                sharedParams.showDialog.value = true
                sharedParams.dialogType.value = DialogType.SMART_PIXELS
            }
        )
        SettingsListRow(
            headlineText = "Component Visibility in Overlay Dialog",
            supportingText = "Select which components should be visible",
            onClick = {
                sharedParams.showDialog.value = true
                sharedParams.dialogType.value = DialogType.COMPONENT_VISIBILITY
            }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingsSectionHeader("About & Privacy")
    RoundedCardContainer {
        SettingsListRow(
            headlineText = "Permissions & Privacy",
            supportingText = "View required permissions, security details, and privacy FAQ",
            trailingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_open_in_new_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                sharedParams.onNavigateToPermissions?.invoke()
                    ?: Log.w("SettingsScreen", "onNavigateToPermissions callback is null")
            }
        )
        SettingsListRow(
            headlineText = "Open Source Licenses",
            supportingText = "View licenses of the libraries that made this app possible",
            trailingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_open_in_new_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                sharedParams.onNavigateToLicenses?.invoke()
                    ?: Log.w("SettingsScreen", "onNavigateToLicenses callback is null")
            }
        )
        val context = sharedParams.context
        val appVersion = remember(context) { getAppVersion(context) }
        SettingsListRow(
            headlineText = "About",
            supportingText = if (appVersion != null) "Version v$appVersion • JoestarLabs" else "JoestarLabs",
            onClick = {
                sharedParams.showDialog.value = true
                sharedParams.dialogType.value = DialogType.APP_VERSION
            }
        )
    }
}

/**
 * Hosts the active settings dialog based on [SharedParams.dialogType].
 */
@Composable
fun SettingsDialogHost(sharedParams: SharedParams) {
    if (sharedParams.showDialog.value) {
        when (sharedParams.dialogType.value) {
            DialogType.APP_VERSION -> {
                AppVersionDialog(
                    onDismiss = { sharedParams.showDialog.value = false },
                    context = sharedParams.context
                )
            }

            DialogType.COMPONENT_VISIBILITY -> {
                ComponentVisibilityDialog(
                    onDismiss = { sharedParams.showDialog.value = false },
                    sharedPreferences = sharedParams.sharedPreferences
                )
            }

            DialogType.SMART_PIXELS -> {
                SmartPixelsDialog(
                    onDismiss = { sharedParams.showDialog.value = false },
                    context = sharedParams.context
                )
            }

            DialogType.NONE -> {}
        }
    }
}

/**
 * Creates a settings list row item.
 */
@Composable
fun SettingsListRow(
    headlineText: String,
    supportingText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        supportingContent = { Text(supportingText) },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Text(headlineText)
    }
}
