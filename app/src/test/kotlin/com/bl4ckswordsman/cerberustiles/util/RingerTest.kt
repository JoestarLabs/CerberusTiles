package com.bl4ckswordsman.cerberustiles.util

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.RingerMode
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

class RingerTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var audioManager: AudioManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager

        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast

        mockkObject(SettingsUtils)
        every { SettingsUtils.canWriteSettings(any()) } returns true
        every { SettingsUtils.openPermissionSettings(any()) } returns Unit
        every { SettingsUtils.openDndPermissionSettings(any()) } returns Unit
        every { SettingsUtils.showToast(any(), any(), any()) } returns Unit

        mockkObject(AutomaticZenManager)
        every { AutomaticZenManager.activateSilentMode(any()) } returns true
        every { AutomaticZenManager.deactivateSilentMode(any()) } returns true
        every { AutomaticZenManager.isSilentModeActive(any()) } returns false
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getCurrentRingerMode returns NORMAL when audioManager is RINGER_MODE_NORMAL`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL

        val result = Ringer.getCurrentRingerMode(context)

        assertEquals(RingerMode.NORMAL, result)
    }

    @Test
    fun `getCurrentRingerMode returns SILENT when audioManager is RINGER_MODE_SILENT`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_SILENT

        val result = Ringer.getCurrentRingerMode(context)

        assertEquals(RingerMode.SILENT, result)
    }

    @Test
    fun `getCurrentRingerMode returns VIBRATE when audioManager is RINGER_MODE_VIBRATE`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_VIBRATE

        val result = Ringer.getCurrentRingerMode(context)

        assertEquals(RingerMode.VIBRATE, result)
    }

    @Test
    fun `getCurrentRingerMode defaults to NORMAL for unknown ringerMode value`() {
        every { audioManager.ringerMode } returns 999

        val result = Ringer.getCurrentRingerMode(context)

        assertEquals(RingerMode.NORMAL, result)
    }

    @Test
    fun `setRingerMode opens permission settings and aborts when canWriteSettings is false`() {
        every { SettingsUtils.canWriteSettings(context) } returns false

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.VIBRATE)

        verify { SettingsUtils.openPermissionSettings(context) }
        assertFalse("Expected callback not to be called", callbackCalled)
        verify(exactly = 0) { audioManager.ringerMode = any() }
    }

    @Test
    fun `setRingerMode is no-op when requested mode equals current mode`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.NORMAL)

        assertFalse("Expected callback not to be called", callbackCalled)
        verify(exactly = 0) { audioManager.ringerMode = any() }
    }

    @Test
    fun `setRingerMode successfully switches to SILENT mode with DND activation`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL andThen AudioManager.RINGER_MODE_SILENT

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.SILENT)

        verify { AutomaticZenManager.activateSilentMode(context) }
        verify { audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT }
        verify { SettingsUtils.showToast(context, "Silent mode", true) }
        assertTrue("Expected callback to be called", callbackCalled)
    }

    @Test
    fun `setRingerMode successfully switches to VIBRATE mode and deactivates DND when active`() {
        every { AutomaticZenManager.isSilentModeActive(context) } returns true
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL andThen AudioManager.RINGER_MODE_VIBRATE

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.VIBRATE)

        verify { AutomaticZenManager.deactivateSilentMode(context) }
        verify { audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE }
        verify { SettingsUtils.showToast(context, "Vibrate mode", true) }
        assertTrue("Expected callback to be called", callbackCalled)
    }

    @Test
    fun `setRingerMode successfully switches to NORMAL mode`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_VIBRATE andThen AudioManager.RINGER_MODE_NORMAL

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.NORMAL)

        verify { audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL }
        verify { SettingsUtils.showToast(context, "Sound mode", true) }
        assertTrue("Expected callback to be called", callbackCalled)
    }

    @Test
    fun `setRingerMode does not invoke callback when mode change verification fails`() {
        // audioManager remains NORMAL despite attempting to set VIBRATE
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.VIBRATE)

        assertFalse("Expected callback not to be called on verification failure", callbackCalled)
        verify(exactly = 0) { SettingsUtils.showToast(any(), any(), any()) }
    }

    @Test
    fun `setRingerMode handles SecurityException when switching to SILENT mode`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_NORMAL
        every { AutomaticZenManager.activateSilentMode(context) } throws SecurityException("DND permission missing")

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.SILENT)

        verify {
            Toast.makeText(
                context,
                "Cannot set silent mode. Please grant Do Not Disturb permission in settings.",
                Toast.LENGTH_LONG
            )
        }
        verify { SettingsUtils.openDndPermissionSettings(context) }
        assertFalse("Expected callback not to be called", callbackCalled)
    }

    @Test
    fun `setRingerMode handles SecurityException when switching to non-silent mode`() {
        every { audioManager.ringerMode } returns AudioManager.RINGER_MODE_SILENT
        every { audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL } throws SecurityException(
            "Not allowed"
        )

        var callbackCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { callbackCalled = true }
        )

        Ringer.setRingerMode(params, RingerMode.NORMAL)

        verify {
            Toast.makeText(
                context,
                match<CharSequence> { it.toString().contains("Cannot change ringer mode") },
                Toast.LENGTH_LONG
            )
        }
        assertFalse("Expected callback not to be called", callbackCalled)
    }
}
