package com.bl4ckswordsman.cerberustiles

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.widget.Toast
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsUtilsTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var contentResolver: ContentResolver

    @MockK(relaxed = true)
    private lateinit var notificationManager: NotificationManager

    @MockK(relaxed = true)
    private lateinit var audioManager: AudioManager

    private val packageName = "com.bl4ckswordsman.cerberustiles"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.contentResolver } returns contentResolver
        every { context.packageName } returns packageName
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager

        mockkStatic(Settings.System::class)
        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast

        mockkStatic(android.net.Uri::class)
        val mockUri = mockk<android.net.Uri>(relaxed = true)
        every { android.net.Uri.parse(any()) } returns mockUri
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // --- General SettingsUtils tests ---

    @Test
    fun `canWriteSettings returns true when Settings System canWrite returns true`() {
        every { Settings.System.canWrite(context) } returns true

        assertTrue(SettingsUtils.canWriteSettings(context))
    }

    @Test
    fun `canWriteSettings returns false when Settings System canWrite returns false`() {
        every { Settings.System.canWrite(context) } returns false

        assertFalse(SettingsUtils.canWriteSettings(context))
    }

    @Test
    fun `canAccessNotificationPolicy returns true when access is granted`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true

        assertTrue(SettingsUtils.canAccessNotificationPolicy(context))
    }

    @Test
    fun `canAccessNotificationPolicy returns false when access is denied`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns false

        assertFalse(SettingsUtils.canAccessNotificationPolicy(context))
    }

    @Test
    fun `showToast displays enabled message when isEnabled is true`() {
        SettingsUtils.showToast(context, "Test Feature", true)

        verify { Toast.makeText(context, "Test Feature enabled", Toast.LENGTH_SHORT) }
    }

    @Test
    fun `showToast displays disabled message when isEnabled is false`() {
        SettingsUtils.showToast(context, "Test Feature", false)

        verify { Toast.makeText(context, "Test Feature disabled", Toast.LENGTH_SHORT) }
    }

    @Test
    fun `openPermissionSettings starts activity for write settings`() {
        SettingsUtils.openPermissionSettings(context)

        verify { context.startActivity(any()) }
    }

    @Test
    fun `openDndPermissionSettings starts activity for DND access settings`() {
        SettingsUtils.openDndPermissionSettings(context)

        verify { context.startActivity(any()) }
    }

    // --- Brightness tests ---

    @Test
    fun `isAdaptiveBrightnessEnabled returns true when SCREEN_BRIGHTNESS_MODE is 1`() {
        every {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
        } returns 1

        assertTrue(SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(context))
    }

    @Test
    fun `isAdaptiveBrightnessEnabled returns false when SCREEN_BRIGHTNESS_MODE is 0`() {
        every {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
        } returns 0

        assertFalse(SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(context))
    }

    @Test
    fun `toggleAdaptiveBrightness flips from 1 to 0 when permission granted`() {
        every { Settings.System.canWrite(context) } returns true
        every {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
        } returns 1
        every {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0)
        } returns true

        var callbackValue: Boolean? = null
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackValue = it }
        )

        SettingsUtils.Brightness.toggleAdaptiveBrightness(params)

        verify {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                0
            )
        }
        verify { Toast.makeText(context, "Adaptive brightness disabled", Toast.LENGTH_SHORT) }
        assertEquals(false, callbackValue)
    }

    @Test
    fun `toggleAdaptiveBrightness flips from 0 to 1 when permission granted`() {
        every { Settings.System.canWrite(context) } returns true
        every {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
        } returns 0
        every {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 1)
        } returns true

        var callbackValue: Boolean? = null
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackValue = it }
        )

        SettingsUtils.Brightness.toggleAdaptiveBrightness(params)

        verify {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                1
            )
        }
        verify { Toast.makeText(context, "Adaptive brightness enabled", Toast.LENGTH_SHORT) }
        assertEquals(true, callbackValue)
    }

    @Test
    fun `toggleAdaptiveBrightness does nothing when canWrite is false`() {
        every { Settings.System.canWrite(context) } returns false

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        SettingsUtils.Brightness.toggleAdaptiveBrightness(params)

        assertFalse(callbackCalled)
        verify(exactly = 0) { Settings.System.putInt(any(), any(), any()) }
    }

    @Test
    fun `setAdaptiveBrightness writes 0 and shows disabled toast when enabled is false`() {
        every { Settings.System.canWrite(context) } returns true
        every {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0)
        } returns true

        val success = SettingsUtils.Brightness.setAdaptiveBrightness(context, false)

        assertTrue(success)
        verify {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                0
            )
        }
        verify { Toast.makeText(context, "Adaptive brightness disabled", Toast.LENGTH_SHORT) }
    }

    @Test
    fun `setAdaptiveBrightness writes 1 and shows enabled toast when enabled is true`() {
        every { Settings.System.canWrite(context) } returns true
        every {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 1)
        } returns true

        val success = SettingsUtils.Brightness.setAdaptiveBrightness(context, true)

        assertTrue(success)
        verify {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                1
            )
        }
        verify { Toast.makeText(context, "Adaptive brightness enabled", Toast.LENGTH_SHORT) }
    }

    @Test
    fun `setAdaptiveBrightness does nothing and returns false when canWrite is false`() {
        every { Settings.System.canWrite(context) } returns false

        val success = SettingsUtils.Brightness.setAdaptiveBrightness(context, false)

        assertFalse(success)
        verify(exactly = 0) { Settings.System.putInt(any(), any(), any()) }
    }

    @Test
    fun `getScreenBrightness returns value from system settings`() {
        every {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } returns 180

        val brightness = SettingsUtils.Brightness.getScreenBrightness(context)

        assertEquals(180, brightness)
    }

    @Test
    fun `setScreenBrightness writes calculated exponential curve value when canWrite is true`() {
        every { Settings.System.canWrite(context) } returns true
        every {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, any())
        } returns true

        // 255.0.pow(1.0) = 255
        val result1 = SettingsUtils.Brightness.setScreenBrightness(context, 1.0f)
        assertTrue(result1)
        verify { Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 255) }

        // 255.0.pow(0.5) = 15.968... -> 15
        val result2 = SettingsUtils.Brightness.setScreenBrightness(context, 0.5f)
        assertTrue(result2)
        verify { Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 15) }
    }

    @Test
    fun `setScreenBrightness does not write when canWrite is false`() {
        every { Settings.System.canWrite(context) } returns false

        val result = SettingsUtils.Brightness.setScreenBrightness(context, 0.5f)

        assertFalse(result)
        verify(exactly = 0) { Settings.System.putInt(any(), any(), any()) }
    }

    @Test
    fun `setScreenBrightness returns false when putInt returns false`() {
        every { Settings.System.canWrite(context) } returns true
        every {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, any())
        } returns false

        val result = SettingsUtils.Brightness.setScreenBrightness(context, 0.5f)

        assertFalse(result)
        verify { Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 15) }
    }

    // --- Vibration tests ---

    @Test
    fun `isVibrationModeEnabled returns true when ringerMode is RINGER_MODE_VIBRATE`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_VIBRATE

        assertTrue(SettingsUtils.Vibration.isVibrationModeEnabled(context))
    }

    @Test
    fun `isVibrationModeEnabled returns false when ringerMode is not RINGER_MODE_VIBRATE`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL

        assertFalse(SettingsUtils.Vibration.isVibrationModeEnabled(context))
    }

    @Test
    fun `toggleVibrationMode opens permission settings and returns false when canWrite is false`() {
        every { Settings.System.canWrite(context) } returns false

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        val result = SettingsUtils.Vibration.toggleVibrationMode(params)

        assertFalse("Expected toggleVibrationMode to return false", result)
        assertFalse("Expected callback not to be called", callbackCalled)
        verify { context.startActivity(any()) }
    }

    @Test
    fun `toggleVibrationMode successfully toggles from VIBRATE to NORMAL`() {
        every { Settings.System.canWrite(context) } returns true
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_VIBRATE

        var callbackValue: Boolean? = null
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackValue = it }
        )

        val result = SettingsUtils.Vibration.toggleVibrationMode(params)

        assertTrue("Expected toggleVibrationMode to return true", result)
        verify { audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL }
        verify { Toast.makeText(context, "Vibration mode disabled", Toast.LENGTH_SHORT) }
        assertEquals(false, callbackValue)
    }

    @Test
    fun `toggleVibrationMode successfully toggles from NORMAL to VIBRATE`() {
        every { Settings.System.canWrite(context) } returns true
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL

        var callbackValue: Boolean? = null
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackValue = it }
        )

        val result = SettingsUtils.Vibration.toggleVibrationMode(params)

        assertTrue("Expected toggleVibrationMode to return true", result)
        verify { audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE }
        verify { Toast.makeText(context, "Vibration mode enabled", Toast.LENGTH_SHORT) }
        assertEquals(true, callbackValue)
    }

    @Test
    fun `toggleVibrationMode handles SecurityException in DND mode and returns false`() {
        every { Settings.System.canWrite(context) } returns true
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL
        every {
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        } throws SecurityException(
            "DND active"
        )

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        val result = SettingsUtils.Vibration.toggleVibrationMode(params)

        assertFalse("Expected toggleVibrationMode to return false on exception", result)
        assertFalse("Expected callback not to be called", callbackCalled)
        verify {
            Toast.makeText(
                context,
                "Cannot change vibration settings in Do Not Disturb mode",
                Toast.LENGTH_SHORT
            )
        }
    }
}
