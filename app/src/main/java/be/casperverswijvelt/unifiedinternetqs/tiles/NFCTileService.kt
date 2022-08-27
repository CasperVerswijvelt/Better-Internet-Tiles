package be.casperverswijvelt.unifiedinternetqs.tiles

import android.content.*
import android.nfc.NfcAdapter
import android.os.Handler
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getNFCEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess

class NFCTileService : TileService() {

    private companion object {
        const val TAG = "NFCTile"
    }

    private var sharedPreferences: SharedPreferences? = null

    private var isTurningOnNFC = false
    private var isTurningOffNFC = false
    private var receiverRegistered = false

    private val runToggleNFC = Runnable {
        toggleNFC()
        syncTile()
    }
    private val nfcReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                syncTile()
            }
        }
    }

    private var mainHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        log("NFC tile service created")

        mainHandler = Handler(mainLooper)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()

        setListeners()
        syncTile()
    }


    override fun onStopListening() {
        super.onStopListening()

        removeListeners()
    }

    override fun onTileAdded() {
        super.onTileAdded()

        setListeners()
        syncTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        removeListeners()
    }

    override fun onClick() {
        super.onClick()

        if (!hasShellAccess()) {

            // Either root or Shizuku access is needed to enable/disable NFC.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(applicationContext))
            return
        }

        if (
            sharedPreferences?.getBoolean(
                resources.getString(R.string.require_unlock_key),
                true
            ) == true
        ) {

            unlockAndRun(runToggleNFC)

        } else {

            mainHandler?.post(runToggleNFC)
        }
    }

    private fun toggleNFC() {

        val nfcEnabled = getNFCEnabled(applicationContext)

        if (nfcEnabled || isTurningOnNFC) {
            isTurningOnNFC = false
            isTurningOffNFC = true
            executeShellCommandAsync("svc nfc disable") {
                syncTile()
            }
        } else {
            isTurningOnNFC = true
            isTurningOffNFC = false
            executeShellCommandAsync("svc nfc enable") {
                syncTile()
            }
        }
    }

    private fun syncTile() {

        qsTile?.let {

            val nfcEnabled = getNFCEnabled(applicationContext)

            if ((nfcEnabled && !isTurningOffNFC) || isTurningOnNFC) {

                if (nfcEnabled) isTurningOnNFC = false

                // Update tile properties

                it.state = Tile.STATE_ACTIVE
                it.subtitle = if (isTurningOnNFC) resources.getString(R.string.turning_on) else resources.getString(R.string.on)

            } else {

                if (!nfcEnabled) isTurningOffNFC = false

                it.state = Tile.STATE_INACTIVE
                it.subtitle = resources.getString(R.string.off)
            }

            it.updateTile()
        }
    }

    private fun setListeners() {

        log("Setting listeners")

        registerReceiver(nfcReceiver, IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED))
        receiverRegistered = true
    }

    private fun removeListeners() {

        log("Removing listeners")

        if (receiverRegistered){
            unregisterReceiver(nfcReceiver)
            receiverRegistered = false
        }
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}