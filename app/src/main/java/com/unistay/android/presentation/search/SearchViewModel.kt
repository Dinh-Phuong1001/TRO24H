package com.unistay.android.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.domain.model.model.Room
import com.unistay.android.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Room>>(emptyList())
    val searchResults: StateFlow<List<Room>> = _searchResults

    fun searchRooms(
        keyword: String = "",
        targetUni: String = "",
        targetDistrict: String = "",
        minPrice: Double = 0.0,
        maxPrice: Double = Double.MAX_VALUE,
        selectedAmenities: List<String> = emptyList(),
        roomType: String = ""
    ) {
        viewModelScope.launch {
            // Lấy toàn bộ phòng từ Database
            val allRooms = repository.getAllRooms()

            // Bắt đầu lọc với cơ chế null-safe 100%
            val filteredRooms = allRooms.filter { room ->
                val title = room.title ?: ""
                val address = room.address ?: ""
                val roomUni = room.targetUniversity ?: ""
                val roomDistrict = room.district ?: ""
                val roomAmenities = room.amenities ?: ""
                val roomTypeStr = room.roomType ?: ""
                val basePrice = room.basePrice

                // 0. Lọc theo từ khóa
                val matchKeyword = keyword.isBlank() ||
                        title.contains(keyword, ignoreCase = true) ||
                        address.contains(keyword, ignoreCase = true)

                // 1. Lọc theo trường
                val matchUniversity = targetUni.isBlank() ||
                        targetUni.startsWith("Tất cả") ||
                        roomUni.contains(targetUni, ignoreCase = true) ||
                        targetUni.contains(roomUni, ignoreCase = true)

                // 2. Lọc theo quận/huyện
                val matchDistrict = targetDistrict.isBlank() ||
                        targetDistrict.startsWith("Tất cả") ||
                        roomDistrict.contains(targetDistrict, ignoreCase = true) ||
                        address.contains(targetDistrict, ignoreCase = true)

                // 3. Lọc theo giá
                val matchPrice = basePrice in minPrice..maxPrice

                // 4. Lọc theo tiện ích (hỗ trợ nhiều tiện ích tích chọn cùng lúc, không crash)
                val matchAmenities = if (selectedAmenities.isEmpty()) {
                    true
                } else {
                    selectedAmenities.all { item ->
                        val cleanItem = item.trim().removePrefix("Có ").trim()
                        roomAmenities.contains(item, ignoreCase = true) ||
                                (cleanItem.isNotEmpty() && roomAmenities.contains(cleanItem, ignoreCase = true))
                    }
                }

                // 5. Lọc theo loại phòng
                val matchRoomType = roomType.isBlank() ||
                        roomType.startsWith("Chọn") ||
                        roomType.startsWith("Tất cả") ||
                        roomTypeStr.contains(roomType, ignoreCase = true) ||
                        roomType.contains(roomTypeStr, ignoreCase = true)

                matchKeyword && matchUniversity && matchDistrict && matchPrice && matchAmenities && matchRoomType
            }

            _searchResults.value = filteredRooms
        }
    }
}