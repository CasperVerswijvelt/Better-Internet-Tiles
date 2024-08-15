package be.casperverswijvelt.unifiedinternetqs.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.settings.IBooleanTileSetting
import be.casperverswijvelt.unifiedinternetqs.settings.IChoiceSetting
import be.casperverswijvelt.unifiedinternetqs.settings.ISetting
import be.casperverswijvelt.unifiedinternetqs.settings.TileChoiceOption
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericSetting(setting: ISetting<*>, tileType: TileType? = null) {
    val context = LocalContext.current
    val preferences = remember { BITPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    when (setting) {
        is IChoiceSetting<*> -> {
            val value by setting.getValue(preferences, tileType)
                .collectAsState(initial = setting.defaultValue)
            var dialogOpen by remember { mutableStateOf(false) }

            PreferenceEntry(
                icon = { Icon(setting.icon, "") },
                title = stringResource(id = setting.nameResource),
                subTitle = stringResource(id = value.stringResource),
                onClick = {
                    dialogOpen = true
                }
            )
            if (dialogOpen) {
                val padding = 16.dp

                BasicAlertDialog(onDismissRequest = {
                    dialogOpen = false
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
                                    .padding(
                                        horizontal = padding * 2,
                                        vertical = padding
                                    )
                                    .fillMaxWidth(),
                                text = stringResource(setting.nameResource),
                                fontSize = 26.sp,
                                lineHeight = 36.sp
                            )
                            setting.choices.forEach { choice ->
                                RadioEntry(
                                    modifier = Modifier.height(50.dp),
                                    title = stringResource(choice.stringResource),
                                    enabled = value == choice,
                                    onClick = {
                                        (setting as? IChoiceSetting<TileChoiceOption>)?.setValue(
                                            preferences,
                                            tileType,
                                            coroutineScope,
                                            choice
                                        )
                                        dialogOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        is IBooleanTileSetting -> {
            val value by setting.getValue(preferences, tileType)
                .collectAsState(initial = setting.defaultValue)
            TogglePreferenceEntry(
                icon = { Icon(setting.icon, "") },
                title = stringResource(id = setting.nameResource),
                subTitle = stringResource(id = setting.summaryResource),
                checked = value,
                onToggled = {
                    setting.setValue(preferences, tileType, coroutineScope, !value)
                }
            )
        }
    }
}