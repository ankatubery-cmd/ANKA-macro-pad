package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AccountStatus
import com.example.data.DiscordAccount
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiscordAccountDialog(
    accountStatus: AccountStatus,
    discordAccount: DiscordAccount?,
    onConnect: (String, String) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    var usernameInput by remember { mutableStateOf(discordAccount?.username ?: "") }
    var displayNameInput by remember { mutableStateOf(discordAccount?.displayName ?: "ANKA Master") }

    val discordBlurple = Color(0xFF5865F2)

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = discordBlurple,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Discord Hesap Yönetimi",
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

                // Account Status Badge Box
                val badgeBg = when (accountStatus) {
                    AccountStatus.CONNECTED -> StatusGreen.copy(alpha = 0.15f)
                    AccountStatus.DISCONNECTED -> PhoenixFlameRed.copy(alpha = 0.15f)
                    AccountStatus.UNCONNECTED -> Color(0xFF2A1C12)
                }
                val badgeBorder = when (accountStatus) {
                    AccountStatus.CONNECTED -> StatusGreen
                    AccountStatus.DISCONNECTED -> PhoenixFlameRed
                    AccountStatus.UNCONNECTED -> PhoenixAmber
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(badgeBg)
                        .border(1.dp, badgeBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(badgeBorder)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hesap Durumu: ",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Text(
                                text = accountStatus.label,
                                color = badgeBorder,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (accountStatus == AccountStatus.UNCONNECTED) {
                            Text(
                                text = "Hesapsız kullanım modundasınız. Makro butonlar, profiller ve PC bağlantısı kısıtlanmadan çalışır. Topluluk eklentilerine erişim için Discord hesabınızı bağlayabilirsiniz.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        } else if (accountStatus == AccountStatus.CONNECTED) {
                            Text(
                                text = "Discord hesabınız başarıyla bağlandı. Topluluk extension mağazasını ve online ekosistem özelliklerini tam yetkiyle kullanabilirsiniz.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "Discord hesabınızın bağlantısı kesildi. Yeniden bağlanmak için aşağıdaki butonu kullanabilirsiniz.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (accountStatus == AccountStatus.CONNECTED && discordAccount != null) {
                    // Connected User Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceVariantDark)
                            .border(1.dp, discordBlurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(discordBlurple)
                                        .border(2.dp, PhoenixGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = discordAccount.displayName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "@${discordAccount.username}#${discordAccount.discriminator}",
                                        color = discordBlurple,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                        .format(Date(discordAccount.connectedAtMillis))
                                    Text(
                                        text = "Bağlandı: $dateStr",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Granted Scopes
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF141221))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "🛡️ İzin Verilen OAuth2 Kapsamları:",
                                        color = PhoenixAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("identify", "guilds.members.read", "connections").forEach { scope ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(discordBlurple.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(scope, color = discordBlurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Disconnect Button
                            Button(
                                onClick = onDisconnect,
                                colors = ButtonDefaults.buttonColors(containerColor = PhoenixFlameRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("discord_disconnect_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Discord Hesabını Kopar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Connect OAuth Form
                    Text(
                        text = "Discord Kullanıcı Adınız",
                        color = PhoenixAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        placeholder = { Text("Ör. AnkaGamer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("discord_username_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = discordBlurple,
                            unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Görünen Ad (Profil Başlığı)",
                        color = PhoenixAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = displayNameInput,
                        onValueChange = { displayNameInput = it },
                        placeholder = { Text("Ör. ANKA Phoenix Master") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("discord_displayname_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = discordBlurple,
                            unfocusedBorderColor = PhoenixAmber.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Security Note Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B172B))
                            .border(1.dp, discordBlurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = discordBlurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Güvenli OAuth2 Kimlik Doğrulama",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Discord şifreniz asla istenmez veya cihazınızda saklanmaz. Bağlantı resmi Discord OAuth2 standartı üzerinden güvenli bir şekilde sağlanır.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Connect Button
                    Button(
                        onClick = {
                            onConnect(usernameInput.trim(), displayNameInput.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = discordBlurple, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("discord_connect_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Discord İle Bağlan (OAuth2)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
