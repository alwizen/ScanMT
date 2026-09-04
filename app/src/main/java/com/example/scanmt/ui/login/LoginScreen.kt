package com.example.scanmt.ui.login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanmt.R
import com.example.scanmt.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    deviceUuid: String,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    val submitLogin = {
        viewModel.doLogin { success, message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (success) {
                onLoginSuccess()
            }
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF3F4F6),
            Color(0xFFE5E7EB)
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
                        colors = listOf(Color(0x1A111827), Color.Transparent)
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
                        colors = listOf(Color(0x1A374151), Color.Transparent)
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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(180.dp)
                    .height(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ComCheck",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Compartement Check",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card Tap & Hardware Scanner Hint Badge
            // Box(
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .clip(RoundedCornerShape(12.dp))
            //         .background(Color(0xFFE3F2FD))
            //         .border(1.dp, Color(0xFFBBDEFB), RoundedCornerShape(12.dp))
            //         .padding(horizontal = 14.dp, vertical = 10.dp)
            // ) {
            //     Row(
            //         verticalAlignment = Alignment.CenterVertically,
            //         horizontalArrangement = Arrangement.spacedBy(10.dp)
            //     ) {
            //         Icon(
            //             imageVector = Icons.Default.Nfc,
            //             contentDescription = null,
            //             tint = Color(0xFF1E88E5),
            //             modifier = Modifier.size(20.dp)
            //         )
            //         Text(
            //             text = "Tempelkan Kartu Akses NFC atau ketik / scan ID AMT",
            //             fontSize = 12.sp,
            //             fontWeight = FontWeight.Medium,
            //             color = Color(0xFF1976D2)
            //         )
            //     }
            // }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFFFFF))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat Datang 👋",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Masukkan atau scan Kartu Akses AMT",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = viewModel.driverNo,
                        onValueChange = { viewModel.onDriverNoChanged(it) },
                        label = { Text("ID AMT") },
                        placeholder = { Text("Tempelkan Kartu Akses") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown &&
                                    (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)
                                ) {
                                    submitLogin()
                                    true
                                } else {
                                    false
                                }
                            },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = null,
                                tint = Color(0xFF6B7280)
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { submitLogin() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedLabelColor = Color(0xFF1E88E5),
                            unfocusedLabelColor = Color(0xFF6B7280),
                            cursorColor = Color(0xFF1E88E5),
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedPlaceholderColor = Color(0xFF9CA3AF),
                            unfocusedPlaceholderColor = Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = { submitLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !viewModel.isLoading,
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
                                    if (!viewModel.isLoading)
                                        Color(0xFF1E88E5)
                                    else
                                        Color(0xFF9CA3AF),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (viewModel.isLoading) {
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
                                    Text("Masuk", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Env Settings Panel
            EnvSettingsPanel(viewModel = viewModel)

            Spacer(modifier = Modifier.height(32.dp))

               // Device UUID Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Device UUID", deviceUuid))
                        Toast.makeText(context, "UUID disalin!", Toast.LENGTH_SHORT).show()
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
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device UUID",
                            fontSize = 10.sp,
                            color = Color(0xFF6B7280),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = deviceUuid,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
