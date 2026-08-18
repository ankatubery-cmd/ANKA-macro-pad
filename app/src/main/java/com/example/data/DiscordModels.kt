package com.example.data

enum class AccountStatus(val label: String) {
    UNCONNECTED("Hesapsız Kullanım"),
    CONNECTED("Discord Bağlı"),
    DISCONNECTED("Discord Bağlantısı Kesildi")
}

data class DiscordAccount(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val discriminator: String = "0",
    val avatarUrl: String? = null,
    val avatarType: String = "phoenix",
    val connectedAtMillis: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true
)

enum class DiscordChannel(val channelName: String, val description: String) {
    ALL("#tüm-kanallar", "Tüm topluluk başlıkları"),
    EXTENSION_SHARES("#extension-paylaşımı", "Kullanıcılar ve geliştiriciler tarafından oluşturulan yeni extension'lar"),
    UPDATES("#extension-güncellemeleri", "Extension güncellemeleri ve sürüm duyuruları"),
    DEV_COMMUNITY("#geliştirici-topluluğu", "Extension geliştiricileri için teknik tartışmalar ve SDK desteği"),
    SUPPORT("#extension-destek", "Extension kurulum ve kullanım destek kanalı"),
    BUG_REPORTS("#hata-bildirimleri", "Bulunan hatalar ve düzeltme bildirimleri"),
    SUGGESTIONS("#extension-önerileri", "Yeni extension fikirleri ve topluluk istekleri")
}

data class DiscordCommunityThread(
    val threadId: String,
    val title: String,
    val channel: DiscordChannel,
    val authorName: String,
    val authorAvatar: String = "phoenix",
    val replyCount: Int = 12,
    val updatedAt: String = "Bugün 14:30",
    val extension: ExtensionEntity
)
