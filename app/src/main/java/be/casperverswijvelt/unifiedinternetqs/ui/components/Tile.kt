package be.casperverswijvelt.unifiedinternetqs.ui.components

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileState


@Composable
fun LiveTile(
    modifier: Modifier = Modifier,
    tileBehaviour: TileBehaviour
) {
    var tileState by remember { mutableStateOf(tileBehaviour.finalTileState) }
    val context = LocalContext.current

    Tile(
        modifier = modifier,
        iconId = tileState.icon,
        title = tileState.label,
        subTitle = tileState.subtitle,
        active = tileState.state == android.service.quicksettings.Tile.STATE_ACTIVE,
        unavailable = tileState.state == android.service.quicksettings.Tile.STATE_UNAVAILABLE,
        onClick = {
            tileBehaviour.onClick()
        },
        onLongClick = {
            context.startActivity(Intent(tileBehaviour.onLongClickIntentAction))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tile(
    modifier: Modifier = Modifier,
    iconId: Int,
    title: String,
    active: Boolean = true,
    unavailable: Boolean = false,
    subTitle: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val darkTheme = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    val colorAnimationSpec = tween<Color>(350, easing = EaseInOut)
    val disabledBgColor by buttonBackgroundColor()

    val bgColor by animateColorAsState(
        targetValue = if (active)
            if (darkTheme) scheme.onPrimaryContainer else scheme.primaryContainer
        else
            disabledBgColor,
        animationSpec = colorAnimationSpec,
        label = "tile background color"
    )
    val fgColor by animateColorAsState(
        targetValue = if (active) Color.Black else if (darkTheme) Color.White else Color.Black,
        animationSpec = colorAnimationSpec,
        label = "tile foreground color"
    )
    val fgColorLight = Color(fgColor.red, fgColor.green, fgColor.blue, .7f)
    val alpha by animateFloatAsState(
        targetValue = if (unavailable) .35f else 1f,
        animationSpec = tween(350, easing = EaseInOut),
        label = "tile alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(30.dp))
            .alpha(alpha)
            .conditional(!unavailable) {
                combinedClickable(
                    onClick = { onClick() },
                    onLongClick = { onLongClick() }
                )
            }
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
