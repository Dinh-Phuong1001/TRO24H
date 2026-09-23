package com.unistay.android.domain.repository

import android.util.Log
import com.unistay.android.data.local.dao.RoomDao
import com.unistay.android.data.remote.RetrofitClient // Đảm bảo import đúng đường dẫn của bạn
import com.unistay.android.domain.model.model.Room
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val dao: RoomDao
) {
    // ==========================================
    // PHẦN 1: DỮ LIỆU ONLINE (GỌI TỪ SERVER API)
    // ==========================================

    suspend fun getAllRooms(): List<Room> {
        return try {
            // Thay vì dao.getAllRooms(), ta gọi thẳng lên Web C#
            RetrofitClient.instance.getRooms()
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi lấy danh sách phòng: ${e.message}")
            emptyList() // Nếu rớt mạng hoặc Server tắt, trả về danh sách rỗng để không bị văng App
        }
    }

    suspend fun getMyRooms(userId: String): List<Room> {
        return try {
            RetrofitClient.instance.getRooms(userId = userId)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi lấy phòng đã đăng: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRoomsByUniversity(university: String): List<Room> {
        return try {
            RetrofitClient.instance.getRooms(university)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi lọc phòng theo trường: ${e.message}")
            emptyList()
        }
    }

    // Thêm hàm này để gọi API Đăng phòng mới
    suspend fun createRoom(room: Room): Room {
        return RetrofitClient.instance.createRoom(room)
    }


    // =============== ===========================
    // PHẦN 2: DỮ LIỆU OFFLINE (LƯU TẠI ĐIỆN THOẠI)
    // ==========================================

    // Giữ nguyên hàm này nếu bạn muốn lưu cache nội bộ
    suspend fun insertRooms(rooms: List<Room>) = dao.insertRooms(rooms)

    // Các tính năng Cá nhân (Lịch sử, Thả tim)
    suspend fun getSavedRoomsByUser(userId: String): List<Room> {
        return try {
            RetrofitClient.instance.getSavedRooms(userId)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi lấy phòng đã lưu từ API: ${e.message}")
            emptyList()
        }
    }

    suspend fun insertViewHistory(history: com.unistay.android.domain.model.model.ViewHistory) {
        try {
            RetrofitClient.instance.recordViewHistory(history.userId, history.roomId)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi lưu lịch sử lên API: ${e.message}")
        }
    }

    suspend fun getViewHistoryByUser(userId: String): List<Room> {
        return try {
            RetrofitClient.instance.getViewHistory(userId)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi lấy lịch sử từ API: ${e.message}")
            emptyList()
        }
    }

    // Chúng ta không có API isRoomSaved riêng biệt, nên sẽ gọi getSavedRooms và kiểm tra danh sách
    suspend fun isRoomSaved(userId: String, roomId: String): Boolean {
        return try {
            val savedRooms = RetrofitClient.instance.getSavedRooms(userId)
            savedRooms.any { it.roomId == roomId }
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi kiểm tra phòng đã lưu: ${e.message}")
            false
        }
    }

    suspend fun saveRoom(savedRoom: com.unistay.android.domain.model.model.SavedRoom) {
        try {
            RetrofitClient.instance.saveRoom(savedRoom.userId, savedRoom.roomId)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi lưu phòng lên API: ${e.message}")
        }
    }

    suspend fun unsaveRoom(userId: String, roomId: String) {
        try {
            RetrofitClient.instance.unsaveRoom(userId, roomId)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Lỗi xóa phòng khỏi API: ${e.message}")
        }
    }
}