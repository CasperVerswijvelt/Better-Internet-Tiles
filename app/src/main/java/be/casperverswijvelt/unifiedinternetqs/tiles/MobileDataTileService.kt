package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.MobileDataTileBehaviour

class MobileDataTileService : ReportingTileService() {

    override fun getTag(): String {
        return "MobileDataTileService"
    }

    override fun onCreate() {
        log("Mobile data tile service created")

        tileBehaviour = MobileDataTileBehaviour(
            context = this,
            showDialog = { showDialog(it) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}