package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.WifiTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.util.toDialog

class WifiTileService : ReportingTileService() {

    override fun getTag(): String {
        return "WifiTileService"
    }

    override fun onCreate() {
        log("Wi-Fi tile service created")

        tileBehaviour = WifiTileBehaviour(
            context = this,
            showDialog = { showDialog(it.toDialog(applicationContext)) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}