package com.example.scanmt.repository

import com.example.scanmt.model.ScanHistoryResponse
import com.example.scanmt.model.ScanLogItem
import com.example.scanmt.model.ScanRequest
import com.example.scanmt.model.ScanResponse
import com.example.scanmt.network.RetrofitClient
import com.example.scanmt.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanRepository(private val sessionManager: SessionManager) {

    fun getDriverId(): Int = sessionManager.getDriverId()
    fun getDriverName(): String = sessionManager.getDriverName() ?: "Driver"
    fun logout() = sessionManager.logout()

    fun sendScanData(
        deviceUuid: String,
        rfidUid: String,
        latitude: Double?,
        longitude: Double?,
        onResult: (Boolean, String, ScanResponse?) -> Unit
    ) {
        val driverId = sessionManager.getDriverId()
        val baseUrl = sessionManager.getBaseUrl()

        val request = ScanRequest(
            driverId = driverId,
            deviceUuid = deviceUuid,
            rfidUid = rfidUid,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0
        )

        RetrofitClient.getInstance(baseUrl).sendScanData(request).enqueue(object : Callback<ScanResponse> {
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    if (scanResponse != null && scanResponse.success) {
                        onResult(true, "Scan Berhasil Disimpan!", scanResponse)
                    } else {
                        onResult(false, scanResponse?.message ?: "Gagal scan", scanResponse)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Error Server: ${response.code()}"
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val jsonObject = org.json.JSONObject(errorBody)
                            errorMessage = jsonObject.optString("message", errorMessage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    onResult(false, errorMessage, null)
                }
            }

            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                onResult(false, "Gagal koneksi ke server: ${t.message}", null)
            }
        })
    }

    fun getScanHistory(onResult: (Boolean, String, List<ScanLogItem>?) -> Unit) {
        val driverId = sessionManager.getDriverId()
        val baseUrl = sessionManager.getBaseUrl()

        RetrofitClient.getInstance(baseUrl).getScanHistory(driverId).enqueue(object : Callback<ScanHistoryResponse> {
            override fun onResponse(call: Call<ScanHistoryResponse>, response: Response<ScanHistoryResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        onResult(true, body.message, body.data ?: emptyList())
                    } else {
                        onResult(false, body?.message ?: "Gagal mengambil riwayat", null)
                    }
                } else {
                    onResult(false, "Error ${response.code()}", null)
                }
            }

            override fun onFailure(call: Call<ScanHistoryResponse>, t: Throwable) {
                onResult(false, "Koneksi gagal: ${t.message}", null)
            }
        })
    }
}
