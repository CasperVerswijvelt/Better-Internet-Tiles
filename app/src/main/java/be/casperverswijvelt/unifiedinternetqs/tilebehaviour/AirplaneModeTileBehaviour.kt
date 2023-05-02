package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tiles.AirplaneModeTileService
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getAirplaneModeEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.Runnable

class AirplaneModeTileBehaviour(
    context: Context,
    showDialog: (Dialog) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "APModeTileBehaviour"
    }

    override val tileName: String
        get() = resources.getString(R.string.airplane_mode)
    override val defaultIcon: Icon
        get() = Icon.createWithResource(
            context,
            R.drawable.baseline_airplanemode_active_24
        )
    @Suppress("UNCHECKED_CAST")
    override val tileServiceClass: Class<TileService>
        get() = AirplaneModeTileService::class.java as Class<TileService>

    override val tileState: TileState
        get() {
            val tile = TileState()
            val airplaneModeEnabled = getAirplaneModeEnabled(context)

            tile.label = resources.getString(R.string.airplane_mode)
            tile.icon = R.drawable.baseline_airplanemode_active_24

            if ((airplaneModeEnabled && !TileSyncService.isTurningOffAirplaneMode) || TileSyncService.isTurningOnAirplaneMode) {

                if (airplaneModeEnabled) TileSyncService.isTurningOnAirplaneMode = false

                tile.state = Tile.STATE_ACTIVE
                tile.subtitle = resources.getString(R.string.on)

            } else {

                if (!airplaneModeEnabled) TileSyncService.isTurningOffAirplaneMode = false

                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = resources.getString(R.string.off)
            }
            return tile
        }
    override val onLongClickIntentAction: String
        get() = Settings.ACTION_AIRPLANE_MODE_SETTINGS

    override fun onClick() {
        log("onClick")

        if (!hasShellAccess(context)) {

            // Either root or Shizuku access is needed to enable/disable AirplaneMode.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(context))
            return
        }

        if (requiresUnlock) {
            unlockAndRun { toggleAirplaneMode() }
        } else {
            toggleAirplaneMode()
        }
    }

    private fun toggleAirplaneMode() {

        val airplaneModeEnabled = getAirplaneModeEnabled(context)

        if (airplaneModeEnabled || TileSyncService.isTurningOnAirplaneMode) {
            TileSyncService.isTurningOnAirplaneMode = false
            TileSyncService.isTurningOffAirplaneMode = true
            executeShellCommandAsync(
                "cmd connectivity airplane-mode disable",
                context
            ) {
                updateTile()
            }
        } else {
            TileSyncService.isTurningOnAirplaneMode = true
            TileSyncService.isTurningOffAirplaneMode = false
            executeShellCommandAsync(
                "cmd connectivity airplane-mode enable",
                context
            ) {
                updateTile()
            }
        }
        updateTile()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}