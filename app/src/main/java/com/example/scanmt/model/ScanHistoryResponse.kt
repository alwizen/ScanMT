package com.example.scanmt.model

import com.google.gson.annotations.SerializedName

data class ScanHistoryResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<ScanLogItem>?
)

data class ScanLogItem(
    @SerializedName("scan_log_id")
    val scanLogId: Int?,

    @SerializedName("scanned_at")
    val scannedAt: String?,

    @SerializedName("tanker")
    val tanker: TankerInfoData?,

    @SerializedName("compartment")
    val compartment: CompartmentInfoData?,

    @SerializedName("geofence")
    val geofence: GeofenceData?,

    @SerializedName("scan_status")
    val scanStatus: String?
)
