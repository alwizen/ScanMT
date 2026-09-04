package com.example.scanmt

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scanmt.repository.AuthRepository
import com.example.scanmt.ui.login.LoginScreen
import com.example.scanmt.ui.theme.ScanMTTheme
import com.example.scanmt.utils.SessionManager
import com.example.scanmt.viewmodel.LoginViewModel

class LoginActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: LoginViewModel
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val authRepository = AuthRepository(sessionManager)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(authRepository) as T
            }
        })[LoginViewModel::class.java]

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        val deviceUuid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"

        setContent {
            ScanMTTheme {
                LoginScreen(
                    viewModel = viewModel,
                    deviceUuid = deviceUuid,
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
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

            val cardNo = parseNfcIntent(intent)
            if (!cardNo.isNullOrBlank()) {
                viewModel.onDriverNoChanged(cardNo)
                Toast.makeText(this, "Kartu Akses Terdeteksi: $cardNo", Toast.LENGTH_SHORT).show()
                viewModel.doLogin { success, message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    private fun parseNfcIntent(intent: Intent): String? {
        val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMsgs != null && rawMsgs.isNotEmpty()) {
            val msg = rawMsgs[0] as? android.nfc.NdefMessage
            val records = msg?.records
            if (records != null && records.isNotEmpty()) {
                val record = records[0]
                val payload = record.payload
                if (payload != null && payload.isNotEmpty()) {
                    try {
                        val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                        val languageCodeLength = payload[0].toInt() and 63
                        return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, java.nio.charset.Charset.forName(textEncoding)).trim()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        tag?.let {
            return bytesToHex(it.id)
        }
        return null
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }
}
