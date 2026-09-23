package com.unistay.android.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.domain.model.model.Room
import com.unistay.android.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

@HiltViewModel
class MyRoomsViewModel @Inject constructor(
    private val repository: RoomRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _myRooms = MutableStateFlow<List<Room>>(emptyList())
    val myRooms: StateFlow<List<Room>> = _myRooms

    fun loadMyRooms() {
        viewModelScope.launch {
            val userId = sessionManager.userId.firstOrNull()
            if (userId != null) {
                _myRooms.value = repository.getMyRooms(userId)
            }
        }
    }
}
