package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extensions")
data class ExtensionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconName: String = "extension",
    val description: String,
    val developer: String,
    val version: String,
    val minAnkaVersion: String = "1.0.0",
    val permissionsJson: String = "[]", // List<String> serialized
    val changelog: String = "İlk sürüm yayınlandı.",
    val setupGuide: String = "1. Extension'ı aktifleştirin.\n2. PC Sunucunuzun açık olduğundan emin olun.\n3. Makro butonlarınızı kullanmaya başlayın.",
    val downloadUrl: String = "https://discord.gg/anka-macropad",
    val discordThreadUrl: String = "https://discord.com/channels/anka/threads/",
    val category: String = "Genel",
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = false,
    val installedAt: Long = 0L,
    val macroPresetsJson: String = "[]" // Pre-configured macros that get loaded
)
