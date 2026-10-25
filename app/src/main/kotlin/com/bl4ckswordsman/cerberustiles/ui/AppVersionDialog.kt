package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bl4ckswordsman.cerberustiles.Constants.UNKNOWN

/**
 * Dialog displaying app version information and release/source links.
 *
 * @param onDismiss Invoked when the dialog is dismissed or closed.
 * @param context The [Context] used for package manager querying and launching external intents.
 */
@Composable
fun AppVersionDialog(
    onDismiss: () -> Unit,
    context: Context = LocalContext.current
) {
    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        Log.w("AppVersionDialog", "Failed to retrieve app version", e)
        UNKNOWN
    }

    fun openExternalLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            context.startActivity(Intent.createChooser(intent, null))
        } catch (e: Exception) {
            Log.w("AppVersionDialog", "No app to handle URL: $url", e)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Version") },
        text = {
            Column {
                Text("Current Version: v$appVersion")
                Spacer(modifier = Modifier.padding(8.dp))
                Text("Updates are available via IzzyOnDroid and GitHub.")
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        openExternalLink("https://apt.izzysoft.de/fdroid/index/apk/com.bl4ckswordsman.cerberustiles")
                    }) {
                        Text("IzzyOnDroid")
                    }
                    TextButton(onClick = {
                        openExternalLink("https://github.com/JoestarLabs/CerberusTiles")
                    }) {
                        Text("GitHub")
                    }
                }
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
