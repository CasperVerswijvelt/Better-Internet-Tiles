package be.casperverswijvelt.unifiedinternetqs.listeners

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager

class CellularChangeListener(private val callback: (type: NetworkChangeType?, args: List<Any?>?) -> Unit) {

    companion object {
        var currentTelephonyDisplayInfo: TelephonyDisplayInfo? = null
    }

    private var isListening = false

    private val mobileNetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                callback(NetworkChangeType.NETWORK_AVAILABLE, null)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                callback(NetworkChangeType.NETWORK_LOST, null)
            }
        }

    private val telephonyCallback = object : TelephonyCallback(),
        TelephonyCallback.SignalStrengthsListener,
        TelephonyCallback.DisplayInfoListener,
        TelephonyCallback.ServiceStateListener {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            callback(NetworkChangeType.SIGNAL_STRENGTH, null)
        }

        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            currentTelephonyDisplayInfo = telephonyDisplayInfo
            callback(NetworkChangeType.DISPLAY_INFO, null)
        }

        override fun onServiceStateChanged(serviceState: ServiceState) {
            callback(NetworkChangeType.SERVICE_STATE, listOf(serviceState))
        }
    }

    fun startListening(context: Context) {

        // Cellular network state
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        cm.registerNetworkCallback(networkRequest, mobileNetworkCallback)

        // Mobile signal strength listener
        val tm =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.registerTelephonyCallback(
            context.mainExecutor,
            telephonyCallback
        )

        isListening = true
    }

    fun stopListening(context: Context) {

        if (isListening) {

            isListening = false

            // Cellular network state
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(mobileNetworkCallback)

            // Mobile signal strength listener
            val tm =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.unregisterTelephonyCallback(telephonyCallback)

            currentTelephonyDisplayInfo = null
        }
    }
}