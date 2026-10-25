package com.bl4ckswordsman.cerberustiles.quicksettingstiles

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * A [TileService] that toggles the Smart Pixels OLED battery-saving screen overlay.
 */
class SmartPixelsTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        val hasPerm = SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(this)
        if (!hasPerm) {
            SettingsUtils.SmartPixels.openAccessibilitySettings(this)
            return
        }

        val currentState = SettingsUtils.SmartPixels.isSmartPixelsEnabled(this)
        val newState = !currentState
        SettingsUtils.SmartPixels.setSmartPixelsEnabled(this, newState)
        updateTileState()
    }

    fun updateTileState() {
        val tile = qsTile ?: return
        val hasPerm = SettingsUtils.SmartPixels.isAccessibilityServiceEnabled(this)
        val isEnabled = SettingsUtils.SmartPixels.isSmartPixelsEnabled(this)

        tile.state = when {
            !hasPerm -> Tile.STATE_UNAVAILABLE
            isEnabled -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.icon = Icon.createWithResource(this, R.drawable.rounded_grain_24)
        tile.label = getString(R.string.smart_pixels_title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = when {
                !hasPerm -> getString(R.string.permission_missing)
                isEnabled -> getString(R.string.on)
                else -> getString(R.string.off)
            }
        }
        tile.updateTile()
    }
}
