package be.casperverswijvelt.unifiedinternetqs.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.ComponentName
import be.casperverswijvelt.unifiedinternetqs.getDataEnabled
import be.casperverswijvelt.unifiedinternetqs.getWifiEnabled
import be.casperverswijvelt.unifiedinternetqs.tiles.InternetTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.MobileDataTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiTileService


class LongPressReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val qsTile: ComponentName? = intent?.getParcelableExtra(Intent.EXTRA_COMPONENT_NAME)

        println("${qsTile?.className} ${MobileDataTileService::class.java.name}")

        val intent = Intent(when(qsTile?.className) {
            InternetTileService::class.java.name -> {
                when {
                    getDataEnabled(applicationContext) -> {
                        println("OPERATOR")
                        Settings.ACTION_NETWORK_OPERATOR_SETTINGS
                    }
                    getWifiEnabled(applicationContext) -> {
                        println("WIFI")
                        Settings.ACTION_WIFI_SETTINGS
                    }
                    else -> {
                        println("ELSE")
                        Settings.ACTION_WIRELESS_SETTINGS
                    }
                }
            }
            MobileDataTileService::class.java.name -> {
                Settings.ACTION_NETWORK_OPERATOR_SETTINGS
            }
            WifiTileService::class.java.name -> {
                Settings.ACTION_WIFI_SETTINGS
            }
            else -> Settings.ACTION_WIRELESS_SETTINGS
        })

        startActivity(intent)
    }
}