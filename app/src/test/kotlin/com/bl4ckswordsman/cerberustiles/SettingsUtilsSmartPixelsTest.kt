package com.bl4ckswordsman.cerberustiles

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.bl4ckswordsman.cerberustiles.smartpixels.SmartPixelsAccessibilityService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsUtilsSmartPixelsTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var sharedPreferences: SharedPreferences

    @MockK(relaxed = true)
    private lateinit var editor: SharedPreferences.Editor

    @MockK(relaxed = true)
    private lateinit var accessibilityManager: AccessibilityManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.packageName } returns "com.bl4ckswordsman.cerberustiles"
        every { context.getSharedPreferences("settings", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putFloat(any(), any()) } returns editor
        every { context.getSystemService(Context.ACCESSIBILITY_SERVICE) } returns accessibilityManager

        mockkStatic(Settings.Secure::class)

        mockkObject(SmartPixelsAccessibilityService)
        every { SmartPixelsAccessibilityService.updateState(any()) } returns Unit
        every { SmartPixelsAccessibilityService.updateIntensity(any(), any()) } returns Unit
        every { SmartPixelsAccessibilityService.isRunning() } returns false
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isAccessibilityServiceEnabled returns true when service instance is running`() {
        every { SmartPixelsAccessibilityService.isRunning() } returns true
        assertTrue(SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context))
    }

    @Test
    fun `isAccessibilityServiceEnabled returns true when service is in enabled list`() {
        every { SmartPixelsAccessibilityService.isRunning() } returns false

        val serviceInfo = ServiceInfo().apply {
            packageName = "com.bl4ckswordsman.cerberustiles"
            name = SmartPixelsAccessibilityService::class.java.name
        }
        val resolveInfo = ResolveInfo().apply {
            this.serviceInfo = serviceInfo
        }
        val accessInfo = mockk<AccessibilityServiceInfo>(relaxed = true) {
            every { this@mockk.resolveInfo } returns resolveInfo
        }
        every {
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        } returns listOf(accessInfo)

        assertTrue(SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context))
    }

    @Test
    fun `isAccessibilityServiceEnabled returns true when found in Settings Secure string`() {
        every { SmartPixelsAccessibilityService.isRunning() } returns false
        every {
            accessibilityManager.getEnabledAccessibilityServiceList(any())
        } returns emptyList()

        val expectedComponent = "com.bl4ckswordsman.cerberustiles/${SmartPixelsAccessibilityService::class.java.name}"
        every {
            Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        } returns "other.pkg/other.Service:$expectedComponent"

        assertTrue(SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context))
    }

    @Test
    fun `isAccessibilityServiceEnabled returns false when not running and not in settings`() {
        every { SmartPixelsAccessibilityService.isRunning() } returns false
        every {
            accessibilityManager.getEnabledAccessibilityServiceList(any())
        } returns emptyList()
        every {
            Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        } returns null

        assertFalse(SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(context))
    }

    @Test
    fun `openAccessibilitySettings launches intent with ACTION_ACCESSIBILITY_SETTINGS`() {
        SettingsUtils.SmartPixels.openAccessibilitySettings(context)

        verify { context.startActivity(any()) }
    }

    @Test
    fun `isSmartPixelsEnabled reads from shared preferences`() {
        every { sharedPreferences.getBoolean(SettingsUtils.SmartPixels.KEY_SMART_PIXELS_ENABLED, false) } returns true
        assertTrue(SettingsUtils.SmartPixels.isSmartPixelsEnabled(context))

        every { sharedPreferences.getBoolean(SettingsUtils.SmartPixels.KEY_SMART_PIXELS_ENABLED, false) } returns false
        assertFalse(SettingsUtils.SmartPixels.isSmartPixelsEnabled(context))
    }

    @Test
    fun `setSmartPixelsEnabled saves to preferences and updates service state`() {
        SettingsUtils.SmartPixels.setSmartPixelsEnabled(context, true)

        verify { editor.putBoolean(SettingsUtils.SmartPixels.KEY_SMART_PIXELS_ENABLED, true) }
        verify { editor.apply() }
        verify { SmartPixelsAccessibilityService.updateState(context) }
    }

    @Test
    fun `getSmartPixelsIntensity returns stored value or default`() {
        every {
            sharedPreferences.getFloat(
                SettingsUtils.SmartPixels.KEY_SMART_PIXELS_INTENSITY,
                SettingsUtils.SmartPixels.DEFAULT_INTENSITY
            )
        } returns 65f

        assertEquals(65f, SettingsUtils.SmartPixels.getSmartPixelsIntensity(context), 0.01f)
    }

    @Test
    fun `setSmartPixelsIntensity saves value and updates service`() {
        SettingsUtils.SmartPixels.setSmartPixelsIntensity(context, 75f)

        verify { editor.putFloat(SettingsUtils.SmartPixels.KEY_SMART_PIXELS_INTENSITY, 75f) }
        verify { editor.apply() }
        verify { SmartPixelsAccessibilityService.updateIntensity(context, 75f) }
    }

    @Test
    fun `isDisableOnCastEnabled returns stored value or default true`() {
        every {
            sharedPreferences.getBoolean(SettingsUtils.SmartPixels.KEY_SMART_PIXELS_DISABLE_ON_CAST, true)
        } returns false

        assertFalse(SettingsUtils.SmartPixels.isDisableOnCastEnabled(context))
    }

    @Test
    fun `setDisableOnCastEnabled saves value and updates service state`() {
        SettingsUtils.SmartPixels.setDisableOnCastEnabled(context, false)

        verify { editor.putBoolean(SettingsUtils.SmartPixels.KEY_SMART_PIXELS_DISABLE_ON_CAST, false) }
        verify { editor.apply() }
        verify { SmartPixelsAccessibilityService.updateState(context) }
    }
}
