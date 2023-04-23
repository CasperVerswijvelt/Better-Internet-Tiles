package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.WifiTileBehaviour

class WifiTileService : ReportingTileService() {

    override fun getTag(): String {
        return "WifiTileService"
    }

    override fun onCreate() {
        super.onCreate()
        log("Wi-Fi tile service created")

        tileBehaviour = WifiTileBehaviour(
            context = this,
            showDialog = { showDialog(it) },
            unlockAndRun = { unlockAndRun(it) },
            onRequestUpdate = {
                syncTile()
                requestUpdateTile()
            }
        )
    }
}