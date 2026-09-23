package com.unistay.android.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.data.local.dao.ChatDao
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.domain.model.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var pollingJob: Job? = null

    fun startPolling(sessionId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val userId = sessionManager.userId.firstOrNull()
                    if (userId != null) {
                        // Gọi API đánh dấu đã đọc
                        com.unistay.android.data.remote.RetrofitClient.instance.markAsRead(sessionId, userId)
                    }
                    
                    // Lấy từ API
                    val apiMessages = com.unistay.android.data.remote.RetrofitClient.instance.getChatMessages(sessionId)
                    _messages.value = apiMessages
                } catch (e: Exception) {
                    // Nếu rớt mạng, bỏ qua và thử lại sau
                }
                delay(2000) // Tự động làm mới mỗi 2 giây
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    fun sendMessage(
        sessionId: String, 
        content: String, 
        roomId: String, 
        landlordId: String,
        roomTitle: String,
        landlordName: String,
        landlordPhone: String
    ) {
        viewModelScope.launch {
            val senderId = sessionManager.userId.firstOrNull() ?: return@launch
            val message = Message(
                sessionId = sessionId,
                senderId = senderId,
                content = content,
                roomId = roomId,
                landlordId = landlordId,
                roomTitle = roomTitle,
                landlordName = landlordName,
                landlordPhone = landlordPhone
            )
            
            // Cập nhật UI ngay lập tức (Optimistic UI)
            _messages.value = _messages.value + message

            try {
                com.unistay.android.data.remote.RetrofitClient.instance.sendChatMessage(message)
            } catch (e: Exception) {
                // Lỗi
            }
        }
    }
}