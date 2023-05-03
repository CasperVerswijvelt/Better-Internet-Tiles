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
import be.casperverswijvelt.unifiedinternetqs.tiles.BluetoothTileService
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getBluetoothEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.Runnable

class BluetoothTileBehaviour(
    context: Context,
    showDialog: (Dialog) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "BluetoothTileBehaviour"
    }

    override val tileName: String
        get() = resources.getString(R.string.bluetooth)
    override val defaultIcon: Icon
        get() = Icon.createWithResource(
            context,
            R.drawable.baseline_bluetooth_24
        )
    @Suppress("UNCHECKED_CAST")
    override val tileServiceClass: Class<TileService>
        get() = BluetoothTileService::class.java as Class<TileService>

    override val tileState: TileState
        get() {
            val tile = TileState()
            val bluetoothEnabled = getBluetoothEnabled(context)

            tile.icon = R.drawable.baseline_bluetooth_24
            tile.label = resources.getString(R.string.bluetooth)

            if ((bluetoothEnabled && !TileSyncService.isTurningOffBluetooth) || TileSyncService.isTurningOnBluetooth) {

                if (bluetoothEnabled) TileSyncService.isTurningOnBluetooth = false

                val connectedDevice = TileSyncService.connectedBluetoothName

                tile.state = Tile.STATE_ACTIVE
                tile.label = if (connectedDevice.isNullOrEmpty())
                    resources.getString(R.string.bluetooth)
                else
                    connectedDevice
                tile.subtitle =
                    if (TileSyncService.isTurningOnBluetooth)
                        resources.getString(R.string.turning_on)
                    else
                        resources.getString(R.string.on)
                if (!connectedDevice.isNullOrEmpty())
                    tile.icon = R.drawable.baseline_bluetooth_connected_24
            } else {

                if (!bluetoothEnabled) TileSyncService.isTurningOffBluetooth = false

                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = resources.getString(R.string.off)
            }
            return tile
        }
    override val onLongClickIntentAction: String
        get() = Settings.ACTION_BLUETOOTH_SETTINGS

    override fun onClick() {
        log("onClick")

        if (!hasShellAccess(context)) {

            // Either root or Shizuku access is needed to enable/disable Bluetooth.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(context))
            return
        }

        if (requiresUnlock) {
            unlockAndRun { toggleBluetooth() }
        } else {
            toggleBluetooth()
        }
    }

    private fun toggleBluetooth() {

        val bluetoothEnabled = getBluetoothEnabled(context)

        if (bluetoothEnabled || TileSyncService.isTurningOnBluetooth) {
            TileSyncService.isTurningOnBluetooth = false
            TileSyncService.isTurningOffBluetooth = true
            executeShellCommandAsync("svc bluetooth disable", context) {
                updateTile()
            }
        } else {
            TileSyncService.isTurningOnBluetooth = true
            TileSyncService.isTurningOffBluetooth = false
            executeShellCommandAsync("svc bluetooth enable", context) {
                updateTile()
            }
        }
        updateTile()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}