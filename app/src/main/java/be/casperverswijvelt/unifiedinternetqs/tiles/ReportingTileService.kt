package be.casperverswijvelt.unifiedinternetqs.tiles

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.service.quicksettings.TileService
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileState
import be.casperverswijvelt.unifiedinternetqs.util.ExecutorServiceSingleton
import be.casperverswijvelt.unifiedinternetqs.util.reportToAnalytics
import be.casperverswijvelt.unifiedinternetqs.util.saveTileUsed

abstract class ReportingTileService: TileService() {

    protected lateinit var tileBehaviour: TileBehaviour

    private val onUpdateTile: (TileState) -> Unit = {
        syncTile(it)
    }

    override fun onCreate() {
        log("onCreate")

        tileBehaviour.addUpdateTileListeners(onUpdateTile)

        ExecutorServiceSingleton.getInstance().execute {
            saveTileUsed(this)
            reportToAnalytics(this)
        }

        super.onCreate()
    }

    override fun onDestroy() {
        log("onDestroy")

        tileBehaviour.removeUpdateTileListeners(onUpdateTile)

        super.onDestroy()
    }

    override fun onTileAdded() {
        super.onTileAdded()

        // Tile added, request for it to enter listening state
        requestListeningState(
            applicationContext,
            ComponentName(application, javaClass)
        )
    }

    protected abstract fun getTag(): String

    protected fun log(text: String) {
        Log.d(getTag(), text)
    }

    override fun onStartListening() {
        super.onStartListening()
        log("Start listening")
        TileSyncService.addBehaviourListener(tileBehaviour)
        syncTile(tileBehaviour.finalTileState)
    }

    override fun onStopListening() {
        super.onStopListening()
        TileSyncService.removeBehaviourListener(tileBehaviour)
        log("Stop listening")
    }

    override fun onClick() {
        super.onClick()
        tileBehaviour.onClick()
    }

    private fun syncTile(tileState: TileState) {
        qsTile?.let {
            it.label = tileState.label
            it.subtitle = tileState.subtitle
            it.state = tileState.state
            it.icon = Icon.createWithResource(
                applicationContext,
                tileState.icon
            )
            it.updateTile()
        }
    }
}