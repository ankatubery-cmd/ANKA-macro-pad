package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ProfileEntity
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark

@Composable
fun ProfileManagerDialog(
    profiles: List<ProfileEntity>,
    selectedProfile: ProfileEntity?,
    onSelectProfile: (Int) -> Unit,
    onCreateProfile: (String) -> Unit,
    onUpdateProfile: (ProfileEntity) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var newProfileName by remember { mutableStateOf("") }
    var editingProfileId by remember { mutableStateOf<Int?>(null) }
    var editingProfileName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .systemBarsPadding()
                .imePadding()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, PhoenixCardBorder, RoundedCornerShape(24.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profil Yönetimi",
                            color = PhoenixGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles) { profile ->
                        val isSelected = selectedProfile?.id == profile.id
                        val isEditing = editingProfileId == profile.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF3B1C0B) else SurfaceVariantDark)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) PhoenixGold else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (!isEditing) onSelectProfile(profile.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editingProfileName,
                                    onValueChange = { editingProfileName = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PhoenixGold,
                                        unfocusedBorderColor = PhoenixAmber,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (editingProfileName.isNotBlank()) {
                                            onUpdateProfile(profile.copy(name = editingProfileName.trim()))
                                        }
                                        editingProfileId = null
                                    }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Kaydet", tint = PhoenixGold)
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seçili",
                                            tint = PhoenixGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Text(
                                        text = profile.name,
                                        color = if (isSelected) PhoenixGold else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            editingProfileId = profile.id
                                            editingProfileName = profile.name
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Düzenle",
                                            tint = PhoenixAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (profiles.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteProfile(profile) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Sil",
                                                tint = PhoenixFlameRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create New Profile
                Text(
                    text = "Yeni Profil Oluştur",
                    color = PhoenixAmber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        placeholder = { Text("Profil adı (ör. Yayıncılık, Discord)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_profile_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixGold,
                            unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newProfileName.isNotBlank()) {
                                onCreateProfile(newProfileName.trim())
                                newProfileName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhoenixGold, contentColor = Color.Black),
                        modifier = Modifier.testTag("create_profile_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ekle")
                    }
                }
            }
        }
    }
}
