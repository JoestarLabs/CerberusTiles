package com.bl4ckswordsman.cerberustiles.util

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import com.bl4ckswordsman.cerberustiles.models.RingerMode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var contentResolver: ContentResolver

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.contentResolver } returns contentResolver
        mockkStatic(Settings.Secure::class)
        mockkObject(SettingsUtils)
        mockkObject(SettingsUtils.Charging)
        mockkObject(SettingsUtils.Brightness)
        mockkObject(SettingsUtils.Vibration)
        mockkObject(SettingsUtils.SmartPixels)
        mockkObject(SettingsUtils.NetworkSpeed)
        mockkObject(Ringer)
        viewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initial isChargingOptimizationOn state is false`() {
        assertFalse(
            "Expected isChargingOptimizationOn initial value to be false",
            viewModel.isChargingOptimizationOn.value
        )
    }

    @Test
    fun `initial isChargingOptimizationSupported state is false`() {
        assertFalse(
            "Expected isChargingOptimizationSupported initial value to be false",
            viewModel.isChargingOptimizationSupported.value
        )
    }

    @Test
    fun `initial showAdbDialog state is false`() {
        assertFalse(
            "Expected showAdbDialog initial value to be false", viewModel.showAdbDialog.value
        )
    }

    @Test
    fun `updateIsChargingOptimizationOn sets supported to false when device does not support it`() {
        every { SettingsUtils.Charging.isChargingOptimizationSupported(context) } returns false

        viewModel.updateIsChargingOptimizationOn(context)

        assertFalse(
            "Expected isChargingOptimizationSupported to be false",
            viewModel.isChargingOptimizationSupported.value
        )
    }

    @Test
    fun `updateIsChargingOptimizationOn does not update isChargingOptimizationOn when not supported`() {
        every { SettingsUtils.Charging.isChargingOptimizationSupported(context) } returns false
        // Even if the method were called, it would return true — but it should not be called
        every { SettingsUtils.Charging.isChargingOptimizationEnabled(context) } returns true

        viewModel.updateIsChargingOptimizationOn(context)

        assertFalse(
            "Expected isChargingOptimizationOn to remain false when not supported",
            viewModel.isChargingOptimizationOn.value
        )
        verify(exactly = 0) {
            SettingsUtils.Charging.isChargingOptimizationEnabled(context)
        }
    }

    @Test
    fun `updateIsChargingOptimizationOn sets supported to true and on to true when supported and enabled`() {
        every { SettingsUtils.Charging.isChargingOptimizationSupported(context) } returns true
        every { SettingsUtils.Charging.isChargingOptimizationEnabled(context) } returns true

        viewModel.updateIsChargingOptimizationOn(context)

        assertTrue(
            "Expected isChargingOptimizationSupported to be true",
            viewModel.isChargingOptimizationSupported.value
        )
        assertTrue(
            "Expected isChargingOptimizationOn to be true", viewModel.isChargingOptimizationOn.value
        )
    }

    @Test
    fun `updateIsChargingOptimizationOn sets supported to true and on to false when supported but disabled`() {
        every { SettingsUtils.Charging.isChargingOptimizationSupported(context) } returns true
        every { SettingsUtils.Charging.isChargingOptimizationEnabled(context) } returns false

        viewModel.updateIsChargingOptimizationOn(context)

        assertTrue(
            "Expected isChargingOptimizationSupported to be true",
            viewModel.isChargingOptimizationSupported.value
        )
        assertFalse(
            "Expected isChargingOptimizationOn to be false when disabled",
            viewModel.isChargingOptimizationOn.value
        )
    }

    @Test
    fun `updateIsChargingOptimizationOn transitions from supported to not-supported correctly`() {
        // First call: supported and enabled
        every { SettingsUtils.Charging.isChargingOptimizationSupported(context) } returns true
        every { SettingsUtils.Charging.isChargingOptimizationEnabled(context) } returns true
        viewModel.updateIsChargingOptimizationOn(context)

        assertTrue(
            "Pre-condition: should be supported", viewModel.isChargingOptimizationSupported.value
        )
        assertTrue("Pre-condition: should be on", viewModel.isChargingOptimizationOn.value)

        // Second call: not supported anymore (e.g. after device reset)
        every { SettingsUtils.Charging.isChargingOptimizationSupported(context) } returns false
        viewModel.updateIsChargingOptimizationOn(context)

        assertFalse(
            "Expected isChargingOptimizationSupported to be false after update",
            viewModel.isChargingOptimizationSupported.value
        )
        // isChargingOptimizationOn should NOT have been updated in the second call
        assertFalse(
            "Expected isChargingOptimizationOn to be reset to false when not supported",
            viewModel.isChargingOptimizationOn.value
        )
    }

    @Test
    fun `initial canWrite state is null`() {
        assertNull("Expected canWrite initial value to be null", viewModel.canWrite.value)
    }

    @Test
    fun `initial currentRingerMode state is NORMAL`() {
        assertEquals(
            "Expected currentRingerMode initial value to be NORMAL",
            RingerMode.NORMAL,
            viewModel.currentRingerMode.value
        )
    }

    @Test
    fun `initial isAdaptiveBrightnessOn state is false`() {
        assertFalse(
            "Expected isAdaptiveBrightnessOn initial value to be false",
            viewModel.isAdaptiveBrightnessOn.value
        )
    }

    @Test
    fun `initial isVibrationModeOn state is false`() {
        assertFalse(
            "Expected isVibrationModeOn initial value to be false",
            viewModel.isVibrationModeOn.value
        )
    }

    @Test
    fun `updateCanWrite updates canWrite LiveData to true when permission granted`() {
        every { SettingsUtils.canWriteSettings(context) } returns true

        viewModel.updateCanWrite(context)

        assertEquals(true, viewModel.canWrite.value)
    }

    @Test
    fun `updateCanWrite updates canWrite LiveData to false when permission denied`() {
        every { SettingsUtils.canWriteSettings(context) } returns false

        viewModel.updateCanWrite(context)

        assertEquals(false, viewModel.canWrite.value)
    }

    @Test
    fun `updateIsAdaptiveBrightnessOn updates isAdaptiveBrightnessOn value`() {
        every { SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(context) } returns true

        viewModel.updateIsAdaptiveBrightnessOn(context)

        assertTrue(viewModel.isAdaptiveBrightnessOn.value)

        every { SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(context) } returns false

        viewModel.updateIsAdaptiveBrightnessOn(context)

        assertFalse(viewModel.isAdaptiveBrightnessOn.value)
    }

    @Test
    fun `updateIsVibrationModeOn updates isVibrationModeOn value`() {
        every { SettingsUtils.Vibration.isVibrationModeEnabled(context) } returns true

        viewModel.updateIsVibrationModeOn(context)

        assertTrue(viewModel.isVibrationModeOn.value)

        every { SettingsUtils.Vibration.isVibrationModeEnabled(context) } returns false

        viewModel.updateIsVibrationModeOn(context)

        assertFalse(viewModel.isVibrationModeOn.value)
    }

    @Test
    fun `updateCurrentRingerMode updates currentRingerMode LiveData`() {
        every { Ringer.getCurrentRingerMode(context) } returns RingerMode.VIBRATE

        viewModel.updateCurrentRingerMode(context)

        assertEquals(RingerMode.VIBRATE, viewModel.currentRingerMode.value)

        every { Ringer.getCurrentRingerMode(context) } returns RingerMode.SILENT

        viewModel.updateCurrentRingerMode(context)

        assertEquals(RingerMode.SILENT, viewModel.currentRingerMode.value)
    }

    @Test
    fun `initial currentRingerMode value is RingerMode NORMAL`() {
        assertEquals(RingerMode.NORMAL, viewModel.currentRingerMode.value)
    }

    @Test
    fun `initial isSmartPixelsOn state is false`() {
        assertFalse(viewModel.isSmartPixelsOn.value)
    }

    @Test
    fun `updateSmartPixelsState updates all smart pixels properties`() {
        every { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context) } returns true
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(context) } returns true
        every { SettingsUtils.SmartPixels.getSmartPixelsIntensity(context) } returns 70f
        every { SettingsUtils.SmartPixels.isDisableOnCastEnabled(context) } returns false

        viewModel.updateSmartPixelsState(context)

        assertEquals(true, viewModel.isAccessibilityEnabled.value)
        assertTrue(viewModel.isSmartPixelsOn.value)
        assertEquals(70f, viewModel.smartPixelsIntensity.value, 0.01f)
        assertFalse(viewModel.isSmartPixelsDisableOnCast.value)
    }

    @Test
    fun `initial isNetworkSpeedLimitSupported state is false`() {
        assertFalse(viewModel.isNetworkSpeedLimitSupported.value)
    }

    @Test
    fun `initial currentNetworkSpeedLimit is NO_LIMIT`() {
        assertEquals(NetworkSpeedLimit.NO_LIMIT, viewModel.currentNetworkSpeedLimit.value)
    }

    @Test
    fun `updateNetworkSpeedLimitState sets supported to false when not supported and does not query speed limit`() {
        every { SettingsUtils.NetworkSpeed.isSupported() } returns false

        viewModel.updateNetworkSpeedLimitState(context)

        assertFalse(viewModel.isNetworkSpeedLimitSupported.value)
        verify(exactly = 0) { SettingsUtils.NetworkSpeed.getCurrentSpeedLimit(any()) }
    }

    @Test
    fun `updateNetworkSpeedLimitState updates currentNetworkSpeedLimit when supported`() {
        val testLimit = NetworkSpeedLimit.LIMIT_1M
        every { SettingsUtils.NetworkSpeed.isSupported() } returns true
        every { SettingsUtils.NetworkSpeed.getCurrentSpeedLimit(context) } returns testLimit

        viewModel.updateNetworkSpeedLimitState(context)

        assertTrue(viewModel.isNetworkSpeedLimitSupported.value)
        assertEquals(testLimit, viewModel.currentNetworkSpeedLimit.value)
    }
}
