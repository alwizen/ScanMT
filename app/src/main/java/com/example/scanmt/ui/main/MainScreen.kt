package com.example.scanmt.ui.main

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.ui.main.riwayat.RiwayatScreen
import com.example.scanmt.ui.main.scan.ScanScreen
import com.example.scanmt.viewmodel.ScanViewModel

enum class MainTab {
    SCAN, RIWAYAT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ScanViewModel,
    deviceUuid: String,
    onLogoutClicked: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.SCAN) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFFFFFFFF),
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
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Anda akan keluar dari akun driver.\nPastikan semua data scan sudah tersimpan.",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogoutClicked()
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF3F4F6), Color(0xFFE5E7EB))
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color(0xFF1E88E5),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.SCAN,
                    onClick = { selectedTab = MainTab.SCAN },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                    label = { Text("Scan MT", fontWeight = if (selectedTab == MainTab.SCAN) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E88E5),
                        selectedTextColor = Color(0xFF1E88E5),
                        indicatorColor = Color(0xFFE3F2FD),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.RIWAYAT,
                    onClick = { selectedTab = MainTab.RIWAYAT },
                    icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                    label = { Text("Riwayat", fontWeight = if (selectedTab == MainTab.RIWAYAT) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E88E5),
                        selectedTextColor = Color(0xFF1E88E5),
                        indicatorColor = Color(0xFFE3F2FD),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFFFFF))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1E88E5), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(text = "Driver Aktif", fontSize = 10.sp, color = Color(0xFF6B7280))
                            Text(
                                text = viewModel.driverName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFFFFF))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Middle Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        MainTab.SCAN -> ScanScreen(viewModel = viewModel, deviceUuid = deviceUuid)
                        MainTab.RIWAYAT -> RiwayatScreen(viewModel = viewModel)
                    }
                }

                // Bottom Status Bar (GPS Location Info)
                if (selectedTab == MainTab.SCAN) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFFFFF))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(text = "Lokasi GPS", fontSize = 10.sp, color = Color(0xFF6B7280))
                            Text(
                                text = viewModel.locationText,
                                fontSize = 12.sp,
                                color = Color(0xFF111827),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
