package com.byteze.labti.absensi

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class ScannerActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private val CAMERA_PERMISSION_CODE = 100

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner)

        getAppSignature()

        // Setup scanner
        setupScanner()

        // Memeriksa izin kamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            startScanner()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupScanner() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        // Konfigurasi scanner
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK // Atau CAMERA_FRONT
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    val barcodeData = it.text
                    val appSignature = getAppSignature()

                    // Cek apakah hasil scan sesuai dengan kode yang diharapkan
                    if (barcodeData == "AKUCNTAKM") {
                        if (appSignature != null) {
                            verifyBarcodeWithSignature(barcodeData, appSignature)
                        } else {
                            Toast.makeText(this@ScannerActivity, "Signature not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Jika hasil scan tidak sesuai, tampilkan toast dan restart scanner
                        Toast.makeText(this@ScannerActivity, "Kode tidak valid: $barcodeData", Toast.LENGTH_SHORT).show()
                        codeScanner.startPreview() // Restart scanner untuk scan barcode berikutnya
                    }
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Toast.makeText(this@ScannerActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startScanner() {
        codeScanner.startPreview()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk menggunakan scanner", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Menutup aplikasi ketika tombol kembali ditekan
    }

    // Mendapatkan App Signature untuk verifikasi keamanan
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getAppSignature(): String? {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signatures = packageInfo.signingInfo.apkContentsSigners
            val md = MessageDigest.getInstance("SHA")
            for (signature in signatures) {
                md.update(signature.toByteArray())
                val appSignature = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("AppSignature", appSignature)
                return appSignature
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Verifikasi barcode dengan signature ke backend
    private fun verifyBarcodeWithSignature(barcode: String, signature: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = getApiService()
                val response = apiService.verifyBarcodeWithSignature(VerificationRequest(barcode, signature))

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        // Jika verifikasi sukses, tampilkan form absen untuk diisi pengguna
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

    // Menampilkan form absen setelah verifikasi sukses
    private fun showAttendanceForm(barcodeData: String) {
        val intent = Intent(this, AttendanceFormActivity::class.java)
        startActivity(intent)
        finish() // Selesai dengan ScannerActivity
    }
}