package com.example.data

object DiscordOAuthConfig {
    // Replace this with the Application ID from Discord Developer Portal.
    const val CLIENT_ID = "1232330086225809478"
    const val REDIRECT_URI = "ankamacropad://oauth/callback"
    const val AUTHORIZE_URL = "https://discord.com/oauth2/authorize"
    const val TOKEN_URL = "https://discord.com/api/v10/oauth2/token"
    const val USER_URL = "https://discord.com/api/v10/users/@me"
    const val SCOPES = "identify"
}
