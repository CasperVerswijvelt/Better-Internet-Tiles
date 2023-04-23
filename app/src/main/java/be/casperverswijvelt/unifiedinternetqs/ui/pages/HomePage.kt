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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.InternetTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.MobileDataTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.NFCTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileState
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.WifiTileBehaviour

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
                modifier = Modifier.padding(start = 32.dp, top = 8.dp),
                text = stringResource(id = R.string.available_tiles),

            )
            TileOverview()
        }
    }
}

@Composable
fun TileOverview () {
    var wifiTileState: TileState by remember { mutableStateOf(TileState()) }
    var mobileDataTileState: TileState by remember { mutableStateOf(TileState()) }
    var internetTileState: TileState by remember { mutableStateOf(TileState()) }
    var nfcTileState: TileState by remember { mutableStateOf(TileState()) }

    val context = LocalContext.current
    val showDialog: (Dialog) -> Unit = {}
    val tileSpacing = 8.dp

    val wifiTileBehaviour = remember {
        var behaviour: WifiTileBehaviour? = null
        behaviour = WifiTileBehaviour(
            context = context,
            onRequestUpdate = {
                wifiTileState = behaviour!!.getTileState()
            },
            showDialog = showDialog
        )
        behaviour
    }
    val mobileDataTileBehaviour = remember {
        var behaviour: MobileDataTileBehaviour? = null
        behaviour = MobileDataTileBehaviour(
            context = context,
            onRequestUpdate = {
                mobileDataTileState = behaviour!!.getTileState()
            },
            showDialog = showDialog
        )
        behaviour!!
    }
    val internetTileBehaviour = remember {
        var behaviour: InternetTileBehaviour? = null
        behaviour = InternetTileBehaviour(
            context = context,
            onRequestUpdate = {
                internetTileState = behaviour!!.getTileState()
            },
            showDialog = showDialog
        )
        behaviour!!
    }
    val nfcTileBehaviour = remember {
        var behaviour: NFCTileBehaviour? = null
        behaviour = NFCTileBehaviour(
            context = context,
            onRequestUpdate = {
                nfcTileState = behaviour!!.getTileState()
            },
            showDialog = showDialog
        )
        behaviour!!
    }

    Column (
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(tileSpacing)
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(tileSpacing)
        ) {
            LiveTile(
                modifier = Modifier.weight(.5f),
                tileState = wifiTileState,
                onClick = { wifiTileBehaviour.onClick() }
            )
            LiveTile(
                modifier = Modifier.weight(.5f),
                tileState = mobileDataTileState,
                onClick = { mobileDataTileBehaviour.onClick() }
            )
        }
        Row (
            horizontalArrangement = Arrangement.spacedBy(tileSpacing)
        ) {
            LiveTile(
                modifier = Modifier.weight(.5f),
                tileState = internetTileState,
                onClick = { internetTileBehaviour.onClick() }
            )
            LiveTile(
                modifier = Modifier.weight(.5f),
                tileState = nfcTileState,
                onClick = { nfcTileBehaviour.onClick() }
            )
        }
    }
    DisposableEffect(Unit) {
        wifiTileState = wifiTileBehaviour.getTileState()
        mobileDataTileState = mobileDataTileBehaviour.getTileState()
        internetTileState = internetTileBehaviour.getTileState()
        nfcTileState = nfcTileBehaviour.getTileState()

        TileSyncService.addBehaviourListener(wifiTileBehaviour)
        TileSyncService.addBehaviourListener(mobileDataTileBehaviour)
        TileSyncService.addBehaviourListener(internetTileBehaviour)
        TileSyncService.addBehaviourListener(nfcTileBehaviour)

        onDispose {
            TileSyncService.removeBehaviourListener(wifiTileBehaviour)
            TileSyncService.removeBehaviourListener(mobileDataTileBehaviour)
            TileSyncService.removeBehaviourListener(internetTileBehaviour)
            TileSyncService.removeBehaviourListener(nfcTileBehaviour)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T>QuickAddTile(
    modifier: Modifier = Modifier,
    iconId: Int,
    title: String,
    subTitle: String? = null,
    enabled: Boolean,
    serviceClass: Class<T>
) {
    val context = LocalContext.current
    val errorText = stringResource(R.string.tile_added_error)
    val tileAdded = stringResource(R.string.tile_added)
    val tileAlreadyAdded = stringResource(R.string.tile_already_added)
    var android13ModalOpen by remember { mutableStateOf(false) }
    Tile(
        modifier = modifier,
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.getSystemService(
                    context,
                    StatusBarManager::class.java
                )?.requestAddTileService(
                    ComponentName(
                        context,
                        serviceClass,
                    ),
                    title,
                    android.graphics.drawable.Icon.createWithResource(context, iconId),
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
        iconId = iconId,
        title = title,
        subTitle = subTitle,
        enabled = enabled
    )
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
fun LiveTile(
    modifier: Modifier = Modifier,
    tileState: TileState,
    onClick: () -> Unit
) {
    Tile(
        modifier = modifier,
        iconId = tileState.icon,
        title = tileState.label,
        subTitle = tileState.subtitle,
        enabled = tileState.state == android.service.quicksettings.Tile.STATE_ACTIVE,
        onClick = {
            onClick()
        }
    )
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

    val bgColor by animateColorAsState(
        targetValue = if (enabled)
            if (darkTheme) scheme.onPrimaryContainer else scheme.primaryContainer
        else
            if (darkTheme) scheme.onTertiary else Color(0x11000000),
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
            .height(80.dp)
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
                    color = fgColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                subTitle?.let {
                    Text(
                        text = subTitle,
                        color = fgColorLight,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
