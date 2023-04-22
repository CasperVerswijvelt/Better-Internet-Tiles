package be.casperverswijvelt.unifiedinternetqs.ui.pages

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.tiles.InternetTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.MobileDataTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.NFCTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiTileService

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
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
            val tileSpacing = 8.dp
            Column (
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(tileSpacing)
            ) {
                Row (
                    horizontalArrangement = Arrangement.spacedBy(tileSpacing)
                ) {
                    QuickAddTile(
                        modifier = Modifier.weight(.5f),
                        iconId = R.drawable.ic_baseline_signal_wifi_3_bar_24,
                        title= stringResource(id = R.string.wifi),
                        subTitle = stringResource(id = R.string.tap_to_add),
                        serviceClass = WifiTileService::class.java,
                        enabled = true
                    )
                    QuickAddTile(
                        modifier = Modifier.weight(.5f),
                        iconId = R.drawable.ic_baseline_mobile_data_24,
                        title= stringResource(id = R.string.mobile_data),
                        subTitle = stringResource(id = R.string.tap_to_add),
                        serviceClass = MobileDataTileService::class.java,
                        enabled = false
                    )
                }
                Row (
                    horizontalArrangement = Arrangement.spacedBy(tileSpacing)
                ) {
                    QuickAddTile(
                        modifier = Modifier.weight(.5f),
                        iconId = R.drawable.ic_baseline_public_24,
                        title= stringResource(id = R.string.internet),
                        subTitle = stringResource(id = R.string.tap_to_add),
                        serviceClass = InternetTileService::class.java,
                        enabled = false
                    )
                    QuickAddTile(
                        modifier = Modifier.weight(.5f),
                        iconId = R.drawable.nfc_24,
                        title= stringResource(id = R.string.nfc),
                        subTitle = stringResource(id = R.string.tap_to_add),
                        serviceClass = NFCTileService::class.java,
                        enabled = true
                    )
                }
            }
        }
    }
}

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
                    {},
                    {
                        Toast.makeText(
                            context,
                            "$errorText ($it)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        },
        iconId = iconId,
        title = title,
        subTitle = subTitle,
        enabled = enabled
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

    val bgColor = if (enabled)
        if (darkTheme) scheme.onPrimaryContainer else scheme.primaryContainer
    else
        if (darkTheme) scheme.onTertiary else Color(0x11000000)

    val fgColor = if (enabled) Color.Black else if (darkTheme) Color.White else Color.Black

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
@Preview
@Composable
fun tile() {
    Tile(
        iconId = R.drawable.ic_baseline_loop_24,
        title= "Internet",
        subTitle = "Off"
    )
}
