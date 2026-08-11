package com.pdflnx.ahmad

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class AppEngine(private val activity: Activity) {

    private lateinit var webView: WebView
    private var originalFileName = "Document"

    @SuppressLint("SetJavaScriptEnabled")
    fun start() {
        // Storage Permission Check
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        }

        // স্ট্যাটাস বার কালার ফিক্স (ডার্ক থিমের সাথে মেলানো)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor("#0f2027")
        }

        webView = WebView(activity)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(WebAppInterface(), "AndroidAppEngine")
        webView.loadUrl("file:///android_asset/index.html")

        activity.setContentView(webView)

        TerminalBridge.logListener = { message ->
            if (message.startsWith("FINISH:")) {
                val path = message.substringAfter("FINISH:")
                activity.runOnUiThread { webView.evaluateJavascript("javascript:onJobComplete('$path')", null) }
            } else {
                sendUpdateToHtml(message.replace("'", "\\'"))
            }
        }
    }

    // HTML এর সাথে যোগাযোগের ইন্টারফেস
    private inner class WebAppInterface {
        @JavascriptInterface
        fun pickPdfFile() {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            activity.startActivityForResult(intent, 1001)
        }

        @JavascriptInterface
        fun triggerLinuxCommand(pdfData: String) {
            startPdfProcessingService(pdfData)
        }

        @JavascriptInterface
        fun openFinalFile(path: String) {
            try {
                val file = File(path)
                val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/msword")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activity.startActivity(intent)
            } catch (e: Exception) {
                sendUpdateToHtml("No app found to open this file.")
            }
        }

        @JavascriptInterface
        fun shareFinalFile(path: String) {
            try {
                val file = File(path)
                val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "application/msword"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activity.startActivity(Intent.createChooser(intent, "Share Document"))
            } catch (e: Exception) {
                sendUpdateToHtml("Share error: ${e.message}")
            }
        }

        // HTML থেকে অ্যাপ ক্লোজ করার কমান্ড
        @JavascriptInterface
        fun exitApp() {
            activity.runOnUiThread {
                activity.finish()
            }
        }
    }

    @SuppressLint("Range")
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                copyFileToEngine(uri)
            }
        }
    }
    
    fun triggerBackPress() {
        activity.runOnUiThread {
            webView.evaluateJavascript("javascript:onHardwareBackPressed()", null)
        }
    }
    
    @SuppressLint("Range")
    private fun copyFileToEngine(uri: Uri) {
        Thread {
            try {
                sendUpdateToHtml("Loading PDF...")
                val cursor = activity.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        originalFileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
                originalFileName = originalFileName.substringBeforeLast(".")

                val outFile = File(activity.filesDir, "input.pdf") 
                val inputStream = activity.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(outFile)
                
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                activity.runOnUiThread {
                    webView.evaluateJavascript("javascript:onPdfReady('${outFile.absolutePath}')", null)
                }
            } catch (e: Exception) {
                sendUpdateToHtml("Error: ${e.message}")
            }
        }.start()
    }

    private fun sendUpdateToHtml(message: String) {
        activity.runOnUiThread {
            webView.evaluateJavascript("javascript:updateStatusFromKotlin('$message')", null)
        }
    }

    private fun startPdfProcessingService(pdfPath: String) {
        sendUpdateToHtml("Starting Background Process...")
        val intent = Intent(activity, PdfProcessService::class.java)
        intent.putExtra("PDF_PATH", pdfPath)
        intent.putExtra("ORIGINAL_NAME", originalFileName) 
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }
    }
}

object TerminalBridge {
    var logListener: ((String) -> Unit)? = null
}
