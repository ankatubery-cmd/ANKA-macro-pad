package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.MacroButtonEntity
import com.example.data.MacroType
import com.example.data.ProfileEntity
import com.example.data.WidgetMiniAction
import com.example.network.ConnectionStatus
import com.example.ui.components.AddButtonTile
import com.example.ui.components.MacroTile
import com.example.ui.components.MacroWidgetTile
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixDarkBackground
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark

import androidx.compose.ui.res.stringResource
import com.example.R


@Composable
private fun MacroMasonryGrid(
    buttons: List<MacroButtonEntity>,
    onAddMacro: () -> Unit,
    isEditMode: Boolean,
    onExecuteMacro: (MacroButtonEntity) -> Unit,
    onExecuteWidgetMiniAction: (MacroButtonEntity, WidgetMiniAction) -> Unit,
    onExtensionSendShortcut: (String) -> Unit,
    onExtensionOpenApp: (String) -> Unit,
    onExtensionNotification: (String) -> Unit,
    onExtensionGetProfiles: () -> String,
    onEditMacro: (MacroButtonEntity) -> Unit,
    onDeleteMacro: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
            .verticalScroll(scrollState)
            .testTag("macro_buttons_grid"),
        content = {
            buttons.forEach { button ->
                if (button.macroType == MacroType.WIDGET) {
                    MacroWidgetTile(
                        button = button,
                        isEditMode = isEditMode,
                        onEdit = { onEditMacro(button) },
                        onDelete = { onDeleteMacro(button.id) },
                        onMiniActionClick = { miniAction ->
                            onExecuteWidgetMiniAction(button, miniAction)
                        },
                        onExtensionSendShortcut = onExtensionSendShortcut,
                        onExtensionOpenApp = onExtensionOpenApp,
                        onExtensionNotification = onExtensionNotification,
                        onExtensionGetProfiles = onExtensionGetProfiles
                    )
                } else {
                    MacroTile(
                        button = button,
                        isEditMode = isEditMode,
                        onClick = { onExecuteMacro(button) },
                        onEdit = { onEditMacro(button) },
                        onDelete = { onDeleteMacro(button.id) }
                    )
                }
            }

            AddButtonTile(onClick = onAddMacro)
        }
    ) { measurables, constraints ->
        val density = this
        val gap = 10.dp.toPx()
        val availableWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth.toFloat()
        } else {
            constraints.minWidth.toFloat()
        }
        val cellWidth = ((availableWidth - gap * 2f) / 3f).coerceAtLeast(1f)

        // Current bottom edge of each of the three columns.
        val columnBottoms = FloatArray(3)
        val placements = ArrayList<Triple<Int, Int, Placeable>>(measurables.size)

        measurables.forEachIndexed { index, measurable ->
            val span = if (index < buttons.size) {
                buttons[index].sizeSpan.coerceIn(1, 3)
            } else {
                1 // + Yeni Tuş
            }

            val childWidth = cellWidth * span + gap * (span - 1)
            val childConstraints = constraints.copy(
                minWidth = childWidth.toInt(),
                maxWidth = childWidth.toInt(),
                minHeight = 0,
                maxHeight = Constraints.Infinity
            )
            val placeable = measurable.measure(childConstraints)

            var bestColumn = 0
            var bestY = Float.MAX_VALUE

            if (span == 1) {
                for (column in 0..2) {
                    if (columnBottoms[column] < bestY) {
                        bestY = columnBottoms[column]
                        bestColumn = column
                    }
                }
            } else if (span == 2) {
                // Pick the adjacent pair that becomes available first.
                val pair01 = maxOf(columnBottoms[0], columnBottoms[1])
                val pair12 = maxOf(columnBottoms[1], columnBottoms[2])
                if (pair12 < pair01) {
                    bestColumn = 1
                    bestY = pair12
                } else {
                    bestColumn = 0
                    bestY = pair01
                }
            } else {
                bestColumn = 0
                bestY = maxOf(columnBottoms[0], columnBottoms[1], columnBottoms[2])
            }

            placements += Triple(bestColumn, bestY.toInt(), placeable)

            val newBottom = bestY + placeable.height
            for (column in bestColumn until (bestColumn + span).coerceAtMost(3)) {
                columnBottoms[column] = newBottom
            }
        }

        val contentHeight = maxOf(
            constraints.minHeight.toFloat(),
            columnBottoms.maxOrNull() ?: 0f
        ).toInt()

        layout(availableWidth.toInt(), contentHeight) {
            placements.forEach { (column, y, placeable) ->
                val x = (column * (cellWidth + gap)).toInt()
                placeable.placeRelative(x, y)
            }
        }
    }
}

