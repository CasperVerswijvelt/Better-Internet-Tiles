@file:OptIn(ExperimentalMaterial3Api::class)

package be.casperverswijvelt.unifiedinternetqs.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.ui.components.NavigationItem
import be.casperverswijvelt.unifiedinternetqs.ui.pages.HomePage
import be.casperverswijvelt.unifiedinternetqs.ui.pages.InfoPage
import be.casperverswijvelt.unifiedinternetqs.ui.pages.SettingsPage

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display edge to edge
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = when {
                dynamicColor && darkTheme -> dynamicDarkColorScheme(
                    LocalContext.current
                )
                dynamicColor && !darkTheme -> dynamicLightColorScheme(
                    LocalContext.current
                )
                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }
            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    Modifier
                        .background(colorScheme.surface)
                        .fillMaxSize()
                ) {}
                App()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    Box(
        Modifier
            .background(Color.Red)
            .fillMaxSize())

    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            BottomNavigationBar(
                items = listOf(
                    NavigationItem.Home,
                    NavigationItem.Settings,
                    NavigationItem.Info
                ),
                navController = navController
            )
        }) {
        Box(Modifier.padding(it)) {
            NavHost(
                navController = navController, startDestination = NavigationItem.Home.route
            ) {
                composable(NavigationItem.Home.route) {
                    HomePage()
                }
                composable(NavigationItem.Settings.route) {
                    SettingsPage()
                }
                composable(NavigationItem.Info.route) {
                    InfoPage()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        BITPreferences(context).loadPreferences()
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<NavigationItem>
) {
    NavigationBar(windowInsets = WindowInsets.navigationBars) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected by derivedStateOf { currentRoute == item.route }
            val title = stringResource(item.titleId)
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selected) item.filledIcon else item.outlinedIcon,
                        contentDescription = title
                    )
                },
                label = { Text(title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}