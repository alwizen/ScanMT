package com.example.scanmt.model

import com.google.gson.annotations.SerializedName
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: DriverData?
)
data class DriverData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("driver_no")
    val driverNo: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("role")
    val role: String
)