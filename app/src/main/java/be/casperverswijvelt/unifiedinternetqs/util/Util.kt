package be.casperverswijvelt.unifiedinternetqs.util

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import android.util.Log
import androidx.preference.PreferenceManager
import be.casperverswijvelt.unifiedinternetqs.BuildConfig
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.data.ShellMethod
import be.casperverswijvelt.unifiedinternetqs.tiles.InternetTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.MobileDataTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.NFCTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiTileService
import be.casperverswijvelt.unifiedinternetqs.ui.MainActivity
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.lang.reflect.Method
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID

const val TAG = "Util"

// Connectivity

fun getDataEnabled(context: Context): Boolean {

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
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

    return (context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        .isWifiEnabled
}

fun getNFCEnabled(context: Context): Boolean {

    return (context.getSystemService(Context.NFC_SERVICE) as NfcManager)
        .defaultAdapter?.isEnabled ?: false
}

fun getAirplaneModeEnabled(context: Context): Boolean {

    return Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.AIRPLANE_MODE_ON,
        0
    ) != 0
}

fun getConnectedWifiSSID(
    context: Context,
    callback: ((String?) -> Unit)
) {

    if (hasShellAccess(context)) {
        executeShellCommandAsync(
            command = "dumpsys netstats | grep -E 'iface=wlan.*(networkId|wifiNetworkKey)'",
            context = context
        ) {
            val pattern = "(?<=(networkId|wifiNetworkKey)=\").*(?=\")".toRegex()
            it?.out?.forEach { wifiString ->
                pattern.find(wifiString)?.let { matchResult ->
                    callback(matchResult.value)
                    return@executeShellCommandAsync
                }
            }
            callback(null)
        }
    } else {
        callback(null)
    }
}

fun getWifiIcon(context: Context): Int {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val rssi = cm.activeNetwork?.let {
        (cm.getNetworkCapabilities(it)?.transportInfo as? WifiInfo)?.rssi
    }
    val signalStrength = rssi?.let {
        // We use 5 levels for our icon visualisation, so we use this deprecated
        //  calculation with 'numLevels' parameter. We don't want to use the system's
        //  level system since it might differ.
        WifiManager.calculateSignalLevel(it, 5) // 0-4
    } ?: 0

    return when (signalStrength) {
        4 -> R.drawable.ic_baseline_signal_wifi_4_bar_24
        3 -> R.drawable.ic_baseline_signal_wifi_3_bar_24
        2 -> R.drawable.ic_baseline_signal_wifi_2_bar_24
        1 -> R.drawable.ic_baseline_signal_wifi_1_bar_24
        else -> R.drawable.ic_baseline_signal_wifi_0_bar_24
    }
}

fun getCellularNetworkIcon(context: Context): Int {

    val tm =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // TODO: We should try to get the signal strength of the data sim here.
    //  Only solution I found to do this requires fine location access, which I don't really want
    //  to add.

    return when (tm.signalStrength?.level ?: 0) {
        4 -> R.drawable.ic_baseline_signal_cellular_4_bar_24
        3 -> R.drawable.ic_baseline_signal_cellular_3_bar_24
        2 -> R.drawable.ic_baseline_signal_cellular_2_bar_24
        1 -> R.drawable.ic_baseline_signal_cellular_1_bar_24
        else -> R.drawable.ic_baseline_signal_cellular_0_bar
    }
}

fun getCellularNetworkText(
    context: Context,
    telephonyDisplayInfo: TelephonyDisplayInfo?
): String {

    val info = ArrayList<String>()
    val tm =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val subscriptionInfo = getDataSubscriptionInfo(context)
    // No data sim set or no read phone state permission
        ?: return context.getString(R.string.network_not_available)

    subscriptionInfo.displayName?.let {
        info.add(it.toString())
    }

    // TODO: Use signal strength of data SIM
    if (tm.signalStrength?.level == 0) {

        // No service
        return context.getString(R.string.no_service)
    }

    var connType: String? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        connType = telephonyDisplayInfo?.let {
            when (telephonyDisplayInfo.overrideNetworkType) {
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> "4G+"
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> "5Ge"
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> "5G"
                TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> "5G+"
                else -> null
            }
        }
    }

    // Fallback
    if (
        connType == null &&
        context.checkSelfPermission(
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        connType = getNetworkClassString(tm.dataNetworkType)
    }

    connType?.let { info.add(it) }

    return info.joinToString(separator = ", ")
}

