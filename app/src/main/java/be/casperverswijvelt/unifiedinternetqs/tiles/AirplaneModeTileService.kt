package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.AirplaneModeTileBehaviour

class AirplaneModeTileService : ReportingTileService() {

    override fun getTag(): String {
        return "APModeTileService"
    }

    override fun onCreate() {
        log("Airplane Mode tile service created")

        tileBehaviour = AirplaneModeTileBehaviour(
            context = this,
            showDialog = { showDialog(it) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}