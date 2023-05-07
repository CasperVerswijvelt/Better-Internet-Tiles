package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.AirplaneModeTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.util.toDialog

class AirplaneModeTileService : ReportingTileService() {

    override fun getTag(): String {
        return "APModeTileService"
    }

    override fun onCreate() {
        log("Airplane Mode tile service created")

        tileBehaviour = AirplaneModeTileBehaviour(
            context = this,
            showDialog = { showDialog(it.toDialog(applicationContext)) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}