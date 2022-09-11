package be.casperverswijvelt.unifiedinternetqs

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
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

        reportToAnalytics()

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
                Log.d(TAG, "Sending request")
                val url = URL("https://bitanalytics.casperverswijvelt.be/report")
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true

                    val data = ("{" +
                            "\"sdkLevel\": ${Build.VERSION.SDK_INT}," +
                            "\"language\": \"${Locale.getDefault().language}\"," +
                            "\"distribution\": \"${BuildConfig.FLAVOR}\"," +
                            "\"brand\": \"${Build.BRAND}\"," +
                            "\"model\": \"${Build.MODEL}\"," +
                            "\"uuid\": \"${getInstallId()}\"" +
                            "}").toByteArray(Charsets.UTF_8)
                    outputStream.write(data, 0, data.size)

                    Log.d(TAG,
                        "\nSent 'POST' request to URL : $url; Response Code : " +
                                "$responseCode"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending analytics data: $e")
            }
        }.start()
    }

    private fun getInstallId(): String {

        val sharedPref = getSharedPreferences(
            "shared",
            Context.MODE_PRIVATE
        )
        val installationIdKey = "INSTALLATION_ID"
        return sharedPref.getString(installationIdKey, null) ?: run {
            val uuid = UUID.randomUUID().toString()
            sharedPref.edit().putString(installationIdKey, UUID.randomUUID()
                .toString()).apply()
            uuid
        }
    }
}