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
        "home",
        Icons.Outlined.Home,
        Icons.Filled.Home,
        R.string.home
    )
    object Settings: NavigationItem(
        "settings",
        Icons.Outlined.Settings,
        Icons.Filled.Settings,
        R.string.settings
    )
    object Info: NavigationItem(
        "about",
        Icons.Outlined.Info,
        Icons.Filled.Info,
        R.string.about
    )
}
