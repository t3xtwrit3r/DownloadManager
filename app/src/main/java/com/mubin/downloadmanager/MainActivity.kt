package com.mubin.downloadmanager

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private var dl = 0L
    private lateinit var dialoag : AlertDialog
    private val broadcastReceiver = DownloadCompleteReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerReceiver(broadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        requestPermission()

        findViewById<MaterialButton>(R.id.startDownload).setOnClickListener {
            val  fileDownloader = FileDownloader(this)
            dl = fileDownloader.downloadFile("https://file-examples.com/storage/fe1134defc6538ed39b8efa/2017/04/file_example_MP4_1920_18MG.mp4")
            lifecycleScope.launch(Dispatchers.IO) {
                checkProgress(dl)
            }
        }

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO)) {

                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_VIDEO), 16101179)

                } else {

                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_VIDEO), 16101179)

                }

            }

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 16101177)

                } else {

                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 16101177)

                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onResume() {
        super.onResume()

    }

    private suspend fun checkProgress(downloadId: Long) {
        withContext(Dispatchers.Main) {
            dialoag = AlertDialog.Builder(this@MainActivity).create()
            dialoag.setCancelable(false)
            dialoag.setTitle("Downloading")
            dialoag.setMessage("Progress: 0%")
            dialoag.show()
        }
        val downloadManager = getSystemService(DownloadManager::class.java)
        val progress: Int
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS) ?: 0)
            when (status) {
                DownloadManager.STATUS_FAILED -> {
                    withContext(Dispatchers.Main) {
                        dialoag.dismiss()
                    }
                }

                DownloadManager.STATUS_PAUSED -> {}
                DownloadManager.STATUS_PENDING -> {}
                DownloadManager.STATUS_RUNNING -> {
                    val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES) ?: 0)
                    if (total >= 0) {
                        val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR) ?: 0)
                        progress = (downloaded * 100L / total).toInt()
                        withContext(Dispatchers.Main) {
                            dialoag.setMessage("Progress: $progress%")
                        }
                    }
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    progress = 100
                    withContext(Dispatchers.Main) {
                        dialoag.setMessage("Success")
                        dialoag.dismiss()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                dialoag.dismiss()
            }
        }

    }

}