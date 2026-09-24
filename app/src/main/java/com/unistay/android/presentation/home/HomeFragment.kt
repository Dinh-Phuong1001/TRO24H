package com.unistay.android.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.unistay.android.R
import com.unistay.android.databinding.FragmentHomeBinding
import com.unistay.android.domain.model.model.Room
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var roomAdapter: RoomAdapter

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCampusFilter()
        setupCalculatorDialog()

        binding.cardQuickSearch.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
            bottomNav?.selectedItemId = R.id.nav_search
        }

        // Lắng nghe dữ liệu đổ về từ ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rooms.collect { roomList ->
                roomAdapter.updateData(roomList)
            }
        }
    }

    // ==========================================
    // Tải lại toàn bộ phòng từ Database mỗi khi mở lại Tab
    // ==========================================
    override fun onResume() {
        super.onResume()
        viewModel.loadRooms("ALL")
        binding.chipAll.isChecked = true // Trả filter về "Tất cả"
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(emptyList()) { room: Room ->
            val intent = Intent(requireContext(), RoomDetailActivity::class.java).apply {
                putExtra("ROOM_ID", room.roomId)
                putExtra("TARGET_UNIVERSITY", room.targetUniversity)
                putExtra("DISTANCE", room.distanceToCampusKm)
                putExtra("ROOM_TITLE", room.title)
                putExtra("ROOM_ADDRESS", room.address)
                putExtra("ROOM_PRICE", room.basePrice)
                putExtra("ROOM_AMENITIES", room.amenities)
                putExtra("ROOM_IMAGE", room.imageUrl)
                putExtra("ROOM_PHONE", room.contactPhone ?: "")
                putExtra("ROOM_TYPE", room.roomType ?: "Phòng trọ khép kín")
                putExtra("MAX_OCCUPANCY", room.maxOccupancy)
                putExtra("CURRENT_OCCUPANCY", room.currentOccupancy)
                putExtra("OWNER_ID", room.ownerId ?: "")
                putExtra("OWNER_NAME", room.ownerName ?: "")
                putExtra("ROOM_DESCRIPTION", room.description ?: "")
            }
            startActivity(intent)
        }

        binding.rvRooms.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = roomAdapter
        }
    }

    private fun setupCampusFilter() {
        binding.chipAll.setOnClickListener { viewModel.loadRooms("ALL") }
        binding.chipUtc.setOnClickListener { viewModel.loadRooms("UTC") }
        binding.chipFtu.setOnClickListener { viewModel.loadRooms("FTU") }
        binding.chipBkhn.setOnClickListener { viewModel.loadRooms("BKHN") }
        binding.chipVnu.setOnClickListener { viewModel.loadRooms("VNU") }
    }

    private fun setupCalculatorDialog() {
        binding.cardCalculator.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rent_calculator, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val edtRoomPrice = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtRoomPrice)
            val tvPriceFormatted = dialogView.findViewById<android.widget.TextView>(R.id.tvPriceFormatted)

            val btnPreset20 = dialogView.findViewById<android.widget.TextView>(R.id.btnPreset20)
            val btnPreset35 = dialogView.findViewById<android.widget.TextView>(R.id.btnPreset35)
            val btnPreset45 = dialogView.findViewById<android.widget.TextView>(R.id.btnPreset45)
            val btnPreset60 = dialogView.findViewById<android.widget.TextView>(R.id.btnPreset60)

            val chipGroupPeople = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupPeople)
            val chipGroupAc = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupAc)

            val cbWifi = dialogView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbWifi)
            val cbWater = dialogView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbWater)
            val cbCleaning = dialogView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbCleaning)
            val cbMotorbike = dialogView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbMotorbike)

            val tvElecCost = dialogView.findViewById<android.widget.TextView>(R.id.tvElecCost)
            val tvServiceCost = dialogView.findViewById<android.widget.TextView>(R.id.tvServiceCost)
            val tvTotalRoomCost = dialogView.findViewById<android.widget.TextView>(R.id.tvTotalRoomCost)
            val tvTotalPerPerson = dialogView.findViewById<android.widget.TextView>(R.id.tvTotalPerPerson)

            val btnCopyBreakdown = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCopyBreakdown)
            val btnCloseDialog = dialogView.findViewById<android.widget.ImageView>(R.id.btnCloseDialog)
            val btnUnderstood = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnUnderstood)

            val formatter = java.text.DecimalFormat("#,###")

            fun calculateCosts() {
                val priceString = edtRoomPrice.text.toString().trim()
                val roomPrice = priceString.toDoubleOrNull() ?: 0.0

                val peopleCount = when (chipGroupPeople.checkedChipId) {
                    R.id.chipP1 -> 1
                    R.id.chipP2 -> 2
                    R.id.chipP4 -> 4
                    else -> 3
                }

                val acKwh = when (chipGroupAc.checkedChipId) {
                    R.id.chipAcLow -> 50.0
                    R.id.chipAcHigh -> 280.0
                    else -> 150.0
                }

                val elecCost = acKwh * 3800.0

                val wifiCost = if (cbWifi.isChecked) 100_000.0 else 0.0
                val waterCost = if (cbWater.isChecked) peopleCount * 100_000.0 else 0.0
                val cleaningCost = if (cbCleaning.isChecked) 50_000.0 else 0.0
                val motorbikeCost = if (cbMotorbike.isChecked) peopleCount * 100_000.0 else 0.0
                val serviceCost = wifiCost + waterCost + cleaningCost + motorbikeCost

                val totalRoomCost = roomPrice + elecCost + serviceCost
                val totalCostPerPerson = totalRoomCost / peopleCount

                tvPriceFormatted.text = "${formatter.format(roomPrice).replace(',', '.')} đ"
                tvElecCost.text = "${formatter.format(elecCost).replace(',', '.')} đ"
                tvServiceCost.text = "${formatter.format(serviceCost).replace(',', '.')} đ"
                tvTotalRoomCost.text = "${formatter.format(totalRoomCost).replace(',', '.')} đ"
                tvTotalPerPerson.text = "${formatter.format(totalCostPerPerson).replace(',', '.')} đ / người"
            }

            // Quick preset handlers
            fun updatePresetColors(selectedPreset: android.widget.TextView) {
                val presets = listOf(btnPreset20, btnPreset35, btnPreset45, btnPreset60)
                presets.forEach {
                    if (it == selectedPreset) {
                        it.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DBEAFE"))
                        it.setTextColor(android.graphics.Color.parseColor("#2563EB"))
                        it.setTypeface(null, android.graphics.Typeface.BOLD)
                    } else {
                        it.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9"))
                        it.setTextColor(android.graphics.Color.parseColor("#475569"))
                        it.setTypeface(null, android.graphics.Typeface.NORMAL)
                    }
                }
            }

            btnPreset20.setOnClickListener {
                updatePresetColors(btnPreset20)
                edtRoomPrice.setText("2500000")
            }
            btnPreset35.setOnClickListener {
                updatePresetColors(btnPreset35)
                edtRoomPrice.setText("3500000")
            }
            btnPreset45.setOnClickListener {
                updatePresetColors(btnPreset45)
                edtRoomPrice.setText("4500000")
            }
            btnPreset60.setOnClickListener {
                updatePresetColors(btnPreset60)
                edtRoomPrice.setText("6000000")
            }

            edtRoomPrice.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) { calculateCosts() }
            })

            chipGroupPeople.setOnCheckedStateChangeListener { _, _ -> calculateCosts() }
            chipGroupAc.setOnCheckedStateChangeListener { _, _ -> calculateCosts() }

            cbWifi.setOnCheckedChangeListener { _, _ -> calculateCosts() }
            cbWater.setOnCheckedChangeListener { _, _ -> calculateCosts() }
            cbCleaning.setOnCheckedChangeListener { _, _ -> calculateCosts() }
            cbMotorbike.setOnCheckedChangeListener { _, _ -> calculateCosts() }

            // Nút sao chép gửi Zalo
            btnCopyBreakdown.setOnClickListener {
                val priceString = edtRoomPrice.text.toString().trim()
                val roomPrice = priceString.toDoubleOrNull() ?: 0.0
                val peopleCount = when (chipGroupPeople.checkedChipId) {
                    R.id.chipP1 -> 1
                    R.id.chipP2 -> 2
                    R.id.chipP4 -> 4
                    else -> 3
                }
                val acKwh = when (chipGroupAc.checkedChipId) {
                    R.id.chipAcLow -> 50.0
                    R.id.chipAcHigh -> 280.0
                    else -> 150.0
                }
                val elecCost = acKwh * 3800.0
                val wifiCost = if (cbWifi.isChecked) 100_000.0 else 0.0
                val waterCost = if (cbWater.isChecked) peopleCount * 100_000.0 else 0.0
                val cleaningCost = if (cbCleaning.isChecked) 50_000.0 else 0.0
                val motorbikeCost = if (cbMotorbike.isChecked) peopleCount * 100_000.0 else 0.0
                val serviceCost = wifiCost + waterCost + cleaningCost + motorbikeCost
                val totalRoomCost = roomPrice + elecCost + serviceCost
                val costPerPerson = totalRoomCost / peopleCount

                val breakdownText = """
                    🏠 [TRO24H] DỰ TOÁN TIỀN PHÒNG & CHI PHÍ
                    ---------------------------------------
                    • Tiền phòng: ${formatter.format(roomPrice).replace(',', '.')} đ
                    • Tiền điện (${acKwh.toInt()} số @3.8k): ${formatter.format(elecCost).replace(',', '.')} đ
                    • Nước & dịch vụ: ${formatter.format(serviceCost).replace(',', '.')} đ
                    ---------------------------------------
                    👉 TỔNG CẢ PHÒNG: ${formatter.format(totalRoomCost).replace(',', '.')} đ
                    👥 SỐ BẠN Ở: $peopleCount người
                    💰 MỖI BẠN ĐÓNG: ${formatter.format(costPerPerson).replace(',', '.')} đ / tháng
                """.trimIndent()

                val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Rent Breakdown", breakdownText)
                clipboard.setPrimaryClip(clip)

                android.widget.Toast.makeText(
                    requireContext(),
                    "Đã sao chép! Hãy dán vào nhóm Zalo phòng trọ nhé 🎉",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }

            calculateCosts()

            btnCloseDialog.setOnClickListener { dialog.dismiss() }
            btnUnderstood.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
