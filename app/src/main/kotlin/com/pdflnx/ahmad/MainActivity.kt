package com.pdflnx.ahmad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var appEngine: AppEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appEngine = AppEngine(this)
        appEngine.start()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        appEngine.handleActivityResult(requestCode, resultCode, data)
    }

    // ব্যাক বাটন চাপলে ইঞ্জিনকে সরাসরি সিগন্যাল দেওয়া হলো
    override fun onBackPressed() {
        appEngine.triggerBackPress() 
    }
}
