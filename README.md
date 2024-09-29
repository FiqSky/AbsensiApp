# Aplikasi Absensi Barcode

Aplikasi ini digunakan untuk melakukan absensi dengan memindai barcode. Dilengkapi dengan fitur tanda tangan digital, verifikasi barcode, serta proses checkout.

## Fitur Utama
- **Scan Barcode:** Memindai barcode untuk keperluan absensi.
- **Tanda Tangan Digital:** Pengguna dapat memberikan tanda tangan sebagai bukti kehadiran.
- **Checkout:** Pengguna bisa melakukan checkout untuk menyelesaikan proses absensi.
- **Verifikasi Backend:** Aplikasi melakukan verifikasi barcode dengan backend untuk memastikan keabsahan data.

## Teknologi yang Digunakan
- **Android (Kotlin)**
- **ZXing dan CodeScanner Library** untuk pemindaian barcode.
- **Firebase ML Kit** untuk tambahan kemampuan scanning.
- **MongoDB Atlas** untuk penyimpanan data absensi.
- **Express.js (Backend)** untuk mengelola API absensi.

## Cara Menggunakan
1. Buka aplikasi dan scan barcode.
2. Isi form absensi yang terdiri dari nama, NIM, dan nomor WhatsApp.
3. Tambahkan tanda tangan digital.
4. Lakukan checkout setelah proses selesai.

## Pengaturan Tambahan
- Pastikan kamera memiliki izin akses pada perangkat.
- Status absensi disimpan menggunakan `SharedPreferences` agar pengguna tidak perlu mengulangi langkah yang sudah selesai.
