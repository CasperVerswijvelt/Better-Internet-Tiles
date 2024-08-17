package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.settings.settings.FollowOption
import be.casperverswijvelt.unifiedinternetqs.settings.ISetting
import be.casperverswijvelt.unifiedinternetqs.settings.settings.requireUnlockSetting
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

abstract class TileBehaviour(
    protected val context: Context,
    protected val showDialog: (AlertDialogData) -> Unit,
    protected val unlockAndRun: (Runnable) -> Unit = { it.run() }
) {

    protected val preferences = BITPreferences(context)
    protected val resources: Resources = context.resources

    private val updateTileListeners = arrayListOf<(TileState) -> Unit>()

    abstract val type: TileType
    abstract val tileName: String
    abstract val defaultIcon: Icon
    abstract val tileServiceClass: Class<TileService>
    protected abstract val tileState: TileState
    abstract val onLongClickIntentAction: String

    open val behaviourSettings: Array<ISetting<*>> = arrayOf(requireUnlockSetting)
    open val lookSettings: Array<ISetting<*>> = arrayOf()

    val finalTileState: TileState
        get() {
            return if (TileSyncService.isRunning) {
                tileState
            } else {
                TileState().apply {
                    icon = defaultIcon.resId
                    label = tileName
                    subtitle = "Service not active"
                    state = Tile.STATE_INACTIVE
                }
            }
        }

    abstract fun onClick()

    protected fun checkShellAccess (): Boolean {
        if (!hasShellAccess(context)) {

            // Either root or Shizuku access is needed to enable/disable a tile.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root or Shizuku access.
            showDialog(getShellAccessRequiredDialog(context))
            return false
        }
        return true
    }

    val requiresUnlock: Boolean
        get() = runBlocking {
            when(preferences.getRequireUnlock(type).first()) {
                FollowOption.FOLLOW -> {
                    preferences.getRequireUnlock.first()
                }
                FollowOption.YES -> true
                FollowOption.NO -> false
            }
        }

    fun updateTile() {
        updateTileListeners.forEach { it(finalTileState) }
    }

    fun addUpdateTileListeners(listener: (TileState) -> Unit) {
        updateTileListeners.add(listener)
    }

    fun removeUpdateTileListeners(listener: (TileState) -> Unit) {
        updateTileListeners.remove(listener)
    }
}