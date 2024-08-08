package be.casperverswijvelt.unifiedinternetqs.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager

class WifiChangeListener(private val callback: (type: NetworkChangeType?, network: Network?) -> Unit) {

    private val wifiStateReceiverIntentFilter = IntentFilter()
    private var isListening = false

    init {
        wifiStateReceiverIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        wifiStateReceiverIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        wifiStateReceiverIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION)
    }

    fun startListening(context: Context) {

        // Wi-Fi connected network state
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        cm.registerNetworkCallback(networkRequest, wifiNetworkCallback)

        // Wi-Fi enabled state
        context.registerReceiver(
            wifiStateReceiver,
            wifiStateReceiverIntentFilter
        )

        isListening = true
    }

    fun stopListening(context: Context) {

        if (isListening) {

            isListening = false

            // Wi-Fi connected network state
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(wifiNetworkCallback)

            // Wi-Fi enabled state
            context.unregisterReceiver(wifiStateReceiver)
        }
    }

    private val wifiNetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                callback(NetworkChangeType.NETWORK_AVAILABLE, network)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                callback(NetworkChangeType.NETWORK_LOST, network)
            }
        }

    private val wifiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {

            when (intent.action) {
                WifiManager.RSSI_CHANGED_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                WifiManager.WIFI_STATE_CHANGED_ACTION -> callback(
                    NetworkChangeType.SIGNAL_STRENGTH,
                    null
                )
            }
        }
    }
}