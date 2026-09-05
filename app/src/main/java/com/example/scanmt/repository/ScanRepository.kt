package com.example.scanmt.repository

import com.example.scanmt.model.ScanHistoryResponse
import com.example.scanmt.model.ScanLogItem
import com.example.scanmt.model.ScanRequest
import com.example.scanmt.model.ScanResponse
import com.example.scanmt.model.ScanSessionRequest
import com.example.scanmt.model.ScanSessionResponse
import com.example.scanmt.model.Tanker
import com.example.scanmt.model.TankerListResponse
import com.example.scanmt.network.RetrofitClient
import com.example.scanmt.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanRepository(private val sessionManager: SessionManager) {

    fun getDriverId(): Int = sessionManager.getDriverId()
    fun getDriverName(): String = sessionManager.getDriverName() ?: "Driver"
    fun logout() = sessionManager.logout()

    fun getScanSessionId(): Int = sessionManager.getScanSessionId()
    fun getTankerId(): Int = sessionManager.getTankerId()

    fun getAvailableTankers(onResult: (Boolean, String, List<Tanker>?) -> Unit) {
        RetrofitClient.getInstance(sessionManager.getBaseUrl()).getAvailableTankers()
            .enqueue(object : Callback<TankerListResponse> {
                override fun onResponse(call: Call<TankerListResponse>, response: Response<TankerListResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        onResult(true, body.message, body.data ?: emptyList())
                    } else {
                        onResult(false, body?.message ?: "Gagal mengambil daftar tanker", null)
                    }
                }

                override fun onFailure(call: Call<TankerListResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}", null)
                }
            })
    }

    fun startScanSession(tankerId: Int, deviceUuid: String, onResult: (Boolean, String) -> Unit) {
        val request = ScanSessionRequest(sessionManager.getDriverId(), deviceUuid, tankerId)
        RetrofitClient.getInstance(sessionManager.getBaseUrl()).startScanSession(request)
            .enqueue(object : Callback<ScanSessionResponse> {
                override fun onResponse(call: Call<ScanSessionResponse>, response: Response<ScanSessionResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true && body.data != null) {
                        sessionManager.saveScanSession(body.data.scanSessionId, tankerId)
                        onResult(true, body.message)
                    } else {
                        onResult(false, body?.message ?: "Gagal membuat sesi scan")
                    }
                }

                override fun onFailure(call: Call<ScanSessionResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

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
            scanSessionId = sessionManager.getScanSessionId(),
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
