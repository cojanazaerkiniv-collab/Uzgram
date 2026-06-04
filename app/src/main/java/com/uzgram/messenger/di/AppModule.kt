package com.uzgram.messenger.di

import android.content.Context
import com.uzgram.messenger.data.local.prefs.SessionPrefs
import com.uzgram.messenger.data.remote.websocket.SocketManager
import com.uzgram.messenger.utils.NetworkQualityMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionPrefs(@ApplicationContext context: Context): SessionPrefs =
        SessionPrefs(context)

    @Provides
    @Singleton
    fun provideSocketManager(): SocketManager = SocketManager()

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkQualityMonitor =
        NetworkQualityMonitor(context)
}
