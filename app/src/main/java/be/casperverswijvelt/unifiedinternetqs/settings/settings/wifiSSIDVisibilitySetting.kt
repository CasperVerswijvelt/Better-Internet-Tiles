package be.casperverswijvelt.unifiedinternetqs.settings.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.settings.IBooleanTileSetting
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

val wifiSSIDVisibilityOption = object : IBooleanTileSetting {
    override val summaryResource = R.string.hide_wifi_name_description

    override val icon = Icons.Default.RemoveRedEye
    override val nameResource = R.string.hide_wifi_name
    override val defaultValue = false

    override fun getValue(
        preferences: BITPreferences,
        tileType: TileType?,
    ): Flow<Boolean> {
        return preferences.getHideWiFiSSID
    }

    override fun setValue(
        preferences: BITPreferences,
        tileType: TileType?,
        coroutineScope: CoroutineScope,
        value: Boolean
    ) {
        coroutineScope.launch {
            preferences.getHideWiFiSSID(value)
        }
    }
}