package be.casperverswijvelt.unifiedinternetqs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import java.lang.reflect.Method


class InternetTileService : TileService() {

    private companion object {
        const val TAG = "UnifiedInternet"

        init {
            // Set settings before the main shell can be created
            Shell.enableVerboseLogging = BuildConfig.DEBUG;
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            );
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
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION == intent?.action) {
                syncTile()
            }
        }
    }
    private val wifiStateReceiverIntentFilter =
        IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)

    override fun onStartListening() {
        super.onStartListening()

        setListeners()
        syncTile ()
    }


    override fun onStopListening() {
        super.onStopListening()

        removeListeners()
    }

    override fun onTileAdded() {

        super.onTileAdded()

        requestRoot()

        setListeners()
        syncTile ()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        removeListeners()
    }

    override fun onClick() {
        super.onClick()

        requestRoot()

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

    private fun syncTile () {

        val dataEnabled = getDataEnabled()
        val wifiEnabled = getWifiEnabled()

        // Logic
        when {
            wifiEnabled -> {

                qsTile.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_signal_wifi_4_bar_24
                )
                qsTile.state = Tile.STATE_ACTIVE

                // TODO text based on wifi ssid and icon based on signal strength

            }
            dataEnabled -> {

                qsTile.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_signal_cellular_4_bar_24
                )
                qsTile.state = Tile.STATE_ACTIVE

                // TODO text based on network info and icon based on signal strength

            }
            else -> {

                qsTile.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_public_off_24
                )
                qsTile.state = Tile.STATE_INACTIVE
            }
        }

        qsTile.updateTile()
    }

    private fun requestRoot () {

        Shell.getShell()
        log("Root access: " + Shell.rootAccess())
        if (!Shell.rootAccess()) {

            Toast.makeText(
                this,
                "Root is required to do this!",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun getDataEnabled () : Boolean {

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
        }

        return mobileDataEnabled
    }

    private fun getWifiEnabled () : Boolean{

        return (this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager).isWifiEnabled
    }

    private fun setListeners () {

        // Network listener
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // Wi-Fi enabled state
        registerReceiver(wifiStateReceiver, wifiStateReceiverIntentFilter)
    }

    private fun removeListeners () {

        // Network
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)

        // Wi-Fi enabled state
        unregisterReceiver(wifiStateReceiver)
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}