package com.uzgram.messenger.features.miniapps

import android.annotation.SuppressLint
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.ui.theme.UzBlue

/**
 * Full-screen WebView container for Mini Apps (Telegram-like WebApp API).
 * Injects the UzGram WebApp JS bridge on page load.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MiniAppWebViewScreen(
    appId: String,
    botUsername: String,
    launchUrl: String,
    onClose: () -> Unit,
    viewModel: MiniAppsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var pageTitle by remember { mutableStateOf(botUsername) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showCloseConfirm by remember { mutableStateOf(false) }

    // Build theme params JSON for injection
    val themeJson = buildThemeJson(colorScheme)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pageTitle, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Mini Ilova", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCloseConfirm = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Yopish")
                    }
                },
                actions = {
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Yangilash")
                    }
                    IconButton(onClick = { /* share app link */ }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Ulashish")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Loading bar
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadProgress / 100f },
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = UzBlue
                )
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webView = this
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            allowContentAccess = true
                            mediaPlaybackRequiresUserGesture = false
                            setSupportZoom(false)
                            displayZoomControls = false
                            builtInZoomControls = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        }

                        // JS bridge
                        addJavascriptInterface(UzGramWebAppBridge(
                            onClose = { onClose() },
                            onHapticFeedback = { /* vibrate */ },
                            onOpenLink = { url -> /* open external browser */ }
                        ), "UzGramAndroid")

                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                title?.let { pageTitle = it }
                            }
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = newProgress
                                isLoading = newProgress < 100
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                // Inject UzGram WebApp bridge
                                view?.evaluateJavascript(buildBridgeScript(themeJson), null)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                // Allow mini app domain; block external navigation
                                return !url.startsWith("https://") || url.contains("uzgram.online")
                            }
                        }

                        loadUrl(launchUrl.ifBlank { "https://uzgram.online/mini-apps/$appId" })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("Ilovani yopish") },
            text = { Text("${pageTitle}ni yopmoqchimisiz?.") },
            confirmButton = {
                TextButton(onClick = { showCloseConfirm = false; onClose() }) {
                    Text("Yopish", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) { Text("Bekor qilish") }
            }
        )
    }
}

/**
 * JavaScript bridge object injected into Mini App WebViews.
 * Mirrors the UzGram WebApp API surface.
 */
class UzGramWebAppBridge(
    private val onClose: () -> Unit,
    private val onHapticFeedback: (type: String) -> Unit,
    private val onOpenLink: (url: String) -> Unit
) {
    @JavascriptInterface
    fun close() = onClose()

    @JavascriptInterface
    fun hapticFeedback(type: String) = onHapticFeedback(type)

    @JavascriptInterface
    fun openLink(url: String) = onOpenLink(url)

    @JavascriptInterface
    fun ready() { /* App signals it is ready */ }

    @JavascriptInterface
    fun expand() { /* Request full screen */ }

    @JavascriptInterface
    fun sendData(data: String) { /* Bot receives this via webhook */ }
}

private fun buildThemeJson(colorScheme: ColorScheme): String {
    fun Int.toHex() = "#%06X".format(this and 0xFFFFFF)
    return """
        {
          "bg_color": "${colorScheme.background.toArgb().toHex()}",
          "text_color": "${colorScheme.onBackground.toArgb().toHex()}",
          "hint_color": "${colorScheme.onSurfaceVariant.toArgb().toHex()}",
          "link_color": "${colorScheme.primary.toArgb().toHex()}",
          "button_color": "${colorScheme.primary.toArgb().toHex()}",
          "button_text_color": "${colorScheme.onPrimary.toArgb().toHex()}",
          "secondary_bg_color": "${colorScheme.surfaceVariant.toArgb().toHex()}",
          "header_bg_color": "${colorScheme.surface.toArgb().toHex()}",
          "bottom_bar_bg_color": "${colorScheme.surface.toArgb().toHex()}",
          "section_bg_color": "${colorScheme.surfaceVariant.toArgb().toHex()}",
          "section_header_text_color": "${colorScheme.onSurfaceVariant.toArgb().toHex()}",
          "subtitle_text_color": "${colorScheme.onSurfaceVariant.toArgb().toHex()}",
          "destructive_text_color": "${colorScheme.error.toArgb().toHex()}"
        }
    """.trimIndent()
}

private fun buildBridgeScript(themeJson: String): String = """
    (function() {
      if (window.UzGramWebApp) return;
      
      var _themeParams = $themeJson;
      
      window.UzGramWebApp = {
        version: "6.9",
        platform: "android",
        themeParams: _themeParams,
        colorScheme: document.body.classList.contains('dark') ? 'dark' : 'light',
        isExpanded: true,
        viewportHeight: window.innerHeight,
        viewportStableHeight: window.innerHeight,
        
        ready: function() { UzGramAndroid.ready(); },
        close: function() { UzGramAndroid.close(); },
        expand: function() { UzGramAndroid.expand(); },
        sendData: function(data) { UzGramAndroid.sendData(JSON.stringify(data)); },
        openLink: function(url, options) { UzGramAndroid.openLink(url); },
        
        HapticFeedback: {
          impactOccurred: function(style) { UzGramAndroid.hapticFeedback('impact_' + style); },
          notificationOccurred: function(type) { UzGramAndroid.hapticFeedback('notification_' + type); },
          selectionChanged: function() { UzGramAndroid.hapticFeedback('selection'); }
        },
        
        MainButton: {
          text: '',
          color: _themeParams.button_color,
          textColor: _themeParams.button_text_color,
          isVisible: false,
          isActive: true,
          isProgressVisible: false,
          setText: function(t) { this.text = t; return this; },
          show: function() { this.isVisible = true; return this; },
          hide: function() { this.isVisible = false; return this; },
          enable: function() { this.isActive = true; return this; },
          disable: function() { this.isActive = false; return this; },
          onClick: function(fn) { this._cb = fn; return this; },
          offClick: function(fn) { this._cb = null; return this; },
          showProgress: function() { this.isProgressVisible = true; return this; },
          hideProgress: function() { this.isProgressVisible = false; return this; }
        },
        
        BackButton: {
          isVisible: false,
          show: function() { this.isVisible = true; return this; },
          hide: function() { this.isVisible = false; return this; },
          onClick: function(fn) { this._cb = fn; return this; },
          offClick: function(fn) { this._cb = null; return this; }
        },
        
        onEvent: function(event, fn) {},
        offEvent: function(event, fn) {}
      };
      
      // Dispatch ready event
      window.dispatchEvent(new CustomEvent('UzGramWebAppReady'));
      document.dispatchEvent(new CustomEvent('DOMContentLoaded'));
    })();
""".trimIndent()
