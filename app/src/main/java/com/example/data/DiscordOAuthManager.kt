package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class DiscordOAuthManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("discord_oauth", Context.MODE_PRIVATE)
    private val client = OkHttpClient()

    fun beginAuthorization(): Result<Unit> {
        if (DiscordOAuthConfig.CLIENT_ID == "YOUR_DISCORD_APPLICATION_ID") {
            return Result.failure(IllegalStateException("Discord Application ID ayarlanmamış."))
        }

        val verifier = randomUrlSafe(64)
        val challenge = base64Url(sha256(verifier))
        val state = UUID.randomUUID().toString()

        prefs.edit()
            .putString("code_verifier", verifier)
            .putString("state", state)
            .apply()

        val uri = Uri.parse(DiscordOAuthConfig.AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", DiscordOAuthConfig.CLIENT_ID)
            .appendQueryParameter("scope", DiscordOAuthConfig.SCOPES)
            .appendQueryParameter("state", state)
            .appendQueryParameter("redirect_uri", DiscordOAuthConfig.REDIRECT_URI)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()

        return try {
            appContext.startActivity(
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun handleCallback(uri: Uri): Result<DiscordAccount> = withContext(Dispatchers.IO) {
        val expectedState = prefs.getString("state", null)
            ?: return@withContext Result.failure(IllegalStateException("OAuth state bulunamadı."))
        val returnedState = uri.getQueryParameter("state")
        if (returnedState != expectedState) {
            clearPendingAuth()
            return@withContext Result.failure(IllegalStateException("OAuth state doğrulaması başarısız."))
        }

        val error = uri.getQueryParameter("error")
        if (error != null) {
            val description = uri.getQueryParameter("error_description") ?: error
            clearPendingAuth()
            return@withContext Result.failure(IllegalStateException("Discord yetkilendirmesi reddedildi: $description"))
        }

        val code = uri.getQueryParameter("code")
            ?: return@withContext Result.failure(IllegalStateException("OAuth authorization code bulunamadı."))
        val verifier = prefs.getString("code_verifier", null)
            ?: return@withContext Result.failure(IllegalStateException("PKCE verifier bulunamadı."))

        try {
            val tokenBody = FormBody.Builder()
                .add("client_id", DiscordOAuthConfig.CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", DiscordOAuthConfig.REDIRECT_URI)
                .add("code_verifier", verifier)
                .build()

            val tokenRequest = Request.Builder()
                .url(DiscordOAuthConfig.TOKEN_URL)
                .post(tokenBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            client.newCall(tokenRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    throw IllegalStateException("Discord token isteği başarısız (${response.code}): $body")
                }

                val tokenJson = JSONObject(response.body?.string().orEmpty())
                val accessToken = tokenJson.getString("access_token")

                val userRequest = Request.Builder()
                    .url(DiscordOAuthConfig.USER_URL)
                    .get()
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                client.newCall(userRequest).execute().use { userResponse ->
                    if (!userResponse.isSuccessful) {
                        throw IllegalStateException("Discord kullanıcı bilgisi alınamadı (${userResponse.code}).")
                    }

                    val user = JSONObject(userResponse.body?.string().orEmpty())
                    val id = user.getString("id")
                    val username = user.optString("username", "Discord User")
                    val globalName = user.optString("global_name", "").ifBlank { username }
                    val discriminator = user.optString("discriminator", "0")
                    val avatarHash = user.optString("avatar", "")
                    val avatarUrl = if (avatarHash.isNotBlank()) {
                        "https://cdn.discordapp.com/avatars/$id/$avatarHash.png"
                    } else null

                    val now = System.currentTimeMillis()

                    clearPendingAuth()

                    Result.success(
                        DiscordAccount(
                            id = id,
                            username = username,
                            displayName = globalName,
                            discriminator = discriminator,
                            avatarUrl = avatarUrl,
                            avatarType = if (avatarUrl != null) "custom" else "phoenix",
                            connectedAtMillis = now,
                            isVerified = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            clearPendingAuth()
            Result.failure(e)
        }
    }
    fun clearTokens() {
        // OAuth access tokens are not persisted in the identity-only flow.
    }

    private fun clearPendingAuth() {
        prefs.edit().remove("state").remove("code_verifier").apply()
    }

    private fun randomUrlSafe(length: Int): String {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return base64Url(bytes)
    }

    private fun sha256(value: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.US_ASCII))

    private fun base64Url(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
