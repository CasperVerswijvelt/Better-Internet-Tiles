package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.app.ActivityCompat
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tiles.BluetoothTileService
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getBluetoothEnabled
import kotlinx.coroutines.Runnable

class BluetoothTileBehaviour(
    context: Context,
    showDialog: (AlertDialogData) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "BluetoothTileBehaviour"
    }

    override val type: TileType
        get() = TileType.Bluetooth
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

            val hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            } else true
            val connectedBluetoothDevice = TileSyncService.bluetoothProfile?.connectedDevices?.getOrNull(0)
            val bluetoothName = if (hasBluetoothPermission)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    connectedBluetoothDevice?.alias
                else
                    connectedBluetoothDevice?.name
            else
                ""
            val connectedBluetoothBattery = connectedBluetoothDevice?.let {
                TileSyncService.bluetoothBatteryLevel[it.address]
            } ?: -1

            val connectingBluetoothDevice = TileSyncService.bluetoothProfile?.getDevicesMatchingConnectionStates(
                intArrayOf(BluetoothAdapter.STATE_CONNECTING)
            )?.firstOrNull()

            tile.icon = R.drawable.baseline_bluetooth_24
            tile.label = resources.getString(R.string.bluetooth)

            if ((bluetoothEnabled && !TileSyncService.isTurningOffBluetooth) || TileSyncService.isTurningOnBluetooth) {

                if (bluetoothEnabled) TileSyncService.isTurningOnBluetooth = false

                val connectedDevice = bluetoothName ?: ""
                val isBluetoothConnected = connectedDevice.isNotEmpty()

                tile.state = Tile.STATE_ACTIVE
                tile.label = resources.getString(R.string.bluetooth)

                if (isBluetoothConnected) {
                    tile.icon = R.drawable.baseline_bluetooth_connected_24
                    tile.label = connectedDevice
                    tile.subtitle = if (
                        connectedBluetoothBattery >= 0
                    )
                        "${connectedBluetoothBattery}% ${resources.getString(R.string.battery_level)}"
                    else
                        resources.getString(R.string.on)
                } else if (connectingBluetoothDevice != null) {
                    tile.subtitle = resources.getString(R.string.connecting)
                    // TODO: icon animation?
                } else if (TileSyncService.isTurningOnBluetooth) {
                    tile.subtitle = resources.getString(R.string.turning_on)
                } else {
                    tile.subtitle = resources.getString(R.string.on)
                }
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

        if (!checkShellAccess()) return

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