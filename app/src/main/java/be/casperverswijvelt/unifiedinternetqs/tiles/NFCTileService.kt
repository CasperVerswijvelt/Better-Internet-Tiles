package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.NFCTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.util.toDialog

class NFCTileService : ReportingTileService() {

    override fun getTag(): String {
        return "NFCTileService"
    }

    override fun onCreate() {
        log("NFC tile service created")

        tileBehaviour = NFCTileBehaviour(
            context = this,
            showDialog = { showDialog(it.toDialog(applicationContext)) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}