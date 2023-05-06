package be.casperverswijvelt.unifiedinternetqs.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.casperverswijvelt.unifiedinternetqs.R

@Composable
fun PermissionVisualizer (modifier: Modifier) {
    val bgColor = MaterialTheme.colorScheme.errorContainer
    val contentColor = MaterialTheme.colorScheme.error
    val rounding = 15.dp

    var expanded by remember { mutableStateOf(false) }
    val dynamicRounding by animateDpAsState(
        targetValue = if (expanded) 0.dp else rounding,
        animationSpec = tween(durationMillis = 400, easing = EaseOut)
    )
    val expandIconRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    val permissionInfos = listOf(
        PermissionInfo.Shell, 
        PermissionInfo.ReadPhoneState, 
        PermissionInfo.BluetoothConnect
    )

    Column(modifier) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = rounding,
                topEnd = rounding,
                bottomStart = dynamicRounding,
                bottomEnd = dynamicRounding
            ),
            onClick = {
                expanded = !expanded
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.width(24.dp),
                    painter = painterResource(R.drawable.baseline_warning_amber_24),
                    contentDescription = "",
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(contentColor)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.missing_permissions),
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.alpha(.7f),
                        text = stringResource(R.string.missing_permissions_description),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    modifier = Modifier
                        .width(18.dp)
                        .alpha(.7f)
                        .rotate(expandIconRotation),
                    painter = painterResource(R.drawable.arrow_down),
                    contentDescription = "",
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(contentColor)
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column (
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        bottomEnd = rounding,
                        bottomStart = rounding)
                    )
            ) {
                permissionInfos.forEach {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = bgColor.copy(alpha = .7f)
                    ) {
                        Column {
                            Column (Modifier.padding(18.dp)) {
                                Text(
                                    text = stringResource(it.tileResourceId),
                                    color = contentColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    modifier = Modifier.alpha(.7f),
                                    text = stringResource(it.descriptionResourceId),
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp,
                                    color = contentColor
                                )
                                if (it.tiles.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        modifier = Modifier.alpha(.7f),
                                        text = pluralStringResource(
                                            id = R.plurals.used_for_tiles,
                                            count = it.tiles.size,
                                            joinToString(it.tiles.map { stringResource(it) })
                                        ),
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}