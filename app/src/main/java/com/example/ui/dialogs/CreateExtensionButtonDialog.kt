package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExtensionEntity
import com.example.data.MacroButtonEntity
import com.example.data.MacroType
import com.example.data.ProfileEntity
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import org.json.JSONArray

private data class ExtensionButtonOption(
    val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val value: String,
    val description: String
)

@Composable
fun CreateExtensionButtonDialog(
    extension: ExtensionEntity,
    profiles: List<ProfileEntity>,
    selectedProfileId: Int?,
    onSave: (MacroButtonEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val options = remember(extension.macroPresetsJson) {
        runCatching {
            val array = JSONArray(extension.macroPresetsJson)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    if (id.isNotBlank()) {
                        add(
                            ExtensionButtonOption(
                                id = id,
                                name = obj.optString("name", id),
                                icon = obj.optString("icon", "extension"),
                                type = obj.optString("type", "EXTENSION"),
                                value = obj.optString("value", ""),
                                description = obj.optString("description", "")
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    var selectedActionId by remember(options) { mutableStateOf(options.firstOrNull()?.id.orEmpty()) }
    var buttonTitle by remember(options) { mutableStateOf(options.firstOrNull()?.name.orEmpty()) }
    var selectedIcon by remember(options) { mutableStateOf(options.firstOrNull()?.icon ?: "extension") }
    var targetProfileId by remember {
        mutableStateOf(selectedProfileId ?: profiles.firstOrNull()?.id ?: 1)
    }
    var profileMenuOpen by remember { mutableStateOf(false) }

    val selectedAction = options.firstOrNull { it.id == selectedActionId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Extension, null, tint = PhoenixGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${extension.name} - Buton Oluştur",
                    color = PhoenixGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (options.isEmpty()) {
                    Text(
                        "Bu uzantı bir buton ön ayarı sağlamıyor. Uzantı kendi arayüzünden kullanılabilir.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        "Uzantı aksiyonu:",
                        color = PhoenixAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    options.forEach { option ->
                        val selected = option.id == selectedActionId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Color(0xFF3D1D0D) else SurfaceVariantDark)
                                .border(
                                    if (selected) 1.5.dp else 1.dp,
                                    if (selected) PhoenixGold else PhoenixCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedActionId = option.id
                                    buttonTitle = option.name
                                    selectedIcon = option.icon
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    option.name,
                                    color = if (selected) PhoenixGold else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (option.description.isNotBlank()) {
                                    Text(
                                        option.description,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = buttonTitle,
                        onValueChange = { buttonTitle = it },
                        label = { Text("Buton adı", color = PhoenixGold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixGold,
                            unfocusedBorderColor = PhoenixCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Text(
                    "Eklenecek profil:",
                    color = PhoenixAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariantDark)
                        .border(1.dp, PhoenixCardBorder, RoundedCornerShape(10.dp))
                        .clickable { profileMenuOpen = true }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, null, tint = PhoenixGold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            profiles.firstOrNull { it.id == targetProfileId }?.name ?: "Profil Seçilmedi",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = profileMenuOpen,
                        onDismissRequest = { profileMenuOpen = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.name, color = Color.White) },
                                onClick = {
                                    targetProfileId = profile.id
                                    profileMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedAction != null && targetProfileId > 0,
                onClick = {
                    val action = selectedAction ?: return@Button
                    val macroType = when (action.type.uppercase()) {
                        "PROGRAM" -> MacroType.PROGRAM
                        else -> MacroType.EXTENSION_ACTION
                    }

                    onSave(
                        MacroButtonEntity(
                            profileId = targetProfileId,
                            title = buttonTitle.ifBlank { action.name },
                            subtext = extension.name,
                            iconName = selectedIcon,
                            macroType = macroType,
                            primaryValue = action.value,
                            extensionId = extension.id,
                            extensionActionId = action.id,
                            gradientStartHex = "#FF6D00",
                            gradientEndHex = "#D84315",
                            borderColorHex = "#FFAB40"
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhoenixGold,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ana Ekrana Ekle", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = SurfaceDark
    )
}
