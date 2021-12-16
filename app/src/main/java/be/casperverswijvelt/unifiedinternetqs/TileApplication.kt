package be.casperverswijvelt.unifiedinternetqs

import android.app.Application
import android.app.NotificationManager

import android.app.NotificationChannel
import android.content.Intent
import android.util.Log

class TileApplication: Application() {

    companion object {
        const val CHANNEL_ID = "autoStartServiceChannel"
        const val CHANNEL_NAME = "Shuzuku Detection"
        const val TAG = "TileApplication"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Created Tile Application")

        createNotificationChannel()
        val serviceIntent = Intent(this, ShizukuDetectService::class.java)
        startForegroundService(serviceIntent)
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