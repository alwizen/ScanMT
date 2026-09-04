package com.example.scanmt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.model.ScanData

@Composable
fun ScanResultCard(
    scanData: ScanData,
    rfidUid: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    val isInsideGeofence = scanData.geofence?.isInside == true

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(
                1.dp,
                if (isSuccess) (if (isInsideGeofence) Color(0xFF16A34A) else Color(0xFFF59E0B)) else Color(0xFFE5E7EB),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RFID UID Header
            if (rfidUid.isNotEmpty()) {
                Text(
                    text = "RFID / UID: $rfidUid",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF111827)
                )
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(vertical = 4.dp))
            }

            // Geofence Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isInsideGeofence) Color(0xFFECFDF5) else Color(0xFFFFF7ED))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isInsideGeofence) Icons.Default.Place else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isInsideGeofence) Color(0xFF16A34A) else Color(0xFFF59E0B),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = scanData.geofence?.statusText ?: if (isInsideGeofence) "Di dalam lokasi" else "Di luar lokasi parkir MT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isInsideGeofence) Color(0xFF16A34A) else Color(0xFFF59E0B)
                )
            }

            // Tanker & Compartment Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                scanData.tanker?.let { t ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nopol MT", fontSize = 10.sp, color = Color(0xFF6B7280))
                        Text(t.nopol ?: "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                }
                scanData.compartment?.let { c ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kompartemen", fontSize = 10.sp, color = Color(0xFF6B7280))
                        Text("Komp. #${c.compartmentNo} (${c.capacityKl} KL)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                }
            }
        }
    }
}
