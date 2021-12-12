package be.casperverswijvelt.unifiedinternetqs.tiles

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager
import be.casperverswijvelt.unifiedinternetqs.*
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeCallback
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeType
import be.casperverswijvelt.unifiedinternetqs.listeners.WifiChangeListener
import com.topjohnwu.superuser.Shell

class WifiTileService : TileService() {

    private companion object {
        const val TAG = "WifiTile"
    }

    private var wifiConnected = false
    private var sharedPreferences: SharedPreferences? = null

    private val runToggleInternet = Runnable {
        toggleInternet()
        syncTile()
    }
    private val networkChangeCallback = object : NetworkChangeCallback {
        override fun handleChange(type: NetworkChangeType?) {
            when (type) {
                NetworkChangeType.NETWORK_LOST -> wifiConnected = false
                NetworkChangeType.NETWORK_AVAILABLE -> wifiConnected = true
            }
            syncTile()
        }
    }

    private var wifiChangeListener: WifiChangeListener? = null

    override fun onCreate() {
        super.onCreate()
        log("Internet tile service created")

        wifiChangeListener = WifiChangeListener(networkChangeCallback)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
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

        if (!Shell.rootAccess()) {

            // Root access is needed to enable/disable mobile data and Wi-Fi. There is currently
            //  no other way to do this, so this functionality will not work without root access.
            showDialog(getRootAccessRequiredDialog(applicationContext))
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

            runToggleInternet.run()
        }
    }

    private fun toggleInternet() {

        val wifiEnabled = getWifiEnabled(applicationContext)

        if (wifiEnabled) {
            Shell.su("svc wifi disable").exec()
        } else {
            Shell.su("svc wifi enable").exec()
        }
    }

    private fun syncTile() {

        val wifiEnabled = getWifiEnabled(applicationContext)

        if (wifiEnabled) {

            // If Wi-Fi is connected, get Wi-Fi SSID through shell command and regex parsing since app needs access
            //  to fine location to get SSID

            var ssid: String? = null

            if (wifiConnected) {
                ssid = getConnectedWifiSSID()
            }

            // Update tile properties

            qsTile.state = Tile.STATE_ACTIVE
            qsTile.icon = getWifiIcon(applicationContext)
            qsTile.subtitle = ssid

        } else {

            qsTile.state = Tile.STATE_INACTIVE
            qsTile.icon = Icon.createWithResource(
                this,
                R.drawable.ic_baseline_signal_wifi_off_24
            )
            qsTile.subtitle = null
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