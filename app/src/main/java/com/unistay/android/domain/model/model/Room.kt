package com.unistay.android.domain.model.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey val roomId: String = "",
    val roomCode: String? = null,
    val title: String? = "",
    val address: String? = "",
    val district: String? = "",
    val basePrice: Double = 0.0,
    val targetUniversity: String? = "",
    val distanceToCampusKm: Double = 0.0,
    val status: String? = "Còn phòng",
    val maxOccupancy: Int = 1,
    val currentOccupancy: Int = 0,
    val amenities: String? = "",
    val imageUrl: String? = "",
    val contactPhone: String? = "",
    val description: String? = null,
    val roomType: String? = "Phòng trọ khép kín",
    @com.google.gson.annotations.SerializedName("userId")
    val ownerId: String? = "",
    val ownerName: String? = null
)