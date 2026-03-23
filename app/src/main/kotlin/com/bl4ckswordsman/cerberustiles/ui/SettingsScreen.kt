package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

/**
 * Shared parameters used across different components of the app.
 *
 * @property context The application context.
 * @property coroutineScope The coroutine scope for launching coroutines.
 * @property showDialog The state of the dialog visibility.
 * @property dialogType The type of dialog to be shown.
 * @property sharedPreferences The shared preferences for storing app settings.
 * @property navController Optional navigation controller for screen navigation.
 */
data class SharedParams(
    val context: Context,
    val coroutineScope: CoroutineScope,
    val showDialog: MutableState<Boolean>,
    val dialogType: MutableState<DialogType>,
    val sharedPreferences: SharedPreferences,
    val navController: NavController? = null
)

/**
 * The dialog parameters.
 */
data class DialogParams(
    val showDialog: MutableState<Boolean>,
    val titleText: String,
    val content: @Composable () -> Unit,
    val confirmButtonText: String,
    val onConfirmButtonClick: () -> Unit,
    val dismissButtonText: String? = null,
    val onDismissButtonClick: (() -> Unit)? = null
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
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SettingsScreen(params: SettingsScreenParams) {
    Column(modifier = Modifier.padding(params.paddingValues)) {
        val settingsListItemParams = SettingsListItemParams(
            sharedParams = params.sharedParams
        )

        CreateSettingsListItem(settingsListItemParams)
    }

    if (params.sharedParams.showDialog.value) {
        val dialogCreationParams = DialogCreationParams(
            sharedParams = params.sharedParams
        )

        CreateDialog(dialogCreationParams)
    }
}


/** A preview of the settings screen. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {

    val sharedParams = createSharedParams(rememberNavController())
    val settingsScreenParams = SettingsScreenParams(
        paddingValues = PaddingValues(16.dp),
        sharedParams = sharedParams
    )

    SettingsScreen(settingsScreenParams)
}
