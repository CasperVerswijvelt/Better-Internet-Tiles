package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.content.Context
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.ServiceState
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.tiles.MobileDataTileService
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getAirplaneModeEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkIcon
import be.casperverswijvelt.unifiedinternetqs.util.getCellularNetworkText
import be.casperverswijvelt.unifiedinternetqs.util.getDataEnabled
import kotlinx.coroutines.Runnable


class MobileDataTileBehaviour(
    context: Context,
    showDialog: (AlertDialogData) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "MobileDataTileBehaviour"
    }

    override val type: TileType
        get() = TileType.MobileData
    override val tileName: String
        get() = resources.getString(R.string.mobile_data)
    override val defaultIcon: Icon
        get() = Icon.createWithResource(
            context,
            R.drawable.ic_baseline_mobile_data_24
        )
    @Suppress("UNCHECKED_CAST")
    override val tileServiceClass: Class<TileService>
        get() = MobileDataTileService::class.java as Class<TileService>

    override val tileState: TileState
        get() {
            val tile = TileState()
            val airplaneModeEnabled = getAirplaneModeEnabled(context)
            val dataEnabled = getDataEnabled(context)

            tile.label = resources.getString(R.string.mobile_data)
            tile.icon = R.drawable.ic_baseline_mobile_data_24

            if (airplaneModeEnabled) {

                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle = resources.getString(R.string.airplane_mode)

            } else if (
                TileSyncService.serviceState?.let {
                    it.state != ServiceState.STATE_IN_SERVICE
                } == true
            ) {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle = resources.getString(R.string.sim_not_available)

            } else if ((dataEnabled && !TileSyncService.isTurningOffData) || TileSyncService.isTurningOnData) {

                if (dataEnabled) TileSyncService.isTurningOnData = false

                tile.state = Tile.STATE_ACTIVE
                tile.icon = getCellularNetworkIcon(context)
                tile.subtitle = getCellularNetworkText(
                    context,
                    CellularChangeListener.currentTelephonyDisplayInfo
                )

            } else {

                if (!dataEnabled) TileSyncService.isTurningOffData = false

                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = resources.getString(R.string.off)
            }

            return tile
        }
    override val onLongClickIntentAction: String
        get() = Settings.ACTION_NETWORK_OPERATOR_SETTINGS

    override fun onClick() {
        log("onClick")

        if (!checkShellAccess()) return

        if (requiresUnlock) {
            unlockAndRun { toggleMobileData() }
        } else {
            toggleMobileData()
        }
    }

    private fun toggleMobileData() {

        val dataEnabled = getDataEnabled(context)

        if (dataEnabled || TileSyncService.isTurningOnData) {
            TileSyncService.isTurningOnData = false
            TileSyncService.isTurningOffData = true
            executeShellCommandAsync("svc data disable", context) {
                updateTile()
            }
        } else {
            TileSyncService.isTurningOnData = true
            TileSyncService.isTurningOffData = false
            executeShellCommandAsync("svc data enable", context) {
                updateTile()
            }
        }
        updateTile()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}