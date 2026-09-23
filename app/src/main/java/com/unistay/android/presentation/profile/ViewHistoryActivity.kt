package com.unistay.android.presentation.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.unistay.android.databinding.ActivityViewHistoryBinding
import com.unistay.android.domain.model.model.Room
import com.unistay.android.presentation.home.RoomAdapter
import com.unistay.android.presentation.home.RoomDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewHistoryBinding
    private lateinit var adapter: RoomAdapter
    private val viewModel: ViewHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackHistory.setOnClickListener { finish() }

        adapter = RoomAdapter(emptyList()) { room: Room ->
            val intent = Intent(this, RoomDetailActivity::class.java).apply {
                putExtra("ROOM_ID", room.roomId)
                putExtra("TARGET_UNIVERSITY", room.targetUniversity)
                putExtra("DISTANCE", room.distanceToCampusKm)
                putExtra("ROOM_TITLE", room.title)
                putExtra("ROOM_ADDRESS", room.address)
                putExtra("ROOM_PRICE", room.basePrice)
                putExtra("ROOM_AMENITIES", room.amenities)
                putExtra("ROOM_IMAGE", room.imageUrl)
                putExtra("ROOM_PHONE", room.contactPhone)
                putExtra("ROOM_TYPE", room.roomType)
                putExtra("OWNER_ID", room.ownerId)
                putExtra("OWNER_NAME", room.ownerName)
                putExtra("ROOM_DESCRIPTION", room.description)
            }
            startActivity(intent)
        }

        binding.rvHistoryRooms.layoutManager = GridLayoutManager(this, 2)
        binding.rvHistoryRooms.adapter = adapter

        viewModel.loadHistory()

        lifecycleScope.launch {
            viewModel.historyRooms.collect { rooms ->
                if (rooms.isEmpty()) {
                    binding.layoutEmptyHistory.visibility = View.VISIBLE
                    binding.rvHistoryRooms.visibility = View.GONE
                } else {
                    binding.layoutEmptyHistory.visibility = View.GONE
                    binding.rvHistoryRooms.visibility = View.VISIBLE
                    adapter.updateData(rooms)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHistory()
    }
}
