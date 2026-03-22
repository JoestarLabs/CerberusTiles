package com.bl4ckswordsman.cerberustiles.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bl4ckswordsman.cerberustiles.VersionManager
import kotlinx.coroutines.CoroutineScope

/**
 * Shared parameters used across different components of the app.
 *
 * @property context The application context.
 * @property coroutineScope The coroutine scope for launching coroutines.
 * @property showDialog The state of the dialog visibility.
 * @property dialogType The type of dialog to be shown.
 * @property isUpdateAvailable Whether an update is available for the app.
 * @property releaseInfo Information about the latest release.
 * @property downloadId The ID of the current download.
 * @property downloadManager The system's download manager.
 * @property versionManager The version manager for the app.
 * @property sharedPreferences The shared preferences for storing app settings.
 * @property navController Optional navigation controller for screen navigation.
 */
data class SharedParams(
    val context: Context,
    val coroutineScope: CoroutineScope,
    val showDialog: MutableState<Boolean>,
    val dialogType: MutableState<DialogType>,
    val isUpdateAvailable: MutableState<Boolean>,
    val releaseInfo: MutableState<ReleaseInfo>,
    val downloadId: MutableState<Long>,
    val downloadManager: DownloadManager,
    val versionManager: VersionManager,
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
    val onDownloadComplete =
        createDownloadCompleteReceiver(
            params.sharedParams.downloadId,
            params.sharedParams.downloadManager
        )
    DownloadReceiver(params.sharedParams.context, onDownloadComplete)
    FetchLatestReleaseInfoOnCompose(params.sharedParams.context, params.sharedParams.releaseInfo)

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

/**
 * Create a download complete receiver.
 * @param downloadId The download ID to create the receiver for.
 * @param downloadManager The download manager to use.
 */
@Composable
fun createDownloadCompleteReceiver(
    downloadId: MutableState<Long>,
    downloadManager: DownloadManager
): BroadcastReceiver {
    return object : BroadcastReceiver() {
        override fun onReceive(context: Context, downloadIntent: Intent) {
            Log.d("Download", "Download complete")
            if (downloadId.value == downloadIntent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID,
                    -1L
                )
            ) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId.value)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (columnIndex != -1 && DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(
                            columnIndex
                        )
                    ) {
                        Log.d("Download", "Download successful")
                    }
                }
            }
        }
    }
}

/**
 * The download receiver composable.
 * @param context The context of the app.
 * @param onDownloadComplete The download complete receiver.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DownloadReceiver(context: Context, onDownloadComplete: BroadcastReceiver) {
    DisposableEffect(Unit) {
        context.registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(onDownloadComplete)
        }
    }
}

/**
 * Fetches the latest release info on compose.
 * @param context The context of the app.
 * @param releaseInfo The release info to update.
 */
@Composable
fun FetchLatestReleaseInfoOnCompose(context: Context, releaseInfo: MutableState<ReleaseInfo>) {
    LaunchedEffect(Unit) {
        releaseInfo.value = fetchLatestReleaseInfo(context)
    }
}

/**
 * The release info of the app.
 * @param currentVersion The current version of the app.
 * @param latestVersion The latest version of the app.
 * @param releaseNotes The release notes of the latest version.
 */
data class ReleaseInfo(
    val currentVersion: String, val latestVersion: String, val releaseNotes: String
)

/**
 * Fetches the latest release info from the server.
 * @param context The context of the app.
 * @return The latest release info.
 */
suspend fun fetchLatestReleaseInfo(context: Context): ReleaseInfo {
    val versionManager = VersionManager()
    val appVersion = versionManager.getCurrentAppVersion(context)
    val latestReleaseVersion = versionManager.getLatestReleaseVersion()
    val releaseNotes = versionManager.getLatestReleaseNotes()

    return ReleaseInfo(appVersion, latestReleaseVersion, releaseNotes)
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
