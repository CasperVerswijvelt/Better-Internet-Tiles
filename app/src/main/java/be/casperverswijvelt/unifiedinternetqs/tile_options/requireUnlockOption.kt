package be.casperverswijvelt.unifiedinternetqs.tile_options

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.data.TileChoiceOption
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.IChoiceSetting
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class FollowSetting(override val value: String, override val stringResource: Int) :
    TileChoiceOption {
    FOLLOW("follow", R.string.use_default),
    YES("yes", R.string.yes),
    NO("no", R.string.no)
}

val requireUnlockOption = object : IChoiceSetting<FollowSetting> {
    override val icon = Icons.Outlined.Lock
    override val nameResource = R.string.require_unlock_title
    override val defaultValue = FollowSetting.FOLLOW
    override fun getValue(
        preferences: BITPreferences,
        tileType: TileType?
    ): Flow<FollowSetting> {
        return preferences.getRequireUnlock(tileType!!)
    }

    override fun setValue(
        preferences: BITPreferences,
        tileType: TileType?,
        coroutineScope: CoroutineScope,
        value: FollowSetting
    ) {
        coroutineScope.launch {
            preferences.setRequireUnlock(tileType!!, value)
        }
    }

    override val choices = listOf(
        FollowSetting.FOLLOW,
        FollowSetting.YES,
        FollowSetting.NO,
    )
}