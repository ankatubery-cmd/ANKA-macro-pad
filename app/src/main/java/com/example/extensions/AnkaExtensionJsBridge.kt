package com.example.extensions

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class AnkaExtensionJsBridge(
    private val context: Context,
    private val extensionId: String,
    private val permissions: List<String>,
    private val onSendShortcut: (String) -> Unit,
    private val onOpenApp: (String) -> Unit,
    private val onShowNotification: (String) -> Unit,
    private val onGetProfiles: () -> String
) {
    @JavascriptInterface
    fun pressKey(keys: String): String {
        if (!hasPermission("keyboard")) {
            showToast("Erişim Reddedildi: '$extensionId' uzantısının 'keyboard' izni yok.")
            return JSONObject().put("error", "Permission 'keyboard' required").toString()
        }
        onSendShortcut(keys)
        return JSONObject().put("status", "success").put("keys", keys).toString()
    }

    @JavascriptInterface
    fun connectPC(): String {
        if (!hasPermission("pc_connection")) {
            showToast("Erişim Reddedildi: '$extensionId' uzantısının 'pc_connection' izni yok.")
            return JSONObject().put("error", "Permission 'pc_connection' required").toString()
        }
        return JSONObject().put("status", "connected").toString()
    }

    @JavascriptInterface
    fun openApp(appName: String): String {
        if (!hasPermission("pc_connection")) {
            showToast("Erişim Reddedildi: '$extensionId' uzantısının 'pc_connection' izni yok.")
            return JSONObject().put("error", "Permission 'pc_connection' required").toString()
        }
        onOpenApp(appName)
        return JSONObject().put("status", "opening").put("app", appName).toString()
    }

    @JavascriptInterface
    fun showNotification(message: String) {
        if (!hasPermission("notification")) {
            return
        }
        onShowNotification(message)
    }

    @JavascriptInterface
    fun getProfiles(): String {
        if (!hasPermission("profiles")) {
            showToast("Erişim Reddedildi: '$extensionId' uzantısının 'profiles' izni yok.")
            return JSONObject().put("error", "Permission 'profiles' required").toString()
        }
        return onGetProfiles()
    }

    @JavascriptInterface
    fun getPermissions(): String {
        return JSONArray(permissions).toString()
    }

    private fun hasPermission(permission: String): Boolean {
        return permissions.contains(permission) || permissions.contains("*")
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
