package com.example.scanmt.model

import com.google.gson.annotations.SerializedName
data class LoginRequest(
    @SerializedName("driver_no")
    val driverNo: String
)