@Composable
fun PanelScreen(
    connectionStatus: ConnectionStatus,
    targetIp: String,
    targetPort: Int,
    isEditMode: Boolean,
    allProfiles: List<ProfileEntity>,
    selectedProfile: ProfileEntity?,
    buttons: List<MacroButtonEntity>,
    onSelectProfile: (Int) -> Unit,
    onOpenCreateProfile: () -> Unit,
    onExecuteMacro: (MacroButtonEntity) -> Unit,
    onExecuteWidgetMiniAction: (MacroButtonEntity, WidgetMiniAction) -> Unit = { _, _ -> },
    onExtensionSendShortcut: (String) -> Unit = {},
    onExtensionOpenApp: (String) -> Unit = {},
    onExtensionNotification: (String) -> Unit = {},
    onExtensionGetProfiles: () -> String = { "[]" },
    onEditMacro: (MacroButtonEntity) -> Unit,
    onDeleteMacro: (Int) -> Unit,
    onAddMacro: () -> Unit,
    onConnectManual: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleFocusMode: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhoenixDarkBackground)
    ) {
        // Profile Selector Dropdown Area
        var isProfileMenuExpanded by remember { mutableStateOf(false) }

        if (allProfiles.isNotEmpty()) {
            val activeProfile = selectedProfile ?: allProfiles.firstOrNull()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF3D1D0D))
                        .border(1.5.dp, PhoenixGold, RoundedCornerShape(16.dp))
                        .clickable { isProfileMenuExpanded = !isProfileMenuExpanded }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeProfile?.name ?: stringResource(R.string.select_profile),
                            color = PhoenixGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Profil Menüsü",
                            tint = PhoenixGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isProfileMenuExpanded,
                    onDismissRequest = { isProfileMenuExpanded = false },
                    modifier = Modifier
                        .background(SurfaceDark)
                        .border(1.dp, PhoenixGold, RoundedCornerShape(12.dp))
                ) {
                    allProfiles.forEach { profile ->
                        val isSelected = selectedProfile?.id == profile.id
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (isSelected) PhoenixGold else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = profile.name,
                                        color = if (isSelected) PhoenixGold else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            onClick = {
                                onSelectProfile(profile.id)
                                isProfileMenuExpanded = false
                            },
                            modifier = Modifier.background(
                                if (isSelected) Color(0xFF3D1D0D) else Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // Edit Mode Indicator Bar
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PhoenixCardBorder.copy(alpha = 0.2f))
                    .border(1.dp, PhoenixGold, RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.edit_mode_active),
                    color = PhoenixGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Macro Buttons & Widgets: masonry-style layout.
        // Each single tile uses the shortest column, so short buttons never leave
        // a large vertical hole beside a taller multi-column widget.
        MacroMasonryGrid(
            buttons = buttons,
            onAddMacro = onAddMacro,
            isEditMode = isEditMode,
            onExecuteMacro = onExecuteMacro,
            onExecuteWidgetMiniAction = onExecuteWidgetMiniAction,
            onExtensionSendShortcut = onExtensionSendShortcut,
            onExtensionOpenApp = onExtensionOpenApp,
            onExtensionNotification = onExtensionNotification,
            onExtensionGetProfiles = onExtensionGetProfiles,
            onEditMacro = onEditMacro,
            onDeleteMacro = onDeleteMacro
        )
    }
}
