package be.casperverswijvelt.unifiedinternetqs.tiles

import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOffNFC
import be.casperverswijvelt.unifiedinternetqs.TileSyncService.Companion.isTurningOnNFC
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getNFCEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NFCTileService : ReportingTileService() {

    private companion object {
        const val TAG = "NFCTile"
    }

    private lateinit var preferences: BITPreferences

    private val runToggleNFC = Runnable {
        toggleNFC()
        syncTile()
    }

    override fun onCreate() {
        super.onCreate()
        log("NFC tile service created")

        preferences = BITPreferences(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        log("Start listening")

        syncTile()
    }

    override fun onClick() {
        super.onClick()

        log("onClick")

        if (!hasShellAccess(applicationContext)) {

            // Either root or Shizuku access is needed to enable/disable NFC.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(applicationContext))
            return
        }

        val requireUnlock = runBlocking {
            preferences.getRequireUnlock.first()
        }
        if (requireUnlock) {

            unlockAndRun(runToggleNFC)

        } else {

            runToggleNFC.run()
        }

    }

    private fun toggleNFC() {

        val nfcEnabled = getNFCEnabled(applicationContext)

        if (nfcEnabled || isTurningOnNFC) {
            isTurningOnNFC = false
            isTurningOffNFC = true
            executeShellCommandAsync("svc nfc disable", applicationContext) {
                syncTile()
                requestUpdateTile()
            }
        } else {
            isTurningOnNFC = true
            isTurningOffNFC = false
            executeShellCommandAsync("svc nfc enable", applicationContext) {
                syncTile()
                requestUpdateTile()
            }
        }
    }

    private fun syncTile() {

        qsTile?.let {

            val nfcEnabled = getNFCEnabled(applicationContext)

            it.label = getString(R.string.nfc)

            if ((nfcEnabled && !isTurningOffNFC) || isTurningOnNFC) {

                if (nfcEnabled) isTurningOnNFC = false

                it.state = Tile.STATE_ACTIVE
                it.subtitle =
                    if (isTurningOnNFC) resources.getString(R.string.turning_on) else resources.getString(
                        R.string.on
                    )

            } else {

                if (!nfcEnabled) isTurningOffNFC = false

                it.state = Tile.STATE_INACTIVE
                it.subtitle = resources.getString(R.string.off)
            }

            it.updateTile()
        }
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}