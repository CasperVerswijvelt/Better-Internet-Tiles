package be.casperverswijvelt.unifiedinternetqs

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Network
import android.nfc.NfcAdapter
import android.os.IBinder
import android.service.quicksettings.TileService
import android.telephony.ServiceState
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.listeners.CellularChangeListener
import be.casperverswijvelt.unifiedinternetqs.listeners.NetworkChangeType
import be.casperverswijvelt.unifiedinternetqs.listeners.WifiChangeListener
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.AirplaneModeTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.BluetoothTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.InternetTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.MobileDataTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.NFCTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.WifiTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tiles.AirplaneModeTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.BluetoothTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.InternetTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.MobileDataTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.NFCTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiTileService
import be.casperverswijvelt.unifiedinternetqs.ui.MainActivity
import be.casperverswijvelt.unifiedinternetqs.util.getConnectedWifiSSID


const val ACT_BATTERY_LEVEL_CHANGED = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
const val EXTRA_BATTERY_LEVEL = "android.bluetooth.device.extra.BATTERY_LEVEL"

class TileSyncService: Service() {

    companion object {
        const val TAG = "TileSyncService"

        private var latestAvailableWifiNetwork: Network? = null
        var wifiConnected = false
        var wifiSSID: String? = null

        var isTurningOnData = false
        var isTurningOffData = false
        var serviceState: ServiceState? = null

        var isTurningOnWifi = false
        var isTurningOffWifi = false

        var isTurningOnNFC = false
        var isTurningOffNFC = false

        var isTurningOnAirplaneMode = false
        var isTurningOffAirplaneMode = false

        var isTurningOnBluetooth = false
        var isTurningOffBluetooth = false

        val bluetoothConnectionState: MutableMap<String, Int> = mutableMapOf()
        val bluetoothBatteryLevel: MutableMap<String, Int> = mutableMapOf()

        var bluetoothProfile: BluetoothProfile? = null

        private val behaviourListeners = arrayListOf<TileBehaviour>()

        fun addBehaviourListener(tileBehaviour: TileBehaviour) {
            behaviourListeners.add(tileBehaviour)
        }
        fun removeBehaviourListener(tileBehaviour: TileBehaviour) {
            behaviourListeners.remove(tileBehaviour)
        }
    }

    private val wifiChangeListener: WifiChangeListener = WifiChangeListener { type, network ->
        when(type) {
            NetworkChangeType.NETWORK_LOST -> {
                // If the network that is lost is not the latest
                //  network that became available, we are still
                //  connected.
                wifiConnected = latestAvailableWifiNetwork != network
            }
            NetworkChangeType.NETWORK_AVAILABLE -> {
                latestAvailableWifiNetwork = network
                wifiConnected = true
            }
            else -> {}
        }
        wifiSSID = if (wifiConnected)
            getConnectedWifiSSID(applicationContext)
        else
            null
        updateWifiTile()
        updateInternetTile()
    }
    private val cellularChangeListener: CellularChangeListener = CellularChangeListener { type, data ->
        when(type) {
            NetworkChangeType.SERVICE_STATE -> {
                (data?.firstOrNull() as? ServiceState)?.let {
                    serviceState = it
                }
            }
            else -> {}
        }
        updateMobileDataTile()
        updateInternetTile()
    }
    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                updateMobileDataTile()
                updateInternetTile()
                updateAirplaneModeTile()
            }
        }
    }
    private val nfcReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                updateNFCTile()
            }
        }
    }
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val device = it.extras?.get(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
                when (it.action) {
                    BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                        val state = it.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                        device?.let { bluetoothConnectionState[device.address] = state }
                    }
                    ACT_BATTERY_LEVEL_CHANGED -> {
                        val batteryLevel = intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1)
                        device?.let { bluetoothBatteryLevel[device.address] = batteryLevel }
                    }
                    else -> {}
                }
            }
            updateBluetoothTile()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG,"onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"onStartCommand")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification =
            Notification.Builder(this, TileApplication.CHANNEL_ID)
                .setContentTitle(resources.getString(R.string.hide_service_title))
                .setContentText(resources.getString(R.string.hide_service_description))
                .setSmallIcon(R.drawable.ic_baseline_public_24)
                .setContentIntent(pendingIntent)
                .build()
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            TileApplication.CHANNEL_ID,
            TileApplication.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        mNotificationManager.createNotificationChannel(channel)
        Notification.Builder(this, TileApplication.CHANNEL_ID)
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        // Wi-Fi
        wifiChangeListener.startListening(applicationContext)
        cellularChangeListener.startListening(applicationContext)
        registerReceiver(
            airplaneModeReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        )
        registerReceiver(
            nfcReceiver,
            IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        )
        val btIntentFilter = IntentFilter()
        btIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        btIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        btIntentFilter.addAction(ACT_BATTERY_LEVEL_CHANGED)
        registerReceiver(
            bluetoothReceiver,
            btIntentFilter
        )

        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.getProfileProxy(
            applicationContext,
            object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile?) {
                    Log.d(TAG, "Bluetooth profile proxy connected")
                    TileSyncService.bluetoothProfile = bluetoothProfile
                    updateBluetoothTile()
                }

                override fun onServiceDisconnected(p0: Int) {
                    Log.d(TAG, "Bluetooth profile proxy disconnected")
                    bluetoothProfile = null
                    updateBluetoothTile()
                }
            },
            BluetoothProfile.HEADSET
        )

        updateWifiTile()
        updateMobileDataTile()
        updateInternetTile()
        updateNFCTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        wifiChangeListener.stopListening(applicationContext)
        cellularChangeListener.stopListening(applicationContext)
        unregisterReceiver(airplaneModeReceiver)
        unregisterReceiver(nfcReceiver)
        unregisterReceiver(bluetoothReceiver)
    }

    private fun updateWifiTile() {
        requestListeningState(WifiTileService::class.java)
        requestTileBehaviourUpdate(WifiTileBehaviour::class.java)
    }

    private fun updateMobileDataTile() {
        requestListeningState(MobileDataTileService::class.java)
        requestTileBehaviourUpdate(MobileDataTileBehaviour::class.java)
    }

    private fun updateInternetTile () {
        requestListeningState(InternetTileService::class.java)
        requestTileBehaviourUpdate(InternetTileBehaviour::class.java)
    }

    private fun updateNFCTile() {
        requestListeningState(NFCTileService::class.java)
        requestTileBehaviourUpdate(NFCTileBehaviour::class.java)
    }

    private fun updateAirplaneModeTile() {
        requestListeningState(AirplaneModeTileService::class.java)
        requestTileBehaviourUpdate(AirplaneModeTileBehaviour::class.java)
    }

    private fun updateBluetoothTile() {
        requestListeningState(BluetoothTileService::class.java)
        requestTileBehaviourUpdate(BluetoothTileBehaviour::class.java)
    }

    private fun <T>requestListeningState(cls: Class<T>) {
        TileService.requestListeningState(
            applicationContext,
            ComponentName(applicationContext, cls)
        )
    }

    private fun <T>requestTileBehaviourUpdate(cls: Class<T>) {
        behaviourListeners.forEach {
            if (it.javaClass == cls) it.updateTile()
        }
    }
}