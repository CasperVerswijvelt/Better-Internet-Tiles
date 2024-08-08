package be.casperverswijvelt.unifiedinternetqs.ui.pages

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.ui.components.LiveTile
import be.casperverswijvelt.unifiedinternetqs.ui.components.NavRoute
import be.casperverswijvelt.unifiedinternetqs.ui.components.PermissionVisualizer
import be.casperverswijvelt.unifiedinternetqs.ui.components.VerticalGrid
import be.casperverswijvelt.unifiedinternetqs.ui.components.buttonBackgroundColor
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage(
    navController: NavController,
    tileBehaviours: List<TileBehaviour>
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        Column (
            Modifier
                .padding(
                    start = it.calculateStartPadding(LayoutDirection.Ltr),
                    end = it.calculateEndPadding(LayoutDirection.Ltr),
                    top = it.calculateTopPadding(),
                )
                .verticalScroll(rememberScrollState())
        ) {
            PermissionVisualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                navController = navController
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp),
                text = stringResource(id = R.string.available_tiles),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            TileOverview(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                navController = navController,
                tileBehaviours = tileBehaviours
            )
        }
    }
}

@Composable
fun TileOverview (
    modifier: Modifier,
    tileBehaviours: List<TileBehaviour>,
    navController: NavController
) {
    val localConfig = LocalConfiguration.current


    VerticalGrid(
        modifier = modifier,
        columns = floor(localConfig.screenWidthDp / 200f).toInt(),
        horizontalSpacing = 8.dp,
        verticalSpacing = 20.dp,
        content = tileBehaviours.map {
            { LiveTileWithButtons(
                tileBehaviour = it,
                navController = navController
            ) }
        }
    )
}

@Composable
fun LiveTileWithButtons(
    modifier: Modifier = Modifier,
    tileBehaviour: TileBehaviour,
    navController: NavController
) {
    val context = LocalContext.current
    val errorText = stringResource(R.string.tile_added_error)
    val tileAdded = stringResource(R.string.tile_added)
    val tileAlreadyAdded = stringResource(R.string.tile_already_added)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                text = tileBehaviour.tileName,
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LiveTileExtraButton(
                    onClick = {
                        ContextCompat.getSystemService(
                            context,
                            StatusBarManager::class.java
                        )?.requestAddTileService(
                            ComponentName(
                                context,
                                tileBehaviour.tileServiceClass
                            ),
                            tileBehaviour.tileName,
                            tileBehaviour.defaultIcon,
                            { Handler(Looper.getMainLooper()).post(it) },
                            {
                                val text = when(it) {
                                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> {
                                        tileAdded
                                    }
                                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> {
                                        tileAlreadyAdded
                                    }
                                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> {
                                        ""
                                    }
                                    else -> {
                                        errorText
                                    }
                                }
                                if (text.isNotEmpty()) {
                                    Toast.makeText(
                                        context,
                                        text,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    },
                    icon = Icons.Filled.Add
                )
            }
            LiveTileExtraButton(
                onClick = {
                    navController.navigate("${NavRoute.HomeTileSettings.route}/${tileBehaviour.type.value}")
                },
                icon = Icons.Filled.Settings
            )
        }

        LiveTile(
            tileBehaviour = tileBehaviour
        )
    }
}

@Composable
fun LiveTileExtraButton(
    onClick: () -> Unit,
    icon: ImageVector
) {
    val disabledBgColor by buttonBackgroundColor()
    Box(
        modifier = Modifier
            .size(35.dp)
            .clip(CircleShape)
            .background(disabledBgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(.55f),
            imageVector = icon,
            contentDescription = ""
        )
    }
}