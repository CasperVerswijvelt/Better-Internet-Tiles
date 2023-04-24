package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.getWifiEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiIcon
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WifiTileBehaviour(
    context: Context,
    showDialog: (Dialog) -> Unit = {},
    unlockAndRun: (Runnable) -> Unit = { it.run() },
    onRequestUpdate: () -> Unit = {}
): TileBehaviour(context, showDialog, unlockAndRun, onRequestUpdate) {

    companion object {
        private const val TAG = "WifiTileBehaviour"
    }

    private val runToggleInternet = Runnable {
        toggleWifi()
        onRequestUpdate()
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

        val requireUnlock = runBlocking {
            preferences.getRequireUnlock.first()
        }
        if (requireUnlock) {

            unlockAndRun(runToggleInternet)

        } else {

            runToggleInternet.run()
        }
    }

    override fun getTileState(): TileState {
        val tile = TileState()
        val wifiEnabled = getWifiEnabled(context)

        if ((wifiEnabled && !TileSyncService.isTurningOffWifi) || TileSyncService.isTurningOnWifi) {

            if (wifiEnabled) TileSyncService.isTurningOnWifi = false

            tile.label = (if (TileSyncService.wifiConnected) TileSyncService.wifiSSID else null)
                ?: resources.getString(R.string.wifi)
            tile.state = Tile.STATE_ACTIVE
            tile.icon = getWifiIcon(context)
            tile.subtitle =
                if (TileSyncService.isTurningOnWifi) resources.getString(R.string.turning_on) else resources.getString(
                    R.string.on
                )

        } else {

            if (!wifiEnabled) TileSyncService.isTurningOffWifi = false

            tile.label = resources.getString(R.string.wifi)
            tile.state = Tile.STATE_INACTIVE
            tile.icon = R.drawable.ic_baseline_signal_wifi_0_bar_24
            tile.subtitle = resources.getString(R.string.off)
        }
        return tile
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
                onRequestUpdate()
            }
        } else {
            TileSyncService.isTurningOnWifi = true
            TileSyncService.isTurningOffWifi = false
            executeShellCommandAsync("svc wifi enable", context) {
                if (it?.isSuccess != true) {
                    TileSyncService.isTurningOnWifi = false
                }
                onRequestUpdate()
            }
        }
        onRequestUpdate()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}