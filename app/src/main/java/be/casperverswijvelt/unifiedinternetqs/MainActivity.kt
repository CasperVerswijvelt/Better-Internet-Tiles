package be.casperverswijvelt.unifiedinternetqs

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.topjohnwu.superuser.Shell

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shell.getShell()

        val textView = TextView(this)
        textView.text = "Get permissions"

        addContentView(textView, ViewGroup.LayoutParams(500, 100))

        textView.setOnClickListener {

            // Permission mumbo jumo
            val res = checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
            if (res != PackageManager.PERMISSION_GRANTED) {
                println("PERMISSION NOT GRANTED")
                requestPermissions(
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    1002
                )
            } else {
                println("PERMISSION GRANTED")
            }
        }

        finish()
    }
}