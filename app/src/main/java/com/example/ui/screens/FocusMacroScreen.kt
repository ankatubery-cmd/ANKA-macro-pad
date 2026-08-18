package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MacroButtonEntity
import com.example.ui.components.MacroTile
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixDarkBackground
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark
import kotlin.math.ceil
import kotlin.math.sqrt

@Composable
fun FocusMacroScreen(
    buttons: List<MacroButtonEntity>,
    onExecuteMacro: (MacroButtonEntity) -> Unit,
    onExitFocusMode: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        color = PhoenixDarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Floating Top Header with Exit Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Odak Modu (${buttons.size} Makro)",
                    color = PhoenixGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, PhoenixAmber, RoundedCornerShape(12.dp))
                        .clickable { onExitFocusMode() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("exit_focus_mode_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Çıkış",
                            tint = PhoenixGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Çıkış",
                            color = PhoenixGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (buttons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu profilde makro tuşu bulunmuyor.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val total = buttons.size
                val (cols, rows) = when {
                    total <= 1 -> Pair(1, 1)
                    total == 2 -> Pair(2, 1)
                    total in 3..4 -> Pair(2, 2)
                    total in 5..6 -> Pair(3, 2)
                    total in 7..9 -> Pair(3, 3)
                    total in 10..12 -> Pair(3, 4)
                    total in 13..16 -> Pair(4, 4)
                    else -> {
                        val c = ceil(sqrt(total.toDouble())).toInt()
                        val r = ceil(total.toDouble() / c).toInt()
                        Pair(c, r)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until cols) {
                                val index = r * cols + c
                                if (index < total) {
                                    val button = buttons[index]
                                    MacroTile(
                                        button = button,
                                        isEditMode = false,
                                        onClick = { onExecuteMacro(button) },
                                        onEdit = {},
                                        onDelete = {},
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
