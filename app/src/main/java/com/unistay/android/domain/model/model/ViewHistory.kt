package com.unistay.android.domain.model.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "view_history")
data class ViewHistory(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val userId: String,
    val roomId: String,
    val viewedAt: Long = System.currentTimeMillis()
)