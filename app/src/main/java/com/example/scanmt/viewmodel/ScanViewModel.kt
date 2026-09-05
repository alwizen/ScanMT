package com.example.scanmt.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.scanmt.model.ScanData
import com.example.scanmt.model.ScanLogItem
import com.example.scanmt.model.Tanker
import com.example.scanmt.repository.ScanRepository

enum class ScanMode {
    NFC, QR
}

class ScanViewModel(private val repository: ScanRepository) : ViewModel() {

    var availableTankers by mutableStateOf<List<Tanker>>(emptyList())
        private set
    var selectedTanker by mutableStateOf<Tanker?>(null)
        private set
    var tankerLoading by mutableStateOf(false)
        private set
    var tankerError by mutableStateOf<String?>(null)
        private set
    var sessionLoading by mutableStateOf(false)
        private set

    var driverName by mutableStateOf(repository.getDriverName())
        private set

    var scanMode by mutableStateOf(ScanMode.NFC)

    var nfcStatus by mutableStateOf("Tempelkan Kartu NFC")
    var rfidUid by mutableStateOf("")
    var locationText by mutableStateOf("Mencari GPS...")
    var latitude by mutableStateOf<Double?>(null)
    var longitude by mutableStateOf<Double?>(null)

    var isSending by mutableStateOf(false)
    var lastScanSuccess by mutableStateOf<Boolean?>(null)
    var lastScanResult by mutableStateOf<ScanData?>(null)

    // Set of scanned UIDs in the active session to prevent duplicate backend records
    val scannedUidsInSession = mutableStateListOf<String>()

    var scanHistoryList by mutableStateOf<List<ScanLogItem>>(emptyList())
    var isHistoryLoading by mutableStateOf(false)
    var historyError by mutableStateOf<String?>(null)

    fun loadTankers() {
        tankerLoading = true
        tankerError = null
        repository.getAvailableTankers { success, message, tankers ->
            tankerLoading = false
            if (success && tankers != null) {
                availableTankers = tankers
                selectedTanker = tankers.firstOrNull { it.id == repository.getTankerId() }
            } else {
                tankerError = message
            }
        }
    }

    fun selectTanker(tanker: Tanker, deviceUuid: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        if (sessionLoading) return
        sessionLoading = true
        repository.startScanSession(tanker.id, deviceUuid) { success, message ->
            sessionLoading = false
            if (success) {
                selectedTanker = tanker
                scannedUidsInSession.clear()
                resetScanState()
            }
            onResult(success, message)
        }
    }

    private val resetHandler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable {
        resetScanState()
    }

    fun setLocation(lat: Double?, lng: Double?, statusText: String) {
        latitude = lat
        longitude = lng
        locationText = statusText
    }

    fun setNfcAvailabilityStatus(status: String) {
        if (rfidUid.isEmpty() && !isSending && lastScanSuccess == null) {
            nfcStatus = status
        }
    }

    fun onTagDetected(uidHex: String, deviceUuid: String, onResult: ((Boolean, String) -> Unit)? = null) {
        rfidUid = uidHex
        nfcStatus = "Kartu Terdeteksi!"
        lastScanSuccess = null
        lastScanResult = null
        resetHandler.removeCallbacks(resetRunnable)
        kirimDataScan(uidHex, deviceUuid, onResult)
    }

    fun onQrDetected(uidStr: String, deviceUuid: String, onResult: ((Boolean, String) -> Unit)? = null) {
        if (isSending) return

        // Check if this QR / UID has already been scanned in the current session
        if (scannedUidsInSession.contains(uidStr)) {
            nfcStatus = "⚠️ QR ini sudah dilakukan scan sebelumnya"
            rfidUid = uidStr
            onResult?.invoke(false, "QR/Kompartemen ini sudah pernah di-scan!")
            return
        }

        // Add to scanned set and process
        scannedUidsInSession.add(uidStr)
        rfidUid = uidStr
        nfcStatus = "QR Code Terbaca!"
        lastScanSuccess = null
        lastScanResult = null
        resetHandler.removeCallbacks(resetRunnable)
        kirimDataScan(uidStr, deviceUuid, onResult)
    }

    private fun kirimDataScan(uid: String, deviceUuid: String, onResult: ((Boolean, String) -> Unit)?) {
        if (repository.getScanSessionId() < 1) {
            onResult?.invoke(false, "Pilih tanker terlebih dahulu untuk memulai sesi scan")
            return
        }
        isSending = true

        repository.sendScanData(
            deviceUuid = deviceUuid,
            rfidUid = uid,
            latitude = latitude,
            longitude = longitude
        ) { success, message, scanResponse ->
            isSending = false
            if (success && scanResponse?.data != null) {
                lastScanSuccess = true
                lastScanResult = scanResponse.data

                val isInside = scanResponse.data.geofence?.isInside == true
                val locationName = scanResponse.data.geofence?.locationName

                nfcStatus = if (isInside) {
                    "✓ Di Dalam Geofence (${locationName ?: "Lokasi Parkir"})"
                } else {
                    "⚠️ Di Luar Geofence Parkir MT"
                }
                onResult?.invoke(true, "Scan Berhasil Disimpan!")
                loadRiwayat() // Auto refresh history
            } else {
                lastScanSuccess = false
                lastScanResult = null
                nfcStatus = "Scan Gagal"
                onResult?.invoke(false, message)
            }

            resetHandler.removeCallbacks(resetRunnable)
            resetHandler.postDelayed(resetRunnable, 6000)
        }
    }

    fun resetScanState() {
        nfcStatus = if (scanMode == ScanMode.NFC) "Tempelkan Kartu NFC" else "Arahkan Kamera ke QR Code"
        rfidUid = ""
        lastScanSuccess = null
        lastScanResult = null
    }

    fun clearScannedSession() {
        scannedUidsInSession.clear()
        resetScanState()
    }

    fun loadRiwayat() {
        isHistoryLoading = true
        historyError = null
        repository.getScanHistory { success, message, list ->
            isHistoryLoading = false
            if (success && list != null) {
                scanHistoryList = list
                // Sync session scanned UIDs from history if available
                list.forEach { item ->
                    item.compartment?.rfidUid?.let { uid ->
                        if (!scannedUidsInSession.contains(uid)) {
                            scannedUidsInSession.add(uid)
                        }
                    }
                }
            } else {
                historyError = message
            }
        }
    }

    fun logout() {
        scannedUidsInSession.clear()
        repository.logout()
    }
}
