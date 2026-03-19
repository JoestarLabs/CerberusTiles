package com.bl4ckswordsman.cerberustiles.util

import android.app.NotificationManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class AutomaticZenManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        notificationManager = mockk(relaxed = true)

        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager

        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast

        // Just mock the entire object methods
        mockkObject(SettingsUtils)
        every { SettingsUtils.openDndPermissionSettings(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `canManageDndRules returns true when permission is granted`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true

        val result = AutomaticZenManager.canManageDndRules(context)

        assertTrue("Expected canManageDndRules to return true when access is granted", result)
    }

    @Test
    fun `canManageDndRules returns false when permission is denied`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns false

        val result = AutomaticZenManager.canManageDndRules(context)

        assertFalse("Expected canManageDndRules to return false when access is denied", result)
    }

    @Test(expected = ClassCastException::class)
    fun `canManageDndRules throws exception if system service is not NotificationManager`() {
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns Any()

        AutomaticZenManager.canManageDndRules(context)
    }

    @Test
    fun `activateSilentMode returns true and sets filter when permission granted`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true

        val result = AutomaticZenManager.activateSilentMode(context)

        assertTrue("Expected activateSilentMode to return true", result)
        verify { notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE) }
    }

    @Test
    fun `activateSilentMode returns false and shows message when permission denied`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns false

        val result = AutomaticZenManager.activateSilentMode(context)

        assertFalse("Expected activateSilentMode to return false", result)
        verify { Toast.makeText(context, any<CharSequence>(), Toast.LENGTH_SHORT) }
        verify { SettingsUtils.openDndPermissionSettings(context) }
    }

    @Test
    fun `activateSilentMode returns false and shows error message on exception`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true
        every { notificationManager.setInterruptionFilter(any()) } throws SecurityException("test exception")

        val result = AutomaticZenManager.activateSilentMode(context)

        assertFalse("Expected activateSilentMode to return false on exception", result)
        verify { Toast.makeText(context, "Failed to activate silent mode", Toast.LENGTH_SHORT) }
    }

    @Test
    fun `deactivateSilentMode returns true and sets filter when permission granted`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true

        val result = AutomaticZenManager.deactivateSilentMode(context)

        assertTrue("Expected deactivateSilentMode to return true", result)
        verify { notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) }
    }

    @Test
    fun `deactivateSilentMode returns false when permission denied`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns false

        val result = AutomaticZenManager.deactivateSilentMode(context)

        assertFalse("Expected deactivateSilentMode to return false", result)
    }

    @Test
    fun `deactivateSilentMode returns false and shows error message on exception`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true
        every { notificationManager.setInterruptionFilter(any()) } throws SecurityException("test exception")

        val result = AutomaticZenManager.deactivateSilentMode(context)

        assertFalse("Expected deactivateSilentMode to return false on exception", result)
        verify { Toast.makeText(context, "Failed to deactivate silent mode", Toast.LENGTH_SHORT) }
    }

    @Test
    fun `isSilentModeActive returns true when filter is NONE and permission granted`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true
        every { notificationManager.currentInterruptionFilter } returns NotificationManager.INTERRUPTION_FILTER_NONE

        val result = AutomaticZenManager.isSilentModeActive(context)

        assertTrue("Expected isSilentModeActive to return true", result)
    }

    @Test
    fun `isSilentModeActive returns false when filter is not NONE and permission granted`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true
        every { notificationManager.currentInterruptionFilter } returns NotificationManager.INTERRUPTION_FILTER_PRIORITY

        val result = AutomaticZenManager.isSilentModeActive(context)

        assertFalse("Expected isSilentModeActive to return false", result)
    }

    @Test
    fun `isSilentModeActive returns false when permission denied`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns false

        val result = AutomaticZenManager.isSilentModeActive(context)

        assertFalse("Expected isSilentModeActive to return false", result)
    }

    @Test
    fun `isSilentModeActive returns false on exception`() {
        every { notificationManager.isNotificationPolicyAccessGranted } returns true
        every { notificationManager.currentInterruptionFilter } throws SecurityException("test exception")

        val result = AutomaticZenManager.isSilentModeActive(context)

        assertFalse("Expected isSilentModeActive to return false on exception", result)
    }
}
