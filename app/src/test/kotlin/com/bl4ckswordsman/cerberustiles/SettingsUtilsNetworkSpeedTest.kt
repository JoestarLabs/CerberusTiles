package com.bl4ckswordsman.cerberustiles

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import com.bl4ckswordsman.cerberustiles.util.NetworkSpeedLimitRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsUtilsNetworkSpeedTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.contentResolver } returns contentResolver
        mockkStatic(Settings.Global::class)
        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast

        mockkObject(NetworkSpeedLimitRepository)
        every { NetworkSpeedLimitRepository.getAvailableLimits(any()) } returns NetworkSpeedLimit.DEFAULT_PRESETS
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `constant key matches expected AOSP setting string`() {
        assertEquals(
            "ingress_rate_limit_bytes_per_second",
            SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND
        )
    }

    @Test
    fun `canWriteSecureSettings returns true when permission is granted`() {
        every {
            context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)
        } returns PackageManager.PERMISSION_GRANTED

        assertTrue(SettingsUtils.NetworkSpeed.canWriteSecureSettings(context))
    }

    @Test
    fun `canWriteSecureSettings returns false when permission is denied`() {
        every {
            context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)
        } returns PackageManager.PERMISSION_DENIED

        assertFalse(SettingsUtils.NetworkSpeed.canWriteSecureSettings(context))
    }

    @Test
    fun `getRateLimitBytes reads value from Settings Global`() {
        every {
            Settings.Global.getLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                -1L
            )
        } returns 125_000L

        val bytes = SettingsUtils.NetworkSpeed.getRateLimitBytes(context)
        assertEquals(125_000L, bytes)
    }

    @Test
    fun `getCurrentSpeedLimit resolves matching limit from Global settings`() {
        every {
            Settings.Global.getLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                -1L
            )
        } returns 125_000L

        val current = SettingsUtils.NetworkSpeed.getCurrentSpeedLimit(context)
        assertEquals(NetworkSpeedLimit.LIMIT_1M, current)
    }

    @Test
    fun `setSpeedLimit writes bytes to Settings Global and displays toast`() {
        every {
            Settings.Global.putLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                125_000L
            )
        } returns true

        val success = SettingsUtils.NetworkSpeed.setSpeedLimit(
            context,
            NetworkSpeedLimit.LIMIT_1M
        )

        assertTrue(success)
        verify {
            Settings.Global.putLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                125_000L
            )
        }
        verify {
            Toast.makeText(context, "Network speed limit: 1 Mbps", Toast.LENGTH_SHORT)
        }
    }

    @Test
    fun `setSpeedLimit invokes onPermissionDenied when SecurityException is caught`() {
        every {
            Settings.Global.putLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                any()
            )
        } throws SecurityException("Missing WRITE_SECURE_SETTINGS")

        var permissionDeniedCalled = false
        val success = SettingsUtils.NetworkSpeed.setSpeedLimit(
            context,
            NetworkSpeedLimit.LIMIT_1M,
            onPermissionDenied = { permissionDeniedCalled = true }
        )

        assertFalse(success)
        assertTrue(permissionDeniedCalled)
    }

    @Test
    fun `cycleNextSpeedLimit advances from No limit to first rate limit`() {
        every {
            Settings.Global.getLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                -1L
            )
        } returns -1L

        every {
            Settings.Global.putLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                16_000L
            )
        } returns true

        val next = SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(context)

        assertNotNull(next)
        assertEquals(NetworkSpeedLimit.LIMIT_128K, next)
    }

    @Test
    fun `cycleNextSpeedLimit wraps around from last preset to No limit`() {
        every {
            Settings.Global.getLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                -1L
            )
        } returns 1_875_000L // 15 Mbps (last in default list)

        every {
            Settings.Global.putLong(
                contentResolver,
                SettingsUtils.NetworkSpeed.INGRESS_RATE_LIMIT_BYTES_PER_SECOND,
                -1L
            )
        } returns true

        val next = SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(context)

        assertNotNull(next)
        assertEquals(NetworkSpeedLimit.NO_LIMIT, next)
    }
}
