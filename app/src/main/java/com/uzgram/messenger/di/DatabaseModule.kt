package com.uzgram.messenger.di

import android.content.Context
import androidx.room.Room
import com.uzgram.messenger.data.local.dao.ChatDao
import com.uzgram.messenger.data.local.dao.MessageDao
import com.uzgram.messenger.data.local.dao.UserDao
import com.uzgram.messenger.data.local.db.UzGramDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UzGramDatabase =
        Room.databaseBuilder(context, UzGramDatabase::class.java, "uzgram.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: UzGramDatabase): UserDao = db.userDao()
    @Provides fun provideChatDao(db: UzGramDatabase): ChatDao = db.chatDao()
    @Provides fun provideMessageDao(db: UzGramDatabase): MessageDao = db.messageDao()
}
