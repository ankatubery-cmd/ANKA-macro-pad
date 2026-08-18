package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
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
fun DiscordProfileScreen(
    accountStatus: AccountStatus,
    discordAccount: DiscordAccount?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    var showAuthDialog by remember { mutableStateOf(false) }

    val discordBlurple = Color(0xFF5865F2)

    val infiniteTransition = rememberInfiniteTransition(label = "AvatarGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val isConnected = accountStatus == AccountStatus.CONNECTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Section: Glowing Discord Avatar Frame
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            // Neon Glow Background
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isConnected) discordBlurple.copy(alpha = 0.45f) else PhoenixFlameRed.copy(alpha = 0.25f),
                                if (isConnected) PhoenixGold.copy(alpha = 0.25f) else Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
            )

            // Outer Border Ring
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = if (isConnected) {
                            Brush.sweepGradient(
                                colors = listOf(
                                    discordBlurple,
                                    PhoenixGold,
                                    PhoenixAmber,
                                    discordBlurple
                                )
                            )
                        } else {
                            Brush.linearGradient(listOf(PhoenixCardBorder, Color.Gray))
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Actual Avatar Image
                if (isConnected && !discordAccount?.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = discordAccount?.avatarUrl,
                        contentDescription = "Discord Profile Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.phoenix_logo_1786449649730),
                        contentDescription = "Default Avatar",
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                    )
                }
            }

            // Online Active Status Indicator Dot
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(if (isConnected) StatusGreen else PhoenixFlameRed)
                    .border(2.5.dp, SurfaceDark, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display Name & Username
        Text(
            text = if (isConnected) (discordAccount?.displayName ?: "ANKA Macro Pad Üyesi") else "Discord Hesabı Bağlanmadı",
            color = if (isConnected) PhoenixGold else Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = if (isConnected) "@${discordAccount?.username ?: "Discord User"}#${discordAccount?.discriminator ?: "0"}" else "Makro pad özelliklerini etkinleştirmek için yetkilendirin",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Status Badge Chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isConnected) Color(0xFF1E3A2B) else Color(0xFF331B10))
                .border(
                    width = 1.dp,
                    color = if (isConnected) StatusGreen else PhoenixFlameRed,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isConnected) StatusGreen else PhoenixFlameRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isConnected) "Discord Hesabı Bağlı" else "Bağlantı Yok",
                    color = if (isConnected) StatusGreen else PhoenixFlameRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Connection Action Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PhoenixCardBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Discord Yetkilendirme & Profil",
                    color = PhoenixGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isConnected)
                        "ANKA Macro Pad uygulamanız resmi Discord hesabınız ile ilişkilendirildi. Extension mağazası ve topluluk özelliklerine tam erişiminiz mevcuttur."
                    else
                        "Extension mağazası ve topluluk makro paylaşımlarını kullanabilmek için resmi Discord yetkilendirme ekranından hesabınızı bağlayın.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isConnected) {
                    Button(
                        onClick = { showAuthDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = discordBlurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profil Bilgilerini Güncelle / Yeniden Yetkilendir",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            onDisconnect()
                            Toast.makeText(context, "Discord hesabı bağlantısı kesildi.", Toast.LENGTH_SHORT).show()
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, PhoenixFlameRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = PhoenixFlameRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hesap Bağlantısını Kes",
                            color = PhoenixFlameRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { showAuthDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = discordBlurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Discord'a Bağlan",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isConnected && discordAccount != null) {
            val dateStr = remember(discordAccount.connectedAtMillis) {
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("tr")).format(Date(discordAccount.connectedAtMillis))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF231B15))
                    .border(1.dp, PhoenixCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Doğrulanmış ANKA Topluluk Kimliği",
                        color = PhoenixAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bağlantı Tarihi: $dateStr",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Topluluk Rolü: Uzantı & Makro Pad Kullanıcısı",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Official Discord OAuth2 Authorization Dialog
    if (showAuthDialog) {
        DiscordAuthDialog(
            currentAccount = discordAccount,
            onDismiss = { showAuthDialog = false },
            onAuthorize = {
                onConnect()
                showAuthDialog = false
                Toast.makeText(context, "Discord yetkilendirme ekranı açılıyor...", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DiscordAuthDialog(
    currentAccount: DiscordAccount?,
    onDismiss: () -> Unit,
    onAuthorize: () -> Unit
) {
    val discordBlurple = Color(0xFF5865F2)
    val discordDarkBg = Color(0xFF2B2D31)
    val discordCardBg = Color(0xFF1E1F22)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, discordBlurple.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = discordDarkBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo & Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(discordBlurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Discord Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Discord İzin İsteği",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connection Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(discordCardBg)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.phoenix_logo_1786449649730),
                            contentDescription = "ANKA App Logo",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "ANKA Macro Pad",
                                color = PhoenixGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Discord Hesabınıza Erişmek İstiyor",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Permissions List
                Text(
                    text = "UYGULAMAYA VERİLECEK İZİNLER:",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(discordCardBg)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PermissionRow(
                        icon = Icons.Default.Person,
                        title = "Profil ve Kimlik (identify)",
                        subtitle = "Kullanıcı adı, avatar ve benzersiz Discord ID bilgisi"
                    )

                    PermissionRow(
                        icon = Icons.Default.Email,
                        title = "E-posta Doğrulaması (email)",
                        subtitle = "Hesap güvenliği ve topluluk doğrulama durumu"
                    )

                    PermissionRow(
                        icon = Icons.Default.Group,
                        title = "ANKA Topluluk Sunucusu (guilds)",
                        subtitle = "Extension mağazası ve makro senkronizasyonu"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Discord itself handles account selection, profile data, and consent.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(discordCardBg)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Devam ettiğinizde Discord'un resmi giriş/yetkilendirme ekranı açılır. Kullanıcı adı, görünen ad ve avatar bilgileriniz Discord hesabınızdan otomatik alınır.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onAuthorize,
                    colors = ButtonDefaults.buttonColors(containerColor = discordBlurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Yetkilendir",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "İptal Et",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF5865F2),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}
