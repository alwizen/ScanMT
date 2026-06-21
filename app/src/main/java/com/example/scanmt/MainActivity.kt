package com.example.scanmt

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.scanmt.model.ScanRequest
import com.example.scanmt.model.ScanResponse
import com.example.scanmt.network.RetrofitClient
import com.example.scanmt.ui.theme.ScanMTTheme
import com.example.scanmt.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // State Compose untuk memperbarui UI secara real-time
    private var driverNameState = mutableStateOf("")
    private var nfcStatusState = mutableStateOf("Menunggu Kartu NFC...")
    private var rfidUidState = mutableStateOf("-")
    private var locationState = mutableStateOf("Mencari GPS...")
    private var latitudeState = mutableStateOf<Double?>(null)
    private var longitudeState = mutableStateOf<Double?>(null)
    private var isSendingState = mutableStateOf(false)
    private var deviceUuidState = mutableStateOf("")

    // Launcher untuk meminta izin Lokasi/GPS
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getDeviceLocation()
        } else {
            locationState.value = "Izin GPS Ditolak"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Cek login session
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        driverNameState.value = sessionManager.getDriverName() ?: "Driver"
        deviceUuidState.value = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Inisialisasi NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            nfcStatusState.value = "Perangkat tidak mendukung NFC"
        } else if (!nfcAdapter!!.isEnabled) {
            nfcStatusState.value = "NFC Nonaktif. Silakan aktifkan di Pengaturan."
        }

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        // Minta Izin Lokasi
        checkLocationPermissions()

        setContent {
            ScanMTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info Driver
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Driver Aktif:", style = MaterialTheme.typography.bodySmall)
                    Text(driverNameState.value, style = MaterialTheme.typography.titleLarge)
                }
            }

            // Info NFC & RFID
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(nfcStatusState.value, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "RFID UID: ${rfidUidState.value}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isSendingState.value) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Text("Mengirim data scan ke server...", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Info Lokasi & Tombol Logout
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lokasi: ${locationState.value}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Device UUID: ${deviceUuidState.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        sessionManager.logout()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Keluar (Logout)", color = Color.White)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
        getDeviceLocation() // Update GPS ketika aplikasi dibuka kembali
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // Dipicu saat kartu RFID ditempelkan
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {

            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val tagIdHex = bytesToHex(it.id)
                rfidUidState.value = tagIdHex
                nfcStatusState.value = "Kartu Terdeteksi!"

                // Kirim data ke API Laravel
                kirimDataScan(tagIdHex)
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    // Logika mengambil lokasi GPS
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            getDeviceLocation()
        }
    }

    private fun getDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitudeState.value = location.latitude
                    longitudeState.value = location.longitude
                    locationState.value = "${location.latitude}, ${location.longitude}"
                } else {
                    locationState.value = "Gagal mendapatkan GPS (Nyalakan GPS Anda)"
                }
            }
        } catch (e: SecurityException) {
            locationState.value = "Izin lokasi tidak diberikan"
        }
    }

    // Memanggil API /api/scan
    private fun kirimDataScan(rfidUid: String) {
        isSendingState.value = true

        // Ambil Device UUID
        val deviceUuid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val driverId = sessionManager.getDriverId()

        val request = ScanRequest(
            driverId = driverId,
            deviceUuid = deviceUuid,
            rfidUid = rfidUid,
            latitude = latitudeState.value ?: 0.0,
            longitude = longitudeState.value ?: 0.0
        )

        RetrofitClient.instance.sendScanData(request).enqueue(object : Callback<ScanResponse> {
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                isSendingState.value = false
                nfcStatusState.value = "Menunggu Kartu NFC..."

                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    if (scanResponse != null && scanResponse.success) {
                        Toast.makeText(this@MainActivity, "Scan Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, scanResponse?.message ?: "Gagal scan", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                isSendingState.value = false
                nfcStatusState.value = "Menunggu Kartu NFC..."
                Toast.makeText(this@MainActivity, "Gagal koneksi ke server: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
