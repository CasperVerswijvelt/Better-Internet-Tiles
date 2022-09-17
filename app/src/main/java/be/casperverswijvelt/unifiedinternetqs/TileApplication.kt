package be.casperverswijvelt.unifiedinternetqs

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Toast
import be.casperverswijvelt.unifiedinternetqs.util.ExecutorServiceSingleton
import be.casperverswijvelt.unifiedinternetqs.util.ShizukuUtil
import be.casperverswijvelt.unifiedinternetqs.util.reportException
import com.topjohnwu.superuser.Shell
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class TileApplication : Application() {

    companion object {
        const val CHANNEL_ID = "autoStartServiceChannel"
        const val CHANNEL_NAME = "Shizuku Detection"
        const val TAG = "TileApplication"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Created Tile Application")

        ExecutorServiceSingleton.getInstance()

        if (!BuildConfig.DEBUG) reportToAnalytics()

        // Check if main shell has root access
        Shell.getShell {

            // If neither root access or Shizuku access is detected, assume that
            //  Shizuku is used but not bound yet: start Shizuku detection
            //  foreground service.
            // See https://github.com/RikkaApps/Shizuku/issues/175 for why this is
            //  needed
            if (
                Shell.isAppGrantedRoot() != true &&
                !ShizukuUtil.hasShizukuPermission()
            ) {

                startShizukuDetectionService()
            }
        }
    }

    private fun startShizukuDetectionService() {
        try {
            createNotificationChannel()
            startForegroundService(
                Intent(
                    this,
                    ShizukuDetectService::class.java
                )
            )
        } catch (e: Throwable) {
            Log.d(
                TAG,
                "Failed to start foreground service due to an ${e.message}"
            )
            reportException(e)

            // Not sure what the cause of the 'ForegroundServiceStartNotAllowedException'
            //  is or how to solve it.
            Toast.makeText(
                applicationContext,
                R.string.toast_foreground_service_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(serviceChannel)
    }

    private fun reportToAnalytics() {
        Thread {

            try {
                val sharedPref = getSharedPreferences(
                    "shared",
                    Context.MODE_PRIVATE
                )
                val lastReportTimestampKey = "LAST_REPORT_TIMESTAMP"

                val lastReportTimestamp = sharedPref.getLong(
                    lastReportTimestampKey,
                    0
                )
                val currentMillis = System.currentTimeMillis()
                val diff = (currentMillis - lastReportTimestamp) / (60 * 60
                        * 1000)

                // Only send analytics data if last sent out report was more
                //  than 8 hours ago
                if (diff >= 8) {

                    Log.d(TAG, "Sending Analytics data")
                    val url = URL("https://bitanalytics.casperverswijvelt.be/api/report")
                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        doOutput = true

                        // JSON Format
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Accept", "application/json")

                        // Small JSON message containing some basic information
                        //  to report to analytics. This data is used for
                        //  informational purpose only.
                        val data = ("{" +
                                "\"dynamic\": {" +
                                "\"sdk\": ${Build.VERSION.SDK_INT}," +
                                "\"lang\": \"${Locale.getDefault()
                                    .language}\"," +
                                "\"version\": ${BuildConfig.VERSION_CODE}" +
                                "}, \"static\": {" +
                                "\"dist\": \"${BuildConfig.FLAVOR}\"," +
                                "\"brand\": \"${Build.BRAND}\"," +
                                "\"model\": \"${Build.MODEL}\"," +
                                "\"uuid\": \"${getInstallId(sharedPref)}\"" +
                                "}" +
                                "}").toByteArray(Charsets.UTF_8)
                        outputStream.write(data, 0, data.size)

                        Log.d(
                            TAG,
                            "\nSuccessfully sent 'POST' request to URL : $url;" +
                                    " Response Code: " +
                                    "$responseCode"
                        )
                    }

                    // Save timestamp in shared preferences
                    sharedPref.edit().putLong(
                        lastReportTimestampKey,
                        currentMillis
                    ).apply()
                } else {
                    Log.e(TAG, "Already sent analytics report $diff hours ago")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending analytics data: $e")
            }
        }.start()
    }

    private fun getInstallId(sharedPreferences: SharedPreferences): String {
        val installationIdKey = "INSTALLATION_ID"
        return sharedPreferences.getString(installationIdKey, null) ?: run {
            val uuid = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(
                installationIdKey, UUID.randomUUID()
                    .toString()
            ).apply()
            uuid
        }
    }
}