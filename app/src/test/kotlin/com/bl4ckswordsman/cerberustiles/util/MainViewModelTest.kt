package com.bl4ckswordsman.cerberustiles.util

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import com.bl4ckswordsman.cerberustiles.MainViewModel
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

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
        mockkObject(SettingsUtils.Charging)
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
            "Expected showAdbDialog initial value to be false",
            viewModel.showAdbDialog.value
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
            "Expected isChargingOptimizationOn to be true",
            viewModel.isChargingOptimizationOn.value
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

        assertTrue("Pre-condition: should be supported", viewModel.isChargingOptimizationSupported.value)
        assertTrue("Pre-condition: should be on", viewModel.isChargingOptimizationOn.value)

        // Second call: not supported anymore (e.g. after device reset)
        every { SettingsUtils.Charging.isChargingOptimizationSupported(context) } returns false
        viewModel.updateIsChargingOptimizationOn(context)

        assertFalse(
            "Expected isChargingOptimizationSupported to be false after update",
            viewModel.isChargingOptimizationSupported.value
        )
        // isChargingOptimizationOn should NOT have been updated in the second call
        assertTrue(
            "Expected isChargingOptimizationOn to retain its previous value since the branch was not entered",
            viewModel.isChargingOptimizationOn.value
        )
    }
}
