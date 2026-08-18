package com.example.ui.dialogs

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ExtensionEntity
import com.example.extensions.AnkaExtensionJsBridge
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.SurfaceDark
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ExtensionRunnerDialog(
    extension: ExtensionEntity,
    onSendShortcut: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onShowNotification: (String) -> Unit,
    onGetProfiles: () -> String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val extensionDir = File(context.filesDir, "extensions/${extension.id}")
    val manifestFile = File(extensionDir, "manifest.json")

    val entryPath = try {
        val manifest = JSONObject(manifestFile.readText())
        manifest.optString("entry", "index.html")
    } catch (_: Exception) {
        "index.html"
    }

    val entryFile = File(extensionDir, entryPath).canonicalFile
    val extensionRoot = extensionDir.canonicalFile

    val permissionsList = try {
        val array = JSONArray(extension.permissionsJson)
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) list.add(array.getString(i))
        list
    } catch (e: Exception) {
        emptyList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, PhoenixCardBorder, RoundedCornerShape(24.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = extension.name,
                                color = PhoenixGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "v${extension.version} • ${extension.developer}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (entryFile != extensionRoot &&
                    !entryFile.path.startsWith(extensionRoot.path + File.separator)
                ) {
                    Text(
                        text = "Hata: Uzantı entry yolu paket klasörünün dışına çıkıyor.",
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (!entryFile.exists()) {
                    Text(
                        text = "Hata: Uzantının manifest.json dosyasında belirtilen giriş dosyası bulunamadı.\nEntry: $entryPath",
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                                settings.allowFileAccess = true
                                settings.allowContentAccess = false
                                settings.allowFileAccessFromFileURLs = false
                                settings.allowUniversalAccessFromFileURLs = false

                                webViewClient = WebViewClient()

                                val jsBridge = AnkaExtensionJsBridge(
                                    context = ctx,
                                    extensionId = extension.id,
                                    permissions = permissionsList,
                                    onSendShortcut = onSendShortcut,
                                    onOpenApp = onOpenApp,
                                    onShowNotification = onShowNotification,
                                    onGetProfiles = onGetProfiles
                                )

                                addJavascriptInterface(jsBridge, "anka")

                                loadUrl("file://${entryFile.absolutePath}")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, PhoenixCardBorder, RoundedCornerShape(16.dp))
                            .background(Color.Black)
                    )
                }
            }
        }
    }
}
