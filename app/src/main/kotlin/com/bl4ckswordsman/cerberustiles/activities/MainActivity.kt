package com.bl4ckswordsman.cerberustiles.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.SettingsUtils.openPermissionSettings
import com.bl4ckswordsman.cerberustiles.ShortcutHelper
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.AdbPermissionDialog
import com.bl4ckswordsman.cerberustiles.ui.HomeScreen
import com.bl4ckswordsman.cerberustiles.ui.HomeScreenParams
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme
import kotlinx.coroutines.launch
import com.bl4ckswordsman.cerberustiles.Constants as label

/**
 * Main activity of the app, hosting the Home screen and quick settings controls.
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
     * content tree with [HomeScreen].
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)

        if (savedInstanceState == null && intent?.action == "android.service.quicksettings.action.QS_TILE_PREFERENCES") {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        setContent {
            val canWrite by viewModel.canWrite.observeAsState(initial = false)
            val currentRingerMode by viewModel.currentRingerMode.observeAsState(RingerMode.NORMAL)

            CerberusTilesTheme {
                if (viewModel.showAdbDialog.value) {
                    AdbPermissionDialog(
                        context = this@MainActivity,
                        onDismiss = { viewModel.showAdbDialog.value = false }
                    )
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(),
                            title = { Text(label.HOME_SCREEN) },
                            actions = {
                                IconButton(onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            SettingsActivity::class.java
                                        )
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = label.SETTINGS_SCREEN
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(
                        HomeScreenParams(
                            innerPadding = innerPadding,
                            canWriteState = canWrite,
                            isSwitchedOn = viewModel.isAdaptiveBrightnessOn.value,
                            setSwitchedOn = { viewModel.isAdaptiveBrightnessOn.value = it },
                            toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                            openPermissionSettings = { openPermissionSettings(this@MainActivity) },
                            isVibrationModeOn = viewModel.isVibrationModeOn.value,
                            setVibrationMode = { viewModel.isVibrationModeOn.value = it },
                            toggleVibrationMode = ::toggleVibrationMode,
                            isChargingOptimizationOn = viewModel.isChargingOptimizationOn.value,
                            setChargingOptimization = {},
                            toggleChargingOptimization = ::toggleChargingOptimization,
                            currentRingerMode = currentRingerMode,
                            onRingerModeChange = { newMode ->
                                viewModel.currentRingerMode.value = newMode
                                viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                            },
                            isSmartPixelsOn = viewModel.isSmartPixelsOn.value,
                            toggleSmartPixels = ::toggleSmartPixels
                        )
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == "android.service.quicksettings.action.QS_TILE_PREFERENCES") {
            startActivity(Intent(this, SettingsActivity::class.java))
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
