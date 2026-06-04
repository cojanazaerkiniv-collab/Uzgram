package com.uzgram.messenger.domain.model

import java.time.Instant

enum class MiniAppCategory(val labelResKey: String) {
    GAMES("Oyinlar"),
    FINANCE("Moliya"),
    SHOPPING("Xarid"),
    UTILITIES("Qurilmalar"),
    ENTERTAINMENT("Ko'ngilochar"),
    EDUCATION("Ta'lim"),
    HEALTH("Salomatlik"),
    TRAVEL("Sayohat"),
    FOOD("Ovqat"),
    SOCIAL("Ijtimoiy"),
    NEWS("Yangiliklar"),
    CRYPTO("Kripto"),
    OTHER("Boshqa")
}

data class MiniApp(
    val id: String,
    val botUsername: String,
    val name: String,
    val shortDescription: String,
    val description: String,
    val iconUrl: String,
    val bannerUrl: String?,
    val screenshots: List<String>,
    val category: MiniAppCategory,
    val developerName: String,
    val developerVerified: Boolean,
    val version: String,
    val rating: Float,
    val ratingCount: Int,
    val installCount: Int,
    val isInstalled: Boolean,
    val isFeatured: Boolean,
    val isPremiumOnly: Boolean,
    val permissions: List<MiniAppPermission>,
    val startParam: String?,
    val launchUrl: String,
    val updatedAt: Instant
)

enum class MiniAppPermission {
    READ_PROFILE,
    WRITE_PROFILE,
    READ_CONTACTS,
    SEND_MESSAGES,
    NOTIFICATIONS,
    PAYMENTS,
    LOCATION,
    CAMERA,
    MICROPHONE
}

data class MiniAppLaunchParams(
    val appId: String,
    val botUsername: String,
    val launchUrl: String,
    val startParam: String?,
    val themeParams: MiniAppThemeParams
)

data class MiniAppThemeParams(
    val bgColor: String,
    val textColor: String,
    val hintColor: String,
    val linkColor: String,
    val buttonColor: String,
    val buttonTextColor: String,
    val secondaryBgColor: String,
    val headerBgColor: String,
    val bottomBarBgColor: String,
    val sectionBgColor: String,
    val sectionHeaderTextColor: String,
    val subtitleTextColor: String,
    val destructiveTextColor: String
)
