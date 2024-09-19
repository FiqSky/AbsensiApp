package com.byteze.labti.absensi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
author Fiqih
Copyright 2023, FiqSky Project
 **/
interface ApiService {
    @POST("/verifyBarcode")
    suspend fun verifyBarcodeWithSignature(@Body request: VerificationRequest): VerificationResponse

    @POST("/submitAttendance")
    suspend fun submitAttendance(@Body attendanceData: AttendanceData): SubmissionResponse

    @POST("/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse
}

data class VerificationRequest(val barcode: String, val signature: String)
data class VerificationResponse(val success: Boolean, val message: String)
data class AttendanceData(val name: String, val nim: String, val wa: String, val timestamp: Long)
data class SubmissionResponse(val success: Boolean, val message: String)
data class CheckoutRequest(val timestamp: Long)
data class CheckoutResponse(val success: Boolean, val message: String)

fun getApiService(): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://1ac6-112-215-242-49.ngrok-free.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(ApiService::class.java)
}