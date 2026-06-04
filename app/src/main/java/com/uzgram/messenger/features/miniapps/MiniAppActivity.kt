package com.uzgram.messenger.features.miniapps

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.uzgram.messenger.ui.theme.UzGramTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MiniAppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appUrl   = intent.getStringExtra("url") ?: return finish()
        val appTitle = intent.getStringExtra("title") ?: "App"

        setContent {
            UzGramTheme {
                MiniAppScreen(
                    url = appUrl,
                    title = appTitle,
                    onClose = ::finish
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MiniAppScreen(url: String, title: String, onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, "Close")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled  = true
                    settings.allowFileAccess    = false
                    settings.allowContentAccess = false

                    // Inject UzGram Mini App API bridge
                    addJavascriptInterface(UzGramMiniAppBridge(), "UzGram")

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            // Only allow same-origin navigation
                            return false
                        }
                    }
                    loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

class UzGramMiniAppBridge {
    @JavascriptInterface
    fun getVersion(): String = "1.0.0"

    @JavascriptInterface
    fun close() {
        // Signal activity to close
    }

    @JavascriptInterface
    fun hapticFeedback(style: String) {
        // Trigger device haptics
    }

    @JavascriptInterface
    fun openLink(url: String) {
        // Open external link in browser
    }

    @JavascriptInterface
    fun showAlert(message: String) {
        // Show native alert dialog
    }

    @JavascriptInterface
    fun shareData(text: String) {
        // Open Android share sheet
    }
}
