package com.example.scanmt.model

import com.google.gson.annotations.SerializedName
data class ScanRequest(
    @SerializedName("driver_id")
    val driverId: Int,

    @SerializedName("device_uuid")
    val deviceUuid: String,

    @SerializedName("rfid_uid")
    val rfidUid: String,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?
)
