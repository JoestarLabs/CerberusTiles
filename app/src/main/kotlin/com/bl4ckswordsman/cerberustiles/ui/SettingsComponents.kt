package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.util.Ringer

/**
 * Parameters for the settings components.
 */
data class SettingsComponentsParams(
    val componentVisibilityParams: ComponentVisibility = ComponentVisibility(),
    val canWriteState: Boolean,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean,
    val isChargingOptimizationOn: Boolean,
    val setChargingOptimization: (Boolean) -> Unit,
    val toggleChargingOptimization: (Boolean) -> Unit,
    val sharedParams: SharedParams,
    val currentRingerMode: RingerMode,
    val onRingerModeChange: (RingerMode) -> Unit,
    val isOverlayContext: Boolean = false,
    val isSmartPixelsOn: Boolean = false,
    val toggleSmartPixels: ((Boolean) -> Unit)? = null,
    val currentNetworkSpeedLimit: NetworkSpeedLimit = NetworkSpeedLimit.NO_LIMIT,
    val onNetworkSpeedLimitChange: ((NetworkSpeedLimit) -> Unit)? = null,
    val isNetworkSpeedLimitSupported: Boolean = false,
    val onOpenAdbDialog: (() -> Unit)? = null
)

/**
 * A composable that shows settings components.
 */
