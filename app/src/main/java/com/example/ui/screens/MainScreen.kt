package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.AccountStatus
import com.example.data.DiscordAccount
import com.example.data.ExtensionEntity
import com.example.data.MacroButtonEntity
import com.example.ui.components.PhoenixHeader
import com.example.ui.dialogs.ExtensionSecurityDialog
import com.example.ui.dialogs.MacroEditDialog
import com.example.ui.dialogs.PcServerCodeDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixDarkBackground
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.viewmodel.MainViewModel

enum class NavScreen {
    PANEL, EXTENSIONS, PROFILES, DISCORD
}

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val accountStatus by viewModel.accountStatus.collectAsStateWithLifecycle()
    val discordAccount by viewModel.discordAccount.collectAsStateWithLifecycle()
    val developerModeEnabled by viewModel.developerModeEnabled.collectAsStateWithLifecycle()
    val allExtensions by viewModel.allExtensions.collectAsStateWithLifecycle()
    val installedExtensions by viewModel.installedExtensions.collectAsStateWithLifecycle()

    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val buttons by viewModel.activeButtons.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(NavScreen.PANEL) }
    var isFocusMode by remember { mutableStateOf(false) }

    var showMacroEditDialog by remember { mutableStateOf(false) }
    var editingButton by remember { mutableStateOf<MacroButtonEntity?>(null) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showServerCodeDialog by remember { mutableStateOf(false) }

    var showExtensionSecurityDialog by remember { mutableStateOf(false) }
    var pendingInstallExtension by remember { mutableStateOf<ExtensionEntity?>(null) }

    // Collect socket toast notifications
    LaunchedEffect(Unit) {
        viewModel.socketManager.toastMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            if (!isFocusMode) {
                PhoenixHeader(
                    connectionStatus = connectionStatus,
                    accountStatus = accountStatus,
                    discordAccount = discordAccount,
                    ipAddress = settings.ipAddress,
                    port = settings.port,
                    isEditMode = isEditMode,
                    showDiscordIcon = (currentScreen == NavScreen.DISCORD),
                    onToggleEditMode = { viewModel.toggleEditMode() },
                    onOpenSettings = { showSettingsDialog = true },
                    onOpenServerCode = { showServerCodeDialog = true },
                    onStatusClick = { viewModel.manualConnect() },
                    onToggleFocusMode = { isFocusMode = true }
                )
            }
        },
        bottomBar = {
            if (!isFocusMode) {
                MainBottomNavigation(
                    currentScreen = currentScreen,
                    accountStatus = accountStatus,
                    discordAccount = discordAccount,
                    isServerCodeOpen = showServerCodeDialog,
                    onOpenServerCode = {
                        showServerCodeDialog = !showServerCodeDialog
                    },
                    onSelectScreen = { screen ->
                        showServerCodeDialog = false
                        if (screen != NavScreen.PANEL) {
                            viewModel.setEditMode(false)
                        }
                        currentScreen = screen
                    }
                )
            }
        },
        containerColor = PhoenixDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFocusMode) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
                .consumeWindowInsets(if (isFocusMode) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
                .imePadding()
                .background(PhoenixDarkBackground)
        ) {
            if (isFocusMode) {
                FocusMacroScreen(
                    buttons = buttons,
                    onExecuteMacro = { viewModel.executeMacro(it) },
                    onExitFocusMode = { isFocusMode = false }
                )
            } else {
                // Horizontal sliding page transitions
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val isForward = targetState.ordinal > initialState.ordinal
                        if (isForward) {
                            (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> width } + fadeIn(animationSpec = tween(200)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> -width } + fadeOut(animationSpec = tween(200)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> -width } + fadeIn(animationSpec = tween(200)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> width } + fadeOut(animationSpec = tween(200)))
                        }
                    },
                    label = "MainScreenPageTransition"
                ) { targetPage ->
                    when (targetPage) {
                        NavScreen.PANEL -> {
                            PanelScreen(
                                connectionStatus = connectionStatus,
                                targetIp = settings.ipAddress,
                                targetPort = settings.port,
                                isEditMode = isEditMode,
                                allProfiles = allProfiles,
                                selectedProfile = selectedProfile,
                                buttons = buttons,
                                onSelectProfile = { viewModel.selectProfile(it) },
                                onOpenCreateProfile = { currentScreen = NavScreen.PROFILES },
                                onExecuteMacro = { viewModel.executeMacro(it) },
                                onExecuteWidgetMiniAction = { btn, action ->
                                    viewModel.executeWidgetMiniAction(btn, action) { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onExtensionSendShortcut = { keys ->
                                    viewModel.socketManager.sendShortcutDirect(keys)
                                },
                                onExtensionOpenApp = { appName ->
                                    viewModel.executeMacro(
                                        MacroButtonEntity(
                                            profileId = selectedProfile?.id ?: 1,
                                            title = appName,
                                            subtext = "",
                                            iconName = "folder",
                                            macroType = com.example.data.MacroType.PROGRAM,
                                            primaryValue = appName
                                        )
                                    )
                                },
                                onExtensionNotification = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                onExtensionGetProfiles = {
                                    org.json.JSONArray(allProfiles.map {
                                        org.json.JSONObject().put("id", it.id).put("name", it.name)
                                    }).toString()
                                },
                                onEditMacro = { btn ->
                                    editingButton = btn
                                    showMacroEditDialog = true
                                },
                                onDeleteMacro = { viewModel.deleteMacroButton(it) },
                                onAddMacro = {
                                    editingButton = null
                                    showMacroEditDialog = true
                                },
                                onConnectManual = { viewModel.manualConnect() },
                                onOpenSettings = { showSettingsDialog = true },
                                onToggleFocusMode = { isFocusMode = true }
                            )
                        }
                    NavScreen.EXTENSIONS -> {
                        ExtensionsScreen(
                            accountStatus = accountStatus,
                            developerModeEnabled = developerModeEnabled,
                            allExtensions = allExtensions,
                            installedExtensions = installedExtensions,
                            profiles = allProfiles,
                            selectedProfileId = selectedProfile?.id,
                            onToggleExtensionEnabled = { id, enabled ->
                                viewModel.toggleExtensionEnabled(id, enabled)
                            },
                            onUninstallExtension = { id ->
                                viewModel.uninstallExtension(id)
                                Toast.makeText(context, "Uzantı kaldırıldı.", Toast.LENGTH_SHORT).show()
                            },
                            onToggleDeveloperMode = { enabled ->
                                viewModel.setDeveloperMode(enabled)
                            },
                            onImportCustomJson = { jsonStr, callback ->
                                viewModel.importCustomExtensionJson(jsonStr, callback)
                            },
                            onImportZipExtension = { zipUri, callback ->
                                viewModel.importExtensionZip(zipUri, callback)
                            },
                            onCreateButton = { newBtn ->
                                viewModel.saveMacroButton(newBtn) {
                                    currentScreen = NavScreen.PANEL
                                }
                                currentScreen = NavScreen.PANEL
                            },
                            onCreateWidget = { extensionId, title, subtext, iconName, span, targetProfileId, config ->
                                viewModel.createExtensionWidget(
                                    extensionId = extensionId,
                                    widgetConfig = config,
                                    title = title,
                                    subtext = subtext,
                                    iconName = iconName,
                                    span = span,
                                    targetProfileId = targetProfileId,
                                    onCreated = {
                                        currentScreen = NavScreen.PANEL
                                    }
                                )
                            },
                            onSendShortcut = { keys ->
                                viewModel.socketManager.sendShortcutDirect(keys)
                            },
                            onOpenApp = { appName ->
                                viewModel.executeMacro(
                                    com.example.data.MacroButtonEntity(
                                        profileId = selectedProfile?.id ?: 1,
                                        title = appName,
                                        subtext = "",
                                        iconName = "folder",
                                        macroType = com.example.data.MacroType.PROGRAM,
                                        primaryValue = appName
                                    )
                                )
                            },
                            onShowNotification = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onGetProfiles = {
                                org.json.JSONArray(allProfiles.map { org.json.JSONObject().put("id", it.id).put("name", it.name) }).toString()
                            }
                        )
                    }
                    NavScreen.PROFILES -> {
                        ProfilesScreen(
                            profiles = allProfiles,
                            selectedProfile = selectedProfile,
                            onSelectProfile = { viewModel.selectProfile(it) },
                            onCreateProfile = { name -> viewModel.createProfile(name) },
                            onUpdateProfile = { profile -> viewModel.updateProfile(profile) },
                            onDeleteProfile = { profile -> viewModel.deleteProfile(profile) }
                        )
                    }
                    NavScreen.DISCORD -> {
                        DiscordProfileScreen(
                            accountStatus = accountStatus,
                            discordAccount = discordAccount,
                            onConnect = {
                                viewModel.startDiscordOAuth { success, error ->
                                    if (!success) {
                                        android.widget.Toast.makeText(context, error ?: "Discord OAuth başlatılamadı.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onDisconnect = {
                                viewModel.disconnectDiscord()
                            }
                        )
                    }
                }
            }
        }
    }
}

    // Modal Dialogs for secondary actions (Macro Edit, Settings, Security)
    if (showMacroEditDialog) {
        val targetProfileId = selectedProfile?.id ?: 1
        MacroEditDialog(
            buttonToEdit = editingButton,
            profileId = targetProfileId,
            installedExtensions = installedExtensions,
            onSave = { updatedButton ->
                viewModel.saveMacroButton(updatedButton)
            },
            onDelete = { buttonId ->
                viewModel.deleteMacroButton(buttonId)
            },
            onDismiss = { showMacroEditDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onSaveSettings = { newSettings ->
                viewModel.updateSettings(newSettings)
            },
            onTestConnection = { ip, port, mode, callback ->
                viewModel.testConnection(ip, port, mode, callback)
            },
            onOpenServerCode = { showServerCodeDialog = true },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showServerCodeDialog) {
        PcServerCodeDialog(
            onDismiss = { showServerCodeDialog = false }
        )
    }

    if (showExtensionSecurityDialog && pendingInstallExtension != null) {
        ExtensionSecurityDialog(
            extension = pendingInstallExtension!!,
            onConfirmInstall = {
                val extToInstall = pendingInstallExtension!!
                viewModel.installExtension(extToInstall) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = {
                showExtensionSecurityDialog = false
                pendingInstallExtension = null
            }
        )
    }
}

@Composable
fun MainBottomNavigation(
    currentScreen: NavScreen,
    accountStatus: AccountStatus,
    discordAccount: DiscordAccount?,
    isServerCodeOpen: Boolean = false,
    onOpenServerCode: () -> Unit = {},
    onSelectScreen: (NavScreen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = SurfaceDark
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PhoenixAmber.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = androidx.compose.ui.graphics.RectangleShape
                )
                .padding(top = 8.dp, bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Panel Tab (Left 1)
                NavItem(
                    label = stringResource(R.string.nav_panel),
                    icon = Icons.Default.Dashboard,
                    isSelected = currentScreen == NavScreen.PANEL && !isServerCodeOpen,
                    onClick = { onSelectScreen(NavScreen.PANEL) }
                )

                // 2. Uzantılar Tab (Left 2)
                NavItem(
                    label = stringResource(R.string.nav_extensions),
                    icon = Icons.Default.Extension,
                    isSelected = currentScreen == NavScreen.EXTENSIONS && !isServerCodeOpen,
                    onClick = { onSelectScreen(NavScreen.EXTENSIONS) }
                )

                // 3. Center Prominent Hero Item: DISCORD PROFİLİ
                val isDiscordSelected = currentScreen == NavScreen.DISCORD && !isServerCodeOpen
                val avatarBorderBrush: Brush = if (isDiscordSelected) {
                    Brush.linearGradient(listOf(PhoenixGold, PhoenixAmber))
                } else {
                    SolidColor(PhoenixCardBorder)
                }

                Box(
                    modifier = Modifier
                        .offset(y = (-12).dp)
                        .clickable { onSelectScreen(NavScreen.DISCORD) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // Active Neon Glow Radial Background Ring
                            if (isDiscordSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    PhoenixGold.copy(alpha = 0.45f),
                                                    PhoenixAmber.copy(alpha = 0.25f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F1B24))
                                    .border(
                                        width = if (isDiscordSelected) 2.5.dp else 1.5.dp,
                                        brush = avatarBorderBrush,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (accountStatus == AccountStatus.CONNECTED && !discordAccount?.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = discordAccount?.avatarUrl,
                                        contentDescription = stringResource(R.string.discord_avatar),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.phoenix_logo_1786449649730),
                                        contentDescription = stringResource(R.string.discord_avatar),
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }

                            // Active Account Status Indicator Dot
                            val isConnected = accountStatus == AccountStatus.CONNECTED
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(if (isConnected) StatusGreen else PhoenixFlameRed)
                                    .border(1.5.dp, SurfaceDark, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = stringResource(R.string.nav_discord),
                            color = if (isDiscordSelected) PhoenixGold else Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = if (isDiscordSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                // 4. PC Kod Tab (Bağımsız Buton "<>")
                NavItem(
                    label = stringResource(R.string.nav_pc_code),
                    icon = Icons.Default.Code,
                    isSelected = isServerCodeOpen,
                    onClick = { onOpenServerCode() }
                )

                // 5. Profiller Tab (Right)
                NavItem(
                    label = stringResource(R.string.nav_profiles),
                    icon = Icons.Default.Folder,
                    isSelected = currentScreen == NavScreen.PROFILES && !isServerCodeOpen,
                    onClick = { onSelectScreen(NavScreen.PROFILES) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PhoenixGold else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(if (isSelected) 24.dp else 21.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) PhoenixGold else Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
