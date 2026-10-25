package com.bl4ckswordsman.cerberustiles.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.navbar.BottomNavBar
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.topBarIconEnterTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.topBarIconExitTransition
import com.bl4ckswordsman.cerberustiles.navbar.NavTransitions.topBarTitleTransition
import com.bl4ckswordsman.cerberustiles.navbar.Screen
import com.bl4ckswordsman.cerberustiles.Constants as label

/**
 * Parameters for the main screen scaffold.
 */
data class MainScreenScaffoldParams(
    val navController: NavHostController,
    val selectedScreen: Screen,
    val canWriteState: Boolean,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean,
    val isChargingOptimizationOn: Boolean,
    val isChargingOptimizationSupported: Boolean,
    val setChargingOptimization: (Boolean) -> Unit,
    val toggleChargingOptimization: (Boolean) -> Unit,
    val currentRingerMode: LiveData<RingerMode>,
    val onRingerModeChange: (RingerMode) -> Unit,
    val isSmartPixelsOn: Boolean = false,
    val toggleSmartPixels: ((Boolean) -> Unit)? = null
)

/**
 * Parameters for the main screen.
 */
data class MainScreenParams(
    val canWrite: LiveData<Boolean>,
    val isAdaptive: LiveData<Boolean>,
    val toggleAdaptiveBrightness: () -> Unit,
    val isVibrationMode: LiveData<Boolean>,
    val toggleVibrationMode: () -> Boolean,
    val isChargingOptimization: LiveData<Boolean>,
    val isChargingOptimizationSupported: LiveData<Boolean>,
    val toggleChargingOptimization: (Boolean) -> Unit,
    val showAdbDialog: LiveData<Boolean>,
    val onAdbDialogDismiss: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val currentRingerMode: LiveData<RingerMode>,
    val onRingerModeChange: (RingerMode) -> Unit,
    val isSmartPixels: LiveData<Boolean>? = null,
    val toggleSmartPixels: ((Boolean) -> Unit)? = null,
    val openSettings: Boolean = false
)

/**
 * Converts [MainScreenScaffoldParams] to [MainScreenNavHostParams].
 */
fun MainScreenScaffoldParams.toNavHostParams(
    innerPadding: androidx.compose.foundation.layout.PaddingValues
): MainScreenNavHostParams = MainScreenNavHostParams(
    navController = navController,
    innerPadding = innerPadding,
    canWriteState = canWriteState,
    isSwitchedOn = isSwitchedOn,
    setSwitchedOn = setSwitchedOn,
    toggleAdaptiveBrightness = toggleAdaptiveBrightness,
    openPermissionSettings = openPermissionSettings,
    isVibrationModeOn = isVibrationModeOn,
    setVibrationMode = setVibrationMode,
    toggleVibrationMode = toggleVibrationMode,
    isChargingOptimizationOn = isChargingOptimizationOn,
    isChargingOptimizationSupported = isChargingOptimizationSupported,
    setChargingOptimization = setChargingOptimization,
    toggleChargingOptimization = toggleChargingOptimization,
    currentRingerMode = currentRingerMode,
    onRingerModeChange = onRingerModeChange,
    isSmartPixelsOn = isSmartPixelsOn,
    toggleSmartPixels = toggleSmartPixels
)

