package com.unistay.android.presentation.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.unistay.android.R
import com.unistay.android.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var isFilterVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
    }

    // Đưa việc nạp dữ liệu Dropdown vào onResume để không bị mất lựa chọn khi chuyển Tab
    override fun onResume() {
        super.onResume()
        setupDropdownMenus()
    }

    private fun setupDropdownMenus() {
        // Dropdown Tỉnh/Thành phố
        val cities = listOf("Hà Nội", "Hồ Chí Minh", "Đà Nẵng")
        binding.actvCity.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        )

        // Dropdown Trường đại học
        val universities = listOf(
            "Tất cả các trường quanh Hà Nội",
            "Đại học Giao thông Vận tải (UTC)",
            "Đại học Ngoại Thương (FTU)",
            "Đại học Bách Khoa (BKHN)",
            "Đại học Quốc Gia Hà Nội (VNU)",
            "Học viện Ngoại Giao (DAV)",
            "Đại học Luật Hà Nội"
        )
        binding.actvUniversity.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, universities)
        )

        // Dropdown Quận/huyện
        val districts = listOf("Tất cả quận/huyện", "Cầu Giấy", "Đống Đa", "Hai Bà Trưng", "Thanh Xuân", "Ba Đình", "Nam Từ Liêm")
        binding.actvDistrict.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, districts)
        )

        // Dropdown Loại phòng (Nếu có)
        // Nếu trong XML của bạn không có ID actvRoomType thì hãy xóa 4 dòng dưới đây đi nhé
        val roomTypes = listOf("Chọn loại phòng", "Phòng trọ khép kín", "Chung cư mini", "Căn hộ dịch vụ", "Ở ghép / Ký túc xá")
        binding.actvRoomType?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roomTypes)
        )
    }

    private fun setupButtons() {
        // KHÔI PHỤC NÚT QUAY LẠI
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Nút Thu gọn / Hiện bộ lọc
        binding.btnToggleFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            if (isFilterVisible) {
                binding.layoutFilterForm.visibility = View.VISIBLE
                binding.btnToggleFilter.text = "⏏ Thu gọn bộ lọc"
            } else {
                binding.layoutFilterForm.visibility = View.GONE
                binding.btnToggleFilter.text = "▼ Hiện bộ lọc"
            }
        }

        // Nút Đặt lại
        binding.tvResetFilter.setOnClickListener {
            binding.actvCity.setText("Hà Nội", false)
            binding.actvUniversity.setText("Tất cả các trường quanh Hà Nội", false)
            binding.actvDistrict.setText("Tất cả quận/huyện", false)
            binding.actvRoomType?.setText("Chọn loại phòng", false)
            binding.chipGroupPrice.clearCheck()
            binding.chipGroupAmenities.clearCheck()

            // Phải nạp lại Adapter sau khi Reset để không bị kẹt UI
            setupDropdownMenus()

            Toast.makeText(requireContext(), "Đã đặt lại toàn bộ bộ lọc", Toast.LENGTH_SHORT).show()
        }

        // Nút Áp dụng tìm kiếm
        binding.btnApply.setOnClickListener {
            var minPrice = 0.0
            var maxPrice = Double.MAX_VALUE

            when (binding.chipGroupPrice.checkedChipId) {
                R.id.chipPriceUnder2 -> { minPrice = 0.0; maxPrice = 2000000.0 }
                R.id.chipPrice2to3 -> { minPrice = 2000000.0; maxPrice = 3000000.0 }
                R.id.chipPrice3to4 -> { minPrice = 3000000.0; maxPrice = 4000000.0 }
                R.id.chipPrice4to5 -> { minPrice = 4000000.0; maxPrice = 5000000.0 }
                R.id.chipPrice5to6 -> { minPrice = 5000000.0; maxPrice = 6000000.0 }
                R.id.chipPrice6to8 -> { minPrice = 6000000.0; maxPrice = 8000000.0 }
            }

            // Lấy danh sách tiện ích đã tích chọn một cách an toàn
            val selectedAmenities = ArrayList<String>()
            for (id in binding.chipGroupAmenities.checkedChipIds) {
                val chip = binding.chipGroupAmenities.findViewById<com.google.android.material.chip.Chip>(id)
                val chipText = chip?.text?.toString()?.trim()
                if (!chipText.isNullOrEmpty()) {
                    selectedAmenities.add(chipText)
                }
            }

            val selectedUniversity = binding.actvUniversity.text.toString().trim()
            val selectedDistrict = binding.actvDistrict.text.toString().trim()
            val selectedRoomType = binding.actvRoomType?.text?.toString()?.trim() ?: ""

            val intent = Intent(requireContext(), SearchResultActivity::class.java).apply {
                putExtra("EXTRA_UNIVERSITY", selectedUniversity)
                putExtra("EXTRA_DISTRICT", selectedDistrict)
                putExtra("EXTRA_ROOM_TYPE", selectedRoomType)
                putExtra("EXTRA_MIN_PRICE", minPrice)
                putExtra("EXTRA_MAX_PRICE", maxPrice)
                putStringArrayListExtra("EXTRA_AMENITIES", selectedAmenities)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}