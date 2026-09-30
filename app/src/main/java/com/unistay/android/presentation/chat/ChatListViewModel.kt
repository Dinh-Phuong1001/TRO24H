package com.unistay.android.presentation.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.data.local.dao.ChatDao
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.domain.model.model.ChatSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val sessionManager: UserSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("chat_sessions_prefs", Context.MODE_PRIVATE)

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions

    private var rawSessions: List<ChatSession> = emptyList()
    private var pollingJob: Job? = null
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

    private fun getPinnedIds(): MutableSet<String> {
        return prefs.getStringSet("pinned_sessions", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun getBlockedIds(): MutableSet<String> {
        return prefs.getStringSet("blocked_sessions", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun getDeletedIds(): MutableSet<String> {
        return prefs.getStringSet("deleted_sessions", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun processAndSort(list: List<ChatSession>): List<ChatSession> {
        val pinned = getPinnedIds()
        val blocked = getBlockedIds()
        val deleted = getDeletedIds()

        return list
            .filterNot { it.sessionId in deleted }
            .map { session ->
                session.apply {
                    isPinned = sessionId in pinned
                    isBlocked = sessionId in blocked
                }
            }
            .sortedWith(
                compareByDescending<ChatSession> { it.isPinned }
                    .thenByDescending { it.lastUpdated }
            )
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val apiSessions = com.unistay.android.data.remote.RetrofitClient.instance.getChatSessions(currentUserId)
                    rawSessions = apiSessions
                    _chatSessions.value = processAndSort(apiSessions)
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    fun togglePin(sessionId: String): Boolean {
        val pinned = getPinnedIds()
        val isNowPinned = if (sessionId in pinned) {
            pinned.remove(sessionId)
            false
        } else {
            pinned.add(sessionId)
            true
        }
        prefs.edit().putStringSet("pinned_sessions", pinned).apply()
        _chatSessions.value = processAndSort(rawSessions)
        return isNowPinned
    }

    fun toggleBlock(sessionId: String): Boolean {
        val blocked = getBlockedIds()
        val isNowBlocked = if (sessionId in blocked) {
            blocked.remove(sessionId)
            false
        } else {
            blocked.add(sessionId)
            true
        }
        prefs.edit().putStringSet("blocked_sessions", blocked).apply()
        _chatSessions.value = processAndSort(rawSessions)
        return isNowBlocked
    }

    fun deleteSession(sessionId: String) {
        val deleted = getDeletedIds()
        deleted.add(sessionId)
        prefs.edit().putStringSet("deleted_sessions", deleted).apply()
        _chatSessions.value = processAndSort(rawSessions)

        viewModelScope.launch {
            try {
                com.unistay.android.data.remote.RetrofitClient.instance.deleteChatSession(sessionId)
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}