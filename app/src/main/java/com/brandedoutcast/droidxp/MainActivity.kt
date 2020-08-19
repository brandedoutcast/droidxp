package com.brandedoutcast.droidxp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import android.provider.Telephony.Sms as sms

class MainActivity : AppCompatActivity() {
    private val requestReadSMS: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_SMS), requestReadSMS)
            }
        }

        btnSaveMsgs.setOnClickListener { writeMessages() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestReadSMS) writeMessages()
    }

    private fun writeMessages() {
        val messages = getMessages()
        val conversations = messages.groupBy { it.address }.map { Cons(it.key, it.value) }
        val json = Gson().toJson(conversations)

        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "msgs.json")
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(json.toByteArray())
        fileOutputStream.close()

        Toast.makeText(this, "Saved messages to $file", Toast.LENGTH_LONG).show()
    }

    private fun getMessages(): List<Msg> {
        return contentResolver.query(
            sms.CONTENT_URI,
            arrayOf(sms.TYPE, sms.DATE_SENT, sms.ADDRESS, sms.BODY),
            null,
            null,
            sms.DEFAULT_SORT_ORDER
        )?.use { c ->
            (1..c.count).map {
                c.moveToNext()
                Msg(c.getInt(0), c.getLong(1), c.getString(2), c.getString(3))
            }
        }!!
    }
}

data class Cons(val from: String, val msgs: List<Msg>)

data class Msg(
    val type: Int,
    val date: Long,
    val address: String,
    val body: String
)
