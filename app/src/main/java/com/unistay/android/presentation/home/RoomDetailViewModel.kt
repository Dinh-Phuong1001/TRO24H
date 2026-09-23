package com.unistay.android.presentation.home // Đổi lại package theo đúng vị trí file của bạn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.domain.model.model.SavedRoom
import com.unistay.android.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.unistay.android.data.local.dao.ChatDao
import kotlinx.coroutines.flow.firstOrNull

@HiltViewModel
class RoomDetailViewModel @Inject constructor(
    private val repository: RoomRepository,       // Code cũ của bạn
    private val sessionManager: UserSessionManager, // Code cũ của bạn
    private val chatDao: ChatDao                  // <-- MỚI THÊM DÒNG NÀY (Nhớ có dấu phẩy ở dòng trên nếu cần)
) : ViewModel() {

    // ... Các code khác giữ nguyên

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private var currentUserId: String? = null

    fun checkSavedStatus(roomId: String) {
        viewModelScope.launch {
            // Lắng nghe xem ai đang đăng nhập
            sessionManager.userId.collect { userId ->
                currentUserId = userId
                if (userId != null) {
                    _isSaved.value = repository.isRoomSaved(userId, roomId)
                } else {
                    _isSaved.value = false
                }
            }
        }
    }

    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner

    fun checkIsOwner(ownerId: String) {
        viewModelScope.launch {
            sessionManager.userId.collect { userId ->
                _isOwner.value = (userId != null && userId == ownerId)
            }
        }
    }

    fun toggleSaveRoom(roomId: String, onRequireLogin: () -> Unit) {
        val userId = currentUserId
        if (userId == null) {
            onRequireLogin() // Báo lỗi nếu chưa đăng nhập
            return
        }

        viewModelScope.launch {
            if (_isSaved.value) {
                repository.unsaveRoom(userId, roomId)
                _isSaved.value = false
            } else {
                repository.saveRoom(SavedRoom(userId = userId, roomId = roomId))
                _isSaved.value = true
            }
        }
    }
    // Thêm hàm ghi nhận lịch sử
    fun recordViewHistory(roomId: String) {
        viewModelScope.launch {
            sessionManager.userId.collect { userId ->
                if (userId != null) {
                    val history = com.unistay.android.domain.model.model.ViewHistory(
                        userId = userId,
                        roomId = roomId,
                        viewedAt = System.currentTimeMillis()
                    )
                    repository.insertViewHistory(history)
                }
            }
        }
    }
    // Đảm bảo bạn đã khai báo: private val chatDao: ChatDao trong constructor

    fun getOrCreateChatSession(
        roomId: String,
        roomTitle: String,
        landlordId: String,
        landlordName: String,
        landlordPhone: String,
        onSessionReady: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val studentId = sessionManager.userId.firstOrNull()
            if (studentId == null) {
                onError("Vui lòng đăng nhập để có thể nhắn tin với chủ trọ!")
                return@launch
            }

            try {
                // Fetch from network instead of local database to ensure synchronization
                val apiSessions = com.unistay.android.data.remote.RetrofitClient.instance.getChatSessions(studentId)
                val existingSession = apiSessions.find { it.roomId == roomId && it.landlordId == landlordId }

                if (existingSession != null) {
                    onSessionReady(existingSession.sessionId)
                } else {
                    val newSessionId = java.util.UUID.randomUUID().toString()
                    onSessionReady(newSessionId)
                }
            } catch (e: Exception) {
                onError("Lỗi kết nối hộp thư: ${e.message}")
            }
        }
    }
    // Khai báo biến lưu phòng gợi ý (Dữ liệu thật)
    private val _suggestedRoom = MutableStateFlow<com.unistay.android.domain.model.model.Room?>(null)
    val suggestedRoom: StateFlow<com.unistay.android.domain.model.model.Room?> = _suggestedRoom

    // Hàm tìm phòng gợi ý từ Database thật
    fun loadSuggestedRoom(targetUniversity: String, currentRoomId: String) {
        viewModelScope.launch {
            // Lấy toàn bộ dữ liệu thật từ DB
            val allRooms = repository.getAllRooms()

            // Tìm 1 phòng có cùng trường đại học nhưng khác ID với phòng hiện tại
            val foundRoom = allRooms.find {
                it.targetUniversity == targetUniversity && it.roomId != currentRoomId
            }
            _suggestedRoom.value = foundRoom
        }
    }

    fun deleteRoom(roomId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = currentUserId
        if (userId == null) {
            onError("Bạn chưa đăng nhập.")
            return
        }

        viewModelScope.launch {
            try {
                com.unistay.android.data.remote.RetrofitClient.instance.deleteRoom(roomId, userId)
                onSuccess()
            } catch (e: Exception) {
                onError("Không thể xóa phòng: ${e.message}")
            }
        }
    }
}