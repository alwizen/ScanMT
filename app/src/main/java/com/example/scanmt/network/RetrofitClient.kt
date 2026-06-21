package com.example.scanmt.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
object RetrofitClient {
    // JIKA menggunakan emulator Android Studio, gunakan IP 10.0.2.2.
    // JIKA menggunakan HP fisik langsung, ganti dengan IP lokal laptop Anda (contoh: 192.168.1.10)
    // Pastikan HP dan Laptop terhubung ke Wi-Fi yang SAMA.
    private const val BASE_URL = "http://192.168.110.112:8000/"
    val instance: ApiService by lazy {
        // Interceptor untuk memantau log request/response di Logcat Android Studio
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Waktu tunggu koneksi
            .readTimeout(30, TimeUnit.SECONDS)    // Waktu tunggu baca data
            .writeTimeout(30, TimeUnit.SECONDS)   // Waktu tunggu kirim data
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}