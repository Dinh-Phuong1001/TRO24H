package com.unistay.android.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unistay.android.databinding.ItemRoomCardBinding
import com.unistay.android.domain.model.model.Room

class RoomAdapter(
    private var rooms: List<Room>,
    private val onItemClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(val binding: ItemRoomCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        with(holder.binding) {
            // Liên kết các ID mới từ bản thiết kế an toàn
            tvRoomTitle.text = room.title ?: ""
            tvRoomAddress.text = room.address ?: ""
            tvRoomTypeTag.text = room.roomType ?: "Phòng trọ"

            // Tính toán và hiển thị giá
            val priceInMillion = room.basePrice / 1_000_000.0
            tvRoomPrice.text = String.format("%.1f triệu", priceInMillion)

            // Hiển thị trạng thái chỗ trống (ví dụ: Trống 2/4)
            val maxOcc = if (room.maxOccupancy > 0) room.maxOccupancy else 1
            val availableSpots = (maxOcc - room.currentOccupancy).coerceAtLeast(0)
            tvRoomStatus.text = "🏠 Trống $availableSpots/$maxOcc"

            // Load ảnh bằng thư viện Coil
            ivRoomImage.load(room.imageUrl) {
                crossfade(true)
            }

            // Gắn sự kiện click cho TOÀN BỘ thẻ phòng để mở màn hình Chi tiết
            root.setOnClickListener {
                onItemClick(room)
            }
        }
    }

    override fun getItemCount(): Int = rooms.size

    fun updateData(newRooms: List<Room>) {
        rooms = newRooms
        notifyDataSetChanged()
    }
}