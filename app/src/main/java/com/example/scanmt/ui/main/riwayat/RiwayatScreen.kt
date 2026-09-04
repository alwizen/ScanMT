package com.example.scanmt.ui.main.riwayat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.model.ScanLogItem
import com.example.scanmt.viewmodel.ScanViewModel

@Composable
fun RiwayatScreen(viewModel: ScanViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadRiwayat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header of Riwayat tab with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Scan Driver",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Daftar aktivitas scan yang telah dilakukan",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            IconButton(
                onClick = { viewModel.loadRiwayat() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFFFFF))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (viewModel.isHistoryLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = Color(0xFF1E88E5))
                    Text("Memuat data riwayat...", fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }
        } else if (viewModel.historyError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(48.dp))
                    Text(viewModel.historyError ?: "Gagal memuat data", color = Color(0xFFDC2626), fontSize = 14.sp)
                    Button(
                        onClick = { viewModel.loadRiwayat() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            }
        } else if (viewModel.scanHistoryList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(56.dp))
                    Text("Belum Ada Riwayat Scan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                    Text("Lakukan scan kompartemen untuk melihat histori", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(viewModel.scanHistoryList) { item ->
                    RiwayatItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun RiwayatItemCard(item: ScanLogItem) {
    val isDone = item.scanStatus == "done"
    val isInsideGeofence = item.geofence?.isInside == true

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Scanned at & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                    Text(
                        text = item.scannedAt ?: "-",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Status Scan Badge (done / kurang)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDone) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDone) "Done (Lengkap)" else "Kurang",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) Color(0xFF15803D) else Color(0xFFB45309)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            // Body Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.tanker?.nopol ?: "Nopol -",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Komp. #${item.compartment?.compartmentNo ?: "-"} (${item.compartment?.capacityKl ?: 0} KL)",
                        fontSize = 13.sp,
                        color = Color(0xFF374151)
                    )
                }

                // Geofence status chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isInsideGeofence) Color(0xFFECFDF5) else Color(0xFFFFF7ED))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isInsideGeofence) Icons.Default.Place else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isInsideGeofence) Color(0xFF16A34A) else Color(0xFFF59E0B),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.geofence?.locationName ?: if (isInsideGeofence) "Di Lokasi" else "Luar Lokasi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isInsideGeofence) Color(0xFF16A34A) else Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}
