package com.bl4ckswordsman.cerberustiles.quicksettingstiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
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

class SmartPixelsTileServiceTest {

    @MockK(relaxed = true)
    private lateinit var mockQsTile: Tile

    private lateinit var tileService: SmartPixelsTileService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Icon::class)
        val mockIcon = mockk<Icon>(relaxed = true)
        every { Icon.createWithResource(any<android.content.Context>(), any()) } returns mockIcon

        mockkObject(SettingsUtils.SmartPixels)
        every { SettingsUtils.SmartPixels.setSmartPixelsEnabled(any(), any()) } returns Unit
        every { SettingsUtils.SmartPixels.openAccessibilitySettings(any()) } returns Unit

        tileService = spyk(SmartPixelsTileService()) {
            every { qsTile } returns mockQsTile
            every { getString(R.string.smart_pixels_title) } returns "Smart Pixels"
            every { getString(R.string.permission_missing) } returns "Permission missing"
            every { getString(R.string.on) } returns "On"
            every { getString(R.string.off) } returns "Off"
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onStartListening sets STATE_UNAVAILABLE when accessibility permission is missing`() {
        every { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(tileService) } returns false
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(tileService) } returns false

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_UNAVAILABLE }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onStartListening sets tile state to STATE_ACTIVE when enabled and permission granted`() {
        every { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(tileService) } returns true
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(tileService) } returns true

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_ACTIVE }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onStartListening sets tile state to STATE_INACTIVE when disabled and permission granted`() {
        every { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(tileService) } returns true
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(tileService) } returns false

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_INACTIVE }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onClick opens accessibility settings when permission is not granted`() {
        every { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(tileService) } returns false

        tileService.onClick()

        verify { SettingsUtils.SmartPixels.openAccessibilitySettings(tileService) }
        verify(exactly = 0) { SettingsUtils.SmartPixels.setSmartPixelsEnabled(any(), any()) }
    }

    @Test
    fun `onClick toggles feature from disabled to enabled when permission is granted`() {
        every { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(tileService) } returns true
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(tileService) } returns false andThen true

        tileService.onClick()

        verify { SettingsUtils.SmartPixels.setSmartPixelsEnabled(tileService, true) }
        verify { mockQsTile.state = Tile.STATE_ACTIVE }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onClick toggles feature from enabled to disabled when permission is granted`() {
        every { SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(tileService) } returns true
        every { SettingsUtils.SmartPixels.isSmartPixelsEnabled(tileService) } returns true andThen false

        tileService.onClick()

        verify { SettingsUtils.SmartPixels.setSmartPixelsEnabled(tileService, false) }
        verify { mockQsTile.state = Tile.STATE_INACTIVE }
        verify { mockQsTile.updateTile() }
    }
}
