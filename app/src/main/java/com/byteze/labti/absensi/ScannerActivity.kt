package com.byteze.labti.absensi

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class ScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)

        // Mulai scanner barcode
        startBarcodeScanner()
    }

    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan Barcode")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getAppSignature(): String? {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signatures = packageInfo.signingInfo.apkContentsSigners
            val md = MessageDigest.getInstance("SHA")
            for (signature in signatures) {
                md.update(signature.toByteArray())
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Setelah barcode discan, lakukan verifikasi ke backend
                val barcodeData = result.contents
                val appSignature = getAppSignature()

                if (appSignature != null) {
                    verifyBarcodeWithSignature(barcodeData, appSignature)
                } else {
                    Toast.makeText(this, "Signature not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun verifyBarcodeWithSignature(barcode: String, signature: String) {
        // Jalankan proses verifikasi di background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = getApiService()
                val response = apiService.verifyBarcodeWithSignature(VerificationRequest(barcode, signature))

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        // Jika verifikasi sukses, tampilkan form untuk diisi pengguna
                        showAttendanceForm(barcode)
                    } else {
                        Toast.makeText(this@ScannerActivity, "Verifikasi gagal: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScannerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAttendanceForm(barcodeData: String) {
        // Tampilkan form absen di sini (bisa DialogFragment atau Activity baru)
        Log.d("ScannerActivity", "Verifikasi sukses, tampilkan form absen")
        // Implementasikan tampilan form absen yang akan diisi pengguna
    }
}