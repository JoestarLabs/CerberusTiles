package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Dialog displaying app version information, source repository, and developer ecosystem links.
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
        title = { Text("About Cerberus Tiles") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Current Version: v$appVersion",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Discover more open-source apps on JoestarLabs' GitHub.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { openExternalLink("https://github.com/JoestarLabs") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("More Apps")
                    }
                    OutlinedButton(
                        onClick = { openExternalLink("https://github.com/JoestarLabs/CerberusTiles") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Source Code")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                openExternalLink("https://apt.izzysoft.de/fdroid/index/apk/com.bl4ckswordsman.cerberustiles")
            }) {
                Text("IzzyOnDroid")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
