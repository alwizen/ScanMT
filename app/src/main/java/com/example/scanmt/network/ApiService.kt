package com.example.scanmt.network

import com.example.scanmt.model.LoginRequest
import com.example.scanmt.model.LoginResponse
import com.example.scanmt.model.ScanHistoryResponse
import com.example.scanmt.model.ScanRequest
import com.example.scanmt.model.ScanResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/driver-login")
    fun loginDriver(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("api/scan")
    fun sendScanData(
        @Body request: ScanRequest
    ): Call<ScanResponse>

    @GET("api/scan-history")
    fun getScanHistory(
        @Query("driver_id") driverId: Int
    ): Call<ScanHistoryResponse>
}