package com.byteze.labti.absensi

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
                        Toast.makeText(this@AttendanceFormActivity, "Data absen berhasil dikirim", Toast.LENGTH_SHORT).show()
                        navigateToCheckoutActivity()
                    } else {
                        Toast.makeText(this@AttendanceFormActivity, "Gagal kirim: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AttendanceFormActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToCheckoutActivity() {
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
        finish() // Selesai dengan AttendanceFormActivity
    }
}
