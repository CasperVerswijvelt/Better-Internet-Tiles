package be.casperverswijvelt.unifiedinternetqs.tile_options

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import be.casperverswijvelt.unifiedinternetqs.BuildConfig
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.data.TileChoiceOption
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.IChoiceSetting
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class WifiSSIDVisibilityOption(override val value: String, override val stringResource: Int) :
    TileChoiceOption {
    VISIBLE("visible", R.string.visible),
    HIDDEN("hidden", R.string.hidden),
    HIDDEN_DURING_RECORDING("hidden_during_recording", R.string.hidden_during_recording)
}

val wifiSSIDVisibilityOption = object : IChoiceSetting<WifiSSIDVisibilityOption> {
    override val choices = listOfNotNull(
        WifiSSIDVisibilityOption.VISIBLE,
        WifiSSIDVisibilityOption.HIDDEN,
        if (Build.VERSION.SDK_INT >= 35) WifiSSIDVisibilityOption.HIDDEN_DURING_RECORDING else null
    ).apply {

    }

    override val icon = Icons.Default.RemoveRedEye
    override val nameResource = R.string.ssid_visibility
    override val defaultValue = WifiSSIDVisibilityOption.VISIBLE

    override fun getValue(
        preferences: BITPreferences,
        tileType: TileType?,
    ): Flow<WifiSSIDVisibilityOption> {
        return preferences.getSSIDVisibility
    }

    override fun setValue(
        preferences: BITPreferences,
        tileType: TileType?,
        coroutineScope: CoroutineScope,
        value: WifiSSIDVisibilityOption
    ) {
        coroutineScope.launch {
            preferences.setSSIDVisibility(value)
        }
    }
}