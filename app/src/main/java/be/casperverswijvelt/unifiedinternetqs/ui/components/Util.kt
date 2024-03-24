package be.casperverswijvelt.unifiedinternetqs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.ui.pages.DrawableIcon
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData

@Composable
fun PreferenceEntry(
    icon: @Composable () -> Unit = {},
    title: String,
    subTitle: String? = null,
    checked: Boolean? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            icon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                fontSize = 18.sp,
                text = title
            )
            subTitle?.let {
                Text(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = subTitle,
                    lineHeight = 18.sp
                )
            }
        }
        checked?.let {
            Switch(
                modifier = Modifier.padding(start = 8.dp),
                checked = it,
                onCheckedChange = null,
            )
        }
    }
}

@Composable
fun RadioEntry(
    modifier: Modifier = Modifier,
    title: String,
    enabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable{ onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(50.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            RadioButton(selected = enabled, onClick = { onClick() })
        }
        Text(
            modifier = Modifier.padding(bottom = 4.dp).weight(1f),
            fontSize = 18.sp,
            text = title
        )
    }
}

@Composable
fun TogglePreferenceEntry(
    icon: @Composable () -> Unit = {},
    title: String,
    subTitle: String? = null,
    checked: Boolean,
    onToggled: (Boolean) -> Unit = {}
) {
    PreferenceEntry(
        icon = icon,
        title = title,
        subTitle = subTitle,
        checked = checked,
        onClick = {
            onToggled(!checked)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopBarPage(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        Column (
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}

@Composable
fun buttonBackgroundColor(): State<Color> {
    val darkTheme = isSystemInDarkTheme()
    return object : State<Color> {
        override val value: Color
            get() = Color(if (darkTheme) 0x18ffffff else 0x18000000)

    }
}

@Composable
fun ColorPalette () {
    val colors = arrayOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.inversePrimary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.onTertiary,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.onBackground,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.surfaceTint,
        MaterialTheme.colorScheme.inverseSurface,
        MaterialTheme.colorScheme.inverseOnSurface,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.onError,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.outlineVariant,
        MaterialTheme.colorScheme.scrim
    )
    colors.forEach {color ->
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(color))
    }
}

@Composable
fun AlertDialog(
    alertDialogData: AlertDialogData,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = alertDialogData.iconResource?.let { { DrawableIcon(it) } },
        title = { Text(stringResource(alertDialogData.titleResource)) },
        text = { Text(stringResource(alertDialogData.messageResource)) },
        confirmButton = {
            TextButton(onClick = {
                alertDialogData.onPositiveButtonClicked()
                onDismissRequest()
            }) {
                Text(stringResource(alertDialogData.positiveButtonResource))
            }
        }

    )
}

sealed class PermissionInfo (
    val tileResourceId: Int,
    val descriptionResourceId: Int,
    val tiles: Array<Int> = arrayOf()
) {
    data object Shell: PermissionInfo(
        R.string.shell_access,
        R.string.shell_access_description,
        arrayOf(
            R.string.wifi,
            R.string.mobile_data,
            R.string.internet,
            R.string.airplane_mode,
            R.string.nfc,
            R.string.bluetooth,
        )
    )
    data object Location: PermissionInfo(
        R.string.location,
        R.string.location_description,
        arrayOf(R.string.wifi)
    )
    data object ReadPhoneState: PermissionInfo(
        R.string.read_phone_state,
        R.string.read_phone_state_description,
        arrayOf(
            R.string.mobile_data,
            R.string.internet,
        )
    )
    data object BluetoothConnect: PermissionInfo(
        R.string.bluetooth_connect,
        R.string.bluetooth_connect_description,
        arrayOf(
            R.string.bluetooth
        )
    )
}

fun joinToString(texts: List<String>): String {
    if (texts.isEmpty()) return ""
    if (texts.size == 1) return texts[0]

    return "${texts.dropLast(1).joinToString(separator = ", ")} and ${texts.last()}"
}