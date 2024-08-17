package be.casperverswijvelt.unifiedinternetqs.settings

import androidx.compose.ui.graphics.vector.ImageVector
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ISetting<T> {
    val icon: ImageVector
    val nameResource: Int
    val defaultValue: T
    fun getValue(preferences: BITPreferences, tileType: TileType?): Flow<T>
    fun setValue(preferences: BITPreferences, tileType: TileType?, coroutineScope: CoroutineScope, value: T)
}

interface IChoiceSetting<T: TileChoiceOption>: ISetting<T> {
    val choices: List<T>
}
interface IBooleanTileSetting: ISetting<Boolean> {
    val summaryResource: Int
}