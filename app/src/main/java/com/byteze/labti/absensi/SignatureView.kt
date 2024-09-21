package com.byteze.labti.absensi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
author Fiqih
Copyright 2023, FiqSky Project
 **/
class SignatureView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    private val path = Path()
    private var isSignatureDrawn = false
    private var totalDistance = 0f // Variabel untuk melacak panjang garis
    private var lastX = 0f // Titik X terakhir
    private var lastY = 0f // Titik Y terakhir
    private var isTouchStarted = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(it.x, it.y)
                    lastX = it.x
                    lastY = it.y
                    isTouchStarted = true
                    isSignatureDrawn = true // Tandai bahwa tanda tangan telah dimulai
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isTouchStarted) {
                        val dx = Math.abs(it.x - lastX)
                        val dy = Math.abs(it.y - lastY)
                        totalDistance += Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat() // Tambah panjang garis
                        lastX = it.x
                        lastY = it.y
                        path.lineTo(it.x, it.y)
                        invalidate()
                    }
                }
            }
        }
        return true
    }

    fun clear() {
        path.reset()
        invalidate()
        isSignatureDrawn = false
        totalDistance = 0f // Reset panjang garis ketika tanda tangan dihapus
        isTouchStarted = false
    }

    fun isSignatureDrawn(): Boolean {
        return isSignatureDrawn
    }

    // Fungsi untuk mengecek apakah panjang garis melebihi batas minimal
    fun isSignatureValid(minDistance: Float): Boolean {
        return totalDistance >= minDistance
    }

    fun getSignatureBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}