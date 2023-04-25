package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.service.quicksettings.TileService
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

abstract class TileBehaviour(
    protected val context: Context,
    protected val showDialog: (Dialog) -> Unit,
    protected val unlockAndRun: (Runnable) -> Unit = { it.run() }
) {

    private val preferences = BITPreferences(context)
    protected val resources: Resources = context.resources

    private val updateTileListeners = arrayListOf<(TileState) -> Unit>()

    abstract val tileName: String
    abstract val defaultIcon: Icon
    abstract val tileServiceClass: Class<TileService>
    abstract val tileState: TileState
    abstract val onLongClickIntentAction: String

    abstract fun onClick()

    val requiresUnlock: Boolean
        get() = runBlocking {
            preferences.getRequireUnlock.first()
        }

    fun updateTile() {
        updateTileListeners.forEach { it(tileState) }
    }

    fun addUpdateTileListeners(listener: (TileState) -> Unit) {
        updateTileListeners.add(listener)
    }

    fun removeUpdateTileListeners(listener: (TileState) -> Unit) {
        updateTileListeners.remove(listener)
    }
}