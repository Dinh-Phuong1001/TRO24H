package com.unistay.android.domain.model.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey val roomId: String,
    val roomCode: String,
    val title: String,
    val address: String,
    val district: String,
    val basePrice: Double,
    val targetUniversity: String,
    val distanceToCampusKm: Double,
    val status: String,
    val maxOccupancy: Int,
    val currentOccupancy: Int,
    val amenities: String,
    val imageUrl: String,
    val contactPhone: String,
    val description: String? = null,
    val roomType: String = "Phòng trọ khép kín",
    @com.google.gson.annotations.SerializedName("userId")
    val ownerId: String,
    val ownerName: String? = null
)