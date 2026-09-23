package com.unistay.android.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.domain.model.model.Room
import com.unistay.android.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    private val repository: RoomRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    fun createNewRoom(
        title: String, price: Double, phone: String, address: String, district: String,
        university: String, distance: Double, maxOccupancy: Int, amenities: String, description: String,
        roomType: String = "Phòng trọ khép kín",
        imageFile: java.io.File?,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Kiểm tra xem người dùng đã đăng nhập chưa
            val userId = sessionManager.userId.firstOrNull()
            if (userId == null) {
                onError("Vui lòng đăng nhập để có thể đăng tin!")
                return@launch
            }
            // 2. Upload ảnh (nếu có)
            var finalImageUrl = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=600"
            if (imageFile != null) {
                try {
                    val mediaType = "image/*".toMediaTypeOrNull()
                    val requestBody = imageFile.asRequestBody(mediaType)
                    val body = okhttp3.MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
                    val response = com.unistay.android.data.remote.RetrofitClient.instance.uploadImage(body)
                    finalImageUrl = response.imageUrl
                } catch (e: Exception) {
                    onError("Lỗi tải ảnh lên: ${e.message}")
                    return@launch
                }
            }

            // 3. Tạo mã phòng ngẫu nhiên và gán dữ liệu
            val randomId = UUID.randomUUID().toString()
            val roomCode = "HN-${System.currentTimeMillis().toString().takeLast(4)}"

            val newRoom = Room(
                roomId = randomId,
                roomCode = roomCode,
                title = title,
                address = address,
                district = district,
                basePrice = price,
                targetUniversity = university,
                distanceToCampusKm = distance,
                status = "Available",
                maxOccupancy = maxOccupancy,
                currentOccupancy = 0,
                amenities = amenities,
                imageUrl = finalImageUrl,
                contactPhone = phone,
                description = description,
                roomType = roomType,
                ownerId = userId
            )

            // 4. Gọi API để đăng phòng lên Server
            try {
                repository.createRoom(newRoom)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                onError("Lỗi máy chủ (${e.code()}): $errorBody")
            } catch (e: Exception) {
                onError("Có lỗi kết nối: ${e.message}")
            }
        }
    }
}