fun getDataSubscriptionInfo(context: Context): SubscriptionInfo? {

    if (
        context.checkSelfPermission(
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val sm =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as
                    SubscriptionManager
        val subId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SubscriptionManager.getActiveDataSubscriptionId()
        } else {
            SubscriptionManager.getDefaultSubscriptionId()
        }

        if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {

            return sm.getActiveSubscriptionInfo(subId)
        }
    }
    // No data sim set or no read phone state permission
    return null
}

private fun getNetworkClassString(networkType: Int): String? {

    return when (networkType) {
        TelephonyManager.NETWORK_TYPE_GSM,
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT -> "2G"
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN -> "4G"
        TelephonyManager.NETWORK_TYPE_NR -> "5G"
        else -> null
    }
}

fun setLastConnectedWifi(context: Context, ssid: String?) = runBlocking  {
    BITPreferences(context).setLastConnectedSSID(ssid)
}

fun getLastConnectedWifi(context: Context): String? = runBlocking {
    BITPreferences(context).getLastConnectedSSID.first()
}

// Shell access

fun getShellAccessRequiredDialog(context: Context): Dialog {

    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    return AlertDialog.Builder(context)
        .setTitle(R.string.shell_access_required)
        .setMessage(R.string.shell_access_not_set_up)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            if (ShizukuUtil.shizukuAvailable) {
                ShizukuUtil.requestShizukuPermission { }
            } else {
                context.startActivity(intent)
            }
        }
        .setCancelable(true)
        .create()
}

fun executeShellCommand(command: String, context: Context): Shell.Result? {
    val preferences = BITPreferences(context = context)
    val shellMethod = runBlocking {
         preferences.getShellMethod.first()
    }

    when (shellMethod) {
        ShellMethod.ROOT -> {
            if (Shell.isAppGrantedRoot() == true) {
                return Shell.cmd(command).exec()
            }
        }
        ShellMethod.SHIZUKU -> {
            if (ShizukuUtil.hasShizukuPermission()) {
                return executeShizukuCommand(command)
            }
        }
        ShellMethod.AUTO -> {
            if (Shell.isAppGrantedRoot() == true) {
                return Shell.cmd(command).exec()
            } else if (ShizukuUtil.hasShizukuPermission()) {
                return executeShizukuCommand(command)
            }
        }
    }

    return null
}

private fun executeShizukuCommand(command: String): Shell.Result {
    val process = ShizukuUtil.executeCommand(command)
    return object : Shell.Result() {
        override fun getOut(): MutableList<String> {
            return process
                .inputStream.bufferedReader()
                .use { it.readText() }
                .split("\n".toRegex())
                .toMutableList()
        }

        override fun getErr(): MutableList<String> {
            return process
                .errorStream.bufferedReader()
                .use { it.readText() }
                .split("\n".toRegex())
                .toMutableList()
        }

        override fun getCode(): Int {
            return process.exitValue()
        }
    }
}

fun executeShellCommandAsync(
    command: String,
    context: Context,
    callback: ((Shell.Result?) -> Unit)? = {}
) {
    ExecutorServiceSingleton.getInstance().execute {
        val result = executeShellCommand(command, context)
        callback?.let { it(result) }
    }
}

/**
 * Check if app has shell access (either root or shizuku).
 * If shell access is detected, the shizuku detection service is automatically stopped
 */
fun hasShellAccess(context: Context? = null): Boolean {

    return Shell.isAppGrantedRoot() == true || ShizukuUtil.hasShizukuPermission()
}

fun grantReadPhoneState(context: Context, callback: ((Shell.Result?) -> Unit)? = {}) {
    return executeShellCommandAsync(
        context = context,
        command = "pm grant ${BuildConfig.APPLICATION_ID} ${Manifest.permission.READ_PHONE_STATE}",
        callback = callback,
    )
}

// Analytics

class Analytics {
    companion object {
        var reportMutex = Object()
    }
}

fun saveTileUsed(instance: TileService) {
    // TODO replace with datastore
    PreferenceManager.getDefaultSharedPreferences(instance)
        ?.edit()
        ?.putLong(instance.javaClass.name, System.currentTimeMillis())
        ?.apply()
}

