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
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    fun getDriverId(): Int {
        return prefs.getInt(KEY_DRIVER_ID, -1)
    }
    fun getDriverName(): String? {
        return prefs.getString(KEY_DRIVER_NAME, "")
    }
    fun logout() {
        prefs.edit().clear().apply()
    }
}