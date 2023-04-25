package be.casperverswijvelt.unifiedinternetqs.ui.pages

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.StatusBarManager
import android.content.ComponentName
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.InternetTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.MobileDataTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.NFCTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileState
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.WifiTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.ui.components.VerticalGrid
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage() {
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
            ElevatedCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column (Modifier.padding(16.dp)) {

                    Text(
                        modifier = Modifier.padding(PaddingValues(bottom = 16.dp)),
                        text = stringResource(R.string.about),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.app_description_about)
                    )
                }
            }
            OutlinedCard(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column (Modifier.padding(16.dp)) {

                    Text(
                        modifier = Modifier.padding(PaddingValues(bottom = 16.dp)),
                        text = stringResource(R.string.about),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.app_description_about)
                    )
                }
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp),
                text = stringResource(id = R.string.available_tiles),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            TileOverview()
        }
    }
}

@Composable
fun TileOverview () {
    val context = LocalContext.current
    val showDialog: (Dialog) -> Unit = {}
    val localConfig = LocalConfiguration.current

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
            NFCTileBehaviour(
                context = context,
                showDialog = showDialog
            )
        )
    }

    VerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        columns = floor(localConfig.screenWidthDp / 200f).toInt(),
        horizontalSpacing = 8.dp,
        verticalSpacing = 20.dp,
        content = tileBehaviours.map {
            { LiveTileWithButtons(tileBehaviour = it) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTileWithButtons(
    modifier: Modifier = Modifier,
    tileBehaviour: TileBehaviour
) {
    val context = LocalContext.current
    val errorText = stringResource(R.string.tile_added_error)
    val tileAdded = stringResource(R.string.tile_added)
    val tileAlreadyAdded = stringResource(R.string.tile_already_added)
    var android13ModalOpen by remember { mutableStateOf(false) }
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
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                text = tileBehaviour.tileName,
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
            LiveTileExtraButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                                if(text.isNotEmpty()) {
                                    Toast.makeText(
                                        context,
                                        text,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    } else {
                        android13ModalOpen = true
                    }
                },
                icon = Icons.Filled.Add
            )
            LiveTileExtraButton(
                onClick = {},
                icon = Icons.Filled.Settings
            )
        }

        LiveTile(
            tileBehaviour = tileBehaviour
        )
    }
    if (android13ModalOpen) {
        AlertDialog(
            onDismissRequest = { android13ModalOpen = false }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.require_android_13),
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                    Text(
                        text = stringResource(id = R.string.require_android_13_description),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { android13ModalOpen = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(id = R.string.ok))
                    }
                }
            }
        }
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

@Composable
fun LiveTile(
    modifier: Modifier = Modifier,
    tileBehaviour: TileBehaviour
) {
    var tileState by remember { mutableStateOf(tileBehaviour.tileState) }

    Tile(
        modifier = modifier,
        iconId = tileState.icon,
        title = tileState.label,
        subTitle = tileState.subtitle,
        enabled = tileState.state == android.service.quicksettings.Tile.STATE_ACTIVE,
        onClick = {
            tileBehaviour.onClick()
        }
    )

    DisposableEffect(Unit) {

        TileSyncService.addBehaviourListener(tileBehaviour)
        val tileListener: (TileState) -> Unit = { tileState = it}
        tileBehaviour.addUpdateTileListeners(tileListener)

        onDispose {
            TileSyncService.removeBehaviourListener(tileBehaviour)
            tileBehaviour.removeUpdateTileListeners(tileListener)
        }
    }
}

val tileHeight = 80.dp

@Composable
fun buttonBackgroundColor(): State<Color> {
    val darkTheme = isSystemInDarkTheme()
    val colorState = remember { mutableStateOf(Color(0xFF000000)) }
    var color by colorState

    LaunchedEffect(darkTheme) {
        color = Color(if (darkTheme) 0x18ffffff else 0x18000000)
    }
    return colorState
}

@Composable
fun Tile(
    modifier: Modifier = Modifier,
    iconId: Int,
    title: String,
    enabled: Boolean = true,
    subTitle: String? = null,
    onClick: () -> Unit = {}
) {
    val darkTheme = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    val animationSpec = tween<Color>(350, easing = EaseInOut)
    val disabledBgColor by buttonBackgroundColor()

    val bgColor by animateColorAsState(
        targetValue = if (enabled)
            if (darkTheme) scheme.onPrimaryContainer else scheme.primaryContainer
        else
            disabledBgColor,
        animationSpec = animationSpec
    )
    val fgColor by animateColorAsState(
        targetValue = if (enabled) Color.Black else if (darkTheme) Color.White else Color.Black,
        animationSpec = animationSpec
    )
    val fgColorLight = Color(fgColor.red, fgColor.green, fgColor.blue, .7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(tileHeight)
            .clip(RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .background(bgColor)
            .padding(start = 17.dp, end = 25.dp)
    ) {
        Row (verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(iconId),
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(fgColor)
            )
            Column (
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = fgColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                subTitle?.let {
                    Text(
                        text = subTitle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = fgColorLight,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