@Composable
fun SettingsComponents(
    params: SettingsComponentsParams,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = if (params.isOverlayContext) 0.dp else 16.dp
    val verticalPadding = if (params.isOverlayContext) 0.dp else 12.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (params.componentVisibilityParams.adaptBrightnessSwitch) {
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
                label = if (params.isSwitchedOn) "Adaptive Brightness is ON" else "Adaptive Brightness is OFF",
                icon = {
                    Icon(
                        painter = painterResource(
                            if (params.isSwitchedOn) R.drawable.baseline_brightness_auto_24
                            else R.drawable.outline_brightness_auto_24
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )
        }

        if (params.componentVisibilityParams.chargingOptimizationSwitch) {
            SwitchWithLabel(
                isSwitchedOn = params.isChargingOptimizationOn,
                onCheckedChange = {
                    params.toggleChargingOptimization(it)
                },
                label = if (params.isChargingOptimizationOn) "Charging Optimization is ON" else "Charging Optimization is OFF",
                icon = {
                    Icon(
                        painter = painterResource(
                            if (params.isChargingOptimizationOn) R.drawable.outline_battery_android_frame_shield_24
                            else R.drawable.outline_battery_android_shield_24
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )
        }

        if (params.componentVisibilityParams.smartPixelsSwitch) {
            val context = LocalContext.current
            SwitchWithLabel(
                isSwitchedOn = params.isSmartPixelsOn,
                onCheckedChange = { checked ->
                    val hasPerm = SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context)
                    if (!hasPerm) {
                        SettingsUtils.SmartPixels.openAccessibilitySettings(context)
                    } else {
                        params.toggleSmartPixels?.invoke(checked)
                    }
                },
                label = if (params.isSmartPixelsOn) "Smart Pixels is ON" else "Smart Pixels is OFF",
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.rounded_grain_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )
        }

        if (params.componentVisibilityParams.brightnessSlider) {
            val context = LocalContext.current
            BrightnessPresetButtonGroup(
                context = context,
                isOverlayContext = params.isOverlayContext,
                canWriteState = params.canWriteState,
                isAdaptiveBrightnessOn = params.isSwitchedOn,
                openPermissionSettings = params.openPermissionSettings
            )
        }

        if (params.componentVisibilityParams.ringerModeSelector) {
            val context = LocalContext.current
            RingerModeSelectionButtonGroup(
                currentMode = params.currentRingerMode,
                isOverlayContext = params.isOverlayContext,
                onModeSelected = { newMode ->
                    RingerModeHandler(params, newMode, context).handleModeSelection()
                }
            )
        }

        if (params.componentVisibilityParams.networkSpeedLimitSelector && params.isNetworkSpeedLimitSupported) {
            NetworkSpeedLimitCard(
                currentLimit = params.currentNetworkSpeedLimit,
                onLimitSelected = { newLimit ->
                    params.onNetworkSpeedLimitChange?.invoke(newLimit)
                },
                onPermissionRequired = {
                    params.onOpenAdbDialog?.invoke()
                },
                isOverlayContext = params.isOverlayContext
            )
        }
    }
}

/**
 * A horizontally arranged group of connected toggle buttons for selecting the ringer mode.
 * Each button represents one [RingerMode] and is sized proportionally based on [isOverlayContext].
 *
 * @param currentMode The currently active [RingerMode], used to highlight the selected button.
 * @param modifier Optional [Modifier] applied to the outer [Row].
 * @param isOverlayContext When true, all buttons are equal width and icons are rendered smaller
 *   to fit the compact overlay layout.
 * @param onModeSelected Callback invoked with the newly selected [RingerMode] when the user
 *   taps a button that is not already selected.
 */
@Composable
private fun RingerModeSelectionButtonGroup(
    currentMode: RingerMode,
    modifier: Modifier = Modifier,
    isOverlayContext: Boolean = false,
    onModeSelected: (RingerMode) -> Unit
) {
    val modes = RingerMode.entries

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        modes.forEachIndexed { index, mode ->
            val isSelected = currentMode == mode

            val weightModifier = if (isOverlayContext) {
                Modifier.weight(1f)
            } else {
                when (mode) {
                    RingerMode.NORMAL -> Modifier.weight(1f)
                    RingerMode.VIBRATE -> Modifier.weight(1.2f)
                    RingerMode.SILENT -> Modifier.weight(1f)
                }
            }

            ToggleButton(
                checked = isSelected,
                onCheckedChange = {
                    if (!isSelected) onModeSelected(mode)
                },
                modifier = weightModifier.semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
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


/**
 * Triggers a ringer mode change for the given [mode] using [Ringer.setRingerMode].
 * Any exception is caught and logged without crashing.
 *
 * @param context The application context used to interact with the audio system.
 * @param mode The [RingerMode] to apply.
 * @param onSettled Callback invoked after [SettingsUtils.SettingsToggleParams.onSettingChanged]
 *   is called, indicating the mode change was accepted by the system.
 */
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
    /**
     * Handles the user selecting a new ringer mode.
     * If the app has write-settings permission the mode change is triggered;
     * otherwise the user is redirected to the permission settings screen.
     */
    fun handleModeSelection() {
        if (params.canWriteState) {
            handleModeChangeWithPermission()
        } else {
            params.openPermissionSettings()
        }
    }

    /**
     * Triggers the ringer mode change when the app has the required permission
     * and [newMode] differs from the currently active mode.
     * On success, updates vibration state and notifies [SettingsComponentsParams.onRingerModeChange].
     */
    private fun handleModeChangeWithPermission() {
        if (newMode != params.currentRingerMode) {
            triggerRingerModeChangeOnSelection(context, newMode) {
                updateVibrationMode()
                params.onRingerModeChange(newMode)
            }
        }
    }

    /**
     * Updates the vibration mode state to reflect the selected [newMode]:
     * sets vibration to true for [RingerMode.VIBRATE], false for all other modes.
     */
    private fun updateVibrationMode() {
        when (newMode) {
            RingerMode.VIBRATE -> params.setVibrationMode(true)
            RingerMode.NORMAL, RingerMode.SILENT -> params.setVibrationMode(false)
        }
    }
}


/**
 * Returns the appropriate painter resource for the given [mode] toggle button.
 * A filled icon is returned when [mode] matches [currentMode] (selected state);
 * an outlined icon is returned otherwise.
 *
 * @param mode The [RingerMode] this icon represents.
 * @param currentMode The currently active [RingerMode].
 */
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
