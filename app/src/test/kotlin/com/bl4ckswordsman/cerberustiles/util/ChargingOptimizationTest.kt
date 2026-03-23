package com.bl4ckswordsman.cerberustiles.util

import android.content.ContentResolver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChargingOptimizationTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var contentResolver: ContentResolver

    private val packageName = "com.bl4ckswordsman.cerberustiles"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.contentResolver } returns contentResolver
        every { context.packageName } returns packageName

        mockkStatic(Toast::class)
        val mockToast = io.mockk.mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast

        mockkStatic(Settings.Secure::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isChargingOptimizationEnabled returns true when setting is 1`() {
        every {
            Settings.Secure.getInt(contentResolver, "charge_optimization_mode", 0)
        } returns 1

        val result = SettingsUtils.Charging.isChargingOptimizationEnabled(context)
        assertTrue("Expected isChargingOptimizationEnabled to return true", result)
    }

    @Test
    fun `isChargingOptimizationEnabled returns false when setting is 0`() {
        every {
            Settings.Secure.getInt(contentResolver, "charge_optimization_mode", 0)
        } returns 0

        val result = SettingsUtils.Charging.isChargingOptimizationEnabled(context)
        assertFalse("Expected isChargingOptimizationEnabled to return false", result)
    }

    @Test
    fun `isChargingOptimizationEnabled returns false on exception`() {
        every {
            Settings.Secure.getInt(contentResolver, "charge_optimization_mode", 0)
        } throws SecurityException("Test exception")

        val result = SettingsUtils.Charging.isChargingOptimizationEnabled(context)
        assertFalse("Expected isChargingOptimizationEnabled to return false on exception", result)
    }

    @Test
    fun `toggleChargingOptimization toggles to 1 when currently 0`() {
        every {
            Settings.Secure.getInt(contentResolver, "charge_optimization_mode", 0)
        } returns 0

        every {
            Settings.Secure.putInt(contentResolver, "charge_optimization_mode", 1)
        } returns true

        var callbackCalled = false
        var newValue = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { value ->
                callbackCalled = true
                newValue = value
            }
        )

        SettingsUtils.Charging.toggleChargingOptimization(params)

        verify { Settings.Secure.putInt(contentResolver, "charge_optimization_mode", 1) }
        verify { Toast.makeText(context, "Charging optimization enabled", Toast.LENGTH_SHORT) }
        assertTrue("Expected callback to be called", callbackCalled)
        assertTrue("Expected new value to be true", newValue)
    }

    @Test
    fun `toggleChargingOptimization toggles to 0 when currently 1`() {
        every {
            Settings.Secure.getInt(contentResolver, "charge_optimization_mode", 0)
        } returns 1

        every {
            Settings.Secure.putInt(contentResolver, "charge_optimization_mode", 0)
        } returns true

        var callbackCalled = false
        var newValue = true
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = { value ->
                callbackCalled = true
                newValue = value
            }
        )

        SettingsUtils.Charging.toggleChargingOptimization(params)

        verify { Settings.Secure.putInt(contentResolver, "charge_optimization_mode", 0) }
        verify { Toast.makeText(context, "Charging optimization disabled", Toast.LENGTH_SHORT) }
        assertTrue("Expected callback to be called", callbackCalled)
        assertFalse("Expected new value to be false", newValue)
    }

    @Test
    fun `toggleChargingOptimization invokes onPermissionDenied when SecurityException is thrown`() {
        every {
            Settings.Secure.getInt(contentResolver, "charge_optimization_mode", 0)
        } returns 0

        every {
            Settings.Secure.putInt(contentResolver, "charge_optimization_mode", 1)
        } throws SecurityException("Permission denial")

        var deniedCalled = false
        val params = SettingsUtils.SettingsToggleParams(
            context = context,
            onSettingChanged = {},
            onPermissionDenied = { deniedCalled = true }
        )

        SettingsUtils.Charging.toggleChargingOptimization(params)

        assertTrue("Expected onPermissionDenied to be called", deniedCalled)
    }
}