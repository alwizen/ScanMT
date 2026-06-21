package com.example.scanmt.network

import com.example.scanmt.model.LoginRequest
import com.example.scanmt.model.LoginResponse
import com.example.scanmt.model.ScanRequest
import com.example.scanmt.model.ScanResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
interface ApiService {
    // POST ke http://alamat-ip:8080/api/driver-login
    @POST("api/driver-login")
    fun loginDriver(
        @Body request: LoginRequest
    ): Call<LoginResponse>
    // POST ke http://alamat-ip:8080/api/scan
    @POST("api/scan")
    fun sendScanData(
        @Body request: ScanRequest
    ): Call<ScanResponse>
}