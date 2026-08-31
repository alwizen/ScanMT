package com.example.scanmt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.model.LoginRequest
import com.example.scanmt.model.LoginResponse
import com.example.scanmt.network.RetrofitClient
import com.example.scanmt.ui.theme.ScanMTTheme
import com.example.scanmt.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            ScanMTTheme {
                LoginScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen() {
        val deviceUuid = remember {
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        }

        var driverNo by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        // Env Settings state
        var envExpanded by remember { mutableStateOf(false) }
        var baseUrl by remember { mutableStateOf(sessionManager.getBaseUrl()) }
        var testStatus by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
        var isTesting by remember { mutableStateOf(false) }

        // Background gradient
        val bgGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0E1A),
                Color(0xFF0D1B2A),
                Color(0xFF112240)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-80).dp, y = (-60).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x2200B4D8), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(150.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 60.dp, y = 60.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x1A0078FF), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(125.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Logo / Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00B4D8), Color(0xFF0078FF))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "NFC Icon",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ScanMT",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Driver Scanner System",
                    fontSize = 14.sp,
                    color = Color(0xFF8899AA),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Device UUID Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1A00B4D8))
                        .border(
                            width = 1.dp,
                            color = Color(0x3300B4D8),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Device UUID", deviceUuid))
                            Toast.makeText(this@LoginActivity, "UUID disalin!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Color(0xFF00B4D8),
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Device UUID",
                                fontSize = 10.sp,
                                color = Color(0xFF8899AA),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = deviceUuid,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF00B4D8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF8899AA),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Login Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141E30).copy(alpha = 0.9f))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1E3050),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Login Driver",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Masukkan nomor driver Anda",
                            fontSize = 12.sp,
                            color = Color(0xFF8899AA)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = driverNo,
                            onValueChange = { driverNo = it },
                            label = { Text("Nomor Driver") },
                            placeholder = { Text("Contoh: DRV001") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = Color(0xFF00B4D8)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B4D8),
                                unfocusedBorderColor = Color(0xFF2A3F5F),
                                focusedLabelColor = Color(0xFF00B4D8),
                                unfocusedLabelColor = Color(0xFF8899AA),
                                cursorColor = Color(0xFF00B4D8),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFCCDDEE),
                                focusedPlaceholderColor = Color(0xFF445566),
                                unfocusedPlaceholderColor = Color(0xFF445566)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Login Button
                        Button(
                            onClick = {
                                if (driverNo.trim().isNotEmpty()) {
                                    isLoading = true
                                    doLogin(driverNo.trim()) {
                                        isLoading = false
                                    }
                                } else {
                                    Toast.makeText(this@LoginActivity, "Masukkan Nomor Driver", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (!isLoading)
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF00B4D8), Color(0xFF0078FF))
                                            )
                                        else
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF445566), Color(0xFF445566))
                                            ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text("Memproses...", color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Text("Login", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Env Settings Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D1B2A))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1E3050),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column {
                        // Header toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { envExpanded = !envExpanded }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color(0xFF8899AA),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Env Settings",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8899AA)
                                )
                            }
                            Icon(
                                imageVector = if (envExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF8899AA),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Collapsible content
                        AnimatedVisibility(
                            visible = envExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFF1E3050))

                                Text(
                                    text = "Server Base URL",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8899AA),
                                    letterSpacing = 1.sp
                                )

                                OutlinedTextField(
                                    value = baseUrl,
                                    onValueChange = {
                                        baseUrl = it
                                        testStatus = null
                                        sessionManager.saveBaseUrl(it)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = { Text("http://192.168.x.x:8000/") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00B4D8),
                                        unfocusedBorderColor = Color(0xFF2A3F5F),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color(0xFFCCDDEE),
                                        cursorColor = Color(0xFF00B4D8),
                                        focusedPlaceholderColor = Color(0xFF445566),
                                        unfocusedPlaceholderColor = Color(0xFF445566),
                                        focusedLabelColor = Color(0xFF00B4D8),
                                        unfocusedLabelColor = Color(0xFF8899AA)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                // Test status
                                testStatus?.let { (success, msg) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (success) Color(0x1A00FF88) else Color(0x1AFF4444)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (success) Color(0xFF00FF88) else Color(0xFFFF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = msg,
                                            fontSize = 12.sp,
                                            color = if (success) Color(0xFF00FF88) else Color(0xFFFF4444)
                                        )
                                    }
                                }

                                // Test Koneksi Button
                                OutlinedButton(
                                    onClick = {
                                        if (baseUrl.isNotBlank()) {
                                            isTesting = true
                                            testStatus = null
                                            RetrofitClient.testConnection(baseUrl) { success, msg ->
                                                runOnUiThread {
                                                    testStatus = Pair(success, msg)
                                                    isTesting = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isTesting && baseUrl.isNotBlank(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (!isTesting) Color(0xFF00B4D8) else Color(0xFF2A3F5F)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF00B4D8)
                                    )
                                ) {
                                    if (isTesting) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF00B4D8),
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mengecek...", fontSize = 13.sp)
                                    } else {
                                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Test Koneksi", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "ScanMT v1.0 • Solu8i Project",
                    fontSize = 11.sp,
                    color = Color(0xFF445566),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    private fun doLogin(driverNo: String, onComplete: () -> Unit) {
        val request = LoginRequest(driverNo)
        val baseUrl = sessionManager.getBaseUrl()

        RetrofitClient.getInstance(baseUrl).loginDriver(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                onComplete()
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.success && loginResponse.data != null) {
                        sessionManager.saveSession(
                            driverId = loginResponse.data.id,
                            driverNo = loginResponse.data.driverNo,
                            name = loginResponse.data.name
                        )
                        Toast.makeText(this@LoginActivity, "Selamat datang, ${loginResponse.data.name}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Gagal login", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onComplete()
                Toast.makeText(this@LoginActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
