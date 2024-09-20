package com.byteze.labti.absensi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

/**
author Fiqih
Copyright 2024, FiqSky Project
 **/
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val appTitleTextView = findViewById<TextView>(R.id.appTitleTextView)

        // Tambahkan animasi fade in untuk title text
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Mulai animasi fade in untuk teks
        appTitleTextView.startAnimation(fadeInAnimation)

        // Durasi splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
            // Tambahkan animasi transisi saat pindah ke ScannerActivity
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 1500)
    }
}
