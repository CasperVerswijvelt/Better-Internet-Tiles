package be.casperverswijvelt.unifiedinternetqs.ui

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * Some convenience functions for handling using Shizuku.
 */
object ShizukuUtils {
    /**
     * Checks if Shizuku is available. If the Shizuku Manager app
     * is either uninstalled OR isn't running, this will return
     * false.
     */
    val shizukuAvailable: Boolean
        get() = Shizuku.pingBinder() && (Shizuku.getVersion() >= 11 && !Shizuku.isPreV11())

    /**
     * Checks if the current app has permission to use Shizuku.
     */
    fun hasShizukuPermission(): Boolean {
        if (!shizukuAvailable) {
            return false
        }

        return (
            Shizuku.getVersion() >= 11 &&
            !Shizuku.isPreV11() &&
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        )
    }

    /**
     * Request permission to use Shizuku if it's not already granted. This works
     * for all versions of the Shizuku API.
     *
     * @param callback invoked when the permission grant result is received.
     */
    fun requestShizukuPermission(callback: (granted: Boolean) -> Unit) {
        Shizuku.addRequestPermissionResultListener(object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                Shizuku.removeRequestPermissionResultListener(this)
                callback(grantResult == PackageManager.PERMISSION_GRANTED)
            }
        })
        Shizuku.requestPermission(69101)
    }

    fun executeCommand(command: String): Process {
        val process = Shizuku.newProcess(
            command.split(' ').toTypedArray(),
            null,
            null
        )
        process.waitFor()
        return process
    }
}