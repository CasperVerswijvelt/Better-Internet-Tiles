package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.app.Dialog
import android.content.Context
import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkIcon
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkText
import be.casperverswijvelt.unifiedinternetqs.util.getDataEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.getWifiEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiIcon
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.Runnable

class InternetTileBehaviour(
    context: Context,
    showDialog: (Dialog) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "InternetDataTileBehaviour"
    }

    override fun onClick() {
        log("onClick")

        if (!hasShellAccess(context)) {

            // Either root or Shizuku access is needed to enable/disable mobile data and Wi-Fi.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(context))
            return
        }

        if (getRequiresUnlock()) {
            unlockAndRun { cycleInternet() }
        } else {
            cycleInternet()
        }
    }

    override fun getTileState(): TileState {
        val tile = TileState()
        val dataEnabled = getDataEnabled(context)
        val wifiEnabled = getWifiEnabled(context)

        when {
            (TileSyncService.isTurningOnWifi || wifiEnabled) && !TileSyncService.isTurningOnData -> {

                if (wifiEnabled) {
                    TileSyncService.isTurningOnWifi = false
                }

                tile.state = Tile.STATE_ACTIVE
                tile.icon = getWifiIcon(context)
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
                tile.icon = getCellularNetworkIcon(context)
                tile.label = getCellularNetworkText(
                    context,
                    CellularChangeListener.currentTelephonyDisplayInfo
                )
            }
            else -> {

                tile.state = Tile.STATE_INACTIVE
                tile.icon = R.drawable.ic_baseline_public_off_24
                tile.label = resources.getString(R.string.internet)
            }
        }

        return tile
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