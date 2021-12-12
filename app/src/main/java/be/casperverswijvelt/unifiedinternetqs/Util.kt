package be.casperverswijvelt.unifiedinternetqs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.service.quicksettings.TileService
import android.telephony.TelephonyManager
import android.util.Log
import com.topjohnwu.superuser.Shell
import java.lang.reflect.Method

const val TAG = "Util"

fun getDataEnabled(context: Context): Boolean {

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
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

fun getWifiEnabled(context: Context): Boolean {

    return (context.getSystemService(TileService.WIFI_SERVICE) as WifiManager).isWifiEnabled
}

fun getConnectedWifiSSID(): String? {

    if (Shell.rootAccess()) {
        val wifiDump = Shell.su(
            "dumpsys netstats | grep -E 'iface=wlan.*networkId'"
        ).exec().out
        val pattern = "(?<=networkId=\").*(?=\")".toRegex()
        wifiDump.forEach { wifiString ->
            pattern.find(wifiString)?.let {
                return it.value
            }
        }
    }

    return  null
}

fun getWifiIcon(context: Context) : Icon {

    val wm = context.getSystemService(TileService.WIFI_SERVICE) as WifiManager
    val rssi: Int? = try {
        wm.connectionInfo.rssi
    } catch (e: Exception) {
        log("Could not get Wi-Fi RSSI: ${e.message}")
        null
    }
    val signalStrength = rssi?.let {
        // We use 5 levels for our icon visualisation, so we use this deprecated
        //  calculation with 'numLevels' parameter. We don't want to use the system's
        //  level system since it might differ.
        WifiManager.calculateSignalLevel(it, 5) // 0-4
    } ?: 0

    return Icon.createWithResource(
        context,
        when (signalStrength) {
            4 -> R.drawable.ic_baseline_signal_wifi_4_bar_24
            3 -> R.drawable.ic_baseline_signal_wifi_3_bar_24
            2 -> R.drawable.ic_baseline_signal_wifi_2_bar_24
            1 -> R.drawable.ic_baseline_signal_wifi_1_bar_24
            else -> R.drawable.ic_baseline_signal_wifi_0_bar_24
        }
    )
}

fun getCellularNetworkIcon(context: Context): Icon {

    val tm = context.getSystemService(TileService.TELEPHONY_SERVICE) as TelephonyManager
    val signalStrength = tm.signalStrength?.level ?: 0

    return Icon.createWithResource(
        context,
        when (signalStrength) {
            4 -> R.drawable.ic_baseline_signal_cellular_4_bar_24
            3 -> R.drawable.ic_baseline_signal_cellular_3_bar_24
            2 -> R.drawable.ic_baseline_signal_cellular_2_bar_24
            1 -> R.drawable.ic_baseline_signal_cellular_1_bar_24
            else -> R.drawable.ic_baseline_signal_cellular_null_24
        }
    )
}

fun getCellularNetworkText(context: Context): String? {

    val info = ArrayList<String>()
    val tm = context.getSystemService(TileService.TELEPHONY_SERVICE) as TelephonyManager

    tm.networkOperatorName?.let {
        info.add(it)
    }

    if (
        context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
        == PackageManager.PERMISSION_GRANTED
    ) {
        getNetworkClassString(tm.dataNetworkType)?.let {
            info.add(it)
        }
    }

    return info.joinToString(separator = ", ")
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

private fun log(text: String) {
    Log.d(TAG, text)
}