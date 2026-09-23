package com.unistay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unistay.android.domain.model.model.ChatSession
import com.unistay.android.domain.model.model.Message

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM chat_sessions WHERE studentId = :userId ORDER BY lastUpdated DESC")
    suspend fun getSessionsForStudent(userId: String): List<ChatSession>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySession(sessionId: String): List<Message>
    // Lấy tất cả các phiên chat mà User này tham gia (có thể là student hoặc landlord)
    @Query("SELECT * FROM chat_sessions WHERE studentId = :userId OR landlordId = :userId ORDER BY lastUpdated DESC")
    fun getChatSessionsForUser(userId: String): kotlinx.coroutines.flow.Flow<List<com.unistay.android.domain.model.model.ChatSession>>
    // Thêm phiên chat mới vào CSDL
    // Kiểm tra xem đã từng chat về phòng này chưa
    @Query("SELECT * FROM chat_sessions WHERE studentId = :studentId AND roomId = :roomId LIMIT 1")
    suspend fun getExistingChatSession(studentId: String, roomId: String): com.unistay.android.domain.model.model.ChatSession?
}