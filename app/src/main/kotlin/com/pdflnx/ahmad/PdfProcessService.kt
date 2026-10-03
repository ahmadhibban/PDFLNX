package com.pdflnx.ahmad

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.PowerManager
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

class PdfProcessService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private val channelId = "PdfMasterChannel"
    private var finalDocumentName = "Extracted_Document"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        finalDocumentName = intent?.getStringExtra("ORIGINAL_NAME") ?: "Extracted_Document"
        
        setupForegroundAndWakeLock()

        Thread {
            processPdfNatively()
        }.start()

        return START_STICKY
    }

    private fun setupForegroundAndWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PDFLnx::MasterWakeLock")
        wakeLock?.acquire(120 * 60 * 1000L) 

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Master Engine", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PDF Engine Running")
            .setContentText("Processing '$finalDocumentName' in background...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
        startForeground(1, notification)
    }

    private fun processPdfNatively() {
        try {
            val tessDir = File(filesDir, "tessdata")
            if (!tessDir.exists()) tessDir.mkdirs()
            
            val languages = listOf("eng", "ben", "ara", "urd")
            for (lang in languages) {
                val langFile = File(tessDir, "$lang.traineddata")
                // ইন্টারনেট থেকে ডাউনলোড করার বদলে assets থেকে ফাইলগুলো লোকাল ফোল্ডারে কপি করা হচ্ছে
                if (!langFile.exists()) {
                    TerminalBridge.logListener?.invoke("Extracting $lang engine...")
                    assets.open("tessdata/$lang.traineddata").use { input ->
                        FileOutputStream(langFile).use { output -> 
                            input.copyTo(output) 
                        }
                    }
                }
            }

            val pdfFile = File(filesDir, "input.pdf")
            val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            
            val tess = TessBaseAPI()
            tess.init(filesDir.absolutePath, "eng+ben+ara+urd")
            tess.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD
            
            val extractedText = StringBuilder()
            val totalPages = renderer.pageCount

            for (i in 0 until totalPages) {
                TerminalBridge.logListener?.invoke("Extracting Page ${i + 1} of $totalPages...")
                val page = renderer.openPage(i)
                
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE) 
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                tess.setImage(bitmap)
                extractedText.append(tess.utF8Text)
                extractedText.append("\n\n---\n\n")
                
                page.close()
                bitmap.recycle() 
            }
            
            renderer.close()
            pfd.close()
            tess.recycle()

            TerminalBridge.logListener?.invoke("Saving File...")
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val finalFile = File(downloadsDir, "$finalDocumentName.doc")
            finalFile.writeText(extractedText.toString())
            
            TerminalBridge.logListener?.invoke("FINISH:${finalFile.absolutePath}")
            showFinalNotification("Success!", "Saved as $finalDocumentName.doc in Downloads.")

        } catch (e: Exception) {
            TerminalBridge.logListener?.invoke("Error: ${e.message}")
            showFinalNotification("Failed", e.message ?: "Unknown error")
        } finally {
            wakeLock?.let { if (it.isHeld) it.release() }
            stopForeground(true)
            stopSelf()
        }
    }

    private fun showFinalNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(2, notification)
    }
}
