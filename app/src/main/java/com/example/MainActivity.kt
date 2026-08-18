package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleOAuthIntent(intent)
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = mainViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        if (uri.scheme != "ankamacropad" || uri.host != "oauth" || uri.path != "/callback") return

        mainViewModel.handleDiscordOAuthCallback(uri) { success, error ->
            if (!success) {
                runOnUiThread {
                    android.widget.Toast.makeText(
                        this,
                        error ?: "Discord OAuth tamamlanamadı.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
