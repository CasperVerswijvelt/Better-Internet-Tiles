package be.casperverswijvelt.unifiedinternetqs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.topjohnwu.superuser.Shell

class SettingsFragment : PreferenceFragmentCompat() {

    private val requestReadPhoneStatePermissionId = 1002

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {

        context?.let { context ->
            when (preference?.key) {
                resources.getString(R.string.request_root_access_key) -> {
                    Shell.getShell()

                    Toast.makeText(
                        context,
                        if (Shell.rootAccess())
                            R.string.root_access_granted
                        else
                            R.string.root_access_denied,
                        Toast.LENGTH_SHORT
                    ).show()
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
                else -> {}
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
            when (requestCode) {
                requestReadPhoneStatePermissionId -> {
                    Toast.makeText(
                        context,
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                            R.string.read_phone_state_permission_granted
                        else
                            R.string.read_phone_state_permission_denied,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}