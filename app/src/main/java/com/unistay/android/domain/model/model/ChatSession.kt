package com.unistay.android.domain.model.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val sessionId: String,
    val studentId: String,
    val landlordId: String,
    val roomId: String,
    val roomTitle: String,
    val landlordName: String,
    val landlordPhone: String,
    val lastMessage: String,
    val studentUnreadCount: Int = 0,
    val landlordUnreadCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val studentAvatarUrl: String? = null,
    val landlordAvatarUrl: String? = null,
    val studentName: String? = null
)