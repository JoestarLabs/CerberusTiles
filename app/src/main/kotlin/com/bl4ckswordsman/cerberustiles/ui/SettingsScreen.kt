package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope

/**
 * Shared parameters used across different components of the app.
 *
 * @property context The application context.
 * @property coroutineScope The coroutine scope for launching coroutines.
 * @property showDialog The state of the dialog visibility.
 * @property dialogType The type of dialog to be shown.
 * @property sharedPreferences The shared preferences for storing app settings.
 * @property onNavigateToPermissions Optional callback to navigate to permissions screen.
 * @property onNavigateToLicenses Optional callback to navigate to licenses screen.
 */
data class SharedParams(
    val context: Context,
    val coroutineScope: CoroutineScope,
    val showDialog: MutableState<Boolean>,
    val dialogType: MutableState<DialogType>,
    val sharedPreferences: SharedPreferences,
    val onNavigateToPermissions: (() -> Unit)? = null,
    val onNavigateToLicenses: (() -> Unit)? = null
)

/**
 * The settings screen parameters.
 */
data class SettingsScreenParams(
    val paddingValues: PaddingValues,
    val sharedParams: SharedParams
)

/**
 * The settings screen of the app.
 * @param params The parameters of the settings screen.
 */
@Composable
fun SettingsScreen(params: SettingsScreenParams) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(params.paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        val settingsListItemParams = SettingsListItemParams(
            sharedParams = params.sharedParams
        )

        CreateSettingsListItem(settingsListItemParams)
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (params.sharedParams.showDialog.value) {
        val dialogCreationParams = DialogCreationParams(
            sharedParams = params.sharedParams
        )

        CreateDialog(dialogCreationParams)
    }
}


/** A preview of the settings screen. */
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {

    val sharedParams = createSharedParams()
    val settingsScreenParams = SettingsScreenParams(
        paddingValues = PaddingValues(16.dp),
        sharedParams = sharedParams
    )

    SettingsScreen(settingsScreenParams)
}
