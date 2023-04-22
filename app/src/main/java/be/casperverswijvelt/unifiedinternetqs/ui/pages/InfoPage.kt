package be.casperverswijvelt.unifiedinternetqs.ui.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import be.casperverswijvelt.unifiedinternetqs.BuildConfig
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.ui.components.PreferenceEntry
import be.casperverswijvelt.unifiedinternetqs.ui.components.LargeTopBarPage
import com.jakewharton.processphoenix.ProcessPhoenix

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun InfoPage() {

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LargeTopBarPage(
        title = stringResource(R.string.about)
    ) {

        PreferenceEntry(
            icon = {
                DrawableIcon(R.drawable.ic_baseline_feedback_24)
            },
            title = stringResource(R.string.feedback_title)
        ) {
            uriHandler.openUri("https://forms.gle/mU7oiPbQvro5owWf9")
        }

        PreferenceEntry(
            icon = {
                DrawableIcon(R.drawable.github_24)
            },
            title = stringResource(R.string.github_title)
        ) {
            uriHandler.openUri("https://github.com/CasperVerswijvelt/Better-Internet-Tiles")
        }

        PreferenceEntry(
            icon = {
                DrawableIcon(R.drawable.xda)
            },
            title = stringResource(R.string.xda_title)
        ) {
            uriHandler.openUri("https://forum.xda-developers.com/t/better-internet-tiles-bring-back-wi-fi-and-mobile-data-toggles-non-root.4373925/")
        }

        PreferenceEntry(
            icon = {
                DrawableIcon(R.drawable.baseline_translate_24)
            },
            title = stringResource(R.string.weblate),
            subTitle = stringResource(R.string.help_translate)
        ) {
            uriHandler.openUri("https://hosted.weblate.org/projects/better-internet-tiles/translations/")
        }

        PreferenceEntry(
            icon = {
                DrawableIcon(R.drawable.ic_baseline_loop_24)
            },
            title = stringResource(R.string.restart_app_title),
            subTitle = stringResource(R.string.restart_app_summary)
        ) {
            ProcessPhoenix.triggerRebirth(context)
        }

        PreferenceEntry(
            icon = {
                Icon(Icons.Outlined.Info, "info")
            },
            title = stringResource(R.string.app_info),
            subTitle = "${stringResource(R.string.app_name)} ${
                stringResource(R.string.version)
            } ${BuildConfig.VERSION_NAME}"
        ) {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts(
                "package",
                context.packageName,
                null
            )
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            context.startActivity(intent)
        }
    }
}

@Composable
fun DrawableIcon(id: Int) {
    Image(
        painter = painterResource(id),
        contentDescription = "",
        contentScale = ContentScale.Inside,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
    )
}