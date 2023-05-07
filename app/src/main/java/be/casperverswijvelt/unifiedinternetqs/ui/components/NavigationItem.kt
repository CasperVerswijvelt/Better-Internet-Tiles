package be.casperverswijvelt.unifiedinternetqs.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import be.casperverswijvelt.unifiedinternetqs.R

sealed class NavigationItem(
    var route: String,
    var outlinedIcon: ImageVector,
    var filledIcon: ImageVector,
    var titleId: Int
    ) {
    object Home: NavigationItem(
        NavRoute.HomeBase.route,
        Icons.Outlined.Home,
        Icons.Filled.Home,
        R.string.home
    )
    object Settings: NavigationItem(
        NavRoute.SettingsBase.route,
        Icons.Outlined.Settings,
        Icons.Filled.Settings,
        R.string.settings
    )
    object Info: NavigationItem(
        NavRoute.Info.route,
        Icons.Outlined.Info,
        Icons.Filled.Info,
        R.string.about
    )
}

enum class NavRoute(val route: String) {
    Home("home"),
    HomeBase("home/base"),
    HomeTileSettings("home/tile"),
    Settings("settings"),
    SettingsBase("settings/base"),
    SettingsShell("settings/shell"),
    Info("info"),
}