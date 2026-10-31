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

class AdaptiveBrightnessTileServiceTest {

    @MockK(relaxed = true)
    private lateinit var mockQsTile: Tile

    private lateinit var tileService: AdaptiveBrightnessTileService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Icon::class)
        val mockIcon = mockk<Icon>(relaxed = true)
        every { Icon.createWithResource(any<android.content.Context>(), any()) } returns mockIcon

        mockkObject(SettingsUtils.Brightness)

        tileService = spyk(AdaptiveBrightnessTileService()) {
            every { qsTile } returns mockQsTile
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onStartListening sets tile state to ACTIVE when adaptive brightness is enabled`() {
        every { SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(tileService) } returns true

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_ACTIVE }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onStartListening sets tile state to INACTIVE when adaptive brightness is disabled`() {
        every { SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(tileService) } returns false

        tileService.onStartListening()

        verify { mockQsTile.state = Tile.STATE_INACTIVE }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onClick toggles brightness from enabled to disabled and updates tile to INACTIVE with rounded icon`() {
        every { SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(tileService) } returns true
        every { mockQsTile.state } returns Tile.STATE_INACTIVE

        tileService.onClick()

        verify { SettingsUtils.Brightness.toggleAdaptiveBrightness(any()) }
        verify { mockQsTile.state = Tile.STATE_INACTIVE }
        verify { Icon.createWithResource(tileService, R.drawable.rounded_brightness_auto_24) }
        verify { mockQsTile.updateTile() }
    }

    @Test
    fun `onClick toggles brightness from disabled to enabled and updates tile to ACTIVE with rounded icon`() {
        every { SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(tileService) } returns false
        every { mockQsTile.state } returns Tile.STATE_ACTIVE

        tileService.onClick()

        verify { SettingsUtils.Brightness.toggleAdaptiveBrightness(any()) }
        verify { mockQsTile.state = Tile.STATE_ACTIVE }
        verify { Icon.createWithResource(tileService, R.drawable.rounded_brightness_auto_24) }
        verify { mockQsTile.updateTile() }
    }
}
