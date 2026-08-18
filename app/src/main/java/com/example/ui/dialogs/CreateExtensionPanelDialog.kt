package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExtensionEntity
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark

@Composable
fun CreateExtensionPanelDialog(
    extension: ExtensionEntity,
    onCreatePanel: (panelName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var panelName by remember { mutableStateOf(extension.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dashboard, null, tint = PhoenixGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "${extension.name} - Panel Oluştur",
                    color = PhoenixGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Uzantının kontrollerini içeren yeni bir panel oluşturulacak.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = panelName,
                    onValueChange = { panelName = it },
                    label = { Text("Panel adı", color = PhoenixGold) },
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
        },
        confirmButton = {
            Button(
                onClick = { onCreatePanel(panelName.ifBlank { extension.name }) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhoenixGold,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Dashboard, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Paneli Oluştur", fontWeight = FontWeight.Bold)
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
