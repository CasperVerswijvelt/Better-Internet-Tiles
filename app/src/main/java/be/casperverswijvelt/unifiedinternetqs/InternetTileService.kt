package be.casperverswijvelt.unifiedinternetqs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.PhoneStateListener
import android.util.Log
import android.telephony.TelephonyManager
import android.telephony.SignalStrength
import com.topjohnwu.superuser.Shell
import java.lang.reflect.Method

class InternetTileService : TileService() {

    private companion object {
        const val TAG = "UnifiedInternet"

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

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            syncTile()
        }

        override fun onLost(network: Network?) {
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

    private val wifiStateReceiverIntentFilter = IntentFilter()

    init {
        wifiStateReceiverIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        wifiStateReceiverIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        wifiStateReceiverIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)
    }

    override fun onCreate() {
        super.onCreate()
        log("Internet tile service created")
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

        requestRoot()

        setListeners()
        syncTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        removeListeners()
    }

    override fun onClick() {
        super.onClick()

        requestRoot()

        // Cycle trough internet connection modes:
        //  If Wi-Fi is enabled -> disable Wi-Fi and enable mobile data
        //  If mobile data is enabled -> disable mobile data and enable Wi-Fi
        //  Else -> enable Wi-Fi

        val dataEnabled = getDataEnabled()
        val wifiEnabled = getWifiEnabled()

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

        syncTile()
    }

    private fun syncTile() {

        val dataEnabled = getDataEnabled()
        val wifiEnabled = getWifiEnabled()
        when {
            wifiEnabled -> {

                // Get Wi-Fi SSID through shell command and regex parsing since app needs access
                //  to fine location to get SSID

                val wifiDump = Shell.su(
                    "dumpsys netstats | grep -E 'iface=wlan.*networkId'"
                ).exec().out
                var ssid: String? = null
                val pattern = "(?<=networkId=\").*(?=\")".toRegex()
                wifiDump.forEach { wifiString ->
                    pattern.find(wifiString)?.let {
                        ssid = it.value
                        return@forEach
                    }
                }

                // Wi-fi signal level through WifiManager

                val wm = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                val rssi: Int? = try {
                    wm.connectionInfo.rssi
                } catch (e: Exception) {
                    log("Could not get Wi-Fi RSSI: ${e.message}")
                    null
                }
                val signalStrength = rssi?.let {
                    WifiManager.calculateSignalLevel(it, 5) // 0-4
                } ?: 0

                // Update tile properties

                qsTile.state = Tile.STATE_ACTIVE
                qsTile.icon = Icon.createWithResource(
                    this,
                    when (signalStrength) {
                        4 -> R.drawable.ic_baseline_signal_wifi_4_bar_24
                        3 -> R.drawable.ic_baseline_signal_wifi_3_bar_24
                        2 -> R.drawable.ic_baseline_signal_wifi_2_bar_24
                        1 -> R.drawable.ic_baseline_signal_wifi_1_bar_24
                        else -> R.drawable.ic_baseline_signal_wifi_0_bar_24
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = ssid
                }
            }
            dataEnabled -> {

                val info = ArrayList<String>()
                var signalStrength = 0

                val tm = applicationContext.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

                try {
                    tm.networkOperatorName?.let {
                        info.add(it)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        signalStrength = tm.signalStrength?.level ?: 0
                    }
                    // TODO fix permission to read dataNetworkType
                    getNetworkClassString(tm.dataNetworkType)?.let {
                        info.add(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Update tile properties

                qsTile.state = Tile.STATE_ACTIVE
                qsTile.icon = Icon.createWithResource(
                    this,
                    when (signalStrength) {
                        4 -> R.drawable.ic_baseline_signal_cellular_4_bar_24
                        3 -> R.drawable.ic_baseline_signal_cellular_3_bar_24
                        2 -> R.drawable.ic_baseline_signal_cellular_2_bar_24
                        1 -> R.drawable.ic_baseline_signal_cellular_1_bar_24
                        else -> R.drawable.ic_baseline_signal_cellular_null_24
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = info.joinToString(separator = ", ")
                }
            }
            else -> {

                // Update tile properties

                qsTile.state = Tile.STATE_INACTIVE
                qsTile.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_public_off_24
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = null
                }
            }
        }

        qsTile.updateTile()
    }

    private fun requestRoot() {

        Shell.getShell()
        log("Root access: " + Shell.rootAccess())

        if (!Shell.rootAccess()) {

            // TODO: Show message to user?
        }
    }

    private fun getDataEnabled(): Boolean {

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        var mobileDataEnabled = false

        // Get mobile data enabled state
        try {
            val cmClass = Class.forName(connectivityManager.javaClass.name)
            val method: Method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = method.invoke(connectivityManager) as Boolean
        } catch (e: Exception) {
            // Empty
        }

        return mobileDataEnabled
    }

    private fun getWifiEnabled(): Boolean {

        return (this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager).isWifiEnabled
    }

    private fun getNetworkClassString(networkType: Int): String? {

        // Use hardcoded values since some are inaccessible, see TelephonyManager

        return when (networkType) {
            1, 16, 2, 4, 7, 11 -> "2G"
            3, 5, 6, 8, 9, 10, 12, 14, 15, 17 -> "3G"
            13, 18, 19 -> "4G"
            20 -> "5G"
            else -> null
        }
    }

    private fun setListeners() {

        // Network listener

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // Mobile signal strength listener

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

        // Wi-Fi enabled state

        registerReceiver(wifiStateReceiver, wifiStateReceiverIntentFilter)
    }

    private fun removeListeners() {

        // Network

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)

        // Mobile signal strength listener

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)

        // Wi-Fi enabled state

        unregisterReceiver(wifiStateReceiver)
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}