package com.example.scanmt.model

import com.google.gson.annotations.SerializedName

data class Tanker(
    val id: Int,
    val nopol: String,
    @SerializedName("capacity_kl") val capacityKl: Int,
    @SerializedName("compartments_count") val compartmentsCount: Int = 0
)

data class TankerListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Tanker>?
)

data class ScanSessionRequest(
    @SerializedName("driver_id") val driverId: Int,
    @SerializedName("device_uuid") val deviceUuid: String,
    @SerializedName("tanker_id") val tankerId: Int
)

data class ScanSessionData(
    @SerializedName("scan_session_id") val scanSessionId: Int,
    @SerializedName("tanker_id") val tankerId: Int,
    val status: String
)

data class ScanSessionResponse(
    val success: Boolean,
    val message: String,
    val data: ScanSessionData?
)