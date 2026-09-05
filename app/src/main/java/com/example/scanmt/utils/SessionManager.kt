package com.example.scanmt.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("scanner_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_DRIVER_ID = "driver_id"
        private const val KEY_DRIVER_NAME = "driver_name"
        private const val KEY_DRIVER_NO = "driver_no"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_SCAN_SESSION_ID = "scan_session_id"
        private const val KEY_TANKER_ID = "tanker_id"
        const val DEFAULT_BASE_URL = "http://192.168.110.112:8000/"
    }

    fun saveSession(driverId: Int, driverNo: String, name: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_DRIVER_ID, driverId)
            putString(KEY_DRIVER_NO, driverNo)
            putString(KEY_DRIVER_NAME, name)
            apply()
        }
    }

    fun saveBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getDriverId(): Int {
        return prefs.getInt(KEY_DRIVER_ID, -1)
    }

    fun getDriverName(): String? {
        return prefs.getString(KEY_DRIVER_NAME, "")
    }

    fun saveScanSession(sessionId: Int, tankerId: Int) {
        prefs.edit()
            .putInt(KEY_SCAN_SESSION_ID, sessionId)
            .putInt(KEY_TANKER_ID, tankerId)
            .apply()
    }

    fun getScanSessionId(): Int = prefs.getInt(KEY_SCAN_SESSION_ID, -1)

    fun getTankerId(): Int = prefs.getInt(KEY_TANKER_ID, -1)

    fun clearScanSession() {
        prefs.edit()
            .remove(KEY_SCAN_SESSION_ID)
            .remove(KEY_TANKER_ID)
            .apply()
    }

    fun logout() {
        val savedUrl = getBaseUrl()
        prefs.edit().clear().apply()
        saveBaseUrl(savedUrl) // preserve base url after logout
    }
}