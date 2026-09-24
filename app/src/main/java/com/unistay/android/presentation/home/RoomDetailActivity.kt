package com.unistay.android.presentation.home

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.chip.Chip
import com.unistay.android.databinding.ActivityRoomDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@AndroidEntryPoint
class RoomDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailBinding
    private val viewModel: RoomDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roomId = intent.getStringExtra("ROOM_ID") ?: ""
        val targetUniversity = intent.getStringExtra("TARGET_UNIVERSITY") ?: "ĐH"
        val distance = intent.getDoubleExtra("DISTANCE", 0.0)
        val title = intent.getStringExtra("ROOM_TITLE") ?: "Căn Hộ Dịch Vụ Cho Thuê"
        val ownerName = intent.getStringExtra("OWNER_NAME") ?: "Không rõ"
        val address = intent.getStringExtra("ROOM_ADDRESS") ?: "Hà Nội"
        val price = intent.getDoubleExtra("ROOM_PRICE", 3800000.0)
        val amenities = intent.getStringExtra("ROOM_AMENITIES") ?: ""
        val imageUrl = intent.getStringExtra("ROOM_IMAGE") ?: ""
        val phone = intent.getStringExtra("ROOM_PHONE") ?: ""
        val roomType = intent.getStringExtra("ROOM_TYPE") ?: "Phòng trọ khép kín"
        val ownerId = intent.getStringExtra("OWNER_ID") ?: "chu_tro_id_123"
        val description = intent.getStringExtra("ROOM_DESCRIPTION")

        viewModel.checkSavedStatus(roomId)
        viewModel.recordViewHistory(roomId)
        viewModel.checkIsOwner(ownerId)

        lifecycleScope.launch {
            viewModel.isSaved.collect { isSaved ->
                if (isSaved) {
                    binding.btnSaveRoom.setImageResource(com.unistay.android.R.drawable.ic_save_filled)
                    binding.btnSaveRoom.setColorFilter(Color.parseColor("#2563EB"))
                } else {
                    binding.btnSaveRoom.setImageResource(com.unistay.android.R.drawable.ic_save_outline)
                    binding.btnSaveRoom.setColorFilter(Color.parseColor("#64748B"))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isOwner.collect { isOwner ->
                if (isOwner) {
                    binding.bottomBar.visibility = View.GONE
                    binding.bottomBarOwner.visibility = View.VISIBLE
                    binding.btnSaveRoom.visibility = View.GONE
                } else {
                    binding.bottomBar.visibility = View.VISIBLE
                    binding.bottomBarOwner.visibility = View.GONE
                    binding.btnSaveRoom.visibility = View.VISIBLE
                }
            }
        }

        binding.btnSaveRoom.setOnClickListener {
            viewModel.toggleSaveRoom(roomId) {
                Toast.makeText(this@RoomDetailActivity, "Bạn cần đăng nhập để lưu phòng!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDeleteRoomBottom.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bài đăng phòng trọ này không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa") { _, _ ->
                    viewModel.deleteRoom(
                        roomId = roomId,
                        onSuccess = {
                            Toast.makeText(this@RoomDetailActivity, "Đã xóa bài đăng phòng thành công", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onError = { error ->
                            Toast.makeText(this@RoomDetailActivity, error, Toast.LENGTH_LONG).show()
                        }
                    )
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        val formatter = DecimalFormat("#,###")
        val formattedPrice = formatter.format(price)

        with(binding) {
            tvUniversityBadge.text = "📍 Gần $targetUniversity (${String.format("%.1f", distance)} km)"
            tvRoomTypeBadge.text = roomType
            tvDetailTitle.text = title
            tvDetailOwnerName.text = "Người đăng: $ownerName"
            tvDetailAddress.text = address
            tvDetailPrice.text = formattedPrice
            tvBottomPrice.text = "$formattedPrice đ"

            if (!description.isNullOrEmpty()) {
                cardDescription.visibility = View.VISIBLE
                tvDetailDescription.text = description
            } else {
                cardDescription.visibility = View.GONE
            }

            ivDetailImage.load(imageUrl) { crossfade(true) }

            chipGroupAmenities.removeAllViews()
            val listAmenities = amenities.split(",").map { it.trim() }
            for (amenity in listAmenities) {
                if (amenity.isNotEmpty()) {
                    val chip = Chip(this@RoomDetailActivity).apply {
                        text = "✔ $amenity"
                        isCheckable = false
                        isClickable = false
                        setChipBackgroundColorResource(android.R.color.white)
                        setChipStrokeColorResource(android.R.color.darker_gray)
                        chipStrokeWidth = 1.5f
                        textSize = 12f
                    }
                    chipGroupAmenities.addView(chip)
                }
            }

            viewModel.loadSuggestedRoom(targetUniversity, roomId)

            lifecycleScope.launch {
                viewModel.suggestedRoom.collect { suggestedRoom ->
                    if (suggestedRoom != null) {
                        tvSuggestTitle.visibility = View.VISIBLE
                        cardSuggestedRoom.visibility = View.VISIBLE

                        tvSuggestTitle.text = "🛏️ Phòng khác gần $targetUniversity"
                        tvSuggestTitleRoom.text = suggestedRoom.title
                        tvSuggestDistance.text = "📍 Cách trường ${String.format("%.1f", suggestedRoom.distanceToCampusKm)} km"
                        tvSuggestPrice.text = "${formatter.format(suggestedRoom.basePrice)} đ/tháng"

                        ivSuggestImage.load(suggestedRoom.imageUrl) { crossfade(true) }

                        cardSuggestedRoom.setOnClickListener {
                            val intent = Intent(this@RoomDetailActivity, RoomDetailActivity::class.java).apply {
                                putExtra("ROOM_ID", suggestedRoom.roomId)
                                putExtra("TARGET_UNIVERSITY", suggestedRoom.targetUniversity)
                                putExtra("DISTANCE", suggestedRoom.distanceToCampusKm)
                                putExtra("ROOM_TITLE", suggestedRoom.title)
                                putExtra("ROOM_ADDRESS", suggestedRoom.address)
                                putExtra("ROOM_PRICE", suggestedRoom.basePrice)
                                putExtra("ROOM_AMENITIES", suggestedRoom.amenities)
                                putExtra("ROOM_IMAGE", suggestedRoom.imageUrl)
                                putExtra("ROOM_PHONE", suggestedRoom.contactPhone)
                                putExtra("OWNER_ID", suggestedRoom.ownerId)
                                putExtra("OWNER_NAME", suggestedRoom.ownerName)
                                putExtra("ROOM_DESCRIPTION", suggestedRoom.description)
                            }
                            startActivity(intent)
                        }
                    } else {
                        tvSuggestTitle.visibility = View.GONE
                        cardSuggestedRoom.visibility = View.GONE
                    }
                }
            }

            btnBack.setOnClickListener { finish() }

            btnCallOwner.setOnClickListener {
                if (phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phone") }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@RoomDetailActivity, "Chủ nhà chưa cập nhật SĐT", Toast.LENGTH_SHORT).show()
                }
            }

            btnChat.setOnClickListener {
                viewModel.getOrCreateChatSession(
                    roomId = roomId,
                    roomTitle = title,
                    landlordId = ownerId,
                    landlordName = ownerName,
                    landlordPhone = phone,
                    onSessionReady = { sessionId ->
                        val chatIntent = Intent(this@RoomDetailActivity, com.unistay.android.presentation.chat.ChatActivity::class.java).apply {
                            putExtra("SESSION_ID", sessionId)
                            putExtra("PARTNER_NAME", ownerName)
                            putExtra("ROOM_TITLE", title)
                            putExtra("PARTNER_PHONE", phone)
                            putExtra("ROOM_ID", roomId)
                            putExtra("LANDLORD_ID", ownerId)
                        }
                        startActivity(chatIntent)
                    },
                    onError = { errorMessage ->
                        Toast.makeText(this@RoomDetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
