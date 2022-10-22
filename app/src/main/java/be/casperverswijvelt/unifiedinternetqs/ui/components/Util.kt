package be.casperverswijvelt.unifiedinternetqs.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceEntry(
    icon: @Composable () -> Unit = {},
    title: String,
    subTitle: String? = null,
    checked: Boolean? = null,
    onClicked: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable { onClicked() }
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
                    text = subTitle
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
        onClicked = {
            onToggled(!checked)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarPage(
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
            modifier = Modifier.padding(top = it.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}