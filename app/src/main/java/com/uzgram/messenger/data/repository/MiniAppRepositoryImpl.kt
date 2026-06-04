package com.uzgram.messenger.data.repository

import com.uzgram.messenger.data.remote.ApiService
import com.uzgram.messenger.data.remote.dto.MiniAppDto
import com.uzgram.messenger.domain.model.*
import com.uzgram.messenger.domain.repository.MiniAppRepository
import com.uzgram.messenger.utils.Result
import com.uzgram.messenger.utils.safeApiCall
import kotlinx.coroutines.flow.*
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiniAppRepositoryImpl @Inject constructor(
    private val api: ApiService
) : MiniAppRepository {

    override fun getAllApps(): Flow<List<MiniApp>> = flow {
        when (val r = safeApiCall { api.getMiniApps() }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override fun getFeaturedApps(): Flow<List<MiniApp>> = flow {
        when (val r = safeApiCall { api.getFeaturedMiniApps() }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override fun getInstalledApps(): Flow<List<MiniApp>> = flow {
        when (val r = safeApiCall { api.getInstalledMiniApps() }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override fun getAppsByCategory(category: MiniAppCategory): Flow<List<MiniApp>> = flow {
        when (val r = safeApiCall { api.getMiniApps(category = category.name.lowercase()) }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override fun searchApps(query: String): Flow<List<MiniApp>> = flow {
        when (val r = safeApiCall { api.searchMiniApps(query) }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun getAppById(appId: String): MiniApp {
        return when (val r = safeApiCall { api.getMiniAppById(appId) }) {
            is Result.Success -> r.data.toDomain()
            is Result.Error   -> throw Exception(r.message)
            else              -> throw Exception("Unknown error")
        }
    }

    override suspend fun installApp(appId: String) {
        when (val r = safeApiCall { api.installMiniApp(appId) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun uninstallApp(appId: String) {
        when (val r = safeApiCall { api.uninstallMiniApp(appId) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun rateApp(appId: String, rating: Float, review: String?) {
        when (val r = safeApiCall { api.rateMiniApp(appId, mapOf("rating" to rating, "review" to review)) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun reportApp(appId: String, reason: String) {
        safeApiCall { api.reportMiniApp(appId, mapOf("reason" to reason)) }
    }
}

private fun MiniAppDto.toDomain(): MiniApp = MiniApp(
    id = id,
    botUsername = botUsername,
    name = name,
    shortDescription = shortDescription,
    description = description,
    iconUrl = iconUrl,
    bannerUrl = bannerUrl,
    screenshots = screenshots,
    category = runCatching { MiniAppCategory.valueOf(category.uppercase()) }.getOrDefault(MiniAppCategory.OTHER),
    developerName = developerName,
    developerVerified = developerVerified,
    version = version,
    rating = rating,
    ratingCount = ratingCount,
    installCount = installCount,
    isInstalled = isInstalled,
    isFeatured = isFeatured,
    isPremiumOnly = isPremiumOnly,
    permissions = permissions.mapNotNull { perm ->
        runCatching { MiniAppPermission.valueOf(perm.uppercase()) }.getOrNull()
    },
    startParam = startParam,
    launchUrl = launchUrl,
    updatedAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.now())
)
