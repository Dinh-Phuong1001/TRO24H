package com.unistay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unistay.android.domain.model.model.Room
import com.unistay.android.domain.model.model.SavedRoom
import com.unistay.android.domain.model.model.ViewHistory

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms")
    suspend fun getAllRooms(): List<Room>

    @Query("SELECT * FROM rooms WHERE targetUniversity = :university")
    suspend fun getRoomsByUniversity(university: String): List<Room>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<Room>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRoom(savedRoom: SavedRoom)

    @Query("DELETE FROM saved_rooms WHERE userId = :userId AND roomId = :roomId")
    suspend fun unsaveRoom(userId: String, roomId: String)

    @Query("SELECT rooms.* FROM rooms INNER JOIN saved_rooms ON rooms.roomId = saved_rooms.roomId WHERE saved_rooms.userId = :userId ORDER BY saved_rooms.savedAt DESC")
    suspend fun getSavedRoomsByUser(userId: String): List<Room>
    @Query("""
        SELECT rooms.* FROM rooms 
        INNER JOIN view_history ON rooms.roomId = view_history.roomId 
        WHERE view_history.userId = :userId 
        GROUP BY rooms.roomId 
        ORDER BY MAX(view_history.viewedAt) DESC
    """)
    suspend fun getViewHistoryByUser(userId: String): List<Room>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViewHistory(history: ViewHistory)
    @Query("SELECT EXISTS(SELECT 1 FROM saved_rooms WHERE userId = :userId AND roomId = :roomId)")
    suspend fun isRoomSaved(userId: String, roomId: String): Boolean
}