fun reportToAnalytics(context: Context) {
    if (BuildConfig.DEBUG) return
    Thread {
        synchronized(Analytics.reportMutex) {
            try {
                val sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(context)
                val lastReportTimestampKey = "LAST_REPORT_TIMESTAMP"

                val lastReportTimestamp = sharedPref.getLong(
                    lastReportTimestampKey,
                    0
                )
                val current = System.currentTimeMillis()
                val minDiff = hoursToMs(12)
                val diff = current - lastReportTimestamp

                // Only send analytics data if last sent out report was more
                //  than 12 hours ago
                if (diff >= minDiff) {

                    log("Sending Analytics data")
                    val url =
                        URL("https://bitanalytics.casperverswijvelt" +
                                ".be/api/report")
                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        doOutput = true
                        connectTimeout = 10000
                        readTimeout = 20000

                        // JSON Format
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Accept", "application/json")

                        // Small JSON message containing some basic information
                        //  to report to analytics. This data is used for
                        //  informational purpose only.
                        val data = JSONObject()
                        val dynamic = JSONObject()
                        val static = JSONObject()
                        val tiles = JSONObject()

                        static.put("uuid", getInstallId(context))
                        static.put("brand", Build.BRAND)
                        static.put("model", Build.MODEL)

                        dynamic.put("sdk", Build.VERSION.SDK_INT)
                        dynamic.put("version", BuildConfig.VERSION_CODE)
                        dynamic.put("lang", Locale.getDefault().language)
                        dynamic.put("dist", BuildConfig.FLAVOR)
                        dynamic.put(
                            "shell",
                            when {
                                Shell.isAppGrantedRoot() == true -> "root"
                                ShizukuUtil.hasShizukuPermission() -> "shizuku"
                                else -> "none"
                            }
                        )

                        tiles.put(
                            "internet",
                            wasTileUsedInLastXHours(
                                InternetTileService::class.java,
                                sharedPref
                            )
                        )
                        tiles.put(
                            "data",
                            wasTileUsedInLastXHours(
                                MobileDataTileService::class.java,
                                sharedPref
                            )
                        )
                        tiles.put(
                            "wifi",
                            wasTileUsedInLastXHours(
                                WifiTileService::class.java,
                                sharedPref
                            )
                        )
                        tiles.put(
                            "nfc",
                            wasTileUsedInLastXHours(
                                NFCTileService::class.java,
                                sharedPref
                            )
                        )

                        dynamic.put("tiles", tiles)
                        data.put("static", static)
                        data.put("dynamic", dynamic)

                        val dataString = data
                            .toString()
                            .toByteArray(Charsets.UTF_8)
                        outputStream.write(
                            dataString,
                            0,
                            dataString.size
                        )

                        log(
                            "\nSuccessfully sent 'POST' request to URL : $url " +
                                    "with data ${dataString};" +
                                    " Response Code: " +
                                    "$responseCode"
                        )
                    }

                    // Save timestamp in shared preferences
                    sharedPref.edit().putLong(
                        lastReportTimestampKey,
                        current
                    ).apply()
                } else {
                    log("Already sent analytics report ${diff/1000/60/60} " +
                            "hours ago")
                }
            } catch (e: Exception) {
                log("Error sending analytics data: $e")
            }
        }
    }.start()
}

fun getInstallId(context: Context): String = runBlocking {

    val preferences = BITPreferences(context)
    val existingId =  preferences.getInstallationId.firstOrNull()

    existingId ?: run {
        val uuid = UUID.randomUUID().toString()
        preferences.setInstallationId(uuid)
        uuid
    }
}

private fun <T> wasTileUsedInLastXHours(
    javaClass: Class<T>,
    sharedPref: SharedPreferences,
    hours: Int = 12
): Boolean {
    val timestamp: Long = try {
        sharedPref.getLong(javaClass.name, 0)
    } catch (e: java.lang.Exception) {
        0
    }
    val current = System.currentTimeMillis()
    val diff = current - timestamp
    val maxDiff = hoursToMs(hours.toLong())
    return diff <= maxDiff
}

private fun hoursToMs(hours: Long): Long {
    return hours * 60 * 60 * 1000
}

private fun log(text: String) {
    Log.d(TAG, text)
}