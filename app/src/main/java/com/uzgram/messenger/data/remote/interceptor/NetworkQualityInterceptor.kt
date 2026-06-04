package com.uzgram.messenger.data.remote.interceptor

import com.uzgram.messenger.utils.NetworkQualityMonitor
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkQualityInterceptor @Inject constructor(
    private val networkMonitor: NetworkQualityMonitor
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val tier = networkMonitor.currentTier
        val request = chain.request().newBuilder().apply {
            // Signal to server the network quality for adaptive responses
            addHeader("X-Network-Tier", tier.name)
            // Request compressed responses on low-quality networks
            if (tier.ordinal >= 2) {
                addHeader("Accept-Encoding", "br, gzip")
            }
        }.build()
        return chain.proceed(request)
    }
}
