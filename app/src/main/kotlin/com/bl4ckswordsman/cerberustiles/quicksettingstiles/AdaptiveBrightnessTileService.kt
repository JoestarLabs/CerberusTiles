package com.bl4ckswordsman.cerberustiles.quicksettingstiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * A [TileService] that toggles the adaptive brightness setting when the user taps the tile.
 */
class AdaptiveBrightnessTileService : TileService() {

    /**
     * Called when the tile becomes visible. Reads the current adaptive brightness state and
     * updates the tile's active/inactive state accordingly.
     */
    override fun onStartListening() {
        super.onStartListening()

        val isAdaptive = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(this)

        qsTile.state = if (isAdaptive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    /**
     * Called when the user taps the tile. Toggles adaptive brightness and updates the tile
     * state and icon to reflect the new value.
     */
    override fun onClick() {
        super.onClick()

        val isAdaptive = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(this)
        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { _ -> }
        )
        SettingsUtils.Brightness.toggleAdaptiveBrightness(params)

        qsTile.state = if (isAdaptive) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        val iconRes =
            if (qsTile.state == Tile.STATE_ACTIVE) R.drawable.baseline_brightness_auto_24 else R.drawable.outline_brightness_auto_24
        qsTile.icon = Icon.createWithResource(this, iconRes)
        qsTile.updateTile()
    }
    // on hold, open the app
}
