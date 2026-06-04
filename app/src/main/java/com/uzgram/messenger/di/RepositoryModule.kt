package com.uzgram.messenger.di

import com.uzgram.messenger.data.repository.*
import com.uzgram.messenger.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds @Singleton
    abstract fun bindStoryRepository(impl: StoryRepositoryImpl): StoryRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindCallRepository(impl: CallRepositoryImpl): CallRepository

    @Binds @Singleton
    abstract fun bindMiniAppRepository(impl: MiniAppRepositoryImpl): MiniAppRepository
}
