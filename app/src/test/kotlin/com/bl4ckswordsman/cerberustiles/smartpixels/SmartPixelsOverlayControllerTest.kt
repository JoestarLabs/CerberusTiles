package com.bl4ckswordsman.cerberustiles.smartpixels

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.WindowManager
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
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
}
