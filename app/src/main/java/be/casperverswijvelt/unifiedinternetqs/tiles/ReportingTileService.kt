package be.casperverswijvelt.unifiedinternetqs.tiles

import android.service.quicksettings.TileService
import be.casperverswijvelt.unifiedinternetqs.util.reportToAnalytics
import be.casperverswijvelt.unifiedinternetqs.util.saveTileUsed

abstract class ReportingTileService: TileService() {
    override fun onCreate() {
        super.onCreate()

        saveTileUsed(this)
        reportToAnalytics(this)
    }
}