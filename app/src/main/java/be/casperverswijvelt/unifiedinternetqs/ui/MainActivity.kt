package be.casperverswijvelt.unifiedinternetqs.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileApplication
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.AirplaneModeTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.BluetoothTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.InternetTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.MobileDataTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.NFCTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.WifiTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.ui.components.AlertDialog
import be.casperverswijvelt.unifiedinternetqs.ui.components.NavRoute
import be.casperverswijvelt.unifiedinternetqs.ui.components.NavigationItem
import be.casperverswijvelt.unifiedinternetqs.ui.pages.HomePage
import be.casperverswijvelt.unifiedinternetqs.ui.pages.IndividualSettingsPage
import be.casperverswijvelt.unifiedinternetqs.ui.pages.InfoPage
import be.casperverswijvelt.unifiedinternetqs.ui.pages.SettingsPage
import be.casperverswijvelt.unifiedinternetqs.ui.pages.ShellMethodPage
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.reportException

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Display edge to edge
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (darkTheme)
                dynamicDarkColorScheme(LocalContext.current)
            else
                dynamicLightColorScheme(LocalContext.current)

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

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "Resumed, starting tile sync service!")
        if (TileSyncService.isRunning) return
        try {
            startForegroundService(
                Intent(
                    this,
                    TileSyncService::class.java
                )
            )
        } catch (e: Throwable) {
            Log.w(
                TileApplication.TAG,
                "Failed to start tile sync foreground service", e
            )
            reportException(e)

            // Not sure what the cause of the 'ForegroundServiceStartNotAllowedException'
            //  is or how to solve it.
            Toast.makeText(
                applicationContext,
                R.string.toast_foreground_service_error,
                Toast.LENGTH_SHORT
            ).show()
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
    var alertDialog: AlertDialogData? by remember {
        mutableStateOf(null)
    }

    val showDialog: (AlertDialogData) -> Unit = {
        alertDialog = it
    }

    val tileBehaviours = remember {
        listOf(
            WifiTileBehaviour(
                context = context,
                showDialog = showDialog
            ),
            MobileDataTileBehaviour(
                context = context,
                showDialog = showDialog
            ),
            InternetTileBehaviour(
                context = context,
                showDialog = showDialog
            ),
            AirplaneModeTileBehaviour(
                context = context,
                showDialog = showDialog
            ),
            NFCTileBehaviour(
                context = context,
                showDialog = showDialog
            ),
            BluetoothTileBehaviour(
                context = context,
                showDialog = showDialog
            )
        )
    }

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
                navController = navController,
                startDestination = NavRoute.Home.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                }
            ) {
                homeGraph(
                    navController = navController,
                    tileBehaviours = tileBehaviours
                )
                settingsGraph(navController)
                composable(NavRoute.Info.route) {
                    InfoPage()
                }
            }
        }
    }

    alertDialog?.let {
        AlertDialog(
            alertDialogData = it,
            onDismissRequest = {
                alertDialog = null
            }
        )
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
            val selected by derivedStateOf {
                currentRoute
                    ?.split("/")
                    ?.getOrNull(0) ==
                item.route
                    .split("/")
                    .getOrNull(0)
            }
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
                    if (navController.currentDestination?.route != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    tileBehaviours: List<TileBehaviour>
) {
    navigation(
        route = NavRoute.Home.route,
        startDestination = NavRoute.HomeBase.route
    ) {
        composable(
            route= NavRoute.HomeBase.route
        ) {
            HomePage(
                navController = navController,
                tileBehaviours = tileBehaviours
            )
        }
        val tileNavArgument = "tile"
        composable(
            route = "${NavRoute.HomeTileSettings.route}/{${tileNavArgument}}",
            arguments = listOf(navArgument(tileNavArgument) {
                type = NavType.IntType
            })
        ) { backstackEntry ->
            IndividualSettingsPage (
                onBackClicked = {
                    navController.popBackStack()
                },
                tileBehaviour = tileBehaviours.first {
                    it.type.value == backstackEntry.arguments?.getInt(tileNavArgument)
                }
            )
        }
    }
}

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    navigation(
        route = NavRoute.Settings.route,
        startDestination = NavRoute.SettingsBase.route
    ) {
        composable(NavRoute.SettingsBase.route) {
            SettingsPage(navController)
        }
        composable(NavRoute.SettingsShell.route) {
            ShellMethodPage (
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
    }
}