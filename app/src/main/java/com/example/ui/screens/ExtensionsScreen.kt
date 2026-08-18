package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.AccountStatus
import com.example.data.DiscordChannel
import com.example.data.ExtensionEntity
import com.example.data.MacroButtonEntity
import com.example.data.ProfileEntity
import com.example.data.WidgetConfig
import com.example.ui.components.IconMapper
import com.example.ui.dialogs.CreateExtensionButtonDialog
import com.example.ui.dialogs.CreateExtensionWidgetDialog
import com.example.ui.dialogs.ExtensionRunnerDialog
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import org.json.JSONArray

@Composable
fun ExtensionsScreen(
    accountStatus: AccountStatus,
    developerModeEnabled: Boolean,
    allExtensions: List<ExtensionEntity>,
    installedExtensions: List<ExtensionEntity>,
    profiles: List<ProfileEntity> = emptyList(),
    selectedProfileId: Int? = null,
    onToggleExtensionEnabled: (String, Boolean) -> Unit,
    onUninstallExtension: (String) -> Unit,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onImportCustomJson: (String, (Boolean, String) -> Unit) -> Unit,
    onImportZipExtension: (Uri, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    onCreateButton: (MacroButtonEntity) -> Unit = {},
    onCreateWidget: (
        extensionId: String,
        title: String,
        subtext: String,
        iconName: String,
        span: Int,
        targetProfileId: Int,
        widgetConfig: WidgetConfig
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onSendShortcut: (String) -> Unit = {},
    onOpenApp: (String) -> Unit = {},
    onShowNotification: (String) -> Unit = {},
    onGetProfiles: () -> String = { "[]" }
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Yüklü Uzantılar", "Uzantı İndir", "Ayarlar", "Geliştirici Modu")

    var activeRunnerExtension by remember { mutableStateOf<ExtensionEntity?>(null) }
    var inspectingExtension by remember { mutableStateOf<ExtensionEntity?>(null) }

    var showCreateButtonForExt by remember { mutableStateOf<ExtensionEntity?>(null) }
    var showCreateWidgetForExt by remember { mutableStateOf<ExtensionEntity?>(null) }

    val context = LocalContext.current

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportZipExtension(uri) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                tint = PhoenixGold,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ext_platform),
                    color = PhoenixGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.ext_platform_desc),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            
            var headerMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { headerMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.menu),
                        tint = PhoenixGold
                    )
                }
                DropdownMenu(
                    expanded = headerMenuExpanded,
                    onDismissRequest = { headerMenuExpanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.install_ext), color = Color.White, fontWeight = FontWeight.SemiBold) },
                        onClick = {
                            headerMenuExpanded = false
                            zipPickerLauncher.launch("*/*")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = PhoenixGold
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceVariantDark,
            contentColor = PhoenixGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PhoenixGold,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, PhoenixCardBorder, RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) PhoenixGold else Color.White.copy(alpha = 0.7f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> InstalledExtensionsTab(
                installedExtensions = installedExtensions,
                onImportZipClick = { zipPickerLauncher.launch("*/*") },
                onRunExtension = { activeRunnerExtension = it },
                onInspectExtension = { inspectingExtension = it },
                onCreateButtonForExt = { showCreateButtonForExt = it },
                onCreateWidgetForExt = { showCreateWidgetForExt = it },
                onToggleExtensionEnabled = onToggleExtensionEnabled,
                onUninstallExtension = onUninstallExtension
            )
            1 -> DownloadExtensionsDiscordTab()
            2 -> ExtensionSettingsTab()
            3 -> DeveloperModeTab(
                developerModeEnabled = developerModeEnabled,
                onToggleDeveloperMode = onToggleDeveloperMode,
                onImportCustomJson = onImportCustomJson
            )
        }
    }

    // Create Button Dialog
    showCreateButtonForExt?.let { ext ->
        CreateExtensionButtonDialog(
            extension = ext,
            profiles = profiles,
            selectedProfileId = selectedProfileId,
            onSave = { newBtn ->
                onCreateButton(newBtn)
                showCreateButtonForExt = null
                Toast.makeText(context, "Uzantı tuşu eklendi!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCreateButtonForExt = null }
        )
    }

    // Create Widget Dialog
    showCreateWidgetForExt?.let { ext ->
        CreateExtensionWidgetDialog(
            extension = ext,
            profiles = profiles,
            currentProfileId = selectedProfileId,
            onCreateWidget = { title, subtext, iconName, span, targetProfileId, widgetConfig ->
                onCreateWidget(ext.id, title, subtext, iconName, span, targetProfileId, widgetConfig)
                showCreateWidgetForExt = null
                Toast.makeText(context, "'$title' widget'ı ana panele eklendi!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCreateWidgetForExt = null }
        )
    }

    // WebView Runner Dialog
    activeRunnerExtension?.let { ext ->
        ExtensionRunnerDialog(
            extension = ext,
            onSendShortcut = onSendShortcut,
            onOpenApp = onOpenApp,
            onShowNotification = onShowNotification,
            onGetProfiles = onGetProfiles,
            onDismiss = { activeRunnerExtension = null }
        )
    }

    // Inspect Details Dialog
    inspectingExtension?.let { ext ->
        val permissionsList = remember(ext.permissionsJson) {
            try {
                val array = JSONArray(ext.permissionsJson)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) list.add(array.getString(i))
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        AlertDialog(
            onDismissRequest = { inspectingExtension = null },
            title = {
                Text(
                    text = "${ext.name} - İzinler & Detaylar",
                    color = PhoenixGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(text = "Uzantı ID: ${ext.id}", color = Color.White, fontSize = 12.sp)
                    Text(text = "Sürüm: v${ext.version}", color = Color.White, fontSize = 12.sp)
                    Text(text = "Geliştirici: ${ext.developer}", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tanımlı İzinler:",
                        color = PhoenixAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (permissionsList.isEmpty()) {
                        Text(text = "Özel bir izin gerektirmiyor.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    } else {
                        permissionsList.forEach { perm ->
                            Text(text = "• $perm", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectingExtension = null }) {
                    Text("Tamam", color = PhoenixGold)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun InstalledExtensionsTab(
    installedExtensions: List<ExtensionEntity>,
    onImportZipClick: () -> Unit,
    onRunExtension: (ExtensionEntity) -> Unit,
    onInspectExtension: (ExtensionEntity) -> Unit,
    onCreateButtonForExt: (ExtensionEntity) -> Unit,
    onCreateWidgetForExt: (ExtensionEntity) -> Unit,
    onToggleExtensionEnabled: (String, Boolean) -> Unit,
    onUninstallExtension: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // ZIP Import Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .border(1.dp, PhoenixGold, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF291A0E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        tint = PhoenixGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ZIP Uzantı Paketi Yükle",
                            color = PhoenixGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "APK derlemeden manifest.json içeren uzantıları ekleyin",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = onImportZipClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "ZIP Seç", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (installedExtensions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Henüz yüklü bir uzantı yok",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Yukarıdaki 'ZIP Seç' butonu ile yeni bir uzantı paketi ekleyebilirsiniz.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
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
                                            .clip(CircleShape)
                                            .background(Color(0xFF2A1B0E))
                                            .border(1.dp, PhoenixAmber, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconMapper.getIconVector(ext.iconName),
                                            contentDescription = null,
                                            tint = PhoenixGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = ext.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "v${ext.version} • ${ext.developer}",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = ext.isEnabled,
                                        onCheckedChange = { onToggleExtensionEnabled(ext.id, it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PhoenixGold,
                                            checkedTrackColor = Color(0xFF3D1D0D),
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = SurfaceDark
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onUninstallExtension(ext.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Sil",
                                            tint = PhoenixFlameRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ext.description,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons Row (Buton Oluştur, Panel Oluştur, Arayüzü Aç, İzinler)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onCreateButtonForExt(ext) },
                                        enabled = ext.isEnabled,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PhoenixAmber,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Buton Oluştur", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onCreateWidgetForExt(ext) },
                                        enabled = ext.isEnabled,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PhoenixGold,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Widget Oluştur", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onRunExtension(ext) },
                                        enabled = ext.isEnabled,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SurfaceDark,
                                            contentColor = PhoenixGold
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .border(1.dp, PhoenixCardBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Arayüz", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    IconButton(
                                        onClick = { onInspectExtension(ext) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, PhoenixCardBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "İzinler",
                                            tint = PhoenixGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadExtensionsDiscordTab() {
    val context = LocalContext.current
    val discordCommunityUrl = "https://discord.gg/ytfbKGRrft"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, PhoenixGold, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1810)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = "Discord Community",
                    tint = Color(0xFF5865F2),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ANKA Discord Eklenti Kanalı",
                    color = PhoenixGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tüm topluluk eklentileri, resmi güncellemeler ve güvenlik onaylı uzantı paketleri Discord sunucumuzda paylaşılmaktadır.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(discordCommunityUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5865F2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discord Eklenti Kanalını Aç",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Popüler Discord Kanalları:",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(DiscordChannel.entries) { channel ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .border(1.dp, PhoenixCardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(discordCommunityUrl))
                            context.startActivity(intent)
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = channel.channelName,
                                color = PhoenixGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = channel.description,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Launch,
                            contentDescription = "Katıl",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtensionSettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        var autoUpdate by remember { mutableStateOf(true) }
        var sandboxCheck by remember { mutableStateOf(true) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PhoenixCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Güvenlik & Güncelleme Ayarları",
                    color = PhoenixGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Otomatik Uzantı Güncellemeleri",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Topluluk uzantılarını yayınlandıkça otomatik güncelle",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = autoUpdate,
                        onCheckedChange = { autoUpdate = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PhoenixGold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sandbox İzin Denetimi",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Uzantıların sistem tuşlarına erişimini denetle",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = sandboxCheck,
                        onCheckedChange = { sandboxCheck = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PhoenixGold)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeveloperModeTab(
    developerModeEnabled: Boolean,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onImportCustomJson: (String, (Boolean, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var jsonInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PhoenixCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Geliştirici Modu (Dev Mode)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Özel extension JSON yapılandırmalarını yükleyin",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = developerModeEnabled,
                        onCheckedChange = onToggleDeveloperMode,
                        colors = SwitchDefaults.colors(checkedThumbColor = PhoenixGold)
                    )
                }

                if (developerModeEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Özel JSON Formatında Extension Yükle:",
                        color = PhoenixGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        placeholder = {
                            Text(
                                text = "{\n  \"id\": \"custom_macro\",\n  \"name\": \"Özel Makro\",\n  \"version\": \"1.0.0\"\n}",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixGold,
                            unfocusedBorderColor = PhoenixCardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (jsonInput.isBlank()) {
                                Toast.makeText(context, "Lütfen JSON içeriği girin.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onImportCustomJson(jsonInput) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) jsonInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhoenixGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "JSON Extension Yükle & Test Et",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
