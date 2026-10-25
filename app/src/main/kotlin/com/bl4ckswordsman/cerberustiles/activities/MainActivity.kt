package com.bl4ckswordsman.cerberustiles.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.SettingsUtils.openPermissionSettings
import com.bl4ckswordsman.cerberustiles.ShortcutHelper
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.MainScreen
import com.bl4ckswordsman.cerberustiles.ui.MainScreenParams
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme
import kotlinx.coroutines.launch

/**
 * Main activity of the app.
 *
 * All mutable settings state is owned by [MainViewModel], which is obtained via
 * `by viewModels()`. This keeps state consistent with [OverlayActivity], which
 * uses the same ViewModel class (each activity has its own instance because they
 * run in separate tasks, but both follow the identical pattern and field set).
 */
@RequiresApi(Build.VERSION_CODES.N_MR1)
class MainActivity : ComponentActivity(), LifecycleObserver {

    private val viewModel: MainViewModel by viewModels()
    private val shortcutHelper by lazy { ShortcutHelper(this) }

    /**
     * Creates all app shortcuts via [ShortcutHelper] when the activity becomes visible.
     */
    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            shortcutHelper.createAllShortcuts()
        }
    }

    /**
     * Refreshes all ViewModel state values from the device settings each time the activity
     * resumes, ensuring the UI reflects any changes made while the app was in the background.
     */
    override fun onResume() {
        super.onResume()
        viewModel.updateCanWrite(this)
        viewModel.updateIsAdaptiveBrightnessOn(this)
        viewModel.updateIsVibrationModeOn(this)
        viewModel.updateIsChargingOptimizationOn(this)
        viewModel.updateCurrentRingerMode(this)
        viewModel.updateSmartPixelsState(this)
    }

    /**
     * Initialises the activity, registers it as a lifecycle observer, and sets up the Compose
     * content tree with [MainScreen].
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        setContent {
            val canWrite by viewModel.canWrite.observeAsState(initial = false)
            val currentRingerMode by viewModel.currentRingerMode.observeAsState(RingerMode.NORMAL)

            CerberusTilesTheme {
                MainScreen(
                    MainScreenParams(
                        canWrite = canWrite,
                        isAdaptive = viewModel.isAdaptiveBrightnessOn.value,
                        setAdaptive = { viewModel.isAdaptiveBrightnessOn.value = it },
                        toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                        isVibrationMode = viewModel.isVibrationModeOn.value,
                        setVibrationMode = { viewModel.isVibrationModeOn.value = it },
                        toggleVibrationMode = ::toggleVibrationMode,
                        isChargingOptimization = viewModel.isChargingOptimizationOn.value,
                        isChargingOptimizationSupported = viewModel.isChargingOptimizationSupported.value,
                        toggleChargingOptimization = ::toggleChargingOptimization,
                        showAdbDialog = viewModel.showAdbDialog.value,
                        onAdbDialogDismiss = {
                            viewModel.showAdbDialog.value = false
                        },
                        openPermissionSettings = { openPermissionSettings(this) },
                        currentRingerMode = currentRingerMode,
                        onRingerModeChange = { newMode ->
                            viewModel.currentRingerMode.value = newMode
                            viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                        },
                        isSmartPixels = viewModel.isSmartPixelsOn.value,
                        toggleSmartPixels = ::toggleSmartPixels,
                        openSettings = intent?.action == "android.service.quicksettings.action.QS_TILE_PREFERENCES"
                    )
                )
            }
        }
    }

    /**
     * Toggles the Smart Pixels feature state, persists the setting, and updates the ViewModel.
     */
    private fun toggleSmartPixels(enabled: Boolean) {
        SettingsUtils.SmartPixels.setSmartPixelsEnabled(this, enabled)
        viewModel.isSmartPixelsOn.value = enabled
    }

    /**
     * Toggles adaptive brightness and updates [MainViewModel.isAdaptiveBrightnessOn] via the
     * settings changed callback.
     */
    private fun toggleAdaptiveBrightness() {
        val brightnessParams = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue ->
                viewModel.isAdaptiveBrightnessOn.value = newValue
            }
        )
        SettingsUtils.Brightness.toggleAdaptiveBrightness(brightnessParams)
    }

    /**
     * Toggles vibration mode and updates [MainViewModel.isVibrationModeOn] via the settings
     * changed callback. Returns true if the toggle succeeded.
     */
    private fun toggleVibrationMode(): Boolean {
        val vibrationParams = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue ->
                viewModel.isVibrationModeOn.value = newValue
            }
        )
        return SettingsUtils.Vibration.toggleVibrationMode(vibrationParams)
    }

    /**
     * Sets charging optimization to [enabled] and updates [MainViewModel.isChargingOptimizationOn].
     * If the WRITE_SECURE_SETTINGS permission is missing, [MainViewModel.showAdbDialog] is set to
     * true to prompt the user with ADB instructions.
     */
    private fun toggleChargingOptimization(enabled: Boolean) {
        val chargingParams = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue ->
                viewModel.isChargingOptimizationOn.value = newValue
            },
            onPermissionDenied = {
                viewModel.showAdbDialog.value = true
            }
        )
        SettingsUtils.Charging.setChargingOptimization(enabled, chargingParams)
    }
}
