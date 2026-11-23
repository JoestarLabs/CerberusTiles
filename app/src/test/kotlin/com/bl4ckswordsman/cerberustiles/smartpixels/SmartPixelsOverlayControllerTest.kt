package com.bl4ckswordsman.cerberustiles.smartpixels

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.View
import android.view.WindowManager
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmartPixelsOverlayControllerTest {

    @MockK(relaxed = true)
    private lateinit var service: AccessibilityService

    @MockK(relaxed = true)
    private lateinit var windowManager: WindowManager

    @MockK(relaxed = true)
    private lateinit var displayManager: DisplayManager

    private lateinit var controller: SmartPixelsOverlayController

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { service.getSystemService(Context.WINDOW_SERVICE) } returns windowManager
        every { service.getSystemService(Context.DISPLAY_SERVICE) } returns displayManager

        val defaultDisplay = mockk<Display>(relaxed = true) {
            every { displayId } returns Display.DEFAULT_DISPLAY
        }
        every { displayManager.displays } returns arrayOf(defaultDisplay)

        mockkObject(SettingsUtils.SmartPixels)
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(any()) } returns false
        every { SettingsUtils.SmartPixels.isDisableOnCastEnabled(any()) } returns true
        every { SettingsUtils.SmartPixels.getSmartPixelsIntensity(any()) } returns 50f
        every { SettingsUtils.SmartPixels.isPitchBlackEnabled(any()) } returns false

        controller = SmartPixelsOverlayController(service)
        controller.init()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `updateState adds overlay when enabled and no secondary display`() {
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(service) } returns true

        controller.updateState()

        verify { windowManager.addView(any(), any()) }
    }

    @Test
    fun `updateState hides overlay when secondary display present and disableOnCast is true`() {
        val secondaryDisplay = mockk<Display>(relaxed = true) {
            every { displayId } returns 2
        }
        every { displayManager.displays } returns arrayOf(secondaryDisplay)
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(service) } returns true
        every { SettingsUtils.SmartPixels.isDisableOnCastEnabled(service) } returns true

        controller.updateState()

        verify(exactly = 0) { windowManager.addView(any(), any()) }
    }

    @Test
    fun `destroy unregisters listeners and cleans up overlay`() {
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(service) } returns true
        controller.updateState()

        controller.destroy()

        verify { displayManager.unregisterDisplayListener(any()) }
        verify { service.unregisterReceiver(any()) }
        verify { windowManager.removeView(any()) }
    }

    @Test
    fun `updateState reads pitchBlack setting when showing overlay and updates existing view`() {
        val viewSlot = slot<View>()
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(service) } returns true
        every { SettingsUtils.SmartPixels.isPitchBlackEnabled(service) } returns true

        controller.updateState()

        verify(exactly = 1) { windowManager.addView(capture(viewSlot), any()) }
        val overlayView = viewSlot.captured as SmartPixelsOverlayView
        assertTrue(overlayView.isPitchBlack)

        every { SettingsUtils.SmartPixels.isPitchBlackEnabled(service) } returns false
        controller.updateState()

        assertFalse(overlayView.isPitchBlack)
        verify(exactly = 1) { windowManager.addView(any(), any()) }
    }
}
