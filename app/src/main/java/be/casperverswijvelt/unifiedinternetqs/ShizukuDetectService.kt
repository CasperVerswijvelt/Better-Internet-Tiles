package be.casperverswijvelt.unifiedinternetqs

import android.content.Intent
import android.os.IBinder
import android.app.*
import android.content.Context

import be.casperverswijvelt.unifiedinternetqs.ui.MainActivity

class ShizukuDetectService: Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = Notification.Builder(this, TileApplication.CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.hide_service_title))
            .setContentText(resources.getString(R.string.hide_service_description))
            .setSmallIcon(R.drawable.ic_baseline_public_24)
            .setContentIntent(pendingIntent)
            .build()
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            TileApplication.CHANNEL_ID,
            TileApplication.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        mNotificationManager.createNotificationChannel(channel)
        Notification.Builder(this, TileApplication.CHANNEL_ID)
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}