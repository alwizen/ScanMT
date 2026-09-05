package com.example.scanmt.ui.main.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.viewmodel.ScanMode
import com.example.scanmt.viewmodel.ScanViewModel

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    deviceUuid: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TankerSelector(viewModel = viewModel, deviceUuid = deviceUuid)
        // Tab selector for Scan Mode
        TabRow(
            selectedTabIndex = viewModel.scanMode.ordinal,
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color(0xFF1E88E5),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = viewModel.scanMode == ScanMode.NFC,
                onClick = {
                    viewModel.scanMode = ScanMode.NFC
                    viewModel.resetScanState()
                },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("NFC Tap", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            )
            Tab(
                selected = viewModel.scanMode == ScanMode.QR,
                onClick = {
                    viewModel.scanMode = ScanMode.QR
                    viewModel.resetScanState()
                },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Scan QR", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (viewModel.scanMode) {
            ScanMode.NFC -> NfcScanArea(viewModel = viewModel)
            ScanMode.QR -> QrScanArea(viewModel = viewModel, deviceUuid = deviceUuid)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TankerSelector(viewModel: ScanViewModel, deviceUuid: String) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!viewModel.sessionLoading) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = viewModel.selectedTanker?.let { "${it.nopol} (${it.capacityKl} KL)" } ?: "Pilih tanker",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanker aktif") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            supportingText = {
                when {
                    viewModel.sessionLoading -> Text("Membuat sesi scan...")
                    viewModel.tankerLoading -> Text("Memuat daftar tanker...")
                    viewModel.tankerError != null -> Text(viewModel.tankerError!!)
                    viewModel.selectedTanker == null -> Text("Pilih tanker sebelum melakukan scan")
                }
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            viewModel.availableTankers.forEach { tanker ->
                DropdownMenuItem(
                    text = { Text("${tanker.nopol} - ${tanker.capacityKl} KL") },
                    onClick = {
                        expanded = false
                        viewModel.selectTanker(tanker, deviceUuid)
                    }
                )
            }
        }
    }
}
