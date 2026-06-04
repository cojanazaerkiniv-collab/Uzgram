package com.uzgram.messenger.domain.repository

import com.uzgram.messenger.domain.model.MiniApp
import com.uzgram.messenger.domain.model.MiniAppCategory
import kotlinx.coroutines.flow.Flow

interface MiniAppRepository {
    fun getAllApps(): Flow<List<MiniApp>>
    fun getFeaturedApps(): Flow<List<MiniApp>>
    fun getInstalledApps(): Flow<List<MiniApp>>
    fun getAppsByCategory(category: MiniAppCategory): Flow<List<MiniApp>>
    fun searchApps(query: String): Flow<List<MiniApp>>
    suspend fun getAppById(appId: String): MiniApp
    suspend fun installApp(appId: String)
    suspend fun uninstallApp(appId: String)
    suspend fun rateApp(appId: String, rating: Float, review: String?)
    suspend fun reportApp(appId: String, reason: String)
}
