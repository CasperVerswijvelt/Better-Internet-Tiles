package be.casperverswijvelt.unifiedinternetqs.tiles

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Handler
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager
import be.casperverswijvelt.unifiedinternetqs.*
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeCallback
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeType
import be.casperverswijvelt.unifiedinternetqs.listeners.WifiChangeListener
import be.casperverswijvelt.unifiedinternetqs.util.*

class WifiTileService : TileService() {

    private companion object {
        const val TAG = "WifiTile"
    }

    private var wifiConnected = false
    private var wifiSSID: String? = null
    private var sharedPreferences: SharedPreferences? = null

    private var isTurningOnWifi = false
    private var isTurningOffWifi = false

    private val runToggleInternet = Runnable {
        toggleWifi()
        syncTile()
    }
    private val networkChangeCallback = object : NetworkChangeCallback {
        override fun handleChange(type: NetworkChangeType?) {
            if (type == NetworkChangeType.NETWORK_LOST) {
                wifiConnected = false
                wifiSSID = null
                setLastConnectedWifi(applicationContext, wifiSSID)
            }
            else if (type == NetworkChangeType.NETWORK_AVAILABLE) {
                wifiConnected = true
                getConnectedWifiSSID {
                    wifiSSID = it
                    setLastConnectedWifi(applicationContext, wifiSSID)
                    syncTile()
                }
            }
            syncTile()
        }
    }

    private var wifiChangeListener: WifiChangeListener? = null
    private var mainHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        log("Wi-Fi tile service created")

        mainHandler = Handler(mainLooper)

        wifiChangeListener = WifiChangeListener(networkChangeCallback)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (getWifiEnabled(this)) {
            wifiSSID = sharedPreferences
                ?.getString(resources.getString(R.string.last_connected_wifi_key), null)
        }
    }
    override fun onStartListening() {
        super.onStartListening()

        setListeners()
        syncTile()
    }


    override fun onStopListening() {
        super.onStopListening()

        removeListeners()
    }

    override fun onTileAdded() {
        super.onTileAdded()

        setListeners()
        syncTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        removeListeners()
    }

    override fun onClick() {
        super.onClick()

        if (!hasShellAccess()) {

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

            unlockAndRun(runToggleInternet)

        } else {

            mainHandler?.post(runToggleInternet)
        }
    }

    private fun toggleWifi() {

        val wifiEnabled = getWifiEnabled(applicationContext)

        if (wifiEnabled || isTurningOnWifi) {
            isTurningOnWifi = false
            isTurningOffWifi = true
            executeShellCommandAsync("svc wifi disable") {
                if (it?.isSuccess != true) {
                    isTurningOffWifi = false
                }
                syncTile()
            }
        } else {
            isTurningOnWifi = true
            isTurningOffWifi = false
            executeShellCommandAsync("svc wifi enable") {
                if (it?.isSuccess != true) {
                    isTurningOnWifi = false
                }
                syncTile()
            }
        }
    }

    private fun syncTile() {

        val wifiEnabled = getWifiEnabled(applicationContext)

        if ((wifiEnabled && !isTurningOffWifi) || isTurningOnWifi) {

            if (wifiEnabled) isTurningOnWifi = false

            // Update tile properties

            qsTile.label = (if (wifiConnected) wifiSSID else null)
                ?: resources.getString(R.string.wifi)
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.icon = getWifiIcon(applicationContext)
            qsTile.subtitle = if (isTurningOnWifi) resources.getString(R.string.turning_on) else resources.getString(R.string.on)

        } else {

            if (!wifiEnabled) isTurningOffWifi = false

            qsTile.label = resources.getString(R.string.wifi)
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.icon = Icon.createWithResource(
                applicationContext,
                R.drawable.ic_baseline_signal_wifi_0_bar_24
            )
            qsTile.subtitle = resources.getString(R.string.off)
        }

        qsTile.updateTile()
    }

    private fun setListeners() {

        log("Setting listeners")

        wifiConnected = false

        wifiChangeListener?.startListening(applicationContext)
    }

    private fun removeListeners() {

        log("Removing listeners")

        wifiChangeListener?.stopListening(applicationContext)
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}