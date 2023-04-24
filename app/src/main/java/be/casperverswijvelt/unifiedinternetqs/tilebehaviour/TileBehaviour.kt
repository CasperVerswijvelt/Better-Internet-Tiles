package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

abstract class TileBehaviour(
    protected val context: Context,
    protected val showDialog: (Dialog) -> Unit,
    protected val unlockAndRun: (Runnable) -> Unit = { it.run() }
) {

    protected val preferences = BITPreferences(context)
    protected val resources: Resources = context.resources

    private val updateTileListeners = arrayListOf<(TileState) -> Unit>()

    abstract fun onClick()
    abstract fun getTileState() : TileState

    protected fun getRequiresUnlock(): Boolean {
        return runBlocking {
            preferences.getRequireUnlock.first()
        }
    }

    fun updateTile() {
        updateTileListeners.forEach { it(getTileState()) }
    }

    fun addUpdateTileListeners(listener: (TileState) -> Unit) {
        updateTileListeners.add(listener)
    }

    fun removeUpdateTileListeners(listener: (TileState) -> Unit) {
        updateTileListeners.remove(listener)
    }
}