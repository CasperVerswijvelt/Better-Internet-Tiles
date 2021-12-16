package be.casperverswijvelt.unifiedinternetqs

import android.app.Application
import android.app.NotificationManager

import android.app.NotificationChannel
import android.content.Intent
import android.util.Log
import com.topjohnwu.superuser.Shell

class TileApplication: Application() {

    companion object {
        const val CHANNEL_ID = "autoStartServiceChannel"
        const val CHANNEL_NAME = "Shuzuku Detection"
        const val TAG = "TileApplication"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Created Tile Application")

        // If no root access is detected, assume that Shizuku is used and start foreground service
        if (!Shell.rootAccess()) {

            createNotificationChannel()
            startForegroundService(Intent(this, ShizukuDetectService::class.java))
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(serviceChannel)
    }
}