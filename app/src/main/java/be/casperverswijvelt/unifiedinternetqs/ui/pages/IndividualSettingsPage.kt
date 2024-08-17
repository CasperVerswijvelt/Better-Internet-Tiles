package be.casperverswijvelt.unifiedinternetqs.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.settings.TileChoiceOption
import be.casperverswijvelt.unifiedinternetqs.settings.IChoiceSetting
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileBehaviour
import be.casperverswijvelt.unifiedinternetqs.ui.components.LiveTile
import be.casperverswijvelt.unifiedinternetqs.ui.components.PreferenceEntry
import be.casperverswijvelt.unifiedinternetqs.ui.components.RadioEntry
import be.casperverswijvelt.unifiedinternetqs.ui.components.GenericSetting
import be.casperverswijvelt.unifiedinternetqs.ui.components.PreferenceCategoryTitle
import be.casperverswijvelt.unifiedinternetqs.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualSettingsPage(
    onBackClicked: () -> Unit,
    tileBehaviour: TileBehaviour
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    .padding(vertical = 24.dp + (40 * (1 - scrollBehavior.state.collapsedFraction)).dp)
                    .align(Alignment.CenterHorizontally),
                tileBehaviour = tileBehaviour
            )


            if (tileBehaviour.behaviourSettings.isNotEmpty()) {
                PreferenceCategoryTitle(text = stringResource(
                    id = R.string.behaviour
                ))
                tileBehaviour.behaviourSettings.forEach { setting ->
                    GenericSetting(setting, tileBehaviour.type)
                }
            }

            if (tileBehaviour.lookSettings.isNotEmpty()) {

                PreferenceCategoryTitle(text = stringResource(
                    id = R.string.look
                ))
                tileBehaviour.lookSettings.forEach { setting ->
                    GenericSetting(setting, tileBehaviour.type)
                }
            }
        }
    }
}