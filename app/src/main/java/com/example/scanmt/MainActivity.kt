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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.scanmt.model.ScanData
import com.example.scanmt.model.ScanRequest
import com.example.scanmt.model.ScanResponse
import com.example.scanmt.network.RetrofitClient
import com.example.scanmt.ui.theme.ScanMTTheme
import com.example.scanmt.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var driverNameState = mutableStateOf("")
    private var nfcStatusState = mutableStateOf("Tempelkan Kartu NFC")
    private var rfidUidState = mutableStateOf("")
    private var locationState = mutableStateOf("Mencari GPS...")
    private var latitudeState = mutableStateOf<Double?>(null)
    private var longitudeState = mutableStateOf<Double?>(null)
    private var isSendingState = mutableStateOf(false)
    private var lastScanSuccessState = mutableStateOf<Boolean?>(null)
    private var lastScanResultState = mutableStateOf<ScanData?>(null)

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

        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        driverNameState.value = sessionManager.getDriverName() ?: "Driver"

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            nfcStatusState.value = "NFC Tidak Didukung"
        } else if (!nfcAdapter!!.isEnabled) {
            nfcStatusState.value = "NFC Nonaktif"
        }

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        checkLocationPermissions()

        setContent {
            ScanMTTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val bgGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF0A0E1A), Color(0xFF0D1B2A), Color(0xFF112240))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            // Decorative background blobs
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .offset(x = (-100).dp, y = (-80).dp)
                    .background(
                        Brush.radialGradient(colors = listOf(Color(0x1500B4D8), Color.Transparent)),
                        shape = RoundedCornerShape(175.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                TopBar()

                // Center NFC Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    NFCScanArea()
                }

                // Bottom Info
                BottomInfoSection()
            }
        }
    }

    @Composable
    fun TopBar() {
        var showLogoutDialog by remember { mutableStateOf(false) }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = Color(0xFF141E30),
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFFFF4444),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Keluar Aplikasi?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Anda akan keluar dari akun driver.\nPastikan semua data scan sudah tersimpan.",
                        color = Color(0xFF8899AA),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            sessionManager.logout()
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ya, Keluar", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3F5F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal", color = Color(0xFF8899AA))
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Driver info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(colors = listOf(Color(0xFF00B4D8), Color(0xFF0078FF))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Driver Aktif",
                        fontSize = 11.sp,
                        color = Color(0xFF8899AA)
                    )
                    Text(
                        text = driverNameState.value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Logout button
            IconButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1A2A40))
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFF8899AA),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    @Composable
    fun NFCScanArea() {
        val isScanning = !isSendingState.value && rfidUidState.value.isEmpty()
        val isSuccess = lastScanSuccessState.value == true
        val isError = lastScanSuccessState.value == false

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        val ringScale2 by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ring2"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Status text
            Text(
                text = if (isSendingState.value) "Memproses Data..." else nfcStatusState.value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSuccess) Color(0xFF00FF88) else if (isError) Color(0xFFFF4444) else Color(0xFF8899AA),
                textAlign = TextAlign.Center
            )

            // NFC Ring Animation
            Box(contentAlignment = Alignment.Center) {
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(ringScale2)
                            .alpha(pulseAlpha * 0.5f)
                            .background(
                                Brush.radialGradient(colors = listOf(Color(0x3300B4D8), Color.Transparent)),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale)
                            .alpha(pulseAlpha)
                            .background(
                                Brush.radialGradient(colors = listOf(Color(0x5500B4D8), Color.Transparent)),
                                shape = CircleShape
                            )
                    )
                }

                val circleColor = when {
                    isSendingState.value -> Brush.linearGradient(colors = listOf(Color(0xFF445566), Color(0xFF2A3F5F)))
                    isSuccess -> Brush.linearGradient(colors = listOf(Color(0xFF00FF88), Color(0xFF00CC66)))
                    isError -> Brush.linearGradient(colors = listOf(Color(0xFFFF4444), Color(0xFFCC2222)))
                    else -> Brush.linearGradient(colors = listOf(Color(0xFF00B4D8), Color(0xFF0078FF)))
                }

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(circleColor, shape = CircleShape)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0x8800B4D8), Color(0x880078FF))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSendingState.value) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = when {
                                isSuccess -> Icons.Default.CheckCircle
                                isError -> Icons.Default.ErrorOutline
                                else -> Icons.Default.Nfc
                            },
                            contentDescription = "NFC",
                            tint = Color.White,
                            modifier = Modifier.size(58.dp)
                        )
                    }
                }
            }

            // RFID & Scan Result Details
            AnimatedVisibility(
                visible = rfidUidState.value.isNotEmpty() || lastScanResultState.value != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val scanData = lastScanResultState.value
                val isInsideGeofence = scanData?.geofence?.isInside == true

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141E30))
                        .border(
                            1.dp,
                            if (isSuccess) (if (isInsideGeofence) Color(0xFF00FF88) else Color(0xFFFFBB00)) else Color(0xFF2A3F5F),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // RFID UID Header
                        Text(
                            text = "RFID UID: ${rfidUidState.value}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00B4D8)
                        )

                        // Rich Scan Data Details
                        scanData?.let { data ->
                            HorizontalDivider(color = Color(0xFF1E3050), modifier = Modifier.padding(vertical = 4.dp))

                            // Geofence Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isInsideGeofence) Color(0x2200FF88) else Color(0x22FFBB00))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isInsideGeofence) Icons.Default.Place else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isInsideGeofence) Color(0xFF00FF88) else Color(0xFFFFBB00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = data.geofence?.statusText ?: if (isInsideGeofence) "Di dalam lokasi" else "Di luar lokasi parkir MT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isInsideGeofence) Color(0xFF00FF88) else Color(0xFFFFBB00)
                                )
                            }

                            // Tanker & Compartment Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                data.tanker?.let { t ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Nopol MT", fontSize = 10.sp, color = Color(0xFF8899AA))
                                        Text(t.nopol ?: "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                data.compartment?.let { c ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Kompartemen", fontSize = 10.sp, color = Color(0xFF8899AA))
                                        Text("Komp. #${c.compartmentNo} (${c.capacityKl} KL)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BottomInfoSection() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // GPS Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D1B2A))
                    .border(1.dp, Color(0xFF1E3050), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF00B4D8),
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(text = "Lokasi GPS", fontSize = 10.sp, color = Color(0xFF8899AA))
                    Text(
                        text = locationState.value,
                        fontSize = 12.sp,
                        color = Color(0xFFCCDDEE),
                        maxLines = 1
                    )
                }
            }

            // NFC status / hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D1B2A))
                    .border(1.dp, Color(0xFF1E3050), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val nfcColor = if (nfcAdapter?.isEnabled == true) Color(0xFF00FF88) else Color(0xFFFF4444)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(nfcColor, CircleShape)
                )
                Column {
                    Text(text = "Status NFC", fontSize = 10.sp, color = Color(0xFF8899AA))
                    Text(
                        text = if (nfcAdapter == null) "Tidak Didukung"
                        else if (nfcAdapter!!.isEnabled) "Aktif - Siap Scan"
                        else "Nonaktif - Aktifkan di Pengaturan",
                        fontSize = 12.sp,
                        color = nfcColor
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
        getDeviceLocation()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

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
                lastScanSuccessState.value = null
                lastScanResultState.value = null
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
                    locationState.value = "%.6f, %.6f".format(location.latitude, location.longitude)
                } else {
                    // Fallback to active location request if lastLocation is null
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { freshLocation ->
                        if (freshLocation != null) {
                            latitudeState.value = freshLocation.latitude
                            longitudeState.value = freshLocation.longitude
                            locationState.value = "%.6f, %.6f".format(freshLocation.latitude, freshLocation.longitude)
                        } else {
                            locationState.value = "Nyalakan GPS Anda"
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            locationState.value = "Izin lokasi tidak diberikan"
        }
    }

    private fun kirimDataScan(rfidUid: String) {
        isSendingState.value = true

        val deviceUuid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val driverId = sessionManager.getDriverId()
        val baseUrl = sessionManager.getBaseUrl()

        val request = ScanRequest(
            driverId = driverId,
            deviceUuid = deviceUuid,
            rfidUid = rfidUid,
            latitude = latitudeState.value ?: 0.0,
            longitude = longitudeState.value ?: 0.0
        )

        RetrofitClient.getInstance(baseUrl).sendScanData(request).enqueue(object : Callback<ScanResponse> {
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                isSendingState.value = false
                if (response.isSuccessful) {
                    val scanResponse = response.body()
                    if (scanResponse != null && scanResponse.success) {
                        lastScanSuccessState.value = true
                        lastScanResultState.value = scanResponse.data

                        val isInside = scanResponse.data?.geofence?.isInside == true
                        val locationName = scanResponse.data?.geofence?.locationName

                        if (isInside) {
                            nfcStatusState.value = "✓ Di Dalam Geofence (${locationName ?: "Lokasi Parkir"})"
                        } else {
                            nfcStatusState.value = "⚠️ Di Luar Geofence Parkir MT"
                        }

                        Toast.makeText(this@MainActivity, "Scan Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                    } else {
                        lastScanSuccessState.value = false
                        lastScanResultState.value = null
                        nfcStatusState.value = "Scan Gagal"
                        Toast.makeText(this@MainActivity, scanResponse?.message ?: "Gagal scan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    lastScanSuccessState.value = false
                    lastScanResultState.value = null
                    nfcStatusState.value = "Error Server"
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
                // Reset after 5 seconds to give driver time to read geofence status
                android.os.Handler(mainLooper).postDelayed({
                    nfcStatusState.value = "Tempelkan Kartu NFC"
                    rfidUidState.value = ""
                    lastScanSuccessState.value = null
                    lastScanResultState.value = null
                }, 5000)
            }

            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                isSendingState.value = false
                lastScanSuccessState.value = false
                lastScanResultState.value = null
                nfcStatusState.value = "Koneksi Gagal"
                Toast.makeText(this@MainActivity, "Gagal koneksi ke server: ${t.message}", Toast.LENGTH_LONG).show()
                android.os.Handler(mainLooper).postDelayed({
                    nfcStatusState.value = "Tempelkan Kartu NFC"
                    rfidUidState.value = ""
                    lastScanSuccessState.value = null
                    lastScanResultState.value = null
                }, 5000)
            }
        })
    }
}
