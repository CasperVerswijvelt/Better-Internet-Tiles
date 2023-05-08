package be.casperverswijvelt.unifiedinternetqs.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.data.RequireUnlockSetting
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.ui.components.LiveTile
import be.casperverswijvelt.unifiedinternetqs.ui.components.PreferenceEntry
import be.casperverswijvelt.unifiedinternetqs.ui.components.RadioEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualSettingsPage(
    onBackClicked: () -> Unit,
    tileBehaviour: TileBehaviour
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val preferences = BITPreferences(context)
    val coroutineScope = rememberCoroutineScope()
    val requireUnlock by preferences
        .getRequireUnlock(tileBehaviour.type)
        .collectAsState(initial = RequireUnlockSetting.FOLLOW)
    val setRequireUnlock: (RequireUnlockSetting) -> Unit = {
        coroutineScope.launch {
            preferences.setRequireUnlock(tileBehaviour.type, it)
        }
    }

    var requireUnlockDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(tileBehaviour.tileName) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                },
            )
        },
    ) {
        Column (
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            LiveTile(
                modifier = Modifier
                    .width(220.dp)
                    .padding(vertical = 64.dp)
                    .align(Alignment.CenterHorizontally),
                tileBehaviour = tileBehaviour
            )
            PreferenceEntry(
                title = stringResource(R.string.require_unlock_title),
                subTitle = stringResource(requireUnlock.stringResource),
                icon = {
                    Icon(Icons.Outlined.Lock, "")
                },
                onClick = {
                    requireUnlockDialogOpen = true
                }
            )
        }
    }

    if (requireUnlockDialogOpen) {
        val padding = 16.dp

        AlertDialog(onDismissRequest = {
            requireUnlockDialogOpen = false
        }) {
            val color = AlertDialogDefaults.containerColor
            Surface(
                shape = AlertDialogDefaults.shape,
                color = color,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column(modifier = Modifier.padding(vertical = padding)) {
                    Text(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxWidth(),
                        text = stringResource(R.string.require_unlock_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    listOf(
                        RequireUnlockSetting.FOLLOW,
                        RequireUnlockSetting.YES,
                        RequireUnlockSetting.NO
                    ).forEach {
                        RadioEntry(
                            modifier = Modifier.height(50.dp),
                            title = stringResource(it.stringResource),
                            enabled = requireUnlock == it,
                            onClick = {
                                setRequireUnlock(it)
                                requireUnlockDialogOpen = false
                            }
                        )
                    }
                }
            }
        }
    }
}