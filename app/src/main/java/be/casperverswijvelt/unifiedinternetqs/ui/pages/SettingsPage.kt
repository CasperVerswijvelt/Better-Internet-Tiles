package be.casperverswijvelt.unifiedinternetqs.ui.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.ui.components.PreferenceEntry
import be.casperverswijvelt.unifiedinternetqs.ui.components.TogglePreferenceEntry
import be.casperverswijvelt.unifiedinternetqs.ui.components.TopBarPage

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun SettingsPage() {

    val context = LocalContext.current

    TopBarPage(
        title = stringResource(R.string.settings)
    ) {

        var toggled by remember { mutableStateOf(false) }
        TogglePreferenceEntry(
            icon = {
                Icon(Icons.Outlined.Lock, "lock")
            },
            title = stringResource(R.string.require_unlock_title),
            subTitle = stringResource(R.string.require_unlock_summary),
            toggled
        ) {
            toggled = it
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
    }
}