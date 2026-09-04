package com.example.scanmt.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.scanmt.repository.AuthRepository

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    var driverNo by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    
    var envExpanded by mutableStateOf(false)
    var baseUrl by mutableStateOf(repository.getBaseUrl())
    var testStatus by mutableStateOf<Pair<Boolean, String>?>(null)
    var isTesting by mutableStateOf(false)

    fun onDriverNoChanged(newVal: String) {
        driverNo = newVal
    }

    fun onBaseUrlChanged(newUrl: String) {
        baseUrl = newUrl
        testStatus = null
        repository.saveBaseUrl(newUrl)
    }

    fun doLogin(onResult: (Boolean, String) -> Unit) {
        if (driverNo.trim().isEmpty()) {
            onResult(false, "Masukkan Nomor Driver")
            return
        }

        isLoading = true
        repository.login(driverNo.trim()) { success, message, response ->
            isLoading = false
            onResult(success, message)
        }
    }

    fun testConnection() {
        if (baseUrl.isNotBlank()) {
            isTesting = true
            testStatus = null
            repository.testConnection(baseUrl) { success, msg ->
                isTesting = false
                testStatus = Pair(success, msg)
            }
        }
    }
}
