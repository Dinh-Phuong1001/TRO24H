package com.unistay.android.domain.model.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val messageId: Int = 0,
    val sessionId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    @androidx.room.Ignore
    val roomId: String? = null,
    @androidx.room.Ignore
    val landlordId: String? = null,
    @androidx.room.Ignore
    val roomTitle: String? = null,
    @androidx.room.Ignore
    val landlordName: String? = null,
    @androidx.room.Ignore
    val landlordPhone: String? = null
) {
    // Constructor cho Room sử dụng (khi load từ DB)
    constructor(
        messageId: Int = 0,
        sessionId: String,
        senderId: String,
        content: String,
        timestamp: Long = System.currentTimeMillis()
    ) : this(messageId, sessionId, senderId, content, timestamp, null, null, null, null, null)
}