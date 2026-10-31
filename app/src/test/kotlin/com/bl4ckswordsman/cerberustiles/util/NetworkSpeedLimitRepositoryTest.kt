package com.bl4ckswordsman.cerberustiles.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NetworkSpeedLimitRepositoryTest {

    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        resources = mockk(relaxed = true)

        every { context.packageManager } returns packageManager
        NetworkSpeedLimitRepository.clearCache()
    }

    @After
    fun tearDown() {
        NetworkSpeedLimitRepository.clearCache()
    }

    @Test
    fun `getAvailableLimits returns default presets when system package resources fail`() {
        every { packageManager.getResourcesForApplication("com.android.settings") } throws
                PackageManager.NameNotFoundException()

        val limits = NetworkSpeedLimitRepository.getAvailableLimits(context)

        assertEquals(NetworkSpeedLimit.DEFAULT_PRESETS, limits)
    }

    @Test
    fun `resolveFromSystemSettings returns null when resource IDs are 0`() {
        every { packageManager.getResourcesForApplication("com.android.settings") } returns resources
        every { resources.getIdentifier("ingress_rate_limit_entries", "array", "com.android.settings") } returns 0
        every { resources.getIdentifier("ingress_rate_limit_values", "array", "com.android.settings") } returns 0

        val resolved = NetworkSpeedLimitRepository.resolveFromSystemSettings(context)

        assertNull(resolved)
    }

    @Test
    fun `resolveFromSystemSettings returns parsed limits when valid arrays exist`() {
        every { packageManager.getResourcesForApplication("com.android.settings") } returns resources
        every { resources.getIdentifier("ingress_rate_limit_entries", "array", "com.android.settings") } returns 101
        every { resources.getIdentifier("ingress_rate_limit_values", "array", "com.android.settings") } returns 102

        val entryStrings = arrayOf<CharSequence>("No limit", "128kbps", "1Mbps")
        val valueStrings = arrayOf("-1", "16000", "125000")

        every { resources.getTextArray(101) } returns entryStrings
        every { resources.getStringArray(102) } returns valueStrings

        val resolved = NetworkSpeedLimitRepository.resolveFromSystemSettings(context)

        assertEquals(3, resolved?.size)
        assertEquals("No limit", resolved?.get(0)?.label)
        assertEquals(-1L, resolved?.get(0)?.bytesPerSecond)
        assertEquals("128kbps", resolved?.get(1)?.label)
        assertEquals(16_000L, resolved?.get(1)?.bytesPerSecond)
        assertEquals("1Mbps", resolved?.get(2)?.label)
        assertEquals(125_000L, resolved?.get(2)?.bytesPerSecond)
    }
}
