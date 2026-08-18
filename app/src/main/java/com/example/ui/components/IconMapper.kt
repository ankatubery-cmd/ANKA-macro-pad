package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    val AVAILABLE_ICONS = listOf(
        "keyboard" to "Klavye",
        "alarm" to "Alarm",
        "widgets" to "Widget",
        "play" to "Başlat",
        "pause" to "Durdur",
        "copy" to "Kopyala",
        "paste" to "Yapıştır",
        "enter" to "Enter",
        "swap" to "Görev Değiştir",
        "refresh" to "Yenile",
        "folder" to "Dosya",
        "mic" to "Mikrofon",
        "volume" to "Ses",
        "volume_off" to "Sessiz",
        "screen_lock" to "Ekran Kilitle",
        "scissors" to "Ekran Görüntüsü",
        "plus" to "Ekle",
        "minus" to "Azalt",
        "skip_previous" to "Önceki",
        "skip_next" to "Sonraki",
        "gamepad" to "Oyun",
        "dashboard" to "Panel",
        "code" to "Kod",
        "chat" to "Sohbet",
        "extension" to "Extension",
        "discord" to "Discord",
        "music" to "Müzik",
        "camera" to "Kamera"
    )

    fun getIconVector(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "alarm" -> Icons.Default.Alarm
            "widgets", "widget" -> Icons.Default.Widgets
            "play" -> Icons.Default.PlayArrow
            "pause" -> Icons.Default.Pause
            "stop" -> Icons.Default.Stop
            "copy" -> Icons.Default.ContentCopy
            "paste" -> Icons.Default.ContentPaste
            "enter" -> Icons.AutoMirrored.Filled.Input
            "swap" -> Icons.Default.SwapHoriz
            "refresh" -> Icons.Default.Refresh
            "folder" -> Icons.Default.Folder
            "mic" -> Icons.Default.Mic
            "volume", "volume_up" -> Icons.AutoMirrored.Filled.VolumeUp
            "volume_off", "volume_mute" -> Icons.AutoMirrored.Filled.VolumeMute
            "screen_lock" -> Icons.Default.ScreenLockPortrait
            "scissors" -> Icons.Default.ContentCut
            "plus", "add" -> Icons.Default.Add
            "minus", "remove" -> Icons.Default.Remove
            "skip_previous", "prev", "previous" -> Icons.Default.SkipPrevious
            "skip_next", "next" -> Icons.Default.SkipNext
            "fast_forward", "forward" -> Icons.Default.FastForward
            "fast_rewind", "rewind" -> Icons.Default.FastRewind
            "settings" -> Icons.Default.Settings
            "gamepad" -> Icons.Default.Gamepad
            "dashboard" -> Icons.Default.Dashboard
            "code" -> Icons.Default.Code
            "chat" -> Icons.AutoMirrored.Filled.Chat
            "extension" -> Icons.Default.Extension
            "discord" -> Icons.Default.Forum
            "music" -> Icons.Default.MusicNote
            "camera" -> Icons.Default.PhotoCamera
            else -> Icons.Default.Keyboard
        }
    }
}
