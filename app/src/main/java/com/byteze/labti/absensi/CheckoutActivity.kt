package com.byteze.labti.absensi

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
author Fiqih
Copyright 2024, FiqSky Project
 **/
class CheckoutActivity : AppCompatActivity() {

    private var isSubmitting = false // Flag untuk melacak status pengiriman

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        btnCheckout.setOnClickListener {
            if (!isSubmitting) {
                isSubmitting = true // Set flag ke true untuk menandakan proses pengiriman
                submitCheckout()
            }
        }
    }

    private fun submitCheckout() {
        val timestampCheckout = System.currentTimeMillis()

        // Ambil nama dan timestamp hadir dari SharedPreferences
        val sharedPref = getSharedPreferences("UserStatus", MODE_PRIVATE)
        val name = sharedPref.getString("name", null) // Ambil nama

        Log.d("CheckoutActivity", "Nama: $name, Timestamp checkout: $timestampCheckout")

        if (name == null) {
            Toast.makeText(this, "Error: Nama tidak ditemukan", Toast.LENGTH_SHORT).show()
            isSubmitting = false
            return
        }

        // Kirim nama dan timestamp ke backend untuk checkout
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = getApiService()
                val response = apiService.checkout(CheckoutRequest(name, timestampCheckout))

                withContext(Dispatchers.Main) {
                    isSubmitting = false // Set flag kembali ke false setelah pengiriman selesai
                    if (response.success) {
                        Toast.makeText(this@CheckoutActivity, "Checkout berhasil", Toast.LENGTH_SHORT).show()
                        clearUserStatus() // Reset status user setelah checkout berhasil
                        finish() // Kembali ke activity sebelumnya
                    } else {
                        Toast.makeText(this@CheckoutActivity, "Checkout gagal: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSubmitting = false // Set flag kembali ke false jika ada error
                    Toast.makeText(this@CheckoutActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearUserStatus() {
        // Hapus informasi status user dari SharedPreferences
        val sharedPref = getSharedPreferences("UserStatus", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}
