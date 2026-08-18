package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.MacroButtonEntity
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBgEnd
import com.example.ui.theme.PhoenixCardBgStart
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold

@Composable
fun MacroTile(
    button: MacroButtonEntity,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val dynamicSubtext = button.subtext

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "TileScale"
    )

    val customStartColor = try {
        Color(android.graphics.Color.parseColor(button.gradientStartHex))
    } catch (e: Exception) {
        PhoenixCardBgStart
    }

    val customEndColor = try {
        Color(android.graphics.Color.parseColor(button.gradientEndHex))
    } catch (e: Exception) {
        PhoenixCardBgEnd
    }

    val customBorderColor = try {
        Color(android.graphics.Color.parseColor(button.borderColorHex))
    } catch (e: Exception) {
        PhoenixCardBorder
    }

    val defaultModifier = if (modifier == Modifier) Modifier.fillMaxWidth().height(115.dp) else modifier

    Box(
        modifier = defaultModifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        customStartColor,
                        customEndColor
                    )
                )
            )
            .border(
                width = if (isPressed) 2.5.dp else 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        customBorderColor,
                        customBorderColor.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(isEditMode) {
                detectTapGestures(
                    onPress = {
                        if (!isEditMode) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (isEditMode) {
                            onEdit()
                        } else {
                            onClick()
                        }
                    },
                    onLongPress = {
                        onEdit()
                    }
                )
            }
            .padding(10.dp)
            .testTag("macro_tile_${button.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            val iconVector = IconMapper.getIconVector(button.iconName)
            Icon(
                imageVector = iconVector,
                contentDescription = button.title,
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Title
            Text(
                text = button.title,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Subtext (if present)
            if (dynamicSubtext.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dynamicSubtext,
                    color = Color.Black.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Edit Mode overlay buttons
        if (isEditMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(PhoenixAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.size(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(PhoenixFlameRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddButtonTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF331405),
                        Color(0xFF200B03)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = PhoenixCardBorder.copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("add_macro_button_tile"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PhoenixFlameRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Yeni Tuş Ekle",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.new_button),
                color = PhoenixGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
