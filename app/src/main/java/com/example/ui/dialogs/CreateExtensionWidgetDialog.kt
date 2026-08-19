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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExtensionEntity
import com.example.data.WidgetConfig
import com.example.data.WidgetLayout
import com.example.data.WidgetMiniAction
import com.example.data.WidgetType
import com.example.ui.components.IconMapper
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark
import org.json.JSONArray

private data class WidgetActionOption(
    val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val value: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExtensionWidgetDialog(
    extension: ExtensionEntity,
    profiles: List<com.example.data.ProfileEntity>,
    currentProfileId: Int?,
    onCreateWidget: (
        title: String,
        subtext: String,
        iconName: String,
        span: Int,
        targetProfileId: Int,
        widgetConfig: WidgetConfig
    ) -> Unit,
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
                            WidgetActionOption(
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

    var widgetTitle by remember { mutableStateOf(extension.name) }
    var selectedSpan by remember { mutableIntStateOf(2) }
    var targetProfile by remember {
        mutableStateOf(profiles.firstOrNull { it.id == currentProfileId } ?: profiles.firstOrNull())
    }
    var profileExpanded by remember { mutableStateOf(false) }
    var selectedActionIds by remember { mutableStateOf(options.map { it.id }.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dashboard, null, tint = PhoenixGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "${extension.name} - Widget Oluştur",
                    color = PhoenixGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = widgetTitle,
                    onValueChange = { widgetTitle = it },
                    label = { Text("Widget Başlığı", color = PhoenixGold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PhoenixGold,
                        unfocusedBorderColor = PhoenixCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text(
                    "Widget boyutu",
                    color = PhoenixGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "1x1 (1 Sütun)", 2 to "2x2 (2 Sütun)", 3 to "3x3 (3 Sütun)").forEach { (span, label) ->
                        val selected = selectedSpan == span
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PhoenixGold else Color(0xFF26150B))
                                .border(1.dp, if (selected) PhoenixGold else PhoenixCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedSpan = span }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (selected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (options.isNotEmpty()) {
                    Text(
                        "Widget aksiyonları",
                        color = PhoenixGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    options.forEach { option ->
                        val checked = selectedActionIds.contains(option.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedActionIds = if (checked) {
                                        selectedActionIds - option.id
                                    } else {
                                        selectedActionIds + option.id
                                    }
                                }
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    selectedActionIds = if (it) selectedActionIds + option.id
                                    else selectedActionIds - option.id
                                },
                                colors = CheckboxDefaults.colors(checkedColor = PhoenixGold)
                            )
                            Icon(
                                IconMapper.getIconVector(option.icon),
                                null,
                                tint = PhoenixGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(option.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (option.description.isNotBlank()) {
                                    Text(option.description, color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Bu uzantı aksiyon tanımlamıyor. Widget, uzantının entry HTML arayüzünü doğrudan gösterecek.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = profileExpanded,
                    onExpandedChange = { profileExpanded = it }
                ) {
                    OutlinedTextField(
                        value = targetProfile?.name ?: "Profil Seçin",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Eklenecek Profil", color = PhoenixGold) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = profileExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixGold,
                            unfocusedBorderColor = PhoenixCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = profileExpanded,
                        onDismissRequest = { profileExpanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.name, color = Color.White) },
                                onClick = {
                                    targetProfile = profile
                                    profileExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = targetProfile != null,
                onClick = {
                    val profile = targetProfile ?: return@Button
                    val actions = options
                        .filter { selectedActionIds.contains(it.id) }
                        .map {
                            WidgetMiniAction(
                                id = it.id,
                                name = it.name,
                                icon = it.icon,
                                type = it.type,
                                value = it.value
                            )
                        }

                    onCreateWidget(
                        widgetTitle.ifBlank { extension.name },
                        extension.name,
                        "widgets",
                        selectedSpan,
                        profile.id,
                        WidgetConfig(
                            widgetType = if (options.isEmpty()) {
                                WidgetType.EXTENSION_HTML
                            } else {
                                WidgetType.EXTENSION_ACTIONS
                            },
                            layout = WidgetLayout.HORIZONTAL,
                            actions = actions
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhoenixGold,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Widget'ı Ana Panele Ekle", fontWeight = FontWeight.Bold)
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
