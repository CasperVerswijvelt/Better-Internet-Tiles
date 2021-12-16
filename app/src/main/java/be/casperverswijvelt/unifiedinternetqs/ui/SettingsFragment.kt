package be.casperverswijvelt.unifiedinternetqs.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import be.casperverswijvelt.unifiedinternetqs.util.ShizukuUtil
import com.jakewharton.processphoenix.ProcessPhoenix
import com.topjohnwu.superuser.Shell

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {

        context?.let { context ->
            when (preference?.key) {
                resources.getString(R.string.request_shizuku_access_key) -> {
                    if (ShizukuUtil.shizukuAvailable) {
                        if (ShizukuUtil.hasShizukuPermission()) {
                            Toast.makeText(
                                context,
                                R.string.shizuku_access_granted,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            ShizukuUtil.requestShizukuPermission { granted ->
                                if (granted) {
                                    Toast.makeText(
                                        context,
                                        R.string.shizuku_access_granted,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    getShellAccessRequiredDialog(context).show()
                                }
                            }
                        }
                    } else {
                        getShellAccessRequiredDialog(context).show()
                    }
                    return true
                }
                resources.getString(R.string.request_root_access_key) -> {
                    Shell.getShell()

                    if (Shell.rootAccess()) {
                        Toast.makeText(
                            context,
                            R.string.root_access_granted,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        AlertDialog.Builder(context)
                            .setTitle(R.string.root_access_denied)
                            .setMessage(R.string.root_access_denied_description)
                            .create().show()
                    }


                    return true
                }
                resources.getString(R.string.restart_app_key) -> {
                    ProcessPhoenix.triggerRebirth(context)
                }
                resources.getString(R.string.app_info_key) -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    startActivity(intent)
                }
                else -> {
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}