package be.casperverswijvelt.unifiedinternetqs.listeners

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager

class CellularChangeListener(private val callback: NetworkChangeCallback)  {

    private val mobileNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            callback.handleChange(NetworkChangeType.NETWORK_AVAILABLE)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            callback.handleChange(NetworkChangeType.NETWORK_LOST)
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            callback.handleChange(null)
        }
    }

    private val telephonyCallback : TelephonyCallback? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    callback.handleChange(null)
                }
            }
        } else {
            null
        }

    fun startListening(context: Context) {

        // Cellular network state
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        cm.registerNetworkCallback(networkRequest, mobileNetworkCallback)

        // Mobile signal strength listener
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.registerTelephonyCallback(context.mainExecutor, telephonyCallback!!)
        } else {
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
    }

    fun stopListening(context: Context) {

        // Cellular network state
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(mobileNetworkCallback)

        // Mobile signal strength listener
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm.unregisterTelephonyCallback(telephonyCallback!!)
        } else {
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }
}