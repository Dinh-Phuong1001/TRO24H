package com.unistay.android.presentation.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.unistay.android.databinding.ActivitySearchResultBinding
import com.unistay.android.domain.model.model.Room
import com.unistay.android.presentation.home.RoomAdapter
import com.unistay.android.presentation.home.RoomDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@AndroidEntryPoint
class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var adapter: RoomAdapter
    private val viewModel: SearchViewModel by viewModels()

    private var originalRoomList: List<Room> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Các nút điều hướng
        binding.btnBackSearch.setOnClickListener {
            finish()
        }

        // Live Search khi người dùng nhập Text
        binding.edtKeyword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val keyword = s.toString().trim()
                val targetUni = intent.getStringExtra("EXTRA_UNIVERSITY") ?: ""
                val targetDistrict = intent.getStringExtra("EXTRA_DISTRICT") ?: ""
                val targetRoomType = intent.getStringExtra("EXTRA_ROOM_TYPE") ?: ""
                val minPrice = intent.getDoubleExtra("EXTRA_MIN_PRICE", 0.0)
                val maxPrice = intent.getDoubleExtra("EXTRA_MAX_PRICE", Double.MAX_VALUE)
                val selectedAmenities = intent.getStringArrayListExtra("EXTRA_AMENITIES") ?: arrayListOf()

                viewModel.searchRooms(keyword, targetUni, targetDistrict, minPrice, maxPrice, selectedAmenities, targetRoomType)
            }
        })

        // Vẫn giữ tính năng ẩn bàn phím khi ấn "Tìm kiếm" trên bàn phím
        binding.edtKeyword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }

        binding.filterBar.setOnClickListener {
            Toast.makeText(this, "Bấm mũi tên trái để đổi bộ lọc", Toast.LENGTH_SHORT).show()
        }

        binding.btnSort.setOnClickListener { view ->
            showSortMenu(view)
        }

        // Nhận dữ liệu lọc
        val targetUni = intent.getStringExtra("EXTRA_UNIVERSITY") ?: ""
        val targetDistrict = intent.getStringExtra("EXTRA_DISTRICT") ?: ""
        val targetRoomType = intent.getStringExtra("EXTRA_ROOM_TYPE") ?: ""
        val minPrice = intent.getDoubleExtra("EXTRA_MIN_PRICE", 0.0)
        val maxPrice = intent.getDoubleExtra("EXTRA_MAX_PRICE", Double.MAX_VALUE)
        val selectedAmenities = intent.getStringArrayListExtra("EXTRA_AMENITIES") ?: arrayListOf()

        setupActiveFilterChips(targetUni, targetDistrict, targetRoomType, minPrice, maxPrice, selectedAmenities)

        // Khởi tạo danh sách phòng
        adapter = RoomAdapter(emptyList()) { room: Room ->
            val intent = Intent(this, RoomDetailActivity::class.java).apply {
                putExtra("ROOM_ID", room.roomId)
                putExtra("TARGET_UNIVERSITY", room.targetUniversity ?: "")
                putExtra("DISTANCE", room.distanceToCampusKm)
                putExtra("ROOM_TITLE", room.title ?: "")
                putExtra("ROOM_ADDRESS", room.address ?: "")
                putExtra("ROOM_PRICE", room.basePrice)
                putExtra("ROOM_AMENITIES", room.amenities ?: "")
                putExtra("ROOM_IMAGE", room.imageUrl ?: "")
                putExtra("ROOM_PHONE", room.contactPhone ?: "")
                putExtra("ROOM_TYPE", room.roomType ?: "Phòng trọ khép kín")
                putExtra("OWNER_ID", room.ownerId ?: "")
                putExtra("OWNER_NAME", room.ownerName ?: "")
                putExtra("ROOM_DESCRIPTION", room.description ?: "")
            }
            startActivity(intent)
        }

        binding.rvResults.layoutManager = GridLayoutManager(this, 2)
        binding.rvResults.adapter = adapter

        // Bắt đầu lọc dữ liệu
        viewModel.searchRooms("", targetUni, targetDistrict, minPrice, maxPrice, selectedAmenities, targetRoomType)

        lifecycleScope.launch {
            viewModel.searchResults.collect { filteredRooms ->
                originalRoomList = filteredRooms
                if (filteredRooms.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvResults.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvResults.visibility = View.VISIBLE
                    adapter.updateData(filteredRooms)
                    // Reset sort text to default when new data arrives
                    binding.tvSortText.text = "Mới nhất"
                }
            }
        }
    }

    private fun showSortMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "🕒 Mới nhất")
        popup.menu.add(0, 2, 1, "📉 Giá từ thấp đến cao")
        popup.menu.add(0, 3, 2, "📈 Giá từ cao xuống thấp")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    adapter.updateData(originalRoomList)
                    binding.tvSortText.text = "Mới nhất"
                    true
                }
                2 -> {
                    val sorted = originalRoomList.sortedBy { it.basePrice }
                    adapter.updateData(sorted)
                    binding.tvSortText.text = "Giá tăng dần"
                    true
                }
                3 -> {
                    val sorted = originalRoomList.sortedByDescending { it.basePrice }
                    adapter.updateData(sorted)
                    binding.tvSortText.text = "Giá giảm dần"
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupActiveFilterChips(
        targetUni: String, district: String, targetRoomType: String,
        minPrice: Double, maxPrice: Double, amenities: List<String>
    ) {
        binding.chipGroupActiveFilters.removeAllViews()
        val formatter = DecimalFormat("#,###")

        if (targetUni.isNotEmpty() && !targetUni.startsWith("Tất cả")) addChip(targetUni)
        if (district.isNotEmpty() && !district.startsWith("Tất cả")) addChip(district)
        if (targetRoomType.isNotEmpty() && !targetRoomType.startsWith("Chọn") && !targetRoomType.startsWith("Tất cả")) addChip(targetRoomType)
        if (minPrice > 0 || maxPrice < Double.MAX_VALUE) {
            val priceText = "${formatter.format(minPrice / 1000000)}tr - ${formatter.format(maxPrice / 1000000)}tr"
            addChip(priceText)
        }
        amenities.forEach { addChip(it) }
    }

    private fun addChip(text: String) {
        val chip = Chip(this).apply {
            this.text = text
            isCheckable = false
            chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0F2FE"))
            chipStrokeWidth = 1f
            chipStrokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#7DD3FC"))
            setTextColor(android.graphics.Color.parseColor("#0284C7"))
        }
        binding.chipGroupActiveFilters.addView(chip)
    }
}