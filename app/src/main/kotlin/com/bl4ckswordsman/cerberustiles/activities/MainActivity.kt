package com.bl4ckswordsman.cerberustiles.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.lifecycle.lifecycleScope
import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils.openPermissionSettings
import com.bl4ckswordsman.cerberustiles.ShortcutHelper
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.ui.AdbPermissionDialog
import com.bl4ckswordsman.cerberustiles.ui.HomeScreen
import com.bl4ckswordsman.cerberustiles.ui.HomeScreenParams
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme
import kotlinx.coroutines.launch

/**
 * Main activity of the app, hosting the Home screen and quick settings controls.
 *
 * All mutable settings state is owned by [MainViewModel], which is obtained via
 * `by viewModels()`.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val shortcutHelper by lazy { ShortcutHelper(this) }

    /**
     * Refreshes all ViewModel state values from the device settings each time the activity
     * resumes, ensuring the UI reflects any changes made while the app was in the background.
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshAll(this)
    }

    /**
     * Initialises the activity and sets up the Compose content tree with [HomeScreen].
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null && intent?.action == TileService.ACTION_QS_TILE_PREFERENCES) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            lifecycleScope.launch {
                shortcutHelper.createAllShortcuts()
            }
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
                            title = { Text(Constants.HOME_SCREEN) },
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
                                        contentDescription = Constants.SETTINGS_SCREEN
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
                            toggleAdaptiveBrightness = {
                                viewModel.toggleAdaptiveBrightness(this@MainActivity)
                            },
                            openPermissionSettings = { openPermissionSettings(this@MainActivity) },
                            isChargingOptimizationOn = viewModel.isChargingOptimizationOn.value,
                            toggleChargingOptimization = { enabled ->
                                viewModel.toggleChargingOptimization(this@MainActivity, enabled)
                            },
                            currentRingerMode = currentRingerMode,
                            onRingerModeChange = { newMode ->
                                viewModel.currentRingerMode.value = newMode
                                viewModel.isVibrationModeOn.value = newMode == RingerMode.VIBRATE
                            },
                            isSmartPixelsOn = viewModel.isSmartPixelsOn.value,
                            toggleSmartPixels = { enabled ->
                                viewModel.toggleSmartPixels(this@MainActivity, enabled)
                            },
                            currentNetworkSpeedLimit = viewModel.currentNetworkSpeedLimit.value,
                            onNetworkSpeedLimitChange = { newLimit ->
                                viewModel.currentNetworkSpeedLimit.value = newLimit
                            },
                            isNetworkSpeedLimitSupported = viewModel.isNetworkSpeedLimitSupported.value,
                            onOpenAdbDialog = { viewModel.showAdbDialog.value = true }
                        )
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == TileService.ACTION_QS_TILE_PREFERENCES) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
