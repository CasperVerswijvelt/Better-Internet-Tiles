package be.casperverswijvelt.unifiedinternetqs.tiles

import android.graphics.drawable.Icon
import android.os.Handler
import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeCallback
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeType
import be.casperverswijvelt.unifiedinternetqs.listeners.WifiChangeListener
import be.casperverswijvelt.unifiedinternetqs.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WifiTileService : ReportingTileService() {

    private companion object {
        const val TAG = "WifiTile"
    }

    private var wifiConnected = false
    private var wifiSSID: String? = null
    private lateinit var preferences: BITPreferences

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
            } else if (type == NetworkChangeType.NETWORK_AVAILABLE) {
                wifiConnected = true
                getConnectedWifiSSID(applicationContext) {
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
        preferences = BITPreferences(this)

        wifiChangeListener = WifiChangeListener(networkChangeCallback)

        if (getWifiEnabled(this)) {
            // Wi-Fi SSID is retrieved async using shell commands, visualise
            //  last connected Wi-Fi SSID until actual SSID is known.
            // TODO: Get Wi-Fi SSID synchronously using location permission
            wifiSSID = getLastConnectedWifi(this)
        }
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

    override fun onClick() {
        super.onClick()

        if (!hasShellAccess(applicationContext)) {

            // Either root or Shizuku access is needed to enable/disable mobile data and Wi-Fi.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(applicationContext))
            return
        }

        runBlocking {
            if (preferences.getRequireUnlock.first()) {

                unlockAndRun(runToggleInternet)

            } else {

                mainHandler?.post(runToggleInternet)
            }
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
            }
        } else {
            isTurningOnWifi = true
            isTurningOffWifi = false
            executeShellCommandAsync("svc wifi enable", applicationContext) {
                if (it?.isSuccess != true) {
                    isTurningOnWifi = false
                }
                syncTile()
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

    private fun setListeners() {

        log("Setting listeners")

        // set wifiConnected to false, it will be updated asynchronously
        //  after starting to listen to the wifiChangeListener.
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