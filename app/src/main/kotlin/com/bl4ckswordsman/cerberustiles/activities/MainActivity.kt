package com.bl4ckswordsman.cerberustiles.activities

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
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
import com.bl4ckswordsman.cerberustiles.util.Ringer
import kotlinx.coroutines.launch

/** Main activity of the app. */
@RequiresApi(Build.VERSION_CODES.N_MR1)
class MainActivity : ComponentActivity(), LifecycleObserver {
    /** TODO: Use viewModelScope for LiveData instead of using MutableLiveData, this ensures that
     * the LiveData is cleared when the ViewModel is cleared.**/


    private val shortcutHelper by lazy { ShortcutHelper(this) }

    private val _canWrite = MutableLiveData<Boolean>()
    private val canWrite: LiveData<Boolean> get() = _canWrite

    private val _isAdaptive = MutableLiveData<Boolean>()
    private val isAdaptive: LiveData<Boolean> get() = _isAdaptive
    private val _isVibrationMode = MutableLiveData<Boolean>()
    private val isVibrationMode: LiveData<Boolean> get() = _isVibrationMode
    private val _isChargingOptimization = MutableLiveData<Boolean>()
    private val isChargingOptimization: LiveData<Boolean> get() = _isChargingOptimization
    private val _showAdbDialog = MutableLiveData<Boolean>()
    private val showAdbDialog: LiveData<Boolean> get() = _showAdbDialog
    private val _currentRingerMode = MutableLiveData<RingerMode>()
    val currentRingerMode: LiveData<RingerMode> get() = _currentRingerMode


    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            shortcutHelper.createAllShortcuts()
        }
    }

    override fun onResume() {
        super.onResume()
        _canWrite.value = Settings.System.canWrite(this)
        _isAdaptive.value = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(this)
        _isVibrationMode.value = SettingsUtils.Vibration.isVibrationModeEnabled(this)
        _isChargingOptimization.value = SettingsUtils.Charging.isChargingOptimizationEnabled(this)
        val currentMode = Ringer.getCurrentRingerMode(this)
        println("Debug - MainActivity onResume current mode: $currentMode")
        _currentRingerMode.value = currentMode
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        setContent {
            val showOverlayDialog = rememberSaveable { mutableStateOf(false) }
            CustomTilesTheme {
                MainScreen(
                    MainScreenParams(canWrite = canWrite,
                        isAdaptive = isAdaptive,
                        toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                        isVibrationMode = isVibrationMode,
                        toggleVibrationMode = ::toggleVibrationMode,
                        isChargingOptimization = isChargingOptimization,
                        toggleChargingOptimization = ::toggleChargingOptimization,
                        showAdbDialog = showAdbDialog,
                        onAdbDialogDismiss = { _showAdbDialog.value = false },
                        openPermissionSettings = { openPermissionSettings(this) },
                        currentRingerMode = currentRingerMode,
                        onRingerModeChange = { newMode ->
                            _currentRingerMode.value = newMode
                            _isVibrationMode.value = newMode == RingerMode.VIBRATE
                        }
                    )
                )
            }
            val params = OverlayDialogParams(
                showDialog = showOverlayDialog,
                onDismiss = { showOverlayDialog.value = false },
                canWrite = _canWrite,
                isSwitchedOn = _isAdaptive.value == true,
                setSwitchedOn = { _isAdaptive.value = it },
                toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                openPermissionSettings = { openPermissionSettings(this) },
                isVibrationModeOn = _isVibrationMode.value == true,
                setVibrationMode = { _isVibrationMode.value = it },
                toggleVibrationMode = ::toggleVibrationMode,
                isChargingOptimizationOn = _isChargingOptimization.value == true,
                setChargingOptimization = { _isChargingOptimization.value = it },
                toggleChargingOptimization = ::toggleChargingOptimization,
                showAdbDialog = _showAdbDialog.value == true,
                onAdbDialogDismiss = { _showAdbDialog.value = false },
                sharedParams = createSharedParams(),
                currentRingerMode = _currentRingerMode.value ?: RingerMode.NORMAL,
                onRingerModeChange = { newMode ->
                    println("Debug - MainActivity onRingerModeChange: $newMode")
                    _currentRingerMode.value = newMode
                    _isVibrationMode.value = newMode == RingerMode.VIBRATE
                }
            )
            OverlayDialog(params)
        }
    }

    private fun toggleAdaptiveBrightness() {
        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue -> _isAdaptive.value = newValue }
        )
        SettingsUtils.Brightness.toggleAdaptiveBrightness(params)
    }

    private fun toggleVibrationMode(): Boolean {
        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue -> _isVibrationMode.value = newValue }
        )
        return SettingsUtils.Vibration.toggleVibrationMode(params)
    }

    private fun toggleChargingOptimization() {
        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue -> _isChargingOptimization.value = newValue },
            onPermissionDenied = { _showAdbDialog.value = true }
        )
        SettingsUtils.Charging.toggleChargingOptimization(params)
    }
}
