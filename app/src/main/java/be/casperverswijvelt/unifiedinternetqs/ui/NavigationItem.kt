package be.casperverswijvelt.unifiedinternetqs.ui

import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    var route: String,
    var outlinedIcon: ImageVector,
    var filledIcon: ImageVector,
    var title: String
    ) {
    object Home: NavigationItem(
        "home",
        Icons.Outlined.Home,
        Icons.Filled.Home,
        "Home"
    )
    object Settings: NavigationItem(
        "settings",
        Icons.Outlined.Settings,
        Icons.Filled.Settings,
        "Home"
    )
    object Info: NavigationItem(
        "info",
        Icons.Outlined.Info,
        Icons.Filled.Info,
        "Home"
    )
}
