package com.unistay.android.presentation.home

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
class HomeViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    init {
        loadRooms("ALL")
    }

    fun loadRooms(university: String) {
        viewModelScope.launch {
            val result = if (university == "ALL") {
                repository.getAllRooms()
            } else {
                repository.getRoomsByUniversity(university)
            }
            _rooms.value = result
        }
    }
}