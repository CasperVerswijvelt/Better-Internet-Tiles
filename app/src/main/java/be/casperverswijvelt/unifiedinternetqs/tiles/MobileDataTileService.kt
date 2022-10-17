package be.casperverswijvelt.unifiedinternetqs.tiles

import android.content.*
import android.graphics.drawable.Icon
import android.os.Handler
import android.service.quicksettings.Tile
import android.util.Log
import androidx.preference.PreferenceManager
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeCallback
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeType
import be.casperverswijvelt.unifiedinternetqs.util.*

class MobileDataTileService : ReportingTileService() {

    private companion object {
        const val TAG = "MobileDataTile"
    }

    private var sharedPreferences: SharedPreferences? = null

    private var isTurningOnData = false
    private var isTurningOffData = false

    private val runToggleMobileData = Runnable {
        toggleMobileData()
        syncTile()
    }

    private val networkChangeCallback = object : NetworkChangeCallback {
        override fun handleChange(type: NetworkChangeType?) {
            syncTile()
        }
    }
    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                syncTile()
            }
        }
    }

    private var cellularChangeListener: CellularChangeListener? = null
    private var mainHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        log("Mobile data tile service created")

        mainHandler = Handler(mainLooper)

        cellularChangeListener = CellularChangeListener(networkChangeCallback)
        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()

        syncTile()
        setListeners()
    }


    override fun onStopListening() {
        super.onStopListening()

        removeListeners()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        removeListeners()
    }

    override fun onClick() {
        super.onClick()

        if (!hasShellAccess(applicationContext)) {

            // Either root or Shizuku access is needed to enable/disable mobile data and Wi-Fi.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(applicationContext))
            return
        }

        if (
            sharedPreferences?.getBoolean(
                resources.getString(R.string.require_unlock_key),
                true
            ) == true
        ) {

            unlockAndRun(runToggleMobileData)

        } else {

            mainHandler?.post(runToggleMobileData)
        }
    }

    private fun toggleMobileData() {

        val dataEnabled = getDataEnabled(applicationContext)

        if (dataEnabled || isTurningOnData) {
            isTurningOnData = false
            isTurningOffData = true
            executeShellCommandAsync("svc data disable") {
                syncTile()
            }
        } else {
            isTurningOnData = true
            isTurningOffData = false
            executeShellCommandAsync("svc data enable") {
                syncTile()
            }
        }
    }

    private fun syncTile() {

        qsTile?.let {

            val airplaneModeEnabled = getAirplaneModeEnabled(applicationContext)
            val dataEnabled = getDataEnabled(applicationContext)

            if (airplaneModeEnabled) {

                it.state = Tile.STATE_UNAVAILABLE
                it.subtitle = getText(R.string.airplane_mode)

            } else if ((dataEnabled && !isTurningOffData) || isTurningOnData) {

                if (dataEnabled) isTurningOnData = false

                it.state = Tile.STATE_ACTIVE
                it.icon = getCellularNetworkIcon(applicationContext)
                it.subtitle = getCellularNetworkText(
                    applicationContext,
                    cellularChangeListener?.currentTelephonyDisplayInfo
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

    private fun setListeners() {

        log("Setting listeners")

        cellularChangeListener?.startListening(applicationContext)

        registerReceiver(
            airplaneModeReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        )
    }

    private fun removeListeners() {

        log("Removing listeners")

        cellularChangeListener?.stopListening(applicationContext)
        try {
            unregisterReceiver(airplaneModeReceiver)
        } catch (e: IllegalArgumentException) {
            // Ignore
        }
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}