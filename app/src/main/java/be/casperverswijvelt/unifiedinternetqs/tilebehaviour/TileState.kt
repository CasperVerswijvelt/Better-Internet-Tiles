package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile

class TileState {
    var label: String? = null
    var subtitle: String? = null
    var state: Int = Tile.STATE_INACTIVE
    var icon: Icon? = null
}