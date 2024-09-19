package com.byteze.labti.absensi

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
author Fiqih
Copyright 2023, FiqSky Project
 **/
class AttendanceFormDialog : DialogFragment() {

    private lateinit var etName: EditText
    private lateinit var etNIM: EditText
    private lateinit var etWA: EditText
    private lateinit var btnSubmit: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_attendance_form, null)

        etName = view.findViewById(R.id.etName)
        etNIM = view.findViewById(R.id.etNIM)
        etWA = view.findViewById(R.id.etWA)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitAttendanceForm()
            }
        }

        builder.setView(view)
        return builder.create()
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
                        Toast.makeText(requireContext(), "Data absen berhasil dikirim", Toast.LENGTH_SHORT).show()
                        dismiss() // Tutup form
                        navigateToCheckoutActivity() // Pindah ke CheckoutActivity
                    } else {
                        Toast.makeText(requireContext(), "Gagal kirim: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToCheckoutActivity() {
        val intent = Intent(requireContext(), CheckoutActivity::class.java)
        startActivity(intent)
    }
}
