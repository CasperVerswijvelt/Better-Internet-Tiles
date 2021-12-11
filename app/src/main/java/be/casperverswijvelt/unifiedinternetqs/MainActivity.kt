package be.casperverswijvelt.unifiedinternetqs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        addContentView(textView, ViewGroup.LayoutParams(100, 100))

        textView.setOnClickListener(View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val res = checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                if (res != PackageManager.PERMISSION_GRANTED) {
                    println("PERMISSIONG NOT GRANTED")
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_PHONE_STATE),
                        1002
                    )
                } else {
                    println("PERMISSIONG GRANTED")
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // Close activity when resuming (navigating back from internet settings)

        finish()
    }
}