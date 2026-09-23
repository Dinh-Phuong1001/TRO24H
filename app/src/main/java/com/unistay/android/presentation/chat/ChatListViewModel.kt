package com.unistay.android.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.data.local.dao.ChatDao
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.domain.model.model.ChatSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.delay

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions

    private var pollingJob: kotlinx.coroutines.Job? = null
    var currentUserId: String = ""
        private set

    init {
        viewModelScope.launch {
            currentUserId = sessionManager.userId.firstOrNull() ?: ""
            if (currentUserId.isNotEmpty()) {
                startPolling()
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val apiSessions = com.unistay.android.data.remote.RetrofitClient.instance.getChatSessions(currentUserId)
                    _chatSessions.value = apiSessions
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}