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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.SettingsUtils.openPermissionSettings
import com.bl4ckswordsman.cerberustiles.ShortcutHelper
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.MainScreen
import com.bl4ckswordsman.cerberustiles.ui.MainScreenParams
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialog
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialogParams
import com.bl4ckswordsman.cerberustiles.ui.createSharedParams
import com.bl4ckswordsman.cerberustiles.ui.theme.CustomTilesTheme
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

    // LiveData wrappers for MainScreenParams (which expects LiveData<Boolean>).
    // These are updated in the toggle callbacks and onResume so MainScreen
    // reacts to changes via observeAsState inside the composable.
    private val isAdaptiveLiveData = MutableLiveData(false)
    private val isVibrationModeLiveData = MutableLiveData(false)
    private val isChargingOptimizationLiveData = MutableLiveData(false)
    private val isChargingOptimizationSupportedLiveData = MutableLiveData(false)
    private val showAdbDialogLiveData = MutableLiveData(false)

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
     * Also syncs the LiveData wrappers used by [MainScreenParams].
     */
    override fun onResume() {
        super.onResume()
        viewModel.updateCanWrite(this)
        viewModel.updateIsSwitchedOn(this)
        viewModel.updateIsVibrationModeOn(this)
        viewModel.updateIsChargingOptimizationOn(this)
        viewModel.updateCurrentRingerMode(this)
        syncLiveDataWrappers()
    }

    /**
     * Copies the current [MainViewModel] [androidx.compose.runtime.MutableState] values into
     * the corresponding [MutableLiveData] wrappers so [MainScreen] can observe them.
     */
    private fun syncLiveDataWrappers() {
        isAdaptiveLiveData.value = viewModel.isSwitchedOn.value
        isVibrationModeLiveData.value = viewModel.isVibrationModeOn.value
        isChargingOptimizationLiveData.value = viewModel.isChargingOptimizationOn.value
        isChargingOptimizationSupportedLiveData.value =
            viewModel.isChargingOptimizationSupported.value
        showAdbDialogLiveData.value = viewModel.showAdbDialog.value
    }

    /**
     * Initialises the activity, registers it as a lifecycle observer, and sets up the Compose
     * content tree with [MainScreen] and [OverlayDialog].
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        setContent {
            val showOverlayDialog = rememberSaveable { mutableStateOf(false) }
            val currentRingerMode by viewModel.currentRingerMode.observeAsState(RingerMode.NORMAL)

            CustomTilesTheme {
                MainScreen(
                    MainScreenParams(
                        canWrite = viewModel.canWrite,
                        isAdaptive = isAdaptiveLiveData,
                        toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                        isVibrationMode = isVibrationModeLiveData,
                        toggleVibrationMode = ::toggleVibrationMode,
                        isChargingOptimization = isChargingOptimizationLiveData,
                        isChargingOptimizationSupported = isChargingOptimizationSupportedLiveData,
                        toggleChargingOptimization = ::toggleChargingOptimization,
                        showAdbDialog = showAdbDialogLiveData,
                        onAdbDialogDismiss = {
                            viewModel.showAdbDialog.value = false
                            showAdbDialogLiveData.value = false
                        },
                        openPermissionSettings = { openPermissionSettings(this) },
                        currentRingerMode = viewModel.currentRingerMode,
                        onRingerModeChange = { newMode ->
                            viewModel.currentRingerMode.value = newMode
                            viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                            isVibrationModeLiveData.value = newMode == RingerMode.VIBRATE
                        }
                    )
                )
            }
            OverlayDialog(
                OverlayDialogParams(
                    showDialog = showOverlayDialog,
                    onDismiss = { showOverlayDialog.value = false },
                    canWrite = viewModel.canWrite,
                    isSwitchedOn = viewModel.isSwitchedOn.value,
                    setSwitchedOn = { viewModel.isSwitchedOn.value = it },
                    toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                    openPermissionSettings = { openPermissionSettings(this) },
                    isVibrationModeOn = viewModel.isVibrationModeOn.value,
                    setVibrationMode = { viewModel.isVibrationModeOn.value = it },
                    toggleVibrationMode = ::toggleVibrationMode,
                    isChargingOptimizationOn = viewModel.isChargingOptimizationOn.value,
                    isChargingOptimizationSupported = viewModel.isChargingOptimizationSupported.value,
                    setChargingOptimization = { viewModel.isChargingOptimizationOn.value = it },
                    toggleChargingOptimization = ::toggleChargingOptimization,
                    showAdbDialog = viewModel.showAdbDialog.value,
                    onAdbDialogDismiss = { viewModel.showAdbDialog.value = false },
                    sharedParams = createSharedParams(),
                    currentRingerMode = currentRingerMode,
                    onRingerModeChange = { newMode ->
                        viewModel.currentRingerMode.value = newMode
                        viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                    }
                )
            )
        }
    }

    /**
     * Toggles adaptive brightness and updates [MainViewModel.isSwitchedOn] and
     * [isAdaptiveLiveData] via the settings changed callback.
     */
    private fun toggleAdaptiveBrightness() {
        val brightnessParams = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue ->
                viewModel.isSwitchedOn.value = newValue
                isAdaptiveLiveData.value = newValue
            }
        )
        SettingsUtils.Brightness.toggleAdaptiveBrightness(brightnessParams)
    }

    /**
     * Toggles vibration mode and updates [MainViewModel.isVibrationModeOn] and
     * [isVibrationModeLiveData] via the settings changed callback.
     * Returns true if the toggle succeeded.
     */
    private fun toggleVibrationMode(): Boolean {
        val vibrationParams = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue ->
                viewModel.isVibrationModeOn.value = newValue
                isVibrationModeLiveData.value = newValue
            }
        )
        return SettingsUtils.Vibration.toggleVibrationMode(vibrationParams)
    }

    /**
     * Sets charging optimization to [enabled] and updates [MainViewModel.isChargingOptimizationOn]
     * and [isChargingOptimizationLiveData].
     * If the WRITE_SECURE_SETTINGS permission is missing, [MainViewModel.showAdbDialog] and
     * [showAdbDialogLiveData] are set to true to prompt the user with ADB instructions.
     */
    private fun toggleChargingOptimization(enabled: Boolean) {
        val chargingParams = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue ->
                viewModel.isChargingOptimizationOn.value = newValue
                isChargingOptimizationLiveData.value = newValue
            },
            onPermissionDenied = {
                viewModel.showAdbDialog.value = true
                showAdbDialogLiveData.value = true
            }
        )
        SettingsUtils.Charging.setChargingOptimization(enabled, chargingParams)
    }
}
