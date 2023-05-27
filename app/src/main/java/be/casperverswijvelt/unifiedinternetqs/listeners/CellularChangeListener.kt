package be.casperverswijvelt.unifiedinternetqs.listeners

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.*

class CellularChangeListener(private val callback: (type: NetworkChangeType?) -> Unit) {

    companion object {
        var currentTelephonyDisplayInfo: TelephonyDisplayInfo? = null
    }

    private var isListening = false

    private val mobileNetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                callback(NetworkChangeType.NETWORK_AVAILABLE)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                callback(NetworkChangeType.NETWORK_LOST)
            }
        }

    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java",
            ReplaceWith("callback(NetworkChangeType.SIGNAL_STRENGTH)")
        )
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            callback(NetworkChangeType.SIGNAL_STRENGTH)
        }
    }

    private val telephonyCallback: TelephonyCallback? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : TelephonyCallback(),
                TelephonyCallback.SignalStrengthsListener,
                TelephonyCallback.DisplayInfoListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    callback(NetworkChangeType.SIGNAL_STRENGTH)
                }

                override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                    currentTelephonyDisplayInfo = telephonyDisplayInfo
                    callback(NetworkChangeType.DISPLAY_INFO)
                }
            }
        } else {
            null
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(
                context.mainExecutor,
                telephonyCallback!!
            )
        } else {
            tm.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
            )
        }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                tm.unregisterTelephonyCallback(telephonyCallback!!)
            } else {
                tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            }

            currentTelephonyDisplayInfo = null
        }
    }
}