package be.casperverswijvelt.unifiedinternetqs.tiles

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager
import be.casperverswijvelt.unifiedinternetqs.*
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeCallback
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeType
import com.topjohnwu.superuser.Shell

class MobileDataTileService : TileService() {

    private companion object {
        const val TAG = "MobileDataTile"
    }

    private var sharedPreferences: SharedPreferences? = null

    private val runToggleInternet = Runnable { toggleMobileData() }
    private val networkChangeCallback = object : NetworkChangeCallback {
        override fun handleChange(type: NetworkChangeType?) {
            syncTile()
        }
    }

    private var cellularChangeListener: CellularChangeListener? = null

    override fun onCreate() {
        super.onCreate()
        log("Internet tile service created")

        cellularChangeListener = CellularChangeListener(networkChangeCallback)
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

        if (!Shell.rootAccess()) {

            // Root access is needed to enable/disable mobile data and Wi-Fi. There is currently
            //  no other way to do this, so this functionality will not work without root access.
            showDialog(getRootAccessRequiredDialog(applicationContext))
            return
        }

        if (
            sharedPreferences?.getBoolean(
                resources.getString(R.string.require_unlock_key),
                true
            ) == true
        ) {

            unlockAndRun(runToggleInternet)

        } else {

            toggleMobileData()
        }

        syncTile()
    }

    private fun toggleMobileData() {

        val dataEnabled = getDataEnabled(applicationContext)

        if (dataEnabled) {
            Shell.su("svc data disable").exec()
        } else {
            Shell.su("svc data enable").exec()
        }
    }

    private fun syncTile() {

        val dataEnabled = getDataEnabled(applicationContext)

        if (dataEnabled) {

            // Update tile properties

            qsTile.state = Tile.STATE_ACTIVE
            qsTile.icon = getCellularNetworkIcon(applicationContext)
            qsTile.subtitle = getCellularNetworkText(applicationContext)

        } else {

            qsTile.state = Tile.STATE_INACTIVE
            qsTile.icon = Icon.createWithResource(
                this,
                R.drawable.ic_baseline_mobiledata_off_24
            )
            qsTile.subtitle = null
        }

        qsTile.updateTile()
    }

    private fun setListeners() {

        log("Setting listeners")

        cellularChangeListener?.startListening(applicationContext)
    }

    private fun removeListeners() {

        log("Removing listeners")

        cellularChangeListener?.stopListening(applicationContext)
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}