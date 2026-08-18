package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DiscordAccountRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anka_discord_account", Context.MODE_PRIVATE)

    private val _accountStatus = MutableStateFlow(loadAccountStatus())
    val accountStatus: StateFlow<AccountStatus> = _accountStatus.asStateFlow()

    private val _discordAccount = MutableStateFlow(loadDiscordAccount())
    val discordAccount: StateFlow<DiscordAccount?> = _discordAccount.asStateFlow()

    private val _developerModeEnabled = MutableStateFlow(prefs.getBoolean("developer_mode", false))
    val developerModeEnabled: StateFlow<Boolean> = _developerModeEnabled.asStateFlow()

    private fun loadAccountStatus(): AccountStatus {
        val statusStr = prefs.getString("status", AccountStatus.UNCONNECTED.name) ?: AccountStatus.UNCONNECTED.name
        return try {
            AccountStatus.valueOf(statusStr)
        } catch (e: Exception) {
            AccountStatus.UNCONNECTED
        }
    }

    private fun loadDiscordAccount(): DiscordAccount? {
        val username = prefs.getString("username", null) ?: return null
        val id = prefs.getString("discord_id", null) ?: return null
        val displayName = prefs.getString("display_name", null) ?: username
        val discriminator = prefs.getString("discriminator", "0") ?: "0"
        val avatarUrl = prefs.getString("avatar_url", null)
        val avatarType = prefs.getString("avatar_type", "phoenix") ?: "phoenix"
        val connectedAt = prefs.getLong("connected_at", System.currentTimeMillis())

        return DiscordAccount(
            id = id,
            username = username,
            displayName = displayName,
            discriminator = discriminator,
            avatarUrl = avatarUrl,
            avatarType = avatarType,
            connectedAtMillis = connectedAt
        )
    }

    fun connectOAuthAccount(account: DiscordAccount) {
        prefs.edit()
            .putString("status", AccountStatus.CONNECTED.name)
            .putString("discord_id", account.id)
            .putString("username", account.username)
            .putString("display_name", account.displayName)
            .putString("discriminator", account.discriminator)
            .putString("avatar_url", account.avatarUrl)
            .putString("avatar_type", account.avatarType)
            .putLong("connected_at", account.connectedAtMillis)
            .apply()

        _discordAccount.value = account
        _accountStatus.value = AccountStatus.CONNECTED
    }

    fun disconnectAccount() {
        prefs.edit()
            .putString("status", AccountStatus.DISCONNECTED.name)
            .remove("discord_id")
            .remove("username")
            .remove("display_name")
            .remove("avatar_url")
            .apply()

        _discordAccount.value = null
        _accountStatus.value = AccountStatus.DISCONNECTED
    }

    fun setDeveloperMode(enabled: Boolean) {
        prefs.edit().putBoolean("developer_mode", enabled).apply()
        _developerModeEnabled.value = enabled
    }
}
