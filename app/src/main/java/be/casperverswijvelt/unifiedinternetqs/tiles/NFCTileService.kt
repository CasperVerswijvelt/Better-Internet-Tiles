package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.NFCTileBehaviour

class NFCTileService : ReportingTileService() {

    override fun getTag(): String {
        return "NFCTileService"
    }

    override fun onCreate() {
        log("NFC tile service created")

        tileBehaviour = NFCTileBehaviour(
            context = this,
            showDialog = { showDialog(it) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}