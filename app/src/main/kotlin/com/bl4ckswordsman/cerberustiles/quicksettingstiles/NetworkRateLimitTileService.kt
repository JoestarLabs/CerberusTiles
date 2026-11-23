package com.bl4ckswordsman.cerberustiles.quicksettingstiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils

/**
 * A [TileService] that displays the current network download rate limit and cycles through
 * available bandwidth limitation presets when tapped.
 */
class NetworkRateLimitTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return

        if (!SettingsUtils.NetworkSpeed.isSupported()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Speed Limit"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                tile.subtitle = "Unsupported"
            }
            tile.updateTile()
            return
        }

        val current = SettingsUtils.NetworkSpeed.getCurrentSpeedLimit(this)
        tile.state = if (current.isUnlimited) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.label = "Speed Limit"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            tile.subtitle = current.label
        }
        tile.icon = Icon.createWithResource(this, R.drawable.rounded_network_check_24)
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        if (!SettingsUtils.NetworkSpeed.isSupported()) {
            Toast.makeText(
                this,
                "Network speed limit requires Android 13+",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!SettingsUtils.NetworkSpeed.canWriteSecureSettings(this)) {
            Toast.makeText(
                this,
                "Permission required: Grant WRITE_SECURE_SETTINGS via ADB",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val next = SettingsUtils.NetworkSpeed.cycleNextSpeedLimit(this)
        if (next != null) {
            tile.state = if (next.isUnlimited) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                tile.subtitle = next.label
            }
            tile.updateTile()
        }
    }
}
