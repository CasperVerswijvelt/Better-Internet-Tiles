package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.content.Context
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.settings.ISetting
import be.casperverswijvelt.unifiedinternetqs.settings.settings.wifiSSIDVisibilityOption
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiTileService
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getWifiEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiIcon
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WifiTileBehaviour(
    context: Context,
    showDialog: (AlertDialogData) -> Unit = {},
    unlockAndRun: (Runnable) -> Unit = { it.run() }
) : TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "WifiTileBehaviour"
    }

    override val type: TileType
        get() = TileType.WiFi
    override val tileName: String
        get() = resources.getString(R.string.wifi)
    override val defaultIcon: Icon
        get() = Icon.createWithResource(
            context,
            R.drawable.ic_baseline_signal_wifi_3_bar_24
        )

    @Suppress("UNCHECKED_CAST")
    override val tileServiceClass: Class<TileService>
        get() = WifiTileService::class.java as Class<TileService>

    override val tileState: TileState
        get() {
            val tile = TileState()
            val wifiEnabled = getWifiEnabled(context)

            if ((wifiEnabled && !TileSyncService.isTurningOffWifi) || TileSyncService.isTurningOnWifi) {

                if (wifiEnabled) TileSyncService.isTurningOnWifi = false

                val showSSID = runBlocking {
                    !preferences.getHideWiFiSSID.first()
                } && TileSyncService.wifiSSID?.isNotEmpty() == true

                tile.label = when {
                    TileSyncService.wifiConnected && showSSID -> TileSyncService.wifiSSID
                    else -> null
                } ?: resources.getString(R.string.wifi)
                tile.state = Tile.STATE_ACTIVE
                tile.icon = when {
                    TileSyncService.wifiConnected -> getWifiIcon(context)
                    else -> R.drawable.ic_baseline_signal_wifi_0_bar_24
                }
                tile.subtitle = when {
                    TileSyncService.isTurningOnWifi -> resources.getString(R.string.turning_on)
                    TileSyncService.wifiConnected && !showSSID -> resources.getString(R.string.connected)
                    else -> resources.getString(R.string.on)
                }

            } else {

                if (!wifiEnabled) TileSyncService.isTurningOffWifi = false

                tile.label = resources.getString(R.string.wifi)
                tile.state = Tile.STATE_INACTIVE
                tile.icon = R.drawable.ic_baseline_signal_wifi_0_bar_24
                tile.subtitle = resources.getString(R.string.off)
            }
            return tile
        }
    override val onLongClickIntentAction: String
        get() = Settings.ACTION_WIFI_SETTINGS

    override val lookSettings: Array<ISetting<*>>
        get() = arrayOf(
            *super.lookSettings,
            wifiSSIDVisibilityOption
        )

    override fun onClick() {
        log("onClick")

        if (!checkShellAccess()) return

        if (requiresUnlock) {
            unlockAndRun { toggleWifi() }
        } else {
            toggleWifi()
        }
    }

    private fun toggleWifi() {

        val wifiEnabled = getWifiEnabled(context)

        if (wifiEnabled || TileSyncService.isTurningOnWifi) {
            TileSyncService.isTurningOnWifi = false
            TileSyncService.isTurningOffWifi = true
            executeShellCommandAsync("svc wifi disable", context) {
                if (it?.isSuccess != true) {
                    TileSyncService.isTurningOffWifi = false
                }
                updateTile()
            }
        } else {
            TileSyncService.isTurningOnWifi = true
            TileSyncService.isTurningOffWifi = false
            executeShellCommandAsync("svc wifi enable", context) {
                if (it?.isSuccess != true) {
                    TileSyncService.isTurningOnWifi = false
                }
                updateTile()
            }
        }
        updateTile()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}