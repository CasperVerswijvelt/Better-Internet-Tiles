package be.casperverswijvelt.unifiedinternetqs.tiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOffWifi
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOnWifi
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.wifiConnected
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.wifiSSID
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.getWifiEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiIcon
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WifiTileService : ReportingTileService() {

    private companion object {
        const val TAG = "WifiTile"
    }

    private lateinit var preferences: BITPreferences

    private val runToggleInternet = Runnable {
        toggleWifi()
        syncTile()
    }

    override fun onCreate() {
        super.onCreate()
        log("Wi-Fi tile service created")

        preferences = BITPreferences(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        log("Start listening")

        syncTile()
    }

    override fun onClick() {
        super.onClick()

        log("onClick")

        if (!hasShellAccess(applicationContext)) {

            // Either root or Shizuku access is needed to enable/disable mobile data and Wi-Fi.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(applicationContext))
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

    private fun toggleWifi() {

        val wifiEnabled = getWifiEnabled(applicationContext)

        if (wifiEnabled || isTurningOnWifi) {
            isTurningOnWifi = false
            isTurningOffWifi = true
            executeShellCommandAsync("svc wifi disable", applicationContext) {
                if (it?.isSuccess != true) {
                    isTurningOffWifi = false
                }
                syncTile()
                requestUpdateTile()
            }
        } else {
            isTurningOnWifi = true
            isTurningOffWifi = false
            executeShellCommandAsync("svc wifi enable", applicationContext) {
                if (it?.isSuccess != true) {
                    isTurningOnWifi = false
                }
                syncTile()
                requestUpdateTile()
            }
        }
    }

    private fun syncTile() {

        qsTile?.let {

            val wifiEnabled = getWifiEnabled(applicationContext)

            if ((wifiEnabled && !isTurningOffWifi) || isTurningOnWifi) {

                if (wifiEnabled) isTurningOnWifi = false

                it.label = (if (wifiConnected) wifiSSID else null)
                    ?: resources.getString(R.string.wifi)
                it.state = Tile.STATE_ACTIVE
                it.icon = getWifiIcon(applicationContext)
                it.subtitle =
                    if (isTurningOnWifi) resources.getString(R.string.turning_on) else resources.getString(
                        R.string.on
                    )

            } else {

                if (!wifiEnabled) isTurningOffWifi = false

                it.label = resources.getString(R.string.wifi)
                it.state = Tile.STATE_INACTIVE
                it.icon = Icon.createWithResource(
                    applicationContext,
                    R.drawable.ic_baseline_signal_wifi_0_bar_24
                )
                it.subtitle = resources.getString(R.string.off)
            }

            it.updateTile()
        }
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}