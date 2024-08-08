package be.casperverswijvelt.unifiedinternetqs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SyncServiceBroadcastReceiver: BroadcastReceiver() {
    companion object {
        const val TAG = "SyncBroadcastReceiver"
    }
    override fun onReceive(context: Context?, intent: Intent) {
        Log.d(TAG, "Received '${intent.action}' intent, starting TileSyncService")
        context?.startForegroundService(Intent(
            context,
            TileSyncService::class.java
        ))
    }
}