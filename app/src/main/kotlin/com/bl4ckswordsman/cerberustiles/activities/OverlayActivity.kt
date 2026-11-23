package com.bl4ckswordsman.cerberustiles.activities

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils.openPermissionSettings
import com.bl4ckswordsman.cerberustiles.ShortcutHelper
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.AdbPermissionDialog
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialog
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialogParams

/**
 * A [ComponentActivity] that shows an overlay dialog with settings components.
 *
 * State is owned by [MainViewModel], following the same pattern as [MainActivity].
 */
class OverlayActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    /**
     * Refreshes the ViewModel state from current device settings each time the overlay resumes.
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshAll(this)
    }

    /**
     * Initialises the overlay activity with a transparent window background and sets up the
     * Compose content tree with [OverlayDialog], wiring all settings toggles to the ViewModel.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (intent?.action == Constants.OPEN_OVERLAY_ACTION) {
            ShortcutHelper.reportUsage(this, ShortcutHelper.ID_OVERLAY)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        setContent {
            val showOverlayDialog = rememberSaveable { mutableStateOf(true) }
            val currentRingerMode by viewModel.currentRingerMode.observeAsState(RingerMode.NORMAL)
            val canWrite by viewModel.canWrite.observeAsState(initial = false)

            if (viewModel.showAdbDialog.value) {
                AdbPermissionDialog(
                    context = this,
                    onDismiss = { viewModel.showAdbDialog.value = false }
                )
            }

            val params = OverlayDialogParams(
                showDialog = showOverlayDialog,
                onDismiss = { finish() },
                canWrite = canWrite,
                isSwitchedOn = viewModel.isAdaptiveBrightnessOn.value,
                setSwitchedOn = { viewModel.isAdaptiveBrightnessOn.value = it },
                toggleAdaptiveBrightness = { viewModel.toggleAdaptiveBrightness(this) },
                openPermissionSettings = { openPermissionSettings(this) },
                isChargingOptimizationOn = viewModel.isChargingOptimizationOn.value,
                toggleChargingOptimization = { enabled ->
                    viewModel.toggleChargingOptimization(this, enabled)
                },
                sharedPreferences = getSharedPreferences(Constants.SETTINGS_PREFS_NAME, MODE_PRIVATE),
                currentRingerMode = currentRingerMode,
                onRingerModeChange = { newMode ->
                    viewModel.currentRingerMode.value = newMode
                    viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                },
                isSmartPixelsOn = viewModel.isSmartPixelsOn.value,
                toggleSmartPixels = { enabled -> viewModel.toggleSmartPixels(this, enabled) },
                currentNetworkSpeedLimit = viewModel.currentNetworkSpeedLimit.value,
                onNetworkSpeedLimitChange = { newLimit ->
                    viewModel.currentNetworkSpeedLimit.value = newLimit
                },
                isNetworkSpeedLimitSupported = viewModel.isNetworkSpeedLimitSupported.value,
                onOpenAdbDialog = { viewModel.showAdbDialog.value = true }
            )
            OverlayDialog(params)
        }
    }
}
