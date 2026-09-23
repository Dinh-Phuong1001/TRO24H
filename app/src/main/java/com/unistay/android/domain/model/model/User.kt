package com.unistay.android.domain.model.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val phone: String,
    val role: String, // "STUDENT" hoặc "LANDLORD"
    val avatarUrl: String? = null
)