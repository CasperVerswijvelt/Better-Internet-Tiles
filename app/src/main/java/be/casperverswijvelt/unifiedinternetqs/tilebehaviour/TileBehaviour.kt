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
    val showDialog: (Dialog) -> Unit,
    val unlockAndRun: (Runnable) -> Unit = { it.run() },
    val onRequestUpdate: () -> Unit
) {

    protected val preferences = BITPreferences(context)
    protected val resources: Resources = context.resources

    abstract fun onClick()
    abstract fun getTileState() : TileState

    fun getRequiresUnlock(): Boolean {
        return runBlocking {
            preferences.getRequireUnlock.first()
        }
    }
}