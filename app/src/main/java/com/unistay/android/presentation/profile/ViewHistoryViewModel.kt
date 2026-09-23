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
class ViewHistoryViewModel @Inject constructor(
    private val repository: RoomRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _historyRooms = MutableStateFlow<List<Room>>(emptyList())
    val historyRooms: StateFlow<List<Room>> = _historyRooms

    fun loadHistory() {
        viewModelScope.launch {
            val userId = sessionManager.userId.firstOrNull()
            if (userId != null) {
                _historyRooms.value = repository.getViewHistoryByUser(userId)
            }
        }
    }

    fun loadHistoryRooms() = loadHistory()
}