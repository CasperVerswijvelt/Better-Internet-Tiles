package be.casperverswijvelt.unifiedinternetqs.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.getShizukuAccessRequiredDialog

class SettingsFragment : PreferenceFragmentCompat() {

    private val requestReadPhoneStatePermissionId = 1002

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {

        context?.let { context ->
            when (preference?.key) {
                resources.getString(R.string.request_shizuku_access_key) -> {

                    if (ShizukuUtils.shizukuAvailable) {

                        if (ShizukuUtils.hasShizukuPermission()) {
                            Toast.makeText(
                                context,
                                R.string.shizuku_access_granted,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            ShizukuUtils.requestShizukuPermission { granted ->
                                if (granted) {
                                    Toast.makeText(
                                        context,
                                        R.string.shizuku_access_granted,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    getShizukuAccessRequiredDialog(context).show()
                                }
                            }
                        }
                    } else {
                        getShizukuAccessRequiredDialog(context).show()
                    }
                    return true
                }
                resources.getString(R.string.request_read_phone_state_key) -> {
                    val res = checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    if (res != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_PHONE_STATE),
                            requestReadPhoneStatePermissionId
                        )
                    } else {
                        Toast.makeText(
                            context,
                            R.string.read_phone_state_permission_granted,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return true
                }
                resources.getString(R.string.restart_app_key) -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    startActivity(intent)
                }
                else -> {
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        context?.let { context ->
            if (requestCode == requestReadPhoneStatePermissionId) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        context,
                        R.string.read_phone_state_permission_granted,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    AlertDialog.Builder(context)
                        .setTitle(R.string.read_phone_state_permission_denied)
                        .setMessage(R.string.read_phone_state_permission_denied_description)
                        .setPositiveButton(R.string.open_app
                        ) { _, _ -> startActivity(intent) }
                        .create().show()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}