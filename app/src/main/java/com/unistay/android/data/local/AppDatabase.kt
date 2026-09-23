package com.unistay.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.unistay.android.data.local.dao.ChatDao
import com.unistay.android.data.local.dao.RoomDao
import com.unistay.android.data.local.dao.UserDao
import com.unistay.android.domain.model.model.ChatSession
import com.unistay.android.domain.model.model.Message
import com.unistay.android.domain.model.model.Room
import com.unistay.android.domain.model.model.SavedRoom
import com.unistay.android.domain.model.model.User
import com.unistay.android.domain.model.model.ViewHistory

@Database(
    entities = [
        User::class,
        Room::class,
        SavedRoom::class,
        ViewHistory::class,
        ChatSession::class,
        Message::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun chatDao(): ChatDao
}