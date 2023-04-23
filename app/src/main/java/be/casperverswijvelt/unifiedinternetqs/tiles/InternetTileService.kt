package be.casperverswijvelt.unifiedinternetqs.tiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOnData
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOnWifi
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.wifiConnected
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.wifiSSID
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkIcon
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkText
import be.casperverswijvelt.unifiedinternetqs.util.getDataEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.getWifiEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiIcon
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class InternetTileService : ReportingTileService() {

    private companion object {
        const val TAG = "InternetTile"
    }

    private lateinit var preferences: BITPreferences

    private val runCycleInternet = Runnable {
        cycleInternet()
        syncTile()
    }

    override fun onCreate() {
        super.onCreate()
        log("Internet tile service created")

        preferences = BITPreferences(this)

        runBlocking {
            wifiSSID = preferences.getLastConnectedSSID.first()
        }
    }

    override fun onStartListening() {
        super.onStartListening()

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

            unlockAndRun(runCycleInternet)

        } else {

            runCycleInternet.run()
        }
    }

    private fun cycleInternet() {

        // Cycle trough internet connection modes:
        //  If Wi-Fi is enabled -> disable Wi-Fi and enable mobile data
        //  If mobile data is enabled -> disable mobile data and enable Wi-Fi
        //  Else -> enable Wi-Fi

        val dataEnabled = getDataEnabled(applicationContext)
        val wifiEnabled = getWifiEnabled(applicationContext)

        isTurningOnData = false
        isTurningOnWifi = false

        when {
            wifiEnabled -> {
                executeShellCommandAsync("svc wifi disable", applicationContext)

                isTurningOnData = true
                executeShellCommandAsync("svc data enable", applicationContext) {
                    if (it?.isSuccess != true) {
                        isTurningOnData = false
                    }
                    syncTile()
                    requestUpdateTile()
                }
            }
            dataEnabled -> {
                executeShellCommandAsync("svc data disable", applicationContext)

                isTurningOnWifi = true
                executeShellCommandAsync("svc wifi enable", applicationContext) {
                    if (it?.isSuccess != true) {
                        isTurningOnWifi = false
                    }
                    syncTile()
                    requestUpdateTile()
                }
            }
            else -> {
                isTurningOnWifi = true
                executeShellCommandAsync("svc wifi enable", applicationContext) {
                    if (it?.isSuccess != true) {
                        isTurningOnWifi = false
                    }
                    syncTile()
                    requestUpdateTile()
                }
            }
        }
    }

    private fun syncTile() {

        qsTile?.let {

            val dataEnabled = getDataEnabled(applicationContext)
            val wifiEnabled = getWifiEnabled(applicationContext)

            when {
                (isTurningOnWifi || wifiEnabled) && !isTurningOnData -> {

                    if (wifiEnabled) {
                        isTurningOnWifi = false
                    }

                    it.state = Tile.STATE_ACTIVE
                    it.icon = getWifiIcon(applicationContext)
                    it.label = if (isTurningOnWifi)
                        resources.getString(R.string.turning_on)
                    else
                        (if (wifiConnected) wifiSSID else null)
                            ?: resources.getString(R.string.not_connected)
                }
                isTurningOnData || dataEnabled -> {

                    if (dataEnabled) {
                        isTurningOnData = false
                    }

                    it.state = Tile.STATE_ACTIVE
                    it.icon = getCellularNetworkIcon(applicationContext)
                    it.label = getCellularNetworkText(
                        applicationContext,
                        CellularChangeListener.currentTelephonyDisplayInfo
                    )
                }
                else -> {

                    it.state = Tile.STATE_INACTIVE
                    it.icon = Icon.createWithResource(
                        applicationContext,
                        R.drawable.ic_baseline_public_off_24
                    )
                    it.label = resources.getString(R.string.internet)
                }
            }

            it.updateTile()
        }
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}