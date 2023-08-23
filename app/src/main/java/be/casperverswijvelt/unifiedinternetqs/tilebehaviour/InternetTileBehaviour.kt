package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.content.Context
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.ServiceState
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.tiles.InternetTileService
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkIcon
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkText
import be.casperverswijvelt.unifiedinternetqs.util.getDataEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiIcon
import kotlinx.coroutines.Runnable

class InternetTileBehaviour(
    context: Context,
    showDialog: (AlertDialogData) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "InternetDataTileBehaviour"
    }

    override val type: TileType
        get() = TileType.Internet
    override val tileName: String
        get() = resources.getString(R.string.internet)
    override val defaultIcon: Icon
        get() = Icon.createWithResource(
            context,
            R.drawable.ic_baseline_public_24
        )
    @Suppress("UNCHECKED_CAST")
    override val tileServiceClass: Class<TileService>
        get() = InternetTileService::class.java as Class<TileService>

    override val tileState: TileState
        get() {
            val tile = TileState()
            val dataEnabled = getDataEnabled(context)
            val wifiEnabled = getWifiEnabled(context)

            when {
                (TileSyncService.isTurningOnWifi || wifiEnabled) && !TileSyncService.isTurningOnData -> {

                    if (wifiEnabled) {
                        TileSyncService.isTurningOnWifi = false
                    }

                    tile.state = Tile.STATE_ACTIVE
                    tile.icon = if (TileSyncService.wifiConnected)
                        getWifiIcon(context)
                    else
                        R.drawable.ic_baseline_signal_wifi_0_bar_24
                    tile.label = if (TileSyncService.isTurningOnWifi)
                        resources.getString(R.string.turning_on)
                    else
                        (if (TileSyncService.wifiConnected) TileSyncService.wifiSSID else null)
                            ?: resources.getString(R.string.not_connected)
                }
                TileSyncService.isTurningOnData || dataEnabled -> {

                    if (dataEnabled) {
                        TileSyncService.isTurningOnData = false
                    }

                    tile.state = Tile.STATE_ACTIVE
                    if (
                        TileSyncService.serviceState?.let {
                            it.state != ServiceState.STATE_IN_SERVICE
                        } == true
                    ) {
                        tile.icon = R.drawable.ic_baseline_signal_cellular_0_bar
                        tile.label = resources.getString(R.string.sim_not_available)
                    } else {
                        tile.icon = getCellularNetworkIcon(context)
                        tile.label = getCellularNetworkText(
                            context,
                            CellularChangeListener.currentTelephonyDisplayInfo
                        )
                    }
                }
                else -> {

                    tile.state = Tile.STATE_INACTIVE
                    tile.icon = R.drawable.ic_baseline_public_off_24
                    tile.label = resources.getString(R.string.internet)
                }
            }

            return tile
        }
    override val onLongClickIntentAction: String
        get() {
            return when {
                getDataEnabled(context) -> {
                    Settings.ACTION_NETWORK_OPERATOR_SETTINGS
                }
                getWifiEnabled(context) -> {
                    Settings.ACTION_WIFI_SETTINGS
                }
                else -> {
                    Settings.ACTION_WIRELESS_SETTINGS
                }
            }
        }

    override fun onClick() {
        log("onClick")

        if (!checkShellAccess()) return

        if (requiresUnlock) {
            unlockAndRun { cycleInternet() }
        } else {
            cycleInternet()
        }
    }

    private fun cycleInternet() {

        // Cycle trough internet connection modes:
        //  If Wi-Fi is enabled -> disable Wi-Fi and enable mobile data
        //  If mobile data is enabled -> disable mobile data and enable Wi-Fi
        //  Else -> enable Wi-Fi

        val dataEnabled = getDataEnabled(context)
        val wifiEnabled = getWifiEnabled(context)

        TileSyncService.isTurningOnData = false
        TileSyncService.isTurningOnWifi = false

        when {
            wifiEnabled -> {
                executeShellCommandAsync("svc wifi disable", context)

                TileSyncService.isTurningOnData = true
                executeShellCommandAsync("svc data enable", context) {
                    if (it?.isSuccess != true) {
                        TileSyncService.isTurningOnData = false
                    }
                    updateTile()
                }
            }
            dataEnabled -> {
                executeShellCommandAsync("svc data disable", context)

                TileSyncService.isTurningOnWifi = true
                executeShellCommandAsync("svc wifi enable", context) {
                    if (it?.isSuccess != true) {
                        TileSyncService.isTurningOnWifi = false
                    }
                    updateTile()
                }
            }
            else -> {
                TileSyncService.isTurningOnWifi = true
                executeShellCommandAsync("svc wifi enable", context) {
                    if (it?.isSuccess != true) {
                        TileSyncService.isTurningOnWifi = false
                    }
                    updateTile()
                }
            }
        }
        updateTile()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}