package com.example.network

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.data.AppSettings
import com.example.data.ConnectionMode
import com.example.data.MacroButtonEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class SocketClientManager(private val context: Context) {
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private var autoConnectJob: Job? = null
    private var heartbeatJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var currentSettings: AppSettings? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (e: Exception) {
            Log.e("SocketClientManager", "Vibration error: ${e.message}")
        }
    }

    fun startAutoConnect(settings: AppSettings) {
        currentSettings = settings
        autoConnectJob?.cancel()

        if (!settings.autoConnect) {
            disconnect()
            return
        }

        autoConnectJob = scope.launch {
            while (true) {
                if (_connectionStatus.value == ConnectionStatus.DISCONNECTED) {
                    connectInternal(settings.ipAddress, settings.port, settings.connectionMode)
                }
                delay(5000)
            }
        }
    }

    fun updateSettingsAndReconnect(settings: AppSettings) {
        currentSettings = settings
        scope.launch {
            disconnectInternal()
            if (settings.autoConnect) {
                connectInternal(settings.ipAddress, settings.port, settings.connectionMode)
            }
        }
    }

    fun manualConnect(settings: AppSettings) {
        currentSettings = settings
        scope.launch {
            connectInternal(settings.ipAddress, settings.port, settings.connectionMode)
        }
    }

    fun disconnect() {
        autoConnectJob?.cancel()
        scope.launch {
            disconnectInternal()
        }
    }

    private suspend fun connectInternal(ip: String, port: Int, mode: ConnectionMode) {
        if (_connectionStatus.value == ConnectionStatus.CONNECTING ||
            _connectionStatus.value == ConnectionStatus.CONNECTED
        ) {
            return
        }

        _connectionStatus.value = ConnectionStatus.CONNECTING
        val targetHost = if (mode == ConnectionMode.USB) "127.0.0.1" else ip.ifBlank { "127.0.0.1" }

        try {
            withContext(Dispatchers.IO) {
                val newSocket = Socket()
                newSocket.connect(InetSocketAddress(targetHost, port), 3000)
                newSocket.soTimeout = 5000

                val newWriter = PrintWriter(newSocket.getOutputStream(), true)
                val newReader = BufferedReader(InputStreamReader(newSocket.getInputStream()))

                // Send handshake payload
                val handshakeJson = JSONObject().apply {
                    put("type", "handshake")
                    put("client", "ANKA Macro Pad Android")
                }.toString()

                newWriter.println(handshakeJson)

                // Read handshake response
                val responseStr = newReader.readLine()
                if (responseStr != null) {
                    socket = newSocket
                    writer = newWriter
                    reader = newReader
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    startHeartbeat()
                } else {
                    newSocket.close()
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
            }
        } catch (e: Exception) {
            Log.d("SocketClientManager", "Connect attempt failed: ${e.message}")
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (_connectionStatus.value == ConnectionStatus.CONNECTED) {
                delay(4000)
                val pingSuccess = sendPing()
                if (!pingSuccess) {
                    disconnectInternal()
                    break
                }
            }
        }
    }

    private suspend fun sendPing(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val pWriter = writer ?: return@withContext false
                val pingJson = JSONObject().apply { put("type", "ping") }.toString()
                pWriter.println(pingJson)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun sendMacro(button: MacroButtonEntity) {
        triggerHapticFeedback()

        scope.launch {
            val isSuccess = withContext(Dispatchers.IO) {
                try {
                    val pWriter = writer
                    if (pWriter == null || _connectionStatus.value != ConnectionStatus.CONNECTED) {
                        return@withContext false
                    }

                    val json = JSONObject().apply {
                        put("type", "macro_execute")
                        put("macroType", button.macroType.name)
                        put("primaryValue", button.primaryValue)
                        put("extraValuesJson", button.extraValuesJson)
                        put("timestamp", System.currentTimeMillis())
                    }.toString()

                    pWriter.println(json)
                    true
                } catch (e: Exception) {
                    Log.d("SocketClientManager", "Send macro error: ${e.message}")
                    false
                }
            }

            if (!isSuccess) {
                _toastMessage.emit("Komut gönderilemedi (PC bağlı değil)")
                if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
                    disconnectInternal()
                }
            }
        }
    }

    fun sendShortcutDirect(shortcutString: String) {
        triggerHapticFeedback()
        scope.launch {
            val isSuccess = withContext(Dispatchers.IO) {
                try {
                    val pWriter = writer
                    if (pWriter == null || _connectionStatus.value != ConnectionStatus.CONNECTED) {
                        return@withContext false
                    }

                    val json = JSONObject().apply {
                        put("type", "macro_execute")
                        put("macroType", if (shortcutString.contains("+")) "SHORTCUT" else "KEY")
                        put("primaryValue", shortcutString)
                        put("extraValuesJson", "[]")
                        put("timestamp", System.currentTimeMillis())
                    }.toString()

                    pWriter.println(json)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            if (!isSuccess) {
                _toastMessage.emit("Komut gönderilemedi (PC bağlı değil)")
            }
        }
    }

    suspend fun testConnection(ip: String, port: Int, mode: ConnectionMode): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            val targetHost = if (mode == ConnectionMode.USB) "127.0.0.1" else ip.ifBlank { "127.0.0.1" }
            val startTime = System.currentTimeMillis()
            try {
                val testSocket = Socket()
                testSocket.connect(InetSocketAddress(targetHost, port), 2500)
                testSocket.soTimeout = 2500

                val tWriter = PrintWriter(testSocket.getOutputStream(), true)
                val tReader = BufferedReader(InputStreamReader(testSocket.getInputStream()))

                val pingJson = JSONObject().apply { put("type", "ping") }.toString()
                tWriter.println(pingJson)

                val responseStr = tReader.readLine()
                val elapsedTime = System.currentTimeMillis() - startTime
                testSocket.close()

                if (responseStr != null) {
                    Pair(true, "Bağlantı başarılı! Yanıt süresi: ${elapsedTime} ms")
                } else {
                    Pair(false, "Sunucudan yanıt alınamadı.")
                }
            } catch (e: Exception) {
                Pair(false, "Bağlantı başarısız: ${e.localizedMessage ?: "Sunucu bulunamadı"}")
            }
        }
    }

    private suspend fun disconnectInternal() {
        withContext(Dispatchers.IO) {
            heartbeatJob?.cancel()
            try {
                writer?.close()
                reader?.close()
                socket?.close()
            } catch (e: Exception) {
                Log.d("SocketClientManager", "Disconnect error: ${e.message}")
            } finally {
                writer = null
                reader = null
                socket = null
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
        }
    }
}
