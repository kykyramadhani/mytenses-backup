package com.app.mytenses

data class Course(
    val title: String,
    val status: String,
    val progress: Int, // Menggunakan Int untuk konsistensi dengan ProgressBar
    val iconResId: Int
)