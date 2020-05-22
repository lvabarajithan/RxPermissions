package com.abarajithan.rxpermissions.sample

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abarajithan.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ask_btn.setOnClickListener {
            askPermissions()
        }

    }

    private fun askPermissions() {
        permissions_status.text = ""
        RxPermissions(this).request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_SMS
        ).subscribe { results ->
            val builder = StringBuilder(permissions_status.text.toString())
            for ((index, r) in results.withIndex()) {
                val name = r.permission.substringAfterLast(delimiter = '.')
                builder.append("[${index + 1}] $name -> ${r.granted}\n")
            }
            permissions_status.text = builder.toString()
        }
    }
}
