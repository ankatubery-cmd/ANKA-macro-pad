package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountStatus
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.ConnectionMode
import com.example.data.DiscordAccount
import com.example.data.DiscordAccountRepository
import com.example.data.DiscordOAuthManager
import com.example.data.ExtensionEntity
import com.example.data.MacroButtonEntity
import com.example.data.MacroType
import com.example.data.ProfileEntity
import com.example.data.SettingsRepository
import com.example.network.ConnectionStatus
import com.example.network.SocketClientManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

import com.example.extensions.ExtensionManager
import com.example.data.WidgetConfig
import com.example.data.WidgetMiniAction
import com.example.data.WidgetType
import android.net.Uri

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dao = database.macroDao()
    private val extensionDao = database.extensionDao()
    private val settingsRepository = SettingsRepository(application)
    private val discordAccountRepository = DiscordAccountRepository(application)
    private val discordOAuthManager = DiscordOAuthManager(application)
    val socketManager = SocketClientManager(application)
    val extensionManager = ExtensionManager(application, extensionDao)

    val settings: StateFlow<AppSettings> = settingsRepository.settings
    val connectionStatus: StateFlow<ConnectionStatus> = socketManager.connectionStatus

    val accountStatus: StateFlow<AccountStatus> = discordAccountRepository.accountStatus
    val discordAccount: StateFlow<DiscordAccount?> = discordAccountRepository.discordAccount
    val developerModeEnabled: StateFlow<Boolean> = discordAccountRepository.developerModeEnabled

    val allExtensions: StateFlow<List<ExtensionEntity>> = extensionDao.getAllExtensions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installedExtensions: StateFlow<List<ExtensionEntity>> = extensionDao.getInstalledExtensions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    val allProfiles: StateFlow<List<ProfileEntity>> = dao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedProfile: StateFlow<ProfileEntity?> = dao.getSelectedProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeButtons: StateFlow<List<MacroButtonEntity>> = selectedProfile
        .flatMapLatest { profile ->
            if (profile != null) {
                dao.getButtonsForProfile(profile.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Handle auto connect on startup
        viewModelScope.launch {
            settings.collect { currentSettings ->
                socketManager.startAutoConnect(currentSettings)
            }
        }

        // Auto select first profile safely once on startup if no profile is selected, or populate initial data if DB is empty
        viewModelScope.launch {
            ensureDefaultData()
        }

        // Monitor profiles to always ensure an active selected profile exists
        viewModelScope.launch {
            allProfiles.collect { profiles ->
                if (profiles.isNotEmpty()) {
                    val hasSelected = profiles.any { it.isSelected }
                    if (!hasSelected) {
                        selectProfile(profiles.first().id)
                    }
                }
            }
        }

        // Remove legacy sample extensions that were created by older app versions.
        // ANKA Macro Pad must start with no built-in/sample extensions; only user-installed extensions persist.
        viewModelScope.launch {
            val legacySampleIds = listOf(
                "obs-studio",
                "spotify-media",
                "pc-system-tools",
                "discord_rich_presence",
                "spotify_media_master",
                "obs_studio_deck",
                "cs2_valorant_quickbuy",
                "ai_prompt_launchpad"
            )
            legacySampleIds.forEach { extensionId ->
                extensionDao.deleteExtensionById(extensionId)
                val dir = extensionManager.getExtensionDir(extensionId)
                if (dir.exists()) dir.deleteRecursively()
            }
        }

    }

    private suspend fun ensureDefaultData() {
        val existingProfiles = dao.getAllProfilesSync()
        val activeSelected = dao.getSelectedProfileSync()

        if (existingProfiles.isEmpty()) {
            AppDatabase.populateInitialData(dao)
        } else if (activeSelected == null) {
            dao.setSelectedProfile(existingProfiles.first().id)
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun setEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
    }

    fun selectProfile(profileId: Int) {
        viewModelScope.launch {
            dao.clearSelectedProfile()
            dao.setSelectedProfile(profileId)
        }
    }

    fun createProfile(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newProfile = ProfileEntity(
                name = name.trim(),
                iconName = "folder",
                isSelected = false
            )
            val newId = dao.insertProfile(newProfile).toInt()
            selectProfile(newId)
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            dao.updateProfile(profile)
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            dao.deleteButtonsForProfile(profile.id)
            dao.deleteProfile(profile)
            val remaining = dao.getSelectedProfileSync()
            if (remaining == null) {
                val all = dao.getAllProfilesSync()
                if (all.isNotEmpty()) {
                    selectProfile(all.first().id)
                }
            }
        }
    }

    fun saveMacroButton(button: MacroButtonEntity, onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            var targetProfId = button.profileId
            val currentSelected = dao.getSelectedProfileSync()

            if (currentSelected == null) {
                val all = dao.getAllProfilesSync()
                if (all.isEmpty()) {
                    val newId = dao.insertProfile(
                        ProfileEntity(name = "Genel", iconName = "dashboard", isSelected = true)
                    ).toInt()
                    targetProfId = newId
                } else {
                    dao.setSelectedProfile(all.first().id)
                    targetProfId = all.first().id
                }
            } else if (targetProfId <= 0 || dao.getProfileById(targetProfId) == null) {
                targetProfId = currentSelected.id
            }

            val buttonToSave = button.copy(profileId = targetProfId)
            if (buttonToSave.id == 0) {
                dao.insertButton(buttonToSave)
            } else {
                dao.updateButton(buttonToSave)
            }

            // Ensure profile is actively selected so button is immediately visible in panel
            selectProfile(targetProfId)
            onSaved?.invoke()
        }
    }

    fun deleteMacroButton(buttonId: Int) {
        viewModelScope.launch {
            dao.deleteButtonById(buttonId)
        }
    }

    fun executeMacro(button: MacroButtonEntity, onShowToast: ((String) -> Unit)? = null) {
        if (_isEditMode.value) return
        if (button.macroType == MacroType.EXTENSION_ACTION) {
            val parts = button.primaryValue.split(":")
            val shortcutValue = if (parts.size >= 4) parts[3] else button.primaryValue
            if (shortcutValue.isNotBlank() && !shortcutValue.contains("{")) {
                socketManager.sendShortcutDirect(shortcutValue)
            } else {
                socketManager.sendMacro(button)
            }
        } else {
            socketManager.sendMacro(button)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
            socketManager.updateSettingsAndReconnect(newSettings)
        }
    }

    fun manualConnect() {
        socketManager.manualConnect(settings.value)
    }

    fun manualDisconnect() {
        socketManager.disconnect()
    }

    fun testConnection(ip: String, port: Int, mode: ConnectionMode, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = socketManager.testConnection(ip, port, mode)
            onResult(res.first, res.second)
        }
    }

    // --- Discord Account & Extension System Functions ---

    fun startDiscordOAuth(onResult: (Boolean, String?) -> Unit) {
        val result = discordOAuthManager.beginAuthorization()
        onResult(result.isSuccess, result.exceptionOrNull()?.message)
    }

    fun handleDiscordOAuthCallback(uri: android.net.Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = discordOAuthManager.handleCallback(uri)
            result.onSuccess { account ->
                discordAccountRepository.connectOAuthAccount(account)
                onResult(true, null)
            }.onFailure { error ->
                onResult(false, error.message)
            }
        }
    }

    fun disconnectDiscord() {
        discordOAuthManager.clearTokens()
        discordAccountRepository.disconnectAccount()
    }

    fun setDeveloperMode(enabled: Boolean) {
        discordAccountRepository.setDeveloperMode(enabled)
    }

    fun installExtension(extension: ExtensionEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val isConnected = accountStatus.value == AccountStatus.CONNECTED
            val isDev = developerModeEnabled.value

            if (!isConnected && !isDev) {
                onResult(false, "Extension yüklemek için Discord hesabınızı bağlamanız gerekmektedir.")
                return@launch
            }

            extensionDao.setInstallStatus(
                id = extension.id,
                isInstalled = true,
                isEnabled = true,
                installedAt = System.currentTimeMillis()
            )

            // Inject default preset buttons into active profile
            val currentProf = selectedProfile.value
            if (currentProf != null && extension.macroPresetsJson.isNotBlank()) {
                try {
                    val jsonArray = JSONArray(extension.macroPresetsJson)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val actionId = obj.optString("id", "")
                        val title = obj.optString("name", "Extension Action")
                        val description = obj.optString("description", extension.name)
                        val iconName = obj.optString("icon", "extension")
                        val typeStr = obj.optString("type", "EXTENSION").uppercase()
                        val primaryVal = obj.optString("value", "")

                        val macroType = when (typeStr) {
                            "PROGRAM" -> MacroType.PROGRAM
                            "SHORTCUT", "KEY", "EXTENSION", "EXTENSION_ACTION" -> MacroType.EXTENSION_ACTION
                            else -> MacroType.EXTENSION_ACTION
                        }

                        val newBtn = MacroButtonEntity(
                            profileId = currentProf.id,
                            title = title,
                            subtext = description,
                            iconName = iconName,
                            macroType = macroType,
                            primaryValue = primaryVal,
                            extensionId = extension.id,
                            extensionActionId = actionId,
                            orderIndex = 99
                        )
                        dao.insertButton(newBtn)
                    }
                } catch (e: Exception) {
                    // Safe sandboxed handling prevents any malformed preset JSON from crashing the app
                }
            }

            onResult(true, "'${extension.name}' başarıyla yüklendi ve etkinleştirildi!")
        }
    }

    fun toggleExtensionEnabled(extensionId: String, enable: Boolean) {
        viewModelScope.launch {
            extensionDao.setEnabled(extensionId, enable)
        }
    }

    fun uninstallExtension(extensionId: String) {
        viewModelScope.launch {
            extensionDao.deleteExtensionById(extensionId)
            dao.deleteButtonsByExtensionId(extensionId)
            val dir = extensionManager.getExtensionDir(extensionId)
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        }
    }
    fun createExtensionWidget(
        extensionId: String,
        widgetConfig: WidgetConfig,
        title: String,
        subtext: String,
        iconName: String,
        span: Int,
        targetProfileId: Int,
        onCreated: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            var profId = targetProfileId
            val currentSelected = dao.getSelectedProfileSync()

            if (currentSelected == null) {
                val all = dao.getAllProfilesSync()
                if (all.isEmpty()) {
                    val newId = dao.insertProfile(
                        ProfileEntity(name = "Genel", iconName = "dashboard", isSelected = true)
                    ).toInt()
                    profId = newId
                } else {
                    dao.setSelectedProfile(all.first().id)
                    profId = all.first().id
                }
            } else if (profId <= 0 || dao.getProfileById(profId) == null) {
                profId = currentSelected.id
            }

            val configJson = widgetConfig.toJson()

            val widgetEntity = MacroButtonEntity(
                profileId = profId,
                title = title,
                subtext = subtext,
                iconName = iconName,
                macroType = MacroType.WIDGET,
                primaryValue = "widget:extension:$extensionId",
                extraValuesJson = configJson,
                extensionId = extensionId,
                extensionActionId = null,
                sizeSpan = span.coerceIn(1, 3),
                orderIndex = 0,
                gradientStartHex = "#2A180E",
                gradientEndHex = "#160A04",
                borderColorHex = "#FF9100"
            )

            dao.insertButton(widgetEntity)

            // Ensure the target profile is selected so user sees their new widget right away
            selectProfile(profId)
            onCreated?.invoke()
        }
    }
    fun executeWidgetMiniAction(
        button: MacroButtonEntity,
        action: WidgetMiniAction,
        onShowToast: ((String) -> Unit)? = null
    ) {
        if (_isEditMode.value) return

        when (action.type.uppercase()) {
            "SHORTCUT", "KEY", "EXTENSION" -> {
                if (action.value.isNotBlank()) {
                    socketManager.sendShortcutDirect(action.value)
                }
            }
            "PROGRAM" -> {
                if (action.value.isNotBlank()) {
                    socketManager.sendMacro(
                        button.copy(
                            macroType = MacroType.PROGRAM,
                            primaryValue = action.value
                        )
                    )
                }
            }
            else -> {
                if (action.value.isNotBlank()) {
                    socketManager.sendShortcutDirect(action.value)
                }
            }
        }
    }

    fun importExtensionZip(zipUri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = extensionManager.importFromZip(zipUri)
            result.onSuccess { entity ->
                onResult(true, "'${entity.name}' uzantı paketi başarıyla yüklendi!")
            }.onFailure { error ->
                onResult(false, "Uzantı yükleme hatası: ${error.localizedMessage}")
            }
        }
    }

    fun importCustomExtensionJson(jsonText: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val obj = JSONObject(jsonText)
                val id = obj.optString("id", "custom_ext_${System.currentTimeMillis()}")
                val name = obj.optString("name", "Custom Developer Extension")
                val desc = obj.optString("description", "Geliştirici modunda yerel olarak eklenen özel extension.")
                val dev = obj.optString("developer", "Özel Geliştirici")
                val ver = obj.optString("version", "1.0.0")
                val minAnka = obj.optString("minAnkaVersion", "1.0.0")
                val perms = obj.optString("permissions", "[\"Özel Makro Komutları\"]")
                val presets = obj.optString("macroPresets", "[]")

                val customExt = ExtensionEntity(
                    id = id,
                    name = name,
                    description = desc,
                    developer = dev,
                    version = ver,
                    minAnkaVersion = minAnka,
                    permissionsJson = perms,
                    category = "Özel / Developer",
                    isInstalled = true,
                    isEnabled = true,
                    installedAt = System.currentTimeMillis(),
                    macroPresetsJson = presets
                )

                extensionDao.insertOrUpdateExtension(customExt)

                // Inject presets into active profile
                val currentProf = selectedProfile.value
                if (currentProf != null && presets.isNotBlank() && presets != "[]") {
                    val jsonArray = JSONArray(presets)
                    for (i in 0 until jsonArray.length()) {
                        val pObj = jsonArray.getJSONObject(i)
                        val title = pObj.optString("title", "Dev Tuş")
                        val subtext = pObj.optString("subtext", name)
                        val iconName = pObj.optString("iconName", "code")
                        val typeStr = pObj.optString("macroType", "SHORTCUT")
                        val primaryVal = pObj.optString("primaryValue", "")
                        val macroType = try { MacroType.valueOf(typeStr) } catch (e: Exception) { MacroType.SHORTCUT }

                        dao.insertButton(
                            MacroButtonEntity(
                                profileId = currentProf.id,
                                title = title,
                                subtext = subtext,
                                iconName = iconName,
                                macroType = macroType,
                                primaryValue = primaryVal,
                                orderIndex = 99
                            )
                        )
                    }
                }

                onResult(true, "'$name' geliştirici extension'ı başarıyla yüklendi ve profilinize aktarıldı.")
            } catch (e: Exception) {
                onResult(false, "Geçersiz JSON paketi: ${e.localizedMessage}")
            }
        }
    }
}
