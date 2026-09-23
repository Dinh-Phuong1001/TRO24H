package com.unistay.android.domain.model.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_rooms")
data class SavedRoom(
    @PrimaryKey(autoGenerate = true) val saveId: Int = 0,
    val userId: String,
    val roomId: String,
    val savedAt: Long = System.currentTimeMillis()
)