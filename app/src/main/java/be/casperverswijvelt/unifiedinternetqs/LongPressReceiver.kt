package be.casperverswijvelt.unifiedinternetqs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings


class LongPressReceiver : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}