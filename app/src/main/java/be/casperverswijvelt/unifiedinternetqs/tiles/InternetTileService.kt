package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.InternetTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.util.toDialog

class InternetTileService : ReportingTileService() {

    override fun getTag(): String {
        return "InternetTileService"
    }

    override fun onCreate() {
        log("Internet tile service created")

        tileBehaviour = InternetTileBehaviour(
            context = this,
            showDialog = { showDialog(it.toDialog(applicationContext)) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}