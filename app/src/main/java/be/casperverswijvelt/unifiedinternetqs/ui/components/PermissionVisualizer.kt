package be.casperverswijvelt.unifiedinternetqs.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.util.hasShellAccess

@Composable
fun PermissionVisualizer (
    modifier: Modifier,
    navController: NavController
) {
    val context = LocalContext.current

    val errorBgColor = MaterialTheme.colorScheme.errorContainer
    val errorContentColor = MaterialTheme.colorScheme.onErrorContainer
    val successBgColor = if (isSystemInDarkTheme()) Color(0xFF365F32) else Color(0xFFCCEECC)
    val successContentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val rounding = 15.dp
    val contentPadding = 18.dp

    var permissionWarnings by remember {
        mutableStateOf(listOf<PermissionInfo>())
    }

    var expanded by remember { mutableStateOf(false) }
    val dynamicRounding by animateDpAsState(
        targetValue = if (expanded && permissionWarnings.isNotEmpty()) 0.dp else rounding,
        animationSpec = tween(durationMillis = 400, easing = EaseOut)
    )
    val expandIconRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    val syncPermissionWarnings = {
        val tempPermissionWarnings = arrayListOf<PermissionInfo>()

        if (!hasShellAccess(context)) {
            tempPermissionWarnings.add(PermissionInfo.Shell)
        }

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            tempPermissionWarnings.add(PermissionInfo.Location)
        }

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            tempPermissionWarnings.add(PermissionInfo.ReadPhoneState)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_DENIED
        ) {
            tempPermissionWarnings.add(PermissionInfo.BluetoothConnect)
        }
        permissionWarnings = tempPermissionWarnings
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        syncPermissionWarnings()
    }

    if (permissionWarnings.isNotEmpty()) {
        Column(modifier) {
            Surface(
                color = errorBgColor,
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
                        .padding(contentPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.width(24.dp),
                        painter = painterResource(R.drawable.baseline_warning_amber_24),
                        contentDescription = "",
                        contentScale = ContentScale.Inside,
                        colorFilter = ColorFilter.tint(errorContentColor)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.missing_permissions),
                            color = errorContentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            modifier = Modifier.alpha(.7f),
                            text = stringResource(R.string.missing_permissions_description),
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = errorContentColor
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
                        colorFilter = ColorFilter.tint(errorContentColor)
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
                    permissionWarnings.forEach {
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = errorBgColor.copy(alpha = .7f),
                            onClick = {
                                when (it) {
                                    PermissionInfo.Shell -> {
                                        navController.navigate(NavRoute.SettingsShell.route)
                                    }
                                    PermissionInfo.Location -> {
                                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                    PermissionInfo.BluetoothConnect -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                        }
                                    }
                                    PermissionInfo.ReadPhoneState -> {
                                        launcher.launch(Manifest.permission.READ_PHONE_STATE)
                                    }
                                }
                            }
                        ) {
                            Row (
                                modifier = Modifier.padding(contentPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Column (Modifier
                                    .weight(1f)
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(it.tileResourceId),
                                            color = errorContentColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            modifier = Modifier.alpha(.7f),
                                            text = stringResource(it.descriptionResourceId),
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            color = errorContentColor
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
                                                color = errorContentColor
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Image(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .alpha(.3f)
                                        .rotate(-90f),
                                    painter = painterResource(R.drawable.arrow_down),
                                    contentDescription = "",
                                    contentScale = ContentScale.Inside,
                                    colorFilter = ColorFilter.tint(errorContentColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = modifier,
            color = successBgColor,
            shape = RoundedCornerShape(rounding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.width(24.dp),
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = "",
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(successContentColor)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.good_to_go),
                        color = successContentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.alpha(.7f),
                        text = stringResource(R.string.good_to_go_description),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = successContentColor
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        syncPermissionWarnings()
    }

    OnLifecycleEvent { _, event ->
        when(event) {
            Lifecycle.Event.ON_RESUME -> {
                syncPermissionWarnings()
            }
            else -> {}
        }
    }
}