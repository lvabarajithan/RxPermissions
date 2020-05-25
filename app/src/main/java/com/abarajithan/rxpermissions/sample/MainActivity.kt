package com.abarajithan.rxpermissions.sample

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abarajithan.rxpermissions.PermissionResult
import com.abarajithan.rxpermissions.RxPermissions
import io.reactivex.rxjava3.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.READ_SMS
    )

    private var radioId = R.id.request

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            this.radioId = checkedId
        }
        ask_btn.setOnClickListener {
            askPermissions()
        }

    }

    private val handlePermissionResult: Consumer<Array<out PermissionResult>> =
        Consumer { results ->
            val builder = StringBuilder(permissions_status.text.toString())
            for ((index, r) in results.withIndex()) {
                val name = r.permission.substringAfterLast(delimiter = '.')
                builder.append("[${index + 1}] $name -> ${r.granted}\n")
            }
            permissions_status.text = builder.toString()
        }

    private val handleSimpleResult: Consumer<Array<out Boolean>> =
        Consumer { results ->
            val builder = StringBuilder(permissions_status.text.toString())
            for ((index, granted) in results.withIndex()) {
                val name = permissions[index].substringAfterLast(delimiter = '.')
                builder.append("[${index + 1}] $name -> ${granted}\n")
            }
            permissions_status.text = builder.toString()
        }

    private fun askPermissions() {
        permissions_status.text = ""

        when (radioId) {
            R.id.request -> RxPermissions(this).request(*permissions)
                .subscribe(handlePermissionResult)
            R.id.request_skip_granted -> RxPermissions(this).requestSkipGranted(*permissions)
                .subscribe(handlePermissionResult)
            R.id.request_simple -> RxPermissions(this).requestSimple(*permissions)
                .subscribe(handleSimpleResult)
        }
    }
}
