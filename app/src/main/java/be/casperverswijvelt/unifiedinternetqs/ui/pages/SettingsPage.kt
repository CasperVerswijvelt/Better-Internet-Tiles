package be.casperverswijvelt.unifiedinternetqs.ui.pages

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.data.BITPreferences
import be.casperverswijvelt.unifiedinternetqs.data.ShellMethod
import be.casperverswijvelt.unifiedinternetqs.ui.components.LargeTopBarPage
import be.casperverswijvelt.unifiedinternetqs.ui.components.NavRoute
import be.casperverswijvelt.unifiedinternetqs.ui.components.PreferenceEntry
import be.casperverswijvelt.unifiedinternetqs.ui.components.TogglePreferenceEntry
import kotlinx.coroutines.launch

@Composable
fun SettingsPage(
    navController: NavController
) {

    val context = LocalContext.current
    val preferences = BITPreferences(context)
    val shellMethod by preferences.getShellMethod.collectAsState(initial = ShellMethod.AUTO)

    BaseSettings (
        shellMethod = shellMethod,
        onShellMethodClicked = {
            navController.navigate(NavRoute.SettingsShell.route)
        }
    )
}

@Composable
fun BaseSettings(
    onShellMethodClicked: () -> Unit,
    shellMethod: ShellMethod
) {
    val context = LocalContext.current
    val dataStore = BITPreferences(context)
    val coroutineScope = rememberCoroutineScope()
    LargeTopBarPage(
        title = stringResource(R.string.settings)
    ) {

        val toggled by dataStore.getRequireUnlock.collectAsState(initial = true)
        TogglePreferenceEntry(
            icon = {
                Icon(Icons.Outlined.Lock, "lock")
            },
            title = stringResource(R.string.require_unlock_title),
            subTitle = stringResource(R.string.require_unlock_summary),
            toggled
        ) {
            coroutineScope.launch {
                dataStore.setRequireUnlock(it)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            PreferenceEntry(
                icon = {
                    DrawableIcon(R.drawable.baseline_translate_24)
                },
                title = stringResource(R.string.language),
                subTitle = Resources.getSystem().configuration.locales[0].displayLanguage
            ) {
                val intent =
                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                intent.data =
                    Uri.fromParts("package", context.packageName, null)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                context.startActivity(intent)
            }
        }
        PreferenceEntry(
            icon = {
                DrawableIcon(R.drawable.terminal)
            },
            title = stringResource(R.string.shell_method),
            subTitle = stringResource(shellMethod.stringResource)
        ) {
            onShellMethodClicked()
        }
    }
}