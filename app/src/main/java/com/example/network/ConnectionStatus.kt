package com.example.network

import androidx.compose.ui.graphics.Color

enum class ConnectionStatus(
    val label: String,
    val color: Color
) {
    DISCONNECTED("Bağlı değil", Color(0xFFFF3D00)), // Kırmızı
    CONNECTING("Bağlanmaya çalışıyor...", Color(0xFFFFD600)), // Sarı
    CONNECTED("Bağlandı", Color(0xFF00E676)) // Yeşil
}
