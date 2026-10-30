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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.R

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
 * SMART_PIXELS: Dialog for configuring Smart Pixels power-saving overlay.
 */
enum class DialogType {
    NONE, COMPONENT_VISIBILITY, APP_VERSION, SMART_PIXELS
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
fun CreateSettingsListItem(params: SettingsListItemParams) {
    SettingsSectionHeader("Features & Overlay")
    RoundedCardContainer {
        CreateSettingsListItem(
            headlineText = "Smart Pixels",
            supportingText = "Dim pixels to save power on OLED screens",
            onClick = {
                params.sharedParams.showDialog.value = true
                params.sharedParams.dialogType.value = DialogType.SMART_PIXELS
            }
        )
        CreateSettingsListItem(
            headlineText = "Component Visibility in Overlay Dialog",
            supportingText = "Select which components should be visible",
            onClick = {
                params.sharedParams.showDialog.value = true
                params.sharedParams.dialogType.value = DialogType.COMPONENT_VISIBILITY
            }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    SettingsSectionHeader("About & Privacy")
    RoundedCardContainer {
        CreateSettingsListItem(
            headlineText = "Permissions & Privacy",
            supportingText = "View required permissions, security details, and privacy FAQ",
            trailingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.round_open_in_new_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                params.sharedParams.onNavigateToPermissions?.invoke()
                    ?: Log.w("SettingsScreen", "onNavigateToPermissions callback is null")
            }
        )
        CreateSettingsListItem(
            headlineText = "Open Source Licenses",
            supportingText = "View licenses of the libraries that made this app possible",
            trailingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.round_open_in_new_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                params.sharedParams.onNavigateToLicenses?.invoke()
                    ?: Log.w("SettingsScreen", "onNavigateToLicenses callback is null")
            }
        )
        val context = params.sharedParams.context
        val appVersion = remember(context) {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                null
            }
        }
        CreateSettingsListItem(
            headlineText = "About",
            supportingText = if (appVersion != null) "Version v$appVersion • JoestarLabs" else "JoestarLabs",
            onClick = {
                params.sharedParams.showDialog.value = true
                params.sharedParams.dialogType.value = DialogType.APP_VERSION
            }
        )
    }
}

/**
 * Creates the dialog for the settings screen based on [DialogType].
 * @param params The parameters of the dialog.
 */
@Composable
fun CreateDialog(params: DialogCreationParams) {
    if (params.sharedParams.showDialog.value) {
        when (params.sharedParams.dialogType.value) {
            DialogType.APP_VERSION -> {
                AppVersionDialog(
                    onDismiss = { params.sharedParams.showDialog.value = false },
                    context = params.sharedParams.context
                )
            }

            DialogType.COMPONENT_VISIBILITY -> {
                ComponentVisibilityDialog(
                    onDismiss = { params.sharedParams.showDialog.value = false },
                    sharedPreferences = params.sharedParams.sharedPreferences
                )
            }

            DialogType.SMART_PIXELS -> {
                SmartPixelsDialog(
                    onDismiss = { params.sharedParams.showDialog.value = false },
                    context = params.sharedParams.context
                )
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
        ComponentVisibilityDialog(
            onDismiss = { params.sharedParams.showDialog.value = false },
            sharedPreferences = params.sharedParams.sharedPreferences
        )
    }
}

/**
 * Creates the Smart Pixels settings dialog.
 * @param params The parameters of the dialog.
 */
@Composable
fun CreateSmartPixelsDialog(params: DialogCreationParams) {
    if (params.sharedParams.showDialog.value) {
        SmartPixelsDialog(
            onDismiss = { params.sharedParams.showDialog.value = false },
            context = params.sharedParams.context
        )
    }
}

/**
 * Creates a settings list item.
 * @param headlineText The headline text of the item.
 * @param supportingText The supporting text of the item.
 * @param onClick The action to perform when the item is clicked.
 * @param modifier Optional modifier.
 * @param trailingContent Optional trailing composable.
 */
@Composable
fun CreateSettingsListItem(
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
