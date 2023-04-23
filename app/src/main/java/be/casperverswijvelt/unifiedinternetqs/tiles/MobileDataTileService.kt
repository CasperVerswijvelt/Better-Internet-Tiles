package be.casperverswijvelt.unifiedinternetqs.tiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOffData
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOnData
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getAirplaneModeEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkIcon
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkText
import be.casperverswijvelt.unifiedinternetqs.util.getDataEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MobileDataTileService : ReportingTileService() {

    private companion object {
        const val TAG = "MobileDataTile"
    }

    private lateinit var preferences: BITPreferences

    private val runToggleMobileData = Runnable {
        toggleMobileData()
        syncTile()
    }

    override fun onCreate() {
        super.onCreate()
        log("Mobile data tile service created")

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

            unlockAndRun(runToggleMobileData)

        } else {
            runToggleMobileData.run()
        }
    }

    private fun toggleMobileData() {

        val dataEnabled = getDataEnabled(applicationContext)

        if (dataEnabled || isTurningOnData) {
            isTurningOnData = false
            isTurningOffData = true
            executeShellCommandAsync("svc data disable", applicationContext) {
                syncTile()
                requestUpdateTile()
            }
        } else {
            isTurningOnData = true
            isTurningOffData = false
            executeShellCommandAsync("svc data enable", applicationContext) {
                syncTile()
                requestUpdateTile()
            }
        }
        syncTile()
    }

    private fun syncTile() {

        qsTile?.let {

            val airplaneModeEnabled = getAirplaneModeEnabled(applicationContext)
            val dataEnabled = getDataEnabled(applicationContext)

            it.label = getText(R.string.mobile_data)

            if (airplaneModeEnabled) {

                it.state = Tile.STATE_UNAVAILABLE
                it.subtitle = getText(R.string.airplane_mode)

            } else if ((dataEnabled && !isTurningOffData) || isTurningOnData) {

                if (dataEnabled) isTurningOnData = false

                it.state = Tile.STATE_ACTIVE
                it.icon = getCellularNetworkIcon(applicationContext)
                it.subtitle = getCellularNetworkText(
                    applicationContext,
                    CellularChangeListener.currentTelephonyDisplayInfo
                )

            } else {

                if (!dataEnabled) isTurningOffData = false

                it.state = Tile.STATE_INACTIVE
                it.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_mobile_data_24
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