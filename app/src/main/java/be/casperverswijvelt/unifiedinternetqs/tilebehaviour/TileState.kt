package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.service.quicksettings.Tile
import be.casperverswijvelt.unifiedinternetqs.R

class TileState {
    var label: String = ""
    var subtitle: String? = null
    var state: Int = Tile.STATE_INACTIVE
    var icon: Int = R.drawable.ic_outline_info_24
}