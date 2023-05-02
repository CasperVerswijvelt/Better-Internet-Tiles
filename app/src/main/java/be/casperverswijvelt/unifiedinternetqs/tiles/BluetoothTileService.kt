package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.BluetoothTileBehaviour

class BluetoothTileService : ReportingTileService() {

    override fun getTag(): String {
        return "BluetoothTileService"
    }

    override fun onCreate() {
        log("Bluetooth tile service created")

        tileBehaviour = BluetoothTileBehaviour(
            context = this,
            showDialog = { showDialog(it) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}