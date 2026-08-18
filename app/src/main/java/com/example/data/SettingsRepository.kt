package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConnectionMode {
    WIFI,
    USB
}

data class AppSettings(
    val connectionMode: ConnectionMode = ConnectionMode.WIFI,
    val ipAddress: String = "192.168.1.100",
    val port: Int = 8080,
    val autoConnect: Boolean = true
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anka_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val modeStr = prefs.getString("connection_mode", ConnectionMode.WIFI.name) ?: ConnectionMode.WIFI.name
        val mode = try { ConnectionMode.valueOf(modeStr) } catch (e: Exception) { ConnectionMode.WIFI }
        val ip = prefs.getString("ip_address", "192.168.1.100") ?: "192.168.1.100"
        val port = prefs.getInt("port", 8080)
        val autoConnect = prefs.getBoolean("auto_connect", true)
        return AppSettings(
            connectionMode = mode,
            ipAddress = ip,
            port = port,
            autoConnect = autoConnect
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit()
            .putString("connection_mode", newSettings.connectionMode.name)
            .putString("ip_address", newSettings.ipAddress)
            .putInt("port", newSettings.port)
            .putBoolean("auto_connect", newSettings.autoConnect)
            .apply()

        _settings.value = newSettings
    }
}
