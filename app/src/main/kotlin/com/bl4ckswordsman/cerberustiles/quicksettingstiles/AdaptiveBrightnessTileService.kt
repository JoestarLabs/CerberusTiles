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
        val tile = qsTile ?: return
        val isAdaptive = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(this)

        tile.state = if (isAdaptive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    /**
     * Called when the user taps the tile. Toggles adaptive brightness and updates the tile
     * state and icon only when the setting write succeeds.
     */
    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (!SettingsUtils.canWriteSettings(this)) {
            SettingsUtils.openPermissionSettings(this)
            return
        }

        val params = SettingsUtils.SettingsToggleParams(
            context = this,
            onSettingChanged = { newValue ->
                tile.state = if (newValue) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                tile.icon = Icon.createWithResource(this, R.drawable.rounded_brightness_auto_24)
                tile.updateTile()
            }
        )
        SettingsUtils.Brightness.toggleAdaptiveBrightness(params)
    }
}
