package com.bl4ckswordsman.cerberustiles.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.view.WindowCompat
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.SettingsUtils.Brightness
import com.bl4ckswordsman.cerberustiles.SettingsUtils.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils.Vibration.toggleVibrationMode
import com.bl4ckswordsman.cerberustiles.SettingsUtils.openPermissionSettings
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialog
import com.bl4ckswordsman.cerberustiles.ui.OverlayDialogParams
import com.bl4ckswordsman.cerberustiles.ui.createSharedParams
import com.bl4ckswordsman.cerberustiles.util.Ringer

/**
 * A [ComponentActivity] that shows an overlay dialog with settings components.
 */
class OverlayActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val _currentRingerMode = mutableStateOf(RingerMode.NORMAL)

    override fun onResume() {
        super.onResume()
        updateViewModelState()
    }

    private fun updateViewModelState() {
        viewModel.updateCanWrite(this)
        viewModel.updateIsSwitchedOn(this)
        viewModel.updateIsVibrationModeOn(this)
        viewModel.updateIsChargingOptimizationOn(this)
        _currentRingerMode.value = Ringer.getCurrentRingerMode(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // window.statusBarColor = Color.TRANSPARENT // Deprecated in API 35
        // window.navigationBarColor = Color.TRANSPARENT
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContent {
            val showOverlayDialog = rememberSaveable { mutableStateOf(true) }
            val params = OverlayDialogParams(
                showDialog = showOverlayDialog,
                onDismiss = { finish() },
                canWrite = viewModel.canWrite,
                isSwitchedOn = viewModel.isSwitchedOn.value,
                setSwitchedOn = { viewModel.isSwitchedOn.value = it },
                toggleAdaptiveBrightness = {
                    val params = SettingsUtils.SettingsToggleParams(this) { newValue ->
                        viewModel.isSwitchedOn.value = newValue
                    }
                    Brightness.toggleAdaptiveBrightness(params)
                },
                openPermissionSettings = { openPermissionSettings(this) },
                isVibrationModeOn = viewModel.isVibrationModeOn.value,
                setVibrationMode = { viewModel.isVibrationModeOn.value = it },
                toggleVibrationMode = {
                    val params = SettingsUtils.SettingsToggleParams(this) { newValue ->
                        viewModel.isVibrationModeOn.value = newValue
                    }
                    toggleVibrationMode(params)
                },
                isChargingOptimizationOn = viewModel.isChargingOptimizationOn.value,
                setChargingOptimization = { viewModel.isChargingOptimizationOn.value = it },
                toggleChargingOptimization = {
                    val params = SettingsUtils.SettingsToggleParams(this) { newValue ->
                        viewModel.isChargingOptimizationOn.value = newValue
                    }
                    SettingsUtils.Charging.toggleChargingOptimization(params)
                },
                sharedParams = createSharedParams(),
                currentRingerMode = _currentRingerMode.value,
                onRingerModeChange = { newMode ->
                    _currentRingerMode.value = newMode
                    viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                }
            )
            OverlayDialog(params)
        }
    }
}
