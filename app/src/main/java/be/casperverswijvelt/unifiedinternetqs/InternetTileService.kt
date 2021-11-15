package be.casperverswijvelt.unifiedinternetqs

import android.content.Context
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import java.lang.reflect.Method


class InternetTileService : TileService() {

    private companion object {
        val TAG = "UnifiedInternet"
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            log("Network available " + network.toString())
            syncTile()
        }

        override fun onLost(network: Network?) {
            log("Network lost " + network.toString())
            syncTile()
        }
    }

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

        setListeners()
        syncTile ()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        removeListeners()
    }

    override fun onClick() {
        super.onClick()
    }

    private fun syncTile () {

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        var mobileDataEnabled = false
        var wifiEnabled = false

        // Get mobile data enabled state
        try {
            val cmClass = Class.forName(connectivityManager.javaClass.name)
            val method: Method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = method.invoke(connectivityManager) as Boolean
        } catch (e: Exception) {
        }

        // Get Wi-Fi state
        val wifiManager = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        wifiEnabled = wifiManager.isWifiEnabled


        // Logic
        when {
            wifiEnabled -> {

                qsTile.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_signal_wifi_4_bar_24
                )
                qsTile.state = Tile.STATE_ACTIVE

            }
            mobileDataEnabled -> {

                qsTile.icon = Icon.createWithResource(
                    this,
                    R.drawable.ic_baseline_signal_cellular_4_bar_24
                )
                qsTile.state = Tile.STATE_ACTIVE

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

    private fun setListeners () {

        // Network listener
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun removeListeners () {

        // Network
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}