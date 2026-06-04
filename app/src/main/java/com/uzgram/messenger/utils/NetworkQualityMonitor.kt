package com.uzgram.messenger.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkTier { HIGH, MEDIUM, LOW, VERY_LOW, OFFLINE }

@Singleton
class NetworkQualityMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _tier = MutableStateFlow(NetworkTier.OFFLINE)
    val tier: StateFlow<NetworkTier> = _tier.asStateFlow()

    val currentTier: NetworkTier get() = _tier.value

    val isConnected: Boolean
        get() = currentTier != NetworkTier.OFFLINE

    init {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                caps: NetworkCapabilities
            ) {
                _tier.value = caps.toTier()
            }

            override fun onLost(network: Network) {
                _tier.value = NetworkTier.OFFLINE
            }

            override fun onAvailable(network: Network) {
                val caps = connectivityManager.getNetworkCapabilities(network)
                _tier.value = caps?.toTier() ?: NetworkTier.OFFLINE
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
        // Initial check
        val active = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(active)
        _tier.value = caps?.toTier() ?: NetworkTier.OFFLINE
    }

    private fun NetworkCapabilities.toTier(): NetworkTier {
        val bandwidth = linkDownstreamBandwidthKbps
        return when {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                    bandwidth > 5000 -> NetworkTier.HIGH
            bandwidth > 1000 -> NetworkTier.HIGH
            bandwidth > 256 -> NetworkTier.MEDIUM
            bandwidth > 64 -> NetworkTier.LOW
            bandwidth > 0 -> NetworkTier.VERY_LOW
            else -> NetworkTier.OFFLINE
        }
    }
}
