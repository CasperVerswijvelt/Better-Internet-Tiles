package be.casperverswijvelt.unifiedinternetqs.settings.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.settings.IChoiceSetting
import be.casperverswijvelt.unifiedinternetqs.settings.TileChoiceOption
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class FollowOption(override val value: String, override val stringResource: Int) :
    TileChoiceOption {
    FOLLOW("follow", R.string.use_default),
    YES("yes", R.string.yes),
    NO("no", R.string.no)
}

val requireUnlockSetting = object : IChoiceSetting<FollowOption> {
    override val icon = Icons.Outlined.Lock
    override val nameResource = R.string.require_unlock_title
    override val defaultValue = FollowOption.FOLLOW
    override fun getValue(
        preferences: BITPreferences,
        tileType: TileType?
    ): Flow<FollowOption> {
        return preferences.getRequireUnlock(tileType!!)
    }

    override fun setValue(
        preferences: BITPreferences,
        tileType: TileType?,
        coroutineScope: CoroutineScope,
        value: FollowOption
    ) {
        coroutineScope.launch {
            preferences.setRequireUnlock(tileType!!, value)
        }
    }

    override val choices = listOf(
        FollowOption.FOLLOW,
        FollowOption.YES,
        FollowOption.NO,
    )
}