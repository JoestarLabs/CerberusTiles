package com.bl4ckswordsman.cerberustiles.quicksettingstiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import com.bl4ckswordsman.cerberustiles.models.NetworkSpeedLimit
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class NetworkRateLimitTileServiceTest {

    @MockK(relaxed = true)
    private lateinit var mockQsTile: Tile

    private lateinit var tileService: NetworkRateLimitTileService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Icon::class)
        val mockIcon = mockk<Icon>(relaxed = true)
        every { Icon.createWithResource(any<android.content.Context>(), any()) } returns mockIcon

        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast

        mockkObject(SettingsUtils.NetworkSpeed)

        tileService = spyk(NetworkRateLimitTileService()) {
            every { qsTile } returns mockQsTile
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onStartListening sets STATE_UNAVAILABLE when feature is unsupported`() {
        every { SettingsUtils.NetworkSpeed.isSupported() } returns false

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_UNAVAILABLE }
        verify { mockQsTile.label = "Speed Limit" }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onStartListening sets STATE_INACTIVE when speed limit is unlimited`() {
        every { SettingsUtils.NetworkSpeed.isSupported() } returns true
        every { SettingsUtils.NetworkSpeed.getCurrentSpeedLimit(tileService) } returns NetworkSpeedLimit.NO_LIMIT

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_INACTIVE }
        verify { mockQsTile.label = "Speed Limit" }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onStartListening sets STATE_ACTIVE when speed limit is throttled`() {
        val preset = NetworkSpeedLimit.LIMIT_1M
        every { SettingsUtils.NetworkSpeed.isSupported() } returns true
        every { SettingsUtils.NetworkSpeed.getCurrentSpeedLimit(tileService) } returns preset

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_ACTIVE }
        verify { mockQsTile.label = "Speed Limit" }
        verify { Icon.createWithResource(tileService, R.drawable.rounded_speed_24) }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onClick shows toast when feature is unsupported`() {
        every { SettingsUtils.NetworkSpeed.isSupported() } returns false

        tileService.onClick()

        verify {
            Toast.makeText(tileService, "Network speed limit requires Android 13+", Toast.LENGTH_SHORT)
        }
        verify(exactly = 0) { SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(any()) }
    }

    @Test
    fun `onClick shows toast when write secure settings permission is missing`() {
        every { SettingsUtils.NetworkSpeed.isSupported() } returns true
        every { SettingsUtils.NetworkSpeed.canWriteSecureSettings(tileService) } returns false

        tileService.onClick()

        verify {
            Toast.makeText(tileService, "Permission required: Grant WRITE_SECURE_SETTINGS via ADB", Toast.LENGTH_LONG)
        }
        verify(exactly = 0) { SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(any()) }
    }

    @Test
    fun `onClick cycles to next speed limit and updates tile when permission granted`() {
        val nextLimit = NetworkSpeedLimit.LIMIT_1M
        every { SettingsUtils.NetworkSpeed.isSupported() } returns true
        every { SettingsUtils.NetworkSpeed.canWriteSecureSettings(tileService) } returns true
        every { SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(tileService) } returns nextLimit

        tileService.onClick()

        verify { SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(tileService) }
        verify { mockQsTile.state = Tile.STATE_ACTIVE }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onClick sets STATE_INACTIVE when cycled back to unlimited`() {
        every { SettingsUtils.NetworkSpeed.isSupported() } returns true
        every { SettingsUtils.NetworkSpeed.canWriteSecureSettings(tileService) } returns true
        every { SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(tileService) } returns NetworkSpeedLimit.NO_LIMIT

        tileService.onClick()

        verify { SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(tileService) }
        verify { mockQsTile.state = Tile.STATE_INACTIVE }
        verify { mockQsTile.updateTile() }
    }
}
