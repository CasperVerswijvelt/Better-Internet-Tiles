package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.content.Context
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tiles.NFCTileService
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getNFCEnabled
import kotlinx.coroutines.Runnable

class NFCTileBehaviour(
    context: Context,
    showDialog: (AlertDialogData) -> Unit,
    unlockAndRun: (Runnable) -> Unit = { it.run() }
): TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "NFCTileBehaviour"
    }

    override val type: TileType
        get() = TileType.NFC
    override val tileName: String
        get() = resources.getString(R.string.nfc)
    override val defaultIcon: Icon
        get() = Icon.createWithResource(
            context,
            R.drawable.nfc_24
        )
    @Suppress("UNCHECKED_CAST")
    override val tileServiceClass: Class<TileService>
        get() = NFCTileService::class.java as Class<TileService>

    override val tileState: TileState
        get() {
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
    override val onLongClickIntentAction: String
        get() = Settings.ACTION_NFC_SETTINGS

    override fun onClick() {
        log("onClick")

        if (!checkShellAccess()) return

        if (requiresUnlock) {
            unlockAndRun { toggleNFC() }
        } else {
            toggleNFC()
        }
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