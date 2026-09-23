package com.unistay.android.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.domain.model.model.Room
import com.unistay.android.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedRoomsViewModel @Inject constructor(
    private val repository: RoomRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _savedRooms = MutableStateFlow<List<Room>>(emptyList())
    val savedRooms: StateFlow<List<Room>> = _savedRooms

    fun loadSavedRooms() {
        viewModelScope.launch {
            // Lấy ID người dùng hiện tại
            val userId = sessionManager.userId.firstOrNull()
            if (userId != null) {
                // Truy vấn DB lấy các phòng đã lưu
                _savedRooms.value = repository.getSavedRoomsByUser(userId)
            }
        }
    }
}