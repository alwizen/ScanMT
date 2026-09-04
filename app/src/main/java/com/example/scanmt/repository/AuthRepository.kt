package com.example.scanmt.repository

import com.example.scanmt.model.LoginRequest
import com.example.scanmt.model.LoginResponse
import com.example.scanmt.network.RetrofitClient
import com.example.scanmt.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository(private val sessionManager: SessionManager) {

    fun login(driverNo: String, onResult: (Boolean, String, LoginResponse?) -> Unit) {
        val request = LoginRequest(driverNo)
        val baseUrl = sessionManager.getBaseUrl()

        RetrofitClient.getInstance(baseUrl).loginDriver(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        sessionManager.saveSession(
                            driverId = body.data.id,
                            driverNo = body.data.driverNo,
                            name = body.data.name
                        )
                        onResult(true, "Selamat datang, ${body.data.name}", body)
                    } else {
                        onResult(false, body?.message ?: "Gagal login", body)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Login Gagal (Error ${response.code()})"
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

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult(false, "Koneksi gagal: ${t.message}", null)
            }
        })
    }

    fun testConnection(baseUrl: String, onResult: (Boolean, String) -> Unit) {
        RetrofitClient.testConnection(baseUrl, onResult)
    }

    fun saveBaseUrl(url: String) {
        sessionManager.saveBaseUrl(url)
    }

    fun getBaseUrl(): String = sessionManager.getBaseUrl()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
