@file:OptIn(ExperimentalMaterial3Api::class)

package be.casperverswijvelt.unifiedinternetqs.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                Surface(Modifier.background(colorScheme.background)) {}
                App()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar {
                navController.navigate(it.route)
            }
        }
    ) {
        Box (Modifier.padding(it)) {
            NavHost(
                navController = navController,
                startDestination = NavigationItem.Home.route
            ) {
                composable(NavigationItem.Home.route) {
                    HomePage()
                }
                composable(NavigationItem.Settings.route) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Yellow))
                }
                composable(NavigationItem.Info.route) {
                    Box(Modifier.fillMaxSize().background(Color.Green))
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onTabSelected: (NavigationItem) -> Unit) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Settings,
        NavigationItem.Info
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            val selected by derivedStateOf { index == selectedItem }
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    selectedItem = index
                    onTabSelected(item)
                }
            )
        }
    }
}

@Composable
fun BITTopAppBar() {
    TopAppBar(
        title = {
            Text(
                "Large TopAppBar",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Localized description"
                )
            }
        }
    )
}