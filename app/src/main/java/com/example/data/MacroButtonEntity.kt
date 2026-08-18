package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macro_buttons")
data class MacroButtonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileId: Int,
    val title: String,
    val subtext: String = "",
    val iconName: String = "keyboard", // e.g. "keyboard", "copy", "paste", "enter", "folder", "mic", "volume", "screen_lock", "scissors", "plus", "refresh", "code"
    val macroType: MacroType = MacroType.KEY,
    val primaryValue: String = "", // e.g. "H", "CTRL+C", "C:\\Program Files\\..."
    val extraValuesJson: String = "[]", // for multi-program paths or extra parameters
    val colorHex: String = "#FF6D00",
    val gradientStartHex: String = "#FF6D00",
    val gradientEndHex: String = "#D84315",
    val borderColorHex: String = "#FFAB40",
    val sizeSpan: Int = 1, // 1 = standard tile, 2 = double width
    val orderIndex: Int = 0,
    val extensionId: String? = null,
    val extensionActionId: String? = null
)
