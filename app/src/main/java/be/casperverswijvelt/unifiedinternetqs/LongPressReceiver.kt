package be.casperverswijvelt.unifiedinternetqs

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings


class LongPressReceiver : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivityForResult(
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY),
            545
        )

        finish()
    }
}