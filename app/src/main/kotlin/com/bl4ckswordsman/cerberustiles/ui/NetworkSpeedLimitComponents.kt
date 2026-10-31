package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import com.bl4ckswordsman.cerberustiles.util.NetworkSpeedLimitRepository

/**
 * Material 3 Card displaying the currently active network download speed limit
 * with options to select or cycle through available limits.
 */
@Composable
fun NetworkSpeedLimitCard(
    currentLimit: NetworkSpeedLimit,
    onLimitSelected: (NetworkSpeedLimit) -> Unit,
    onPermissionRequired: () -> Unit,
    modifier: Modifier = Modifier,
    isOverlayContext: Boolean = false
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                if (SettingsUtils.NetworkSpeed.canWriteSecureSettings(context)) {
                    showDialog = true
                } else {
                    onPermissionRequired()
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isOverlayContext) 12.dp else 16.dp,
                    vertical = if (isOverlayContext) 10.dp else 14.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_speed_24),
                        contentDescription = "Network Speed Limit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isOverlayContext) 20.dp else 24.dp)
                    )
                    Spacer(modifier = Modifier.width(if (isOverlayContext) 10.dp else 16.dp))
                    Column {
                        Text(
                            text = if (isOverlayContext) "Download Limit" else "Network Download Limit",
                            style = if (isOverlayContext) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (currentLimit.isUnlimited) {
                                "No limit (unthrottled)"
                            } else {
                                "Limited to ${currentLimit.label}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentLimit.isUnlimited) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        val next = SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(
                            context,
                            onPermissionRequired
                        )
                        if (next != null) {
                            onLimitSelected(next)
                        }
                    }
                ) {
                    Text("Cycle", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showDialog) {
        NetworkSpeedLimitDialog(
            currentLimit = currentLimit,
            onDismiss = { showDialog = false },
            onSelect = { selected ->
                val success = SettingsUtils.NetworkSpeed.setSpeedLimit(
                    context,
                    selected,
                    onPermissionRequired
                )
                if (success) {
                    onLimitSelected(selected)
                    showDialog = false
                }
            }
        )
    }
}

/**
 * Dialog displaying all available network speed limits with selectable radio options.
 */
@Composable
fun NetworkSpeedLimitDialog(
    currentLimit: NetworkSpeedLimit,
    onDismiss: () -> Unit,
    onSelect: (NetworkSpeedLimit) -> Unit
) {
    val context = LocalContext.current
    val limits = remember { NetworkSpeedLimitRepository.getAvailableLimits(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Download Speed Limit",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                limits.forEach { limit ->
                    val isSelected = if (currentLimit.isUnlimited) {
                        limit.isUnlimited
                    } else {
                        limit.bytesPerSecond == currentLimit.bytesPerSecond
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = { onSelect(limit) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = limit.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
