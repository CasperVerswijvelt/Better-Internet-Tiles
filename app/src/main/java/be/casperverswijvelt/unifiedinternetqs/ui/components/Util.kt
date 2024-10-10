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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.extensions.contrastColor
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
            .fillMaxSize()
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
            .clickable { onClick() }
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
            modifier = Modifier
                .padding(bottom = 4.dp)
                .weight(1f),
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

@Composable
fun PreferenceCategoryTitle(text: String) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        color = MaterialTheme.colorScheme.primary
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
        Column(
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
fun ColorPalette() {
    val colors = mapOf(
        "primary" to MaterialTheme.colorScheme.primary,
        "onPrimary" to MaterialTheme.colorScheme.onPrimary,
        "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
        "onPrimaryContainer" to MaterialTheme.colorScheme.onPrimaryContainer,
        "inversePrimary" to MaterialTheme.colorScheme.inversePrimary,
        "secondary" to MaterialTheme.colorScheme.secondary,
        "onSecondary" to MaterialTheme.colorScheme.onSecondary,
        "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
        "onSecondaryContainer" to MaterialTheme.colorScheme.onSecondaryContainer,
        "tertiary" to MaterialTheme.colorScheme.tertiary,
        "onTertiary" to MaterialTheme.colorScheme.onTertiary,
        "tertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
        "onTertiaryContainer" to MaterialTheme.colorScheme.onTertiaryContainer,
        "background" to MaterialTheme.colorScheme.background,
        "onBackground" to MaterialTheme.colorScheme.onBackground,
        "surface" to MaterialTheme.colorScheme.surface,
        "onSurface" to MaterialTheme.colorScheme.onSurface,
        "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
        "onSurfaceVariant" to MaterialTheme.colorScheme.onSurfaceVariant,
        "surfaceTint" to MaterialTheme.colorScheme.surfaceTint,
        "inverseSurface" to MaterialTheme.colorScheme.inverseSurface,
        "inverseOnSurface" to MaterialTheme.colorScheme.inverseOnSurface,
        "error" to MaterialTheme.colorScheme.error,
        "onError" to MaterialTheme.colorScheme.onError,
        "errorContainer" to MaterialTheme.colorScheme.errorContainer,
        "onErrorContainer" to MaterialTheme.colorScheme.onErrorContainer,
        "outline" to MaterialTheme.colorScheme.outline,
        "outlineVariant" to MaterialTheme.colorScheme.outlineVariant,
        "scrim" to MaterialTheme.colorScheme.scrim,
        "surfaceBright" to MaterialTheme.colorScheme.surfaceBright,
        "surfaceDim" to MaterialTheme.colorScheme.surfaceDim,
        "surfaceContainer" to MaterialTheme.colorScheme.surfaceContainer,
        "surfaceContainerHigh" to MaterialTheme.colorScheme.surfaceContainerHigh,
        "surfaceContainerHighest" to MaterialTheme.colorScheme.surfaceContainerHighest,
        "surfaceContainerLow" to MaterialTheme.colorScheme.surfaceContainerLow,
        "surfaceContainerLowest" to MaterialTheme.colorScheme.surfaceContainerLowest
    )

    colors.entries.forEach { (name, color) ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(color)
        ) {
            Text(text = name, color = color.contrastColor())
        }
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

sealed class PermissionInfo(
    val tileResourceId: Int,
    val descriptionResourceId: Int,
    val tiles: Array<Int> = arrayOf()
) {
    data object Shell : PermissionInfo(
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

    data object ReadPhoneState : PermissionInfo(
        R.string.read_phone_state,
        R.string.read_phone_state_description,
        arrayOf(
            R.string.mobile_data,
            R.string.internet,
        )
    )

    data object BluetoothConnect : PermissionInfo(
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