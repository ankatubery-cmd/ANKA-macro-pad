package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ExtensionEntity
import com.example.data.MacroButtonEntity
import com.example.data.MacroType
import com.example.extensions.ExtensionAction
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.ui.components.IconMapper
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import org.json.JSONArray

data class ColorPreset(
    val name: String,
    val startHex: String,
    val endHex: String,
    val borderHex: String
)

val COLOR_PRESETS = listOf(
    ColorPreset("Anka Turuncu", "#FF6D00", "#D84315", "#FFAB40"),
    ColorPreset("Alev Kırmızı", "#D50000", "#880E4F", "#FF5252"),
    ColorPreset("Altın Sarı", "#FFAB00", "#E65100", "#FFD740"),
    ColorPreset("Zümrüt Yeşil", "#00C853", "#1B5E20", "#69F0AE"),
    ColorPreset("Çelik Mavi", "#0091EA", "#0D47A1", "#40C4FF"),
    ColorPreset("Mor Gece", "#AA00FF", "#4A148C", "#E040FB")
)

@Composable
fun MacroEditDialog(
    buttonToEdit: MacroButtonEntity?,
    profileId: Int,
    installedExtensions: List<ExtensionEntity> = emptyList(),
    onSave: (MacroButtonEntity) -> Unit,
    onDelete: ((Int) -> Unit)?,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: TUŞLAR, 1: GENEL, 2: GÖRÜNÜM

    var title by remember { mutableStateOf(buttonToEdit?.title ?: "") }
    var subtext by remember { mutableStateOf(buttonToEdit?.subtext ?: "") }
    var iconName by remember { mutableStateOf(buttonToEdit?.iconName ?: "keyboard") }
    var macroType by remember { mutableStateOf(buttonToEdit?.macroType ?: MacroType.KEY) }
    var primaryValue by remember { mutableStateOf(buttonToEdit?.primaryValue ?: "") }
    var multiPathsText by remember {
        mutableStateOf(
            if (buttonToEdit?.macroType == MacroType.MULTI_PROGRAM) {
                try {
                    val array = JSONArray(buttonToEdit.extraValuesJson)
                    val list = mutableListOf<String>()
                    for (i in 0 until array.length()) list.add(array.getString(i))
                    list.joinToString("\n")
                } catch (e: Exception) {
                    buttonToEdit.primaryValue
                }
            } else ""
        )
    }

    // Shortcut Modifiers helper
    var ctrlChecked by remember { mutableStateOf(primaryValue.contains("CTRL", ignoreCase = true)) }
    var altChecked by remember { mutableStateOf(primaryValue.contains("ALT", ignoreCase = true)) }
    var shiftChecked by remember { mutableStateOf(primaryValue.contains("SHIFT", ignoreCase = true)) }
    var winChecked by remember { mutableStateOf(primaryValue.contains("WIN", ignoreCase = true)) }

    var selectedColorPreset by remember {
        mutableStateOf(
            COLOR_PRESETS.find { it.startHex.equals(buttonToEdit?.gradientStartHex, ignoreCase = true) }
                ?: COLOR_PRESETS[0]
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (buttonToEdit == null) "Yeni Tuş Ekle" else "Tuş Düzenle",
                        color = PhoenixGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: TUŞLAR, GENEL, GÖRÜNÜM
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceVariantDark,
                    contentColor = PhoenixGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PhoenixGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "ISLEM",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "GENEL",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "GORUNUM",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> { // ISLEM (Macro Action & Type)
                        Text(
                            text = "İşlem Türü",
                            color = PhoenixAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        MacroType.entries.filter { it != MacroType.WIDGET }.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (macroType == type) Color(0xFF33180B) else SurfaceVariantDark)
                                    .border(
                                        width = 1.dp,
                                        color = if (macroType == type) PhoenixGold else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { macroType = type }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.displayName,
                                        color = if (macroType == type) PhoenixGold else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = type.description,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (macroType) {
                            MacroType.WIDGET -> {
                                Text(
                                    text = "Uzantı Widget'ı",
                                    color = PhoenixAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Bu öge bir uzantı widget'ıdır. Widget ayarlarını ve kontrollerini 'Uzantılar' sekmesinden yapılandırabilirsiniz.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                            MacroType.KEY -> {
                                Text(
                                    text = "Basılacak Tuş",
                                    color = PhoenixAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = primaryValue,
                                    onValueChange = { primaryValue = it },
                                    placeholder = { Text("Ör. Enter, F5, H, Space, Tab") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("key_input_field"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PhoenixGold,
                                        unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Hızlı Seçim:",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    items(listOf("Enter", "Escape", "Space", "Tab", "F5", "F11", "Backspace", "Delete")) { key ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceVariantDark)
                                                .border(1.dp, PhoenixAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    primaryValue = key
                                                    if (title.isBlank()) title = key
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = key, color = PhoenixGold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            MacroType.SHORTCUT -> {
                                Text(
                                    text = "Kısayol Kombinasyonu",
                                    color = PhoenixAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Modifiers Checkboxes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = ctrlChecked,
                                            onCheckedChange = { ctrlChecked = it },
                                            colors = CheckboxDefaults.colors(checkedColor = PhoenixGold)
                                        )
                                        Text("CTRL", color = Color.White, fontSize = 13.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = altChecked,
                                            onCheckedChange = { altChecked = it },
                                            colors = CheckboxDefaults.colors(checkedColor = PhoenixGold)
                                        )
                                        Text("ALT", color = Color.White, fontSize = 13.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = shiftChecked,
                                            onCheckedChange = { shiftChecked = it },
                                            colors = CheckboxDefaults.colors(checkedColor = PhoenixGold)
                                        )
                                        Text("SHIFT", color = Color.White, fontSize = 13.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = winChecked,
                                            onCheckedChange = { winChecked = it },
                                            colors = CheckboxDefaults.colors(checkedColor = PhoenixGold)
                                        )
                                        Text("WIN", color = Color.White, fontSize = 13.sp)
                                    }
                                }

                                OutlinedTextField(
                                    value = primaryValue,
                                    onValueChange = { primaryValue = it },
                                    placeholder = { Text("Ör. CTRL+C, ALT+TAB, WIN+SHIFT+S") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("shortcut_input_field"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PhoenixGold,
                                        unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Helper button to assemble from checked boxes
                                Button(
                                    onClick = {
                                        val parts = mutableListOf<String>()
                                        if (ctrlChecked) parts.add("CTRL")
                                        if (altChecked) parts.add("ALT")
                                        if (shiftChecked) parts.add("SHIFT")
                                        if (winChecked) parts.add("WIN")

                                        // Extract base key from primaryValue if present
                                        val currentBaseKey = primaryValue.split("+").lastOrNull {
                                            !listOf("CTRL", "ALT", "SHIFT", "WIN").contains(it.uppercase().trim())
                                        }?.trim() ?: "C"

                                        parts.add(currentBaseKey)
                                        primaryValue = parts.joinToString("+")
                                        if (title.isBlank()) title = primaryValue
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Seçili Değiştiricilerle Kısayolu Güncelle", color = PhoenixGold, fontSize = 12.sp)
                                }
                            }

                            MacroType.PROGRAM -> {
                                Text(
                                    text = "Program Yolu / Komut",
                                    color = PhoenixAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = primaryValue,
                                    onValueChange = {
                                        primaryValue = it
                                        if (title.isBlank() && it.isNotBlank()) {
                                            title = it.substringAfterLast("\\").substringAfterLast("/").substringBefore(".")
                                        }
                                    },
                                    placeholder = { Text("Ör. C:\\Windows\\explorer.exe veya notepad.exe") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("program_input_field"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PhoenixGold,
                                        unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Hızlı Örnekler:",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    items(listOf(
                                        "explorer.exe" to "Dosya Gezgini",
                                        "notepad.exe" to "Not Defteri",
                                        "calc.exe" to "Hesap Makinesi",
                                        "chrome.exe" to "Chrome",
                                        "spotify.exe" to "Spotify"
                                    )) { (path, label) ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceVariantDark)
                                                .border(1.dp, PhoenixAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    primaryValue = path
                                                    title = label
                                                    iconName = "folder"
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = label, color = PhoenixGold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            MacroType.MULTI_PROGRAM -> {
                                Text(
                                    text = "Açılacak Program Yolları (Her satıra bir program)",
                                    color = PhoenixAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = multiPathsText,
                                    onValueChange = { multiPathsText = it },
                                    placeholder = { Text("chrome.exe\nspotify.exe\nnotepad.exe") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("multi_program_input_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PhoenixGold,
                                        unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }

                            MacroType.EXTENSION_ACTION -> {
                                Text(
                                    text = "Uzantı ve İşlem Seçimi",
                                    color = PhoenixAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val enabledExtensions = installedExtensions.filter { it.isEnabled }
                                if (enabledExtensions.isEmpty()) {
                                    Text(
                                        text = "Yüklü ve etkinleştirilmiş uzantı bulunamadı.\nLütfen 'Uzantılar' sekmesinden uzantı ekleyin veya aktifleştirin.",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                } else {
                                    enabledExtensions.forEach { ext ->
                                        val actions = remember(ext.macroPresetsJson) {
                                            val list = mutableListOf<ExtensionAction>()
                                            try {
                                                val array = JSONArray(ext.macroPresetsJson)
                                                for (i in 0 until array.length()) {
                                                    val obj = array.getJSONObject(i)
                                                    list.add(
                                                        ExtensionAction(
                                                            id = obj.optString("id", ""),
                                                            name = obj.optString("name", "Aksiyon"),
                                                            icon = obj.optString("icon", "extension"),
                                                            description = obj.optString("description", ""),
                                                            type = obj.optString("type", "SHORTCUT"),
                                                            value = obj.optString("value", "")
                                                        )
                                                    )
                                                }
                                            } catch (e: Exception) {}
                                            list
                                        }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .border(1.dp, PhoenixCardBorder, RoundedCornerShape(12.dp)),
                                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = ext.name,
                                                    color = PhoenixGold,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                actions.forEach { act ->
                                                    val isSelected = primaryValue.startsWith("${ext.id}:${act.id}") || primaryValue == act.value
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 3.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) Color(0xFF3D1D0D) else SurfaceDark)
                                                            .border(
                                                                width = if (isSelected) 1.dp else 0.dp,
                                                                color = if (isSelected) PhoenixGold else Color.Transparent,
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable {
                                                                primaryValue = "${ext.id}:${act.id}:${act.type}:${act.value}"
                                                                if (title.isBlank()) title = act.name
                                                                subtext = ext.name
                                                                iconName = if (act.icon.isNotBlank()) act.icon else ext.iconName
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = act.name,
                                                                color = if (isSelected) PhoenixGold else Color.White,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            if (act.description.isNotBlank()) {
                                                                Text(
                                                                    text = act.description,
                                                                    color = Color.White.copy(alpha = 0.5f),
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                        if (isSelected) {
                                                            Text("✓ Seçildi", color = PhoenixGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

                    1 -> { // GENEL (Title, Subtext, Icon)
                        Text(
                            text = "Buton Adı (Başlık)",
                            color = PhoenixAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Ör. CTRL + C, Yenile, Mikrofon") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_title_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhoenixGold,
                                unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Açıklama (Alt Yazı)",
                            color = PhoenixAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = subtext,
                            onValueChange = { subtext = it },
                            placeholder = { Text("Ör. Kopyala, Görev Değiştir") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_subtext_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhoenixGold,
                                unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "İkon Seçimi",
                            color = PhoenixAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(IconMapper.AVAILABLE_ICONS) { (iconKey, label) ->
                                val isSelected = iconName == iconKey
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) PhoenixAmber else SurfaceVariantDark)
                                        .clickable { iconName = iconKey }
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = IconMapper.getIconVector(iconKey),
                                        contentDescription = label,
                                        tint = if (isSelected) Color.Black else PhoenixGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    2 -> { // GORUNUM (Color preset & Style)
                        Text(
                            text = "Renk Teması & Renk Geçişi",
                            color = PhoenixAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            COLOR_PRESETS.forEach { preset ->
                                val isSelected = selectedColorPreset.name == preset.name
                                val startColor = try { Color(android.graphics.Color.parseColor(preset.startHex)) } catch (e: Exception) { PhoenixCardBorder }
                                val endColor = try { Color(android.graphics.Color.parseColor(preset.endHex)) } catch (e: Exception) { PhoenixCardBorder }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceVariantDark)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) PhoenixGold else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedColorPreset = preset }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                                        listOf(startColor, endColor)
                                                    )
                                                )
                                                .border(1.dp, Color.White, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = preset.name,
                                            color = if (isSelected) PhoenixGold else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Text("✓ Seçildi", color = PhoenixGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons (Save & Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (buttonToEdit != null && onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete(buttonToEdit.id)
                                onDismiss()
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PhoenixFlameRed.copy(alpha = 0.2f))
                                .border(1.dp, PhoenixFlameRed, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Butonu Sil",
                                tint = PhoenixFlameRed
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Button(
                        onClick = {
                            val finalPrimary = primaryValue.ifBlank {
                                when (macroType) {
                                    MacroType.KEY -> "Enter"
                                    MacroType.SHORTCUT -> "CTRL+C"
                                    MacroType.PROGRAM -> "explorer.exe"
                                    else -> ""
                                }
                            }
                            val finalTitle = title.ifBlank { finalPrimary.ifBlank { "Buton" } }
                            val extraJson = when (macroType) {
                                MacroType.MULTI_PROGRAM -> {
                                    val lines = multiPathsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    JSONArray(lines).toString()
                                }
                                MacroType.WIDGET -> {
                                    buttonToEdit?.extraValuesJson ?: "{}"
                                }
                                else -> "[]"
                            }

                            val newOrUpdated = MacroButtonEntity(
                                id = buttonToEdit?.id ?: 0,
                                profileId = profileId,
                                title = finalTitle,
                                subtext = subtext,
                                iconName = iconName,
                                macroType = macroType,
                                primaryValue = finalPrimary,
                                extraValuesJson = extraJson,
                                sizeSpan = buttonToEdit?.sizeSpan ?: 1,
                                colorHex = selectedColorPreset.startHex,
                                gradientStartHex = selectedColorPreset.startHex,
                                gradientEndHex = selectedColorPreset.endHex,
                                borderColorHex = selectedColorPreset.borderHex,
                                orderIndex = buttonToEdit?.orderIndex ?: 99
                            )
                            onSave(newOrUpdated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PhoenixGold,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_macro_button")
                    ) {
                        Text(
                            text = "Kaydet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
