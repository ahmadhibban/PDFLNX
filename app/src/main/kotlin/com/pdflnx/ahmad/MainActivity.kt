package com.pdflnx.ahmad

/**
 * PDFLNX - Offline PDF OCR & Text Extraction Android App
 * Author: Ahmad Hibban
 */

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

    // Forward back press event to web engine
    override fun onBackPressed() {
        appEngine.triggerBackPress() 
    }
}
