package com.byteze.labti.absensi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
author Fiqih
Copyright 2024, FiqSky Project
 **/
class AttendanceFormActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etNIM: EditText
    private lateinit var etWA: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_form)

        etName = findViewById(R.id.etName)
        etNIM = findViewById(R.id.etNIM)
        etWA = findViewById(R.id.etWA)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            if (validateForm()) {
                // Nonaktifkan tombol agar tidak bisa diklik berulang kali
                btnSubmit.isEnabled = false
                submitAttendanceForm()
            }
        }
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

    private fun submitAttendanceForm() {
        val name = etName.text.toString()
        val nim = etNIM.text.toString()
        val wa = etWA.text.toString()
        val timestamp = System.currentTimeMillis()

        val attendanceData = AttendanceData(name, nim, wa, timestamp)

        // Kirim form ke backend (menggunakan Retrofit)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = getApiService()
                val response = apiService.submitAttendance(attendanceData)

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        // Simpan status form sudah diisi ke SharedPreferences
                        saveFormCompletedStatus()

                        Toast.makeText(this@AttendanceFormActivity, "Berhasil absen", Toast.LENGTH_SHORT).show()
                        navigateToCheckoutActivity()
                    } else {
                        Toast.makeText(this@AttendanceFormActivity, "Gagal absen, coba lagi: ${response.message}", Toast.LENGTH_SHORT).show()
                        // Aktifkan kembali tombol jika gagal submit
                        btnSubmit.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AttendanceFormActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Aktifkan kembali tombol jika ada error
                    btnSubmit.isEnabled = true
                }
            }
        }
    }

    // Simpan status form sudah diisi ke SharedPreferences
    private fun saveFormCompletedStatus() {
        val sharedPref = getSharedPreferences("UserStatus", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Simpan status bahwa form telah diisi
        editor.putBoolean("formCompleted", true)
        editor.apply()
    }

    private fun navigateToCheckoutActivity() {
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
        finish() // Selesai dengan AttendanceFormActivity
    }
}
