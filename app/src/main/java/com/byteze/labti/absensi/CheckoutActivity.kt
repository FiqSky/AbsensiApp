package com.byteze.labti.absensi

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckoutActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        btnCheckout.setOnClickListener {
            submitCheckout()
        }
    }

    private fun submitCheckout() {
        val timestamp = System.currentTimeMillis()

        // Kirim timestamp ke backend untuk checkout
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = getApiService()
                val response = apiService.checkout(CheckoutRequest(timestamp))

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        Toast.makeText(this@CheckoutActivity, "Checkout berhasil", Toast.LENGTH_SHORT).show()
                        finish() // Kembali ke activity sebelumnya
                    } else {
                        Toast.makeText(this@CheckoutActivity, "Checkout gagal: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CheckoutActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
