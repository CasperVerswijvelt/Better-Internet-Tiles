package be.casperverswijvelt.unifiedinternetqs.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

class LongPressReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

        finish()
    }
}