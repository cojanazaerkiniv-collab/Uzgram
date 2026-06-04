package com.uzgram.messenger.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uzgram.messenger.data.local.dao.ChatDao
import com.uzgram.messenger.data.local.dao.MessageDao
import com.uzgram.messenger.data.local.dao.UserDao
import com.uzgram.messenger.data.local.entity.ChatEntity
import com.uzgram.messenger.data.local.entity.MessageEntity
import com.uzgram.messenger.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ChatEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UzGramDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
