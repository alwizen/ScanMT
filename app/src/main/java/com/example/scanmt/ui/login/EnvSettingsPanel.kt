package com.example.scanmt.ui.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.viewmodel.LoginViewModel

@Composable
fun EnvSettingsPanel(viewModel: LoginViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column {
            // Header toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.envExpanded = !viewModel.envExpanded }
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
                    imageVector = if (viewModel.envExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF8899AA),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Collapsible content
            AnimatedVisibility(
                visible = viewModel.envExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    Text(
                        text = "Server Base URL",
                        fontSize = 11.sp,
                        color = Color(0xFF8899AA),
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = viewModel.baseUrl,
                        onValueChange = { viewModel.onBaseUrlChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("http://192.168.x.x:8000/") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            cursorColor = Color(0xFF1E88E5),
                            focusedPlaceholderColor = Color(0xFF9CA3AF),
                            unfocusedPlaceholderColor = Color(0xFF9CA3AF),
                            focusedLabelColor = Color(0xFF1E88E5),
                            unfocusedLabelColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Test status
                    viewModel.testStatus?.let { (success, msg) ->
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
                                tint = if (success) Color(0xFF16A34A) else Color(0xFFFF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (success) Color(0xFF16A34A) else Color(0xFFFF4444)
                            )
                        }
                    }

                    // Test Koneksi Button
                    OutlinedButton(
                        onClick = { viewModel.testConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isTesting && viewModel.baseUrl.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!viewModel.isTesting) Color(0xFF00B4D8) else Color(0xFF2A3F5F)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00B4D8)
                        )
                    ) {
                        if (viewModel.isTesting) {
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
}
