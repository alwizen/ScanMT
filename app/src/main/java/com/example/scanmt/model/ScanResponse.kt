package com.example.scanmt.model

import com.google.gson.annotations.SerializedName
data class ScanResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ScanData?
)
data class ScanData(
    @SerializedName("scan_log_id")
    val scanLogId: Int?
    // Anda bisa menambahkan field lain yang dikembalikan oleh backend Laravel Anda di sini
)