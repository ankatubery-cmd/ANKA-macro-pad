package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.MacroButtonEntity
import com.example.data.WidgetConfig
import com.example.data.WidgetMiniAction
import com.example.data.WidgetType
import com.example.extensions.AnkaExtensionJsBridge
import com.example.ui.theme.PhoenixAmber
import com.example.ui.theme.PhoenixCardBorder
import com.example.ui.theme.PhoenixFlameRed
import com.example.ui.theme.PhoenixGold
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MacroWidgetTile(
    button: MacroButtonEntity,
    isEditMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMiniActionClick: (WidgetMiniAction) -> Unit,
    onExtensionSendShortcut: (String) -> Unit = {},
    onExtensionOpenApp: (String) -> Unit = {},
    onExtensionNotification: (String) -> Unit = {},
    onExtensionGetProfiles: () -> String = { "[]" },
    modifier: Modifier = Modifier
) {
    val widgetConfig = remember(button.extraValuesJson) {
        WidgetConfig.fromJson(button.extraValuesJson)
    }

    val isHtmlWidget = button.extensionId != null && (
        widgetConfig.widgetType == WidgetType.EXTENSION_HTML ||
            (widgetConfig.widgetType == WidgetType.EXTENSION_ACTIONS && widgetConfig.actions.isEmpty())
        )

    val customStartColor = runCatching {
        Color(AndroidColor.parseColor(button.gradientStartHex))
    }.getOrDefault(Color(0xFF241208))
    val customEndColor = runCatching {
        Color(AndroidColor.parseColor(button.gradientEndHex))
    }.getOrDefault(Color(0xFF140803))
    val customBorderColor = runCatching {
        Color(AndroidColor.parseColor(button.borderColorHex))
    }.getOrDefault(PhoenixGold)

    val editGestureModifier = if (isEditMode) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { onEdit() },
                onLongPress = { onEdit() }
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isHtmlWidget) {
                    // HTML widgets get enough vertical space for their complete UI.
                    // The WebView itself is non-scrollable, so the extension content must fit.
                    Modifier.height(
                        // Keep HTML widgets proportional to the normal macro tile.
                        // A widget is intentionally 2x the standard tile height, while
                        // sizeSpan controls its width (1/2/3 columns).
                        when (button.sizeSpan.coerceIn(1, 3)) {
                            1 -> 250.dp
                            2 -> 250.dp
                            else -> 250.dp
                        }
                    )
                } else {
                    Modifier.height(if (button.sizeSpan == 1) 125.dp else 115.dp)
                }
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        customStartColor.copy(alpha = 0.85f),
                        customEndColor
                    )
                )
            )
            .border(
                1.5.dp,
                customBorderColor.copy(alpha = 0.8f),
                RoundedCornerShape(20.dp)
            )
            .then(editGestureModifier)
            .padding(horizontal = if (isHtmlWidget) 5.dp else 10.dp, vertical = if (isHtmlWidget) 5.dp else 8.dp)
            .testTag("macro_widget_${button.id}")
    ) {
        if (isHtmlWidget && button.extensionId != null) {
            ExtensionHtmlWidget(
                extensionId = button.extensionId,
                modifier = Modifier.fillMaxSize(),
                onSendShortcut = onExtensionSendShortcut,
                onOpenApp = onExtensionOpenApp,
                onShowNotification = onExtensionNotification,
                onGetProfiles = onExtensionGetProfiles
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconMapper.getIconVector(button.iconName.ifBlank { "widgets" }),
                            contentDescription = null,
                            tint = PhoenixGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = button.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (button.subtext.isNotBlank()) {
                        Text(
                            text = button.subtext,
                            color = PhoenixGold.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (widgetConfig.actions.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(widgetConfig.actions, key = { it.id }) { action ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2A160B))
                                    .border(1.dp, PhoenixCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onMiniActionClick(action) }
                                    .padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = IconMapper.getIconVector(action.icon),
                                    contentDescription = null,
                                    tint = PhoenixAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = action.name.ifBlank { action.id },
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (action.badge.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = action.badge,
                                        color = PhoenixGold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Uzantı widget'ı",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (isEditMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PhoenixAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PhoenixFlameRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ExtensionHtmlWidget(
    extensionId: String,
    modifier: Modifier,
    onSendShortcut: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onShowNotification: (String) -> Unit,
    onGetProfiles: () -> String
) {
    val context = LocalContext.current
    val source = remember(extensionId) {
        resolveExtensionHtmlSource(context, extensionId)
    }

    if (source == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                "Uzantı HTML içeriği bulunamadı.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
        return
    }

    AndroidView(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Color.Black),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.textZoom = 100
                setInitialScale(100)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = android.view.View.OVER_SCROLL_NEVER
                settings.allowFileAccess = true
                settings.allowContentAccess = false
                settings.allowFileAccessFromFileURLs = false
                settings.allowUniversalAccessFromFileURLs = false
                settings.mediaPlaybackRequiresUserGesture = false

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Keep extension HTML content compact enough to fit the macro tile.
                        view?.evaluateJavascript(
                            """(function(){
                                var s=document.createElement('style');
                                s.id='anka-widget-fit';
                                s.textContent=`
                                    html,body{overflow:hidden!important;width:100%!important;min-height:0!important;height:auto!important;}
                                    body{margin:0!important;padding:4px!important;box-sizing:border-box!important;}
                                    .wrap{max-width:none!important;width:100%!important;margin:0!important;}
                                    .head{margin:0 0 5px!important;}
                                    .head h1{font-size:16px!important;margin:0!important;}
                                    .head p{font-size:9px!important;margin:2px 0!important;}
                                    .card{padding:7px!important;border-radius:12px!important;box-shadow:none!important;}
                                    .time{font-size:34px!important;letter-spacing:1px!important;margin:4px 0!important;line-height:1!important;}
                                    .status{font-size:9px!important;min-height:12px!important;}
                                    .progress{height:4px!important;margin-top:6px!important;}
                                    .grid{gap:5px!important;margin-top:7px!important;}
                                    .controls{gap:5px!important;margin-top:6px!important;}
                                    .custom{gap:5px!important;margin-top:6px!important;}
                                    .custom input{padding:7px!important;font-size:11px!important;}
                                    .custom button{min-width:0!important;}
                                    .btn{border-radius:8px!important;padding:6px 4px!important;font-size:9px!important;line-height:1.05!important;}
                                    .note{font-size:7px!important;margin-top:5px!important;}
                                `;
                                var old=document.getElementById('anka-widget-fit');
                                if(old) old.remove();
                                document.head.appendChild(s);
                                document.documentElement.style.overflow='hidden';
                                document.body.style.overflow='hidden';
                                document.body.style.margin='0';
                            })()""".trimIndent(),
                            null
                        )
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        url: String?
                    ): Boolean {
                        if (url == null) return true
                        return !isAllowedExtensionUrl(url, source.root)
                    }
                }

                val bridge = AnkaExtensionJsBridge(
                    context = ctx,
                    extensionId = extensionId,
                    permissions = source.permissions,
                    onSendShortcut = onSendShortcut,
                    onOpenApp = onOpenApp,
                    onShowNotification = onShowNotification,
                    onGetProfiles = onGetProfiles
                )
                addJavascriptInterface(bridge, "anka")
                loadUrl(source.entryToString())
            }
        },
        update = { webView ->
            val target = source.entryToString()
            if (webView.url != target) {
                webView.loadUrl(target)
            }
        }
    )
}

private fun isAllowedExtensionUrl(url: String, root: File): Boolean {
    return try {
        if (url.startsWith("http://") || url.startsWith("https://")) return true
        if (!url.startsWith("file://")) return false
        val path = url.removePrefix("file://")
        val file = File(path).canonicalFile
        file.path.startsWith(root.path + File.separator) || file.path == root.path
    } catch (e: Exception) {
        false
    }
}

private data class ExtensionHtmlSource(
    val root: File,
    val entry: File,
    val permissions: List<String>
) {
    fun entryToString(): String = "file://${entry.absolutePath}"
}

private fun ExtensionHtmlSource.entryToUriString(): String = "file://${entry.absolutePath}"

private fun resolveExtensionHtmlSource(
    context: android.content.Context,
    extensionId: String
): ExtensionHtmlSource? {
    if (!extensionId.matches(Regex("[A-Za-z0-9._-]{1,64}"))) return null

    val root = File(context.filesDir, "extensions/$extensionId").canonicalFile
    val manifest = File(root, "manifest.json")
    if (!manifest.isFile) return null

    return try {
        val obj = JSONObject(manifest.readText())
        val entryPath = obj.optString("entry", "index.html")
        if (
            entryPath.isBlank() ||
            entryPath.startsWith("/") ||
            entryPath.startsWith("\\") ||
            entryPath.contains("\\") ||
            entryPath.contains(":") ||
            entryPath.split('/').any { it.isBlank() || it == "." || it == ".." }
        ) return null

        val entry = File(root, entryPath).canonicalFile
        if (!entry.isFile || !entry.path.startsWith(root.path + File.separator)) return null

        val permissions = mutableListOf<String>()
        obj.optJSONArray("permissions")?.let { arr ->
            for (i in 0 until arr.length()) {
                permissions.add(arr.optString(i))
            }
        }

        ExtensionHtmlSource(root, entry, permissions)
    } catch (_: Exception) {
        null
    }
}
