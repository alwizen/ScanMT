package com.example.scanmt.ui.main.scan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.ui.components.ScanResultCard
import com.example.scanmt.viewmodel.ScanViewModel

@Composable
fun NfcScanArea(viewModel: ScanViewModel) {
    val isScanning = !viewModel.isSending && viewModel.rfidUid.isEmpty()
    val isSuccess = viewModel.lastScanSuccess == true
    val isError = viewModel.lastScanSuccess == false

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
            text = if (viewModel.isSending) "Memproses Data..." else viewModel.nfcStatus,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSuccess) Color(0xFF16A34A) else if (isError) Color(0xFFDC2626) else Color(0xFF6B7280),
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
                viewModel.isSending -> Color(0xFF9CA3AF)
                isSuccess -> Color(0xFF16A34A)
                isError -> Color(0xFFDC2626)
                else -> Color(0xFF1E88E5)
            }

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(circleColor, shape = CircleShape)
                    .border(
                        width = 3.dp,
                        color = Color(0xFF1E88E5),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isSending) {
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
            visible = viewModel.rfidUid.isNotEmpty() || viewModel.lastScanResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            viewModel.lastScanResult?.let { scanData ->
                ScanResultCard(
                    scanData = scanData,
                    rfidUid = viewModel.rfidUid,
                    isSuccess = isSuccess
                )
            }
        }
    }
}
