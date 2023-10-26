package com.mubin.downloadmanager

import android.health.connect.datatypes.StepsCadenceRecord

interface DownloaderService {
    fun downloadFile(url: String) : Long
}