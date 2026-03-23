package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.util.Ringer

/**
 * Parameters for controlling the visibility of settings components in the dialog.
 */
data class ComponentVisibilityDialogParams(
    val adaptBrightnessSwitch: MutableState<Boolean>,
    val brightnessSlider: MutableState<Boolean>,
    val ringerModeSelector: MutableState<Boolean>
)

/**
 * Parameters for the settings components.
 */
data class SettingsComponentsParams(
    val componentVisibilityParams: ComponentVisibilityDialogParams,
    val canWriteState: Boolean,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean,
    val sharedParams: SharedParams,
    val currentRingerMode: RingerMode,
    val onRingerModeChange: (RingerMode) -> Unit,
    val isOverlayContext: Boolean = false
)

/**
 * A composable that shows settings components.
 */
@Composable
fun SettingsComponents(params: SettingsComponentsParams) {
    if (params.componentVisibilityParams.adaptBrightnessSwitch.value) {
        SwitchWithLabel(
            isSwitchedOn = params.isSwitchedOn,
            onCheckedChange = {
                params.setSwitchedOn(it)
                if (params.canWriteState) {
                    params.toggleAdaptiveBrightness()
                } else {
                    params.openPermissionSettings()
                }
            },
            label = if (params.isSwitchedOn) "Adaptive Brightness is ON" else "Adaptive Brightness is OFF"
        )
    }

    if (params.componentVisibilityParams.brightnessSlider.value) {
        BrightnessSlider(context = LocalContext.current)
    }

    if (params.componentVisibilityParams.ringerModeSelector.value) {
        val context = LocalContext.current
        RingerModeSelectionButtonGroup(
            currentMode = params.currentRingerMode,
            isOverlayContext = params.isOverlayContext,
            onModeSelected = { newMode ->
                RingerModeHandler(params, newMode, context).handleModeSelection()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RingerModeSelectionButtonGroup(
    currentMode: RingerMode,
    isOverlayContext: Boolean = false,
    onModeSelected: (RingerMode) -> Unit
) {
    val modes = RingerMode.entries

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        modes.forEachIndexed { index, mode ->
            val isSelected = currentMode == mode

            val weightModifier = if (isOverlayContext) {
                Modifier.weight(1f)
            } else {
                when (mode) {
                    RingerMode.NORMAL  -> Modifier.weight(1f)
                    RingerMode.VIBRATE -> Modifier.weight(1.2f)
                    RingerMode.SILENT  -> Modifier.weight(1f)
                }
            }

            ToggleButton(
                checked = isSelected,
                onCheckedChange = {
                    if (!isSelected) onModeSelected(mode)
                },
                modifier = weightModifier.semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0               -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else            -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Icon(
                    painter = getIconForMode(mode, currentMode),
                    contentDescription = mode.name,
                    modifier = if (isOverlayContext)
                        Modifier.size(16.dp)
                    else
                        Modifier.size(ToggleButtonDefaults.IconSize)
                )
                if (!isOverlayContext) {
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }
    }
}


private fun triggerRingerModeChangeOnSelection(
    context: android.content.Context,
    mode: RingerMode,
    onSettled: () -> Unit
) {
    runCatching {
        Ringer.setRingerMode(
            SettingsUtils.SettingsToggleParams(
                context = context,
                onSettingChanged = { _ -> onSettled() }
            ),
            mode
        )
    }.onFailure { e ->
        println("Error changing mode: ${e.message}")
    }
}

/**
 * Single authority for permission checks, ringer mode changes, and state updates.
 */
private class RingerModeHandler(
    private val params: SettingsComponentsParams,
    private val newMode: RingerMode,
    private val context: android.content.Context
) {
    fun handleModeSelection() {
        if (params.canWriteState) {
            handleModeChangeWithPermission()
        } else {
            params.openPermissionSettings()
        }
    }

    private fun handleModeChangeWithPermission() {
        if (newMode != params.currentRingerMode) {
            triggerRingerModeChangeOnSelection(context, newMode) {
                updateVibrationMode()
                params.onRingerModeChange(newMode)
            }
        }
    }

    private fun updateVibrationMode() {
        when (newMode) {
            RingerMode.VIBRATE -> params.setVibrationMode(true)
            RingerMode.NORMAL, RingerMode.SILENT -> params.setVibrationMode(false)
        }
    }
}


@Composable
private fun getIconForMode(mode: RingerMode, currentMode: RingerMode) = when (mode) {
    RingerMode.NORMAL -> if (currentMode == mode)
        painterResource(R.drawable.baseline_volume_up_24)
    else
        painterResource(R.drawable.outline_volume_up_24)

    RingerMode.SILENT -> if (currentMode == mode)
        painterResource(R.drawable.baseline_volume_off_24)
    else
        painterResource(R.drawable.outline_volume_off_24)

    RingerMode.VIBRATE -> if (currentMode == mode)
        painterResource(R.drawable.twotone_vibration_24)
    else
        painterResource(R.drawable.baseline_vibration_24)
}
