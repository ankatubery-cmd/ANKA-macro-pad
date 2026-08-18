package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AccountStatus
import com.example.data.DiscordChannel
import com.example.data.DiscordCommunityThread
import com.example.data.ExtensionEntity
import com.example.ui.components.IconMapper
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import org.json.JSONArray

@Composable
fun ExtensionHubDialog(
    accountStatus: AccountStatus,
    developerModeEnabled: Boolean,
    allExtensions: List<ExtensionEntity>,
    installedExtensions: List<ExtensionEntity>,
    onOpenDiscordAccount: () -> Unit,
    onInstallExtension: (ExtensionEntity) -> Unit,
    onToggleExtensionEnabled: (String, Boolean) -> Unit,
    onUninstallExtension: (String) -> Unit,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onImportCustomJson: (String, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedChannelFilter by remember { mutableStateOf(DiscordChannel.ALL) }

    // Custom JSON text state for dev mode
    var customJsonInput by remember {
        mutableStateOf(
            """{
  "id": "custom_macro_pack",
  "name": "Özel Geliştirici Makro Paketi",
  "developer": "DevUser",
  "version": "1.0.0",
  "description": "Geliştirici modunda test edilen özel tuş kombinasyonları.",
  "permissions": ["Geliştirici Konsolu", "Sistem Makroları"],
  "macroPresets": [
    {"title": "Dev Test 1", "subtext": "F12 Kısayol", "iconName": "code", "macroType": "SHORTCUT", "primaryValue": "F12"}
  ]
}"""
        )
    }

    val discordBlurple = Color(0xFF5865F2)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                    .padding(18.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ANKA Extension Community",
                                color = PhoenixGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Discord Topluluk Dağıtım Ekosistemi",
                                color = PhoenixAmber.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Status Info Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (accountStatus == AccountStatus.CONNECTED) StatusGreen.copy(alpha = 0.12f)
                            else Color(0xFF26190F)
                        )
                        .border(
                            1.dp,
                            if (accountStatus == AccountStatus.CONNECTED) StatusGreen else PhoenixAmber.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (accountStatus == AccountStatus.CONNECTED) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (accountStatus == AccountStatus.CONNECTED) StatusGreen else PhoenixAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (accountStatus == AccountStatus.CONNECTED)
                                    "Discord Bağlı — Extension yükleme açık"
                                else
                                    "Hesapsız Kullanım — Extension yüklemek için Discord bağlayın",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (accountStatus != AccountStatus.CONNECTED) {
                            Button(
                                onClick = onOpenDiscordAccount,
                                colors = ButtonDefaults.buttonColors(containerColor = discordBlurple),
                                modifier = Modifier
                                    .testTag("extension_hub_connect_discord_button")
                                    .padding(start = 6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Discord Bağla", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs (Mağaza, Yüklü, Developer Mode)
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SurfaceVariantDark,
                    contentColor = PhoenixGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PhoenixGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Discord Mağaza", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Yüklü (${installedExtensions.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dev Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Contents
                when (selectedTabIndex) {
                    0 -> StoreTabContent(
                        accountStatus = accountStatus,
                        developerModeEnabled = developerModeEnabled,
                        allExtensions = allExtensions,
                        selectedChannelFilter = selectedChannelFilter,
                        onSelectChannel = { selectedChannelFilter = it },
                        onInstallExtension = onInstallExtension,
                        onOpenDiscordAccount = onOpenDiscordAccount
                    )
                    1 -> InstalledTabContent(
                        installedExtensions = installedExtensions,
                        onToggleEnabled = onToggleExtensionEnabled,
                        onUninstall = onUninstallExtension
                    )
                    2 -> DevModeTabContent(
                        developerModeEnabled = developerModeEnabled,
                        customJsonInput = customJsonInput,
                        onCustomJsonChange = { customJsonInput = it },
                        onToggleDeveloperMode = onToggleDeveloperMode,
                        onImportJson = {
                            onImportCustomJson(customJsonInput) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.StoreTabContent(
    accountStatus: AccountStatus,
    developerModeEnabled: Boolean,
    allExtensions: List<ExtensionEntity>,
    selectedChannelFilter: DiscordChannel,
    onSelectChannel: (DiscordChannel) -> Unit,
    onInstallExtension: (ExtensionEntity) -> Unit,
    onOpenDiscordAccount: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        // Discord Channel Filter Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            items(DiscordChannel.values()) { channel ->
                val isSelected = selectedChannelFilter == channel
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF3B1E0E) else SurfaceVariantDark)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) PhoenixGold else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectChannel(channel) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = channel.channelName,
                        color = if (isSelected) PhoenixGold else Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Extension Threads List
        val filteredList = remember(selectedChannelFilter, allExtensions) {
            if (selectedChannelFilter == DiscordChannel.ALL) {
                allExtensions
            } else {
                allExtensions.filter { ext ->
                    when (selectedChannelFilter) {
                        DiscordChannel.EXTENSION_SHARES -> true
                        DiscordChannel.UPDATES -> ext.category.contains("Discord") || ext.category.contains("Medya")
                        DiscordChannel.DEV_COMMUNITY -> ext.category.contains("Geliştirici")
                        DiscordChannel.SUPPORT -> true
                        DiscordChannel.BUG_REPORTS -> true
                        DiscordChannel.SUGGESTIONS -> true
                        else -> true
                    }
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filteredList, key = { it.id }) { ext ->
                ExtensionCard(
                    extension = ext,
                    accountStatus = accountStatus,
                    developerModeEnabled = developerModeEnabled,
                    onInstall = {
                        if (accountStatus != AccountStatus.CONNECTED && !developerModeEnabled) {
                            Toast.makeText(context, "Extension yüklemek için Discord hesabınızı bağlayın.", Toast.LENGTH_SHORT).show()
                            onOpenDiscordAccount()
                        } else {
                            onInstallExtension(ext)
                        }
                    },
                    onOpenDiscordThread = {
                        Toast.makeText(context, "Discord başlığı açılıyor: ${ext.discordThreadUrl}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExtensionCard(
    extension: ExtensionEntity,
    accountStatus: AccountStatus,
    developerModeEnabled: Boolean,
    onInstall: () -> Unit,
    onOpenDiscordThread: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PhoenixCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3B1D0E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconMapper.getIconVector(extension.iconName),
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = extension.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = extension.developer, color = PhoenixAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "• v${extension.version}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF26180E))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(extension.category, color = PhoenixGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = extension.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Discord Thread Link
                Row(
                    modifier = Modifier
                        .clickable { onOpenDiscordThread() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, tint = Color(0xFF5865F2), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Discord Başlığı", color = Color(0xFF5865F2), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                if (extension.isInstalled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(StatusGreen.copy(alpha = 0.2f))
                            .border(1.dp, StatusGreen, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Yüklü ✅", color = StatusGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(containerColor = PhoenixGold, contentColor = Color.Black),
                        modifier = Modifier
                            .testTag("install_ext_${extension.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yükle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.InstalledTabContent(
    installedExtensions: List<ExtensionEntity>,
    onToggleEnabled: (String, Boolean) -> Unit,
    onUninstall: (String) -> Unit
) {
    if (installedExtensions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Extension, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Henüz yüklü bir extension bulunmuyor.", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                Text("Discord Mağaza sekmesinden eklenti yükleyebilirsiniz.", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(installedExtensions, key = { it.id }) { ext ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PhoenixCardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF331A0B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconMapper.getIconVector(ext.iconName),
                                        contentDescription = null,
                                        tint = PhoenixGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(ext.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("v${ext.version} — ${ext.developer}", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                            }

                            Switch(
                                checked = ext.isEnabled,
                                onCheckedChange = { onToggleEnabled(ext.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = PhoenixGold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = ext.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { onUninstall(ext.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PhoenixFlameRed.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Kaldır", tint = PhoenixFlameRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DevModeTabContent(
    developerModeEnabled: Boolean,
    customJsonInput: String,
    onCustomJsonChange: (String) -> Unit,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onImportJson: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
    ) {
        // Toggle Switch Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceVariantDark)
                .border(1.dp, PhoenixAmber.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Developer Mode (Geliştirici Modu)", color = PhoenixGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Geliştirici modu açıkken hesapsız olsanız bile kendi ürettiğiniz JSON / .ankaext manifest paketlerini yerel olarak test edebilirsiniz.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = developerModeEnabled,
                onCheckedChange = onToggleDeveloperMode,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PhoenixGold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Manifest Importer JSON Field
        Text("Yerel Extension Manifest Paketi (JSON)", color = PhoenixAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = customJsonInput,
            onValueChange = onCustomJsonChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("custom_json_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PhoenixGold,
                unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onImportJson,
            colors = ButtonDefaults.buttonColors(containerColor = PhoenixGold, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("import_custom_json_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kendi Extension Paketini Yükle ve Test Et", fontWeight = FontWeight.Bold)
            }
        }
    }
}
