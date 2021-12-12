package be.casperverswijvelt.unifiedinternetqs

import android.content.*
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.PhoneStateListener
import android.util.Log
import android.telephony.TelephonyManager
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import androidx.preference.PreferenceManager
import com.topjohnwu.superuser.Shell

class InternetTileService : TileService() {

    private companion object {
        const val TAG = "InternetTile"

        init {
            // Set settings before the main shell can be created
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            )
        }
    }

    private val wifiNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            log("Wi-Fi network available")
            wifiConnected = true
            syncTile()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            log("Wi-Fi network lost")
            wifiConnected = false
            syncTile()
        }
    }

    private val mobileNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            log("Mobile network available")
            syncTile()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            log("Mobile network lost")
            syncTile()
        }
    }

    private val wifiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                WifiManager.RSSI_CHANGED_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                WifiManager.WIFI_STATE_CHANGED_ACTION -> syncTile()
            }
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            syncTile()
        }
    }

    private val telephonyCallback : TelephonyCallback? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    syncTile()
                }
            }
        } else {
            null
        }

    private val wifiStateReceiverIntentFilter = IntentFilter()

    private var wifiConnected = false
    private var sharedPreferences: SharedPreferences? = null
    private val runCycleInternet = object: Runnable {
        override fun run() {
            cycleInternet()
        }
    }

    init {
        wifiStateReceiverIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        wifiStateReceiverIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        wifiStateReceiverIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)
    }

    override fun onCreate() {
        super.onCreate()
        log("Internet tile service created")

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
            return
        }

        if (
            sharedPreferences?.getBoolean(
                resources.getString(R.string.require_unlock_key),
                true
            ) == true
        ) {

            unlockAndRun(runCycleInternet)

        } else {

            cycleInternet()
        }

        syncTile()
    }

    private fun cycleInternet() {
        // Cycle trough internet connection modes:
        //  If Wi-Fi is enabled -> disable Wi-Fi and enable mobile data
        //  If mobile data is enabled -> disable mobile data and enable Wi-Fi
        //  Else -> enable Wi-Fi

        val dataEnabled = getDataEnabled(applicationContext)
        val wifiEnabled = getWifiEnabled(applicationContext)

        when {
            wifiEnabled -> {
                Shell.su("svc wifi disable && svc data enable").exec()
            }
            dataEnabled -> {
                Shell.su("svc data disable && svc wifi enable").exec()
            }
            else -> {
                Shell.su("svc wifi enable").exec()
            }
        }
    }

    private fun syncTile() {

        val dataEnabled = getDataEnabled(applicationContext)
        val wifiEnabled = getWifiEnabled(applicationContext)
        when {
            wifiEnabled -> {

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
            }
            dataEnabled -> {

                // Update tile properties

                qsTile.state = Tile.STATE_ACTIVE
                qsTile.icon = getCellularNetworkIcon(applicationContext)
                qsTile.subtitle = getCellularNetworkText(applicationContext)
            }
            else -> {

                // Update tile properties

                qsTile.state = Tile.STATE_INACTIVE
                qsTile.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_public_off_24
                )
                qsTile.subtitle = null
            }
        }

        qsTile.updateTile()
    }

    private fun setListeners() {

        log("Setting listeners")

        wifiConnected = false

        // Network listeners

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        cm.registerNetworkCallback(networkRequest, wifiNetworkCallback)

        networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        cm.registerNetworkCallback(networkRequest, mobileNetworkCallback)

        // Mobile signal strength listener

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(mainExecutor, telephonyCallback!!)
        } else {
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }

        // Wi-Fi enabled state

        registerReceiver(wifiStateReceiver, wifiStateReceiverIntentFilter)
    }

    private fun removeListeners() {

        log("Removing listeners")

        // Network

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(wifiNetworkCallback)

        // Mobile signal strength listener

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.unregisterTelephonyCallback(telephonyCallback!!)
        } else {
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }

        // Wi-Fi enabled state

        unregisterReceiver(wifiStateReceiver)
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}