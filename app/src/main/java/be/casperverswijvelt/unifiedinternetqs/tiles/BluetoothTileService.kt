package be.casperverswijvelt.unifiedinternetqs.tiles;

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Handler
import android.service.quicksettings.Tile
import android.util.Log
import androidx.preference.PreferenceManager
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.util.*

class BluetoothTileService : ReportingTileService() {

    private companion object {
        const val TAG = "BluetoothTile"
    }

    private var sharedPreferences: SharedPreferences? = null

    private var isTurningOnBluetooth = false
    private var isTurningOffBluetooth = false

    private val runToggleBluetooth = Runnable {
        toggleBluetooth()
        syncTile()
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                syncTile()
            }
        }
    }

    private var mainHandler: Handler? = null

    override fun onCreate() {
        super.onCreate();
        log("Bluetooth tile service created")

        mainHandler = Handler(mainLooper)
        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()

        syncTile()
        setListeners()
    }


    override fun onStopListening() {
        super.onStopListening()

        removeListeners()
    }

    override fun onClick() {
        super.onClick()

        if (!hasShellAccess(applicationContext)) {

            // Either root or Shizuku access is needed to enable/disable Bluetooth.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(applicationContext))
            return
        }

        if (
            sharedPreferences?.getBoolean(
                resources.getString(
                    R.string.require_unlock_key),
                    true) == true
        ) {

            unlockAndRun(runToggleBluetooth)

        } else {

            mainHandler?.post(runToggleBluetooth)
        }
    }

    private fun toggleBluetooth() {

        val bluetoothEnabled = getBluetoothEnabled(applicationContext)

        if (bluetoothEnabled || isTurningOnBluetooth) {
            isTurningOnBluetooth = false
            isTurningOffBluetooth = true
            executeShellCommandAsync("svc bluetooth disable") {
                syncTile()
            }
        } else {
            isTurningOnBluetooth = true
            isTurningOffBluetooth = false
            executeShellCommandAsync("svc bluetooth enable") {
                syncTile()
            }
        }
    }

    private fun syncTile() {

        qsTile?.let {

            val bluetoothEnabled = getBluetoothEnabled(applicationContext)

            it.label = getString(R.string.bluetooth)

            if ((bluetoothEnabled && !isTurningOffBluetooth) || isTurningOnBluetooth) {

                if (bluetoothEnabled) isTurningOnBluetooth = false

                it.state = Tile.STATE_ACTIVE
                it.subtitle =
                    if (isTurningOnBluetooth) resources.getString(R.string.turning_on)
                    else resources.getString(R.string.on)

            } else {

                if (!bluetoothEnabled) isTurningOffBluetooth = false

                it.state = Tile.STATE_INACTIVE
                it.subtitle= resources.getString(R.string.off)
            }

            it.updateTile()
        }
    }

    private fun setListeners() {
        log("Setting listeners")

        registerReceiver(
            bluetoothReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    private fun removeListeners() {
        log("Removing listeners")

        try {
            unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            // Ignore
        }
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}
