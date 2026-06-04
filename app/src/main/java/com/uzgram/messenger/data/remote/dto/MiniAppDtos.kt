package com.uzgram.messenger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MiniAppDto(
    @SerializedName("id") val id: String,
    @SerializedName("bot_username") val botUsername: String,
    @SerializedName("name") val name: String,
    @SerializedName("short_description") val shortDescription: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("banner_url") val bannerUrl: String?,
    @SerializedName("screenshots") val screenshots: List<String>,
    @SerializedName("category") val category: String,
    @SerializedName("developer_name") val developerName: String,
    @SerializedName("developer_verified") val developerVerified: Boolean,
    @SerializedName("version") val version: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("rating_count") val ratingCount: Int,
    @SerializedName("install_count") val installCount: Int,
    @SerializedName("is_installed") val isInstalled: Boolean,
    @SerializedName("is_featured") val isFeatured: Boolean,
    @SerializedName("is_premium_only") val isPremiumOnly: Boolean,
    @SerializedName("permissions") val permissions: List<String>,
    @SerializedName("start_param") val startParam: String?,
    @SerializedName("launch_url") val launchUrl: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class MiniAppRateDto(
    @SerializedName("app_id") val appId: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("review") val review: String?
)
