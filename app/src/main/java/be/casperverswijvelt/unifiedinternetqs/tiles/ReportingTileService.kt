package be.casperverswijvelt.unifiedinternetqs.tiles

import android.content.ComponentName
import android.service.quicksettings.TileService
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.util.reportToAnalytics
import be.casperverswijvelt.unifiedinternetqs.util.saveTileUsed

abstract class ReportingTileService: TileService() {

    protected lateinit var tileBehaviour: TileBehaviour

    override fun onCreate() {
        super.onCreate()

        saveTileUsed(this)
        reportToAnalytics(this)
    }

    protected fun requestUpdateTile() {
        requestListeningState(
            applicationContext,
            ComponentName(application, javaClass)
        )
    }

    override fun onTileAdded() {
        super.onTileAdded()
        requestUpdateTile()
    }

    protected abstract fun getTag(): String

    protected fun log(text: String) {
        Log.d(getTag(), text)
    }

    override fun onStartListening() {
        super.onStartListening()
        log("Start listening")

        syncTile()
    }

    override fun onClick() {
        super.onClick()
        tileBehaviour.onClick()
    }

    protected fun syncTile() {
        qsTile?.let {
            val tileState = tileBehaviour.getTileState()
            qsTile.label = tileState.label
            qsTile.subtitle = tileState.subtitle
            qsTile.state = tileState.state
            qsTile.icon = tileState.icon
            qsTile.updateTile()
        }
    }
}