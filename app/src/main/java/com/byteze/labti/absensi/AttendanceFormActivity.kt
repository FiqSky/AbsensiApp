package com.byteze.labti.absensi

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
author Fiqih
Copyright 2024, FiqSky Project
 **/
class AttendanceFormActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etNIM: EditText
    private lateinit var etWA: EditText
    private lateinit var btnSubmit: Button
    private lateinit var signatureView: SignatureView
    private lateinit var btnClearSignature: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_form)

        etName = findViewById(R.id.etName)
        etNIM = findViewById(R.id.etNIM)
        etWA = findViewById(R.id.etWA)
        btnSubmit = findViewById(R.id.btnSubmit)

        signatureView = findViewById(R.id.signatureView)
        btnClearSignature = findViewById(R.id.btnClearSignature)

        btnClearSignature.setOnClickListener {
            signatureView.clear()
            Toast.makeText(this, "Tanda tangan dihapus", Toast.LENGTH_SHORT).show()
        }

        btnSubmit.setOnClickListener {
            if (validateForm()) {
                val minSignatureLength = 1000f // Misalnya, 100px panjang minimal
                if (!signatureView.isSignatureDrawn() || !signatureView.isSignatureValid(minSignatureLength)) {
                    Toast.makeText(this, "Tanda tangan terlalu pendek atau tidak valid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val signatureBitmap = signatureView.getSignatureBitmap()
                val signatureBase64 = encodeBitmapToBase64(signatureBitmap)

                btnSubmit.isEnabled = false
                submitAttendanceForm(signatureBase64)
            }
        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun validateForm(): Boolean {
        if (etName.text.toString().isEmpty()) {
            etName.error = "Nama harus diisi"
            return false
        }
        if (etNIM.text.toString().isEmpty()) {
            etNIM.error = "NIM harus diisi"
            return false
        }
        if (etWA.text.toString().isEmpty()) {
            etWA.error = "Nomor WhatsApp harus diisi"
            return false
        }
        return true
    }

    private fun submitAttendanceForm(signatureBase64: String) {
        val name = etName.text.toString()
        val nim = etNIM.text.toString()
        val wa = etWA.text.toString()
        val timestamp = System.currentTimeMillis()

        val attendanceData = AttendanceData(name, nim, wa, timestamp, signatureBase64)
        Log.d(TAG, "signatureBase64 is: $signatureBase64")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = getApiService()
                val response = apiService.submitAttendance(attendanceData)

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        saveFormCompletedStatus()
                        Toast.makeText(this@AttendanceFormActivity, "Berhasil absen", Toast.LENGTH_SHORT).show()
                        navigateToCheckoutActivity()
                    } else {
                        btnSubmit.isEnabled = true
                        Toast.makeText(this@AttendanceFormActivity, "Gagal absen: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this@AttendanceFormActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // Simpan status form sudah diisi ke SharedPreferences
    private fun saveFormCompletedStatus() {
        val name = etName.text.toString() // name saat user submit form
        val sharedPref = getSharedPreferences("UserStatus", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Simpan status bahwa form telah diisi
        editor.putBoolean("formCompleted", true)
        editor.putString("name", name) // Simpan timestamp hadir
        editor.apply()

        Log.d("AttendanceActivity", "name: $name")
    }

    private fun navigateToCheckoutActivity() {
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
        finish() // Selesai dengan AttendanceFormActivity
    }
}
