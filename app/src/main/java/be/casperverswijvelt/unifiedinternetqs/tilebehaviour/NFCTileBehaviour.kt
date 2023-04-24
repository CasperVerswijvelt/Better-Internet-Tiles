package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.app.Dialog
import android.content.Context
import android.service.quicksettings.Tile
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getNFCEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess
import kotlinx.coroutines.Runnable

class NFCTileBehaviour(
    context: Context,
    showDialog: (Dialog) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "NFCTileBehaviour"
    }

    override fun onClick() {
        log("onClick")

        if (!hasShellAccess(context)) {

            // Either root or Shizuku access is needed to enable/disable NFC.
            //  There is currently no other way to do this, so this functionality will not work
            //  without root Shizuku access.
            showDialog(getShellAccessRequiredDialog(context))
            return
        }

        if (getRequiresUnlock()) {
            unlockAndRun { toggleNFC() }
        } else {
            toggleNFC()
        }
    }

    override fun getTileState(): TileState {
        val tile = TileState()
        val nfcEnabled = getNFCEnabled(context)

        tile.icon = R.drawable.nfc_24
        tile.label = resources.getString(R.string.nfc)

        if ((nfcEnabled && !TileSyncService.isTurningOffNFC) || TileSyncService.isTurningOnNFC) {

            if (nfcEnabled) TileSyncService.isTurningOnNFC = false

            tile.state = Tile.STATE_ACTIVE
            tile.subtitle =
                if (TileSyncService.isTurningOnNFC) resources.getString(R.string.turning_on) else resources.getString(
                    R.string.on
                )

        } else {

            if (!nfcEnabled) TileSyncService.isTurningOffNFC = false

            tile.state = Tile.STATE_INACTIVE
            tile.subtitle = resources.getString(R.string.off)
        }
        return tile
    }

    private fun toggleNFC() {

        val nfcEnabled = getNFCEnabled(context)

        if (nfcEnabled || TileSyncService.isTurningOnNFC) {
            TileSyncService.isTurningOnNFC = false
            TileSyncService.isTurningOffNFC = true
            executeShellCommandAsync("svc nfc disable", context) {
                updateTile()
            }
        } else {
            TileSyncService.isTurningOnNFC = true
            TileSyncService.isTurningOffNFC = false
            executeShellCommandAsync("svc nfc enable", context) {
                updateTile()
            }
        }
        updateTile()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}