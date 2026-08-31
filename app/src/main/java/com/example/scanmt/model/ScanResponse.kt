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
    val scanLogId: Int?,

    @SerializedName("scanned_at")
    val scannedAt: String?,

    @SerializedName("tanker")
    val tanker: TankerInfoData?,

    @SerializedName("compartment")
    val compartment: CompartmentInfoData?,

    @SerializedName("geofence")
    val geofence: GeofenceData?
)

data class TankerInfoData(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("nopol")
    val nopol: String?,

    @SerializedName("capacity_kl")
    val capacityKl: Int?
)

data class CompartmentInfoData(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("compartment_no")
    val compartmentNo: Int?,

    @SerializedName("capacity_kl")
    val capacityKl: Int?,

    @SerializedName("rfid_uid")
    val rfidUid: String?
)

data class GeofenceData(
    @SerializedName("is_inside")
    val isInside: Boolean,

    @SerializedName("location_id")
    val locationId: Int?,

    @SerializedName("location_name")
    val locationName: String?,

    @SerializedName("status_text")
    val statusText: String?
)