package com.bl4ckswordsman.cerberustiles.activities

import android.provider.Settings
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleActivityTest {

    @Before
    fun setUp() {
        mockkStatic(Settings.System::class)
        mockkObject(SettingsUtils)
        mockkObject(SettingsUtils.Brightness)
        mockkObject(SettingsUtils.Vibration)

        every { SettingsUtils.Brightness.toggleAdaptiveBrightness(any()) } returns Unit
        every { SettingsUtils.Vibration.toggleVibrationMode(any()) } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `ToggleAdaptiveBrightnessActivity hasRequiredPermissions returns true when canWrite is true`() {
        val activity = spyk(ToggleAdaptiveBrightnessActivity())
        every { Settings.System.canWrite(activity) } returns true

        assertTrue(activity.hasRequiredPermissions())
    }

    @Test
    fun `ToggleAdaptiveBrightnessActivity hasRequiredPermissions returns false when canWrite is false`() {
        val activity = spyk(ToggleAdaptiveBrightnessActivity())
        every { Settings.System.canWrite(activity) } returns false

        assertFalse(activity.hasRequiredPermissions())
    }

    @Test
    fun `ToggleAdaptiveBrightnessActivity performAction invokes toggleAdaptiveBrightness`() {
        val activity = spyk(ToggleAdaptiveBrightnessActivity())

        activity.performAction()

        verify { SettingsUtils.Brightness.toggleAdaptiveBrightness(any()) }
    }

    @Test
    fun `ToggleVibrationModeActivity hasRequiredPermissions returns true when both permissions granted`() {
        val activity = spyk(ToggleVibrationModeActivity())
        every { SettingsUtils.canWriteSettings(activity) } returns true
        every { SettingsUtils.canAccessNotificationPolicy(activity) } returns true

        assertTrue(activity.hasRequiredPermissions())
    }

    @Test
    fun `ToggleVibrationModeActivity hasRequiredPermissions returns false when canWriteSettings is false`() {
        val activity = spyk(ToggleVibrationModeActivity())
        every { SettingsUtils.canWriteSettings(activity) } returns false
        every { SettingsUtils.canAccessNotificationPolicy(activity) } returns true

        assertFalse(activity.hasRequiredPermissions())
    }

    @Test
    fun `ToggleVibrationModeActivity hasRequiredPermissions returns false when canAccessNotificationPolicy is false`() {
        val activity = spyk(ToggleVibrationModeActivity())
        every { SettingsUtils.canWriteSettings(activity) } returns true
        every { SettingsUtils.canAccessNotificationPolicy(activity) } returns false

        assertFalse(activity.hasRequiredPermissions())
    }

    @Test
    fun `ToggleVibrationModeActivity performAction invokes toggleVibrationMode`() {
        val activity = spyk(ToggleVibrationModeActivity())

        activity.performAction()

        verify { SettingsUtils.Vibration.toggleVibrationMode(any()) }
    }
}
