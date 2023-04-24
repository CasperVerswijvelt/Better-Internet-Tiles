package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.InternetTileBehaviour

class InternetTileService : ReportingTileService() {

    override fun getTag(): String {
        return "InternetTileService"
    }

    override fun onCreate() {
        log("Internet tile service created")

        tileBehaviour = InternetTileBehaviour(
            context = this,
            showDialog = { showDialog(it) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}