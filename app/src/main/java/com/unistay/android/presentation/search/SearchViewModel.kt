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
        targetUni: String,
        targetDistrict: String,
        minPrice: Double,
        maxPrice: Double,
        selectedAmenities: List<String>,
        roomType: String = ""
    ) {
        viewModelScope.launch {
            // Lấy toàn bộ phòng thật từ Database
            val allRooms = repository.getAllRooms()

            // Bắt đầu lọc
            val filteredRooms = allRooms.filter { room ->
                // 0. Lọc theo từ khóa
                val matchKeyword = keyword.isEmpty() ||
                        room.title.contains(keyword, ignoreCase = true) ||
                        room.address.contains(keyword, ignoreCase = true)

                // 1. Lọc theo trường
                val matchUniversity = targetUni.isEmpty() ||
                        targetUni.startsWith("Tất cả") ||
                        room.targetUniversity.contains(targetUni, ignoreCase = true) ||
                        targetUni.contains(room.targetUniversity, ignoreCase = true)

                // 2. Lọc theo quận/huyện
                val matchDistrict = targetDistrict.isEmpty() ||
                        targetDistrict.startsWith("Tất cả") ||
                        room.district.contains(targetDistrict, ignoreCase = true) ||
                        room.address.contains(targetDistrict, ignoreCase = true)

                // 3. Lọc theo giá
                val matchPrice = room.basePrice in minPrice..maxPrice

                // 4. Lọc theo tiện ích
                val matchAmenities = if (selectedAmenities.isEmpty()) {
                    true
                } else {
                    selectedAmenities.all { selectedItem ->
                        room.amenities.contains(selectedItem, ignoreCase = true)
                    }
                }

                // 5. Lọc theo loại phòng
                val matchRoomType = roomType.isEmpty() ||
                        roomType.startsWith("Chọn") ||
                        roomType.startsWith("Tất cả") ||
                        room.roomType.contains(roomType, ignoreCase = true) ||
                        roomType.contains(room.roomType, ignoreCase = true)

                matchKeyword && matchUniversity && matchDistrict && matchPrice && matchAmenities && matchRoomType
            }

            _searchResults.value = filteredRooms
        }
    }
}