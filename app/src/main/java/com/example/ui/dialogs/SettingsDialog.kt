package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppSettings
import com.example.data.ConnectionMode
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onTestConnection: (String, Int, ConnectionMode, (Boolean, String) -> Unit) -> Unit,
    onOpenServerCode: () -> Unit,
    onDismiss: () -> Unit
) {
    var connectionMode by remember { mutableStateOf(settings.connectionMode) }
    var ipAddress by remember { mutableStateOf(settings.ipAddress) }
    var portText by remember { mutableStateOf(settings.port.toString()) }
    var autoConnect by remember { mutableStateOf(settings.autoConnect) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .systemBarsPadding()
                .imePadding()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, PhoenixCardBorder, RoundedCornerShape(24.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bağlantı Ayarları",
                            color = PhoenixGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connection Mode Selector (Wi-Fi vs USB)
                Text(
                    text = "Bağlantı Yöntemi",
                    color = PhoenixAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Wi-Fi Mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (connectionMode == ConnectionMode.WIFI) Color(0xFF33180B) else SurfaceVariantDark)
                            .border(
                                width = 1.dp,
                                color = if (connectionMode == ConnectionMode.WIFI) PhoenixGold else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { connectionMode = ConnectionMode.WIFI }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Wi-Fi",
                                tint = if (connectionMode == ConnectionMode.WIFI) PhoenixGold else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wi-Fi",
                                color = if (connectionMode == ConnectionMode.WIFI) PhoenixGold else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // USB Mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (connectionMode == ConnectionMode.USB) Color(0xFF33180B) else SurfaceVariantDark)
                            .border(
                                width = 1.dp,
                                color = if (connectionMode == ConnectionMode.USB) PhoenixGold else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { connectionMode = ConnectionMode.USB }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Usb,
                                contentDescription = "USB",
                                tint = if (connectionMode == ConnectionMode.USB) PhoenixGold else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "USB",
                                color = if (connectionMode == ConnectionMode.USB) PhoenixGold else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // IP Address (Only visible or editable for Wi-Fi, for USB it defaults to localhost / 127.0.0.1)
                if (connectionMode == ConnectionMode.WIFI) {
                    Text(
                        text = "Bilgisayar IP Adresi",
                        color = PhoenixAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        placeholder = { Text("Ör. 192.168.1.100") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ip_address_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixGold,
                            unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariantDark)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "USB modunda adb port yönlendirmesi veya yerel döngü (127.0.0.1) kullanılır.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Port Input
                Text(
                    text = "Sunucu Port Numarası",
                    color = PhoenixAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it },
                    placeholder = { Text("Ör. 8080") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("port_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PhoenixGold,
                        unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auto Connect Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Otomatik Bağlan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Uygulama açıldığında otomatik olarak dene",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = autoConnect,
                        onCheckedChange = { autoConnect = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = PhoenixGold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test Connection Button
                Button(
                    onClick = {
                        val portInt = portText.toIntOrNull() ?: 8080
                        isTesting = true
                        testResult = null
                        onTestConnection(ipAddress, portInt, connectionMode) { success, msg ->
                            isTesting = false
                            testResult = Pair(success, msg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PhoenixAmber, RoundedCornerShape(12.dp)),
                    enabled = !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PhoenixGold,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Ediliyor...", color = PhoenixGold)
                    } else {
                        Text("⚡ Bağlantıyı Test Et", color = PhoenixGold, fontWeight = FontWeight.Bold)
                    }
                }

                // Test result banner
                testResult?.let { (success, message) ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (success) StatusGreen.copy(alpha = 0.15f) else PhoenixFlameRed.copy(alpha = 0.15f))
                            .border(1.dp, if (success) StatusGreen else PhoenixFlameRed, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = message,
                            color = if (success) StatusGreen else PhoenixFlameRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PC Server Code Drawer Opener
                Button(
                    onClick = {
                        onDismiss()
                        onOpenServerCode()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221309)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PhoenixGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = PhoenixGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PC Sunucu Kodunu Göster (server.py)", color = PhoenixGold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Settings Button
                Button(
                    onClick = {
                        val parsedPort = portText.toIntOrNull() ?: 8080
                        val updated = AppSettings(
                            connectionMode = connectionMode,
                            ipAddress = ipAddress.trim(),
                            port = parsedPort,
                            autoConnect = autoConnect
                        )
                        onSaveSettings(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixGold, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_settings_button")
                ) {
                    Text("Ayarları Kaydet ve Bağlan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
