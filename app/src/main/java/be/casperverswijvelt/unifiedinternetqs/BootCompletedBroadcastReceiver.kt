package be.casperverswijvelt.unifiedinternetqs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedBroadcastReceiver: BroadcastReceiver() {
    companion object {
        const val TAG = "BootBroadcastReceiver"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Received 'android.intent.action.BOOT_COMPLETED' intent, starting TileSyncService")
            context?.startForegroundService(Intent(
                context,
                TileSyncService::class.java
            ))
        }
    }
}