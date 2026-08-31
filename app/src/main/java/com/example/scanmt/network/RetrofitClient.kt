package com.example.scanmt.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getInstance(baseUrl: String): ApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Test koneksi ke server dengan melakukan GET request sederhana.
     * onResult(success: Boolean, message: String)
     */
    fun testConnection(baseUrl: String, onResult: (Boolean, String) -> Unit) {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        Thread {
            try {
                val request = Request.Builder()
                    .url(normalizedUrl)
                    .build()
                val response = client.newCall(request).execute()
                val code = response.code
                response.close()
                onResult(true, "Terhubung! (HTTP $code)")
            } catch (e: Exception) {
                onResult(false, "Gagal: ${e.message ?: "Tidak dapat terhubung"}")
            }
        }.start()
    }
}