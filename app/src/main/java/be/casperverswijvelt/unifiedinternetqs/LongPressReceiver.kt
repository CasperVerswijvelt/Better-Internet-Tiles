package be.casperverswijvelt.unifiedinternetqs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

class LongPressReceiver : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))

        finish()
    }
}