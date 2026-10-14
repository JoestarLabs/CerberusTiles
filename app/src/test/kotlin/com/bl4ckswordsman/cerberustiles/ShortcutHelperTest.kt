package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import com.bl4ckswordsman.cerberustiles.activities.ToggleAdaptiveBrightnessActivity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ShortcutHelperTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var shortcutManager: ShortcutManager

    private lateinit var shortcutHelper: ShortcutHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getSystemService(Context.SHORTCUT_SERVICE) } returns shortcutManager

        mockkStatic(Icon::class)
        val mockIcon = mockk<Icon>(relaxed = true)
        every { Icon.createWithResource(any<Context>(), any<Int>()) } returns mockIcon

        shortcutHelper = ShortcutHelper(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createShortcut builds ShortcutInfo using ShortcutInfo Builder`() = runTest {
        val mockShortcut = mockk<ShortcutInfo>(relaxed = true)
        mockkConstructor(ShortcutInfo.Builder::class)
        every { anyConstructed<ShortcutInfo.Builder>().setShortLabel(any()) } answers { self as ShortcutInfo.Builder }
        every { anyConstructed<ShortcutInfo.Builder>().setLongLabel(any()) } answers { self as ShortcutInfo.Builder }
        every { anyConstructed<ShortcutInfo.Builder>().setIcon(any()) } answers { self as ShortcutInfo.Builder }
        every { anyConstructed<ShortcutInfo.Builder>().setIntent(any()) } answers { self as ShortcutInfo.Builder }
        every { anyConstructed<ShortcutInfo.Builder>().build() } returns mockShortcut

        val info = CustomShortcutInfo(
            id = "test_id",
            shortLabel = "Short",
            longLabel = "Long",
            icon = R.drawable.baseline_brightness_auto_24,
            action = Constants.TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION,
            activityClass = ToggleAdaptiveBrightnessActivity::class.java
        )

        val result = shortcutHelper.createShortcut(info)

        assertNotNull(result)
        verify {
            anyConstructed<ShortcutInfo.Builder>().setShortLabel("Short")
            anyConstructed<ShortcutInfo.Builder>().setLongLabel("Long")
            anyConstructed<ShortcutInfo.Builder>().build()
        }
    }

    @Test
    fun `createAllShortcuts creates and sets all three dynamic shortcuts`() = runTest {
        val spiedHelper = spyk(shortcutHelper)
        val mockShortcut1 = mockk<ShortcutInfo>(relaxed = true)
        val mockShortcut2 = mockk<ShortcutInfo>(relaxed = true)
        val mockShortcut3 = mockk<ShortcutInfo>(relaxed = true)

        val createdInfos = mutableListOf<CustomShortcutInfo>()
        io.mockk.coEvery { spiedHelper.createShortcut(capture(createdInfos)) } returnsMany listOf(
            mockShortcut1,
            mockShortcut2,
            mockShortcut3
        )

        val dynamicShortcutsSlot = slot<List<ShortcutInfo>>()
        every { shortcutManager.setDynamicShortcuts(capture(dynamicShortcutsSlot)) } returns true

        spiedHelper.createAllShortcuts()

        assertEquals(3, createdInfos.size)
        assertEquals("toggle_adaptive_brightness", createdInfos[0].id)
        assertEquals(Constants.TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION, createdInfos[0].action)

        assertEquals("toggle_vibration_ringer_mode", createdInfos[1].id)
        assertEquals(Constants.TOGGLE_VIBRATION_MODE_ACTION, createdInfos[1].action)

        assertEquals("overlay_shortcut", createdInfos[2].id)
        assertEquals("com.bl4ckswordsman.cerberustiles.OPEN_OVERLAY", createdInfos[2].action)

        assertEquals(3, dynamicShortcutsSlot.captured.size)
    }

    @Test
    fun `reportUsage calls ShortcutManager reportShortcutUsed`() {
        ShortcutHelper.reportUsage(context, ShortcutHelper.ID_ADAPTIVE_BRIGHTNESS, shortcutManager)
        verify { shortcutManager.reportShortcutUsed(ShortcutHelper.ID_ADAPTIVE_BRIGHTNESS) }
    }
}