/**
 * The main screen scaffold that contains the top bar, bottom bar, and navigation host.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreenScaffold(params: MainScreenScaffoldParams) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(),
                title = {
                    AnimatedContent(
                        targetState = params.selectedScreen,
                        transitionSpec = { topBarTitleTransition() },
                        label = "TopAppBarTitleAnim"
                    ) { screen ->
                        Text(
                            text = when (screen) {
                                is Screen.Home -> label.HOME_SCREEN
                                is Screen.Settings -> label.SETTINGS_SCREEN
                                is Screen.Licenses -> "Open Source Licenses"
                                is Screen.Permissions -> "Permissions & Privacy"
                            }
                        )
                    }
                },
                navigationIcon = {
                    AnimatedVisibility(
                        visible = params.selectedScreen is Screen.Licenses || params.selectedScreen is Screen.Permissions,
                        enter = topBarIconEnterTransition(),
                        exit = topBarIconExitTransition()
                    ) {
                        IconButton(onClick = { params.navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(params.navController) }
    ) { innerPadding ->
        MainScreenNavHost(params.toNavHostParams(innerPadding))
    }
}

/**
 * The main entry point composable for Cerberus Tiles UI.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreen(params: MainScreenParams) {
    val navController = rememberNavController()
    val canWriteState by params.canWrite.observeAsState(initial = false)
    val isAdaptiveState by params.isAdaptive.observeAsState(initial = false)
    val isVibrationModeState by params.isVibrationMode.observeAsState(initial = false)
    val isChargingOptimizationState by params.isChargingOptimization.observeAsState(initial = false)
    val isChargingOptimizationSupportedState by params.isChargingOptimizationSupported.observeAsState(
        initial = false
    )
    val isSmartPixelsState by (params.isSmartPixels?.observeAsState(initial = false)
        ?: rememberSaveable { mutableStateOf(false) })

    val (isSwitchedOn, setSwitchedOn) = rememberSaveable { mutableStateOf(isAdaptiveState) }
    val (isVibrationModeOn, setVibrationMode) = rememberSaveable {
        mutableStateOf(isVibrationModeState)
    }

    LaunchedEffect(isAdaptiveState) {
        setSwitchedOn(isAdaptiveState)
    }
    LaunchedEffect(isVibrationModeState) {
        setVibrationMode(isVibrationModeState)
    }
    LaunchedEffect(params.openSettings) {
        if (params.openSettings) {
            navController.navigate(Screen.Settings.route)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedScreen = when (navBackStackEntry?.destination?.route) {
        Screen.Home.route -> Screen.Home
        Screen.Settings.route -> Screen.Settings
        Screen.Licenses.route -> Screen.Licenses
        Screen.Permissions.route -> Screen.Permissions
        else -> Screen.Home
    }

    if (params.showAdbDialog.observeAsState(initial = false).value) {
        AdbPermissionDialog(
            context = LocalContext.current,
            onDismiss = params.onAdbDialogDismiss
        )
    }

    MainScreenScaffold(
        MainScreenScaffoldParams(
            navController = navController,
            selectedScreen = selectedScreen,
            canWriteState = canWriteState,
            isSwitchedOn = isSwitchedOn,
            setSwitchedOn = setSwitchedOn,
            toggleAdaptiveBrightness = params.toggleAdaptiveBrightness,
            openPermissionSettings = params.openPermissionSettings,
            isVibrationModeOn = isVibrationModeOn,
            setVibrationMode = setVibrationMode,
            toggleVibrationMode = params.toggleVibrationMode,
            isChargingOptimizationOn = isChargingOptimizationState,
            isChargingOptimizationSupported = isChargingOptimizationSupportedState,
            setChargingOptimization = {},
            toggleChargingOptimization = params.toggleChargingOptimization,
            currentRingerMode = params.currentRingerMode,
            onRingerModeChange = params.onRingerModeChange,
            isSmartPixelsOn = isSmartPixelsState,
            toggleSmartPixels = params.toggleSmartPixels
        )
    )
}

/**
 * A preview of the main screen.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        MainScreenParams(
            canWrite = MutableLiveData(true),
            isAdaptive = MutableLiveData(true),
            toggleAdaptiveBrightness = {},
            isVibrationMode = MutableLiveData(true),
            toggleVibrationMode = { true },
            isChargingOptimization = MutableLiveData(true),
            isChargingOptimizationSupported = MutableLiveData(true),
            toggleChargingOptimization = {},
            showAdbDialog = MutableLiveData(false),
            onAdbDialogDismiss = {},
            openPermissionSettings = {},
            currentRingerMode = MutableLiveData(RingerMode.NORMAL),
            onRingerModeChange = {}
        )
    )
}
