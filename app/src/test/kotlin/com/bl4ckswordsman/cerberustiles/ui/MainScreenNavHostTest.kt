package com.bl4ckswordsman.cerberustiles.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import com.bl4ckswordsman.cerberustiles.navbar.Screen
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenNavHostTest {

    @Test
    fun `toHomeScreenParams maps all fields correctly`() {
        val navHostParams = MainScreenNavHostParams(
            navController = mockk(),
            innerPadding = PaddingValues(16.dp),
            canWriteState = true,
            isSwitchedOn = true,
            setSwitchedOn = {},
            toggleAdaptiveBrightness = {},
            openPermissionSettings = {},
            isVibrationModeOn = true,
            setVibrationMode = {},
            toggleVibrationMode = { true },
            isChargingOptimizationOn = true,
            isChargingOptimizationSupported = true,
            setChargingOptimization = {},
            toggleChargingOptimization = {},
            currentRingerMode = MutableLiveData(RingerMode.NORMAL),
            onRingerModeChange = {},
            isSmartPixelsOn = true,
            toggleSmartPixels = {}
        )

        val homeParams = navHostParams.toHomeScreenParams(RingerMode.VIBRATE)

        assertEquals(PaddingValues(16.dp), homeParams.innerPadding)
        assertTrue(homeParams.canWriteState)
        assertTrue(homeParams.isSwitchedOn)
        assertTrue(homeParams.isVibrationModeOn)
        assertTrue(homeParams.isChargingOptimizationOn)
        assertTrue(homeParams.isSmartPixelsOn)
        assertEquals(RingerMode.VIBRATE, homeParams.currentRingerMode)
    }

    @Test
    fun `toNavHostParams maps all scaffold fields correctly`() {
        val scaffoldParams = MainScreenScaffoldParams(
            navController = mockk(),
            selectedScreen = Screen.Home,
            canWriteState = true,
            isSwitchedOn = false,
            setSwitchedOn = {},
            toggleAdaptiveBrightness = {},
            openPermissionSettings = {},
            isVibrationModeOn = false,
            setVibrationMode = {},
            toggleVibrationMode = { false },
            isChargingOptimizationOn = true,
            isChargingOptimizationSupported = true,
            setChargingOptimization = {},
            toggleChargingOptimization = {},
            currentRingerMode = MutableLiveData(RingerMode.SILENT),
            onRingerModeChange = {},
            isSmartPixelsOn = true,
            toggleSmartPixels = {}
        )

        val navHostParams = scaffoldParams.toNavHostParams(PaddingValues(8.dp))

        assertEquals(PaddingValues(8.dp), navHostParams.innerPadding)
        assertTrue(navHostParams.canWriteState)
        assertTrue(navHostParams.isChargingOptimizationOn)
        assertTrue(navHostParams.isSmartPixelsOn)
    }
}
