package com.unistay.android.presentation.create

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.unistay.android.databinding.FragmentCreateRoomBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateRoomFragment : Fragment() {

    private var _binding: FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateRoomViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivRoomImage.setImageURI(uri)
            binding.llImagePlaceholder.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isSubmitting = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()

        // Xử lý chọn ảnh
        binding.cvImagePicker.setOnClickListener {
            if (!isSubmitting) {
                imagePickerLauncher.launch("image/*")
            }
        }

        // Xử lý khi bấm nút Đăng phòng (Chống bấm nhiều lần + Reset sạch form)
        binding.btnSubmitRoom.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            val title = binding.edtTitle.text.toString().trim()
            val priceStr = binding.edtPrice.text.toString().trim()
            val phone = binding.edtPhone.text.toString().trim()
            val address = binding.edtAddress.text.toString().trim()
            val district = binding.actvDistrict.text.toString().trim()
            val university = binding.actvUniversity.text.toString().trim()
            val distanceStr = binding.edtDistance.text.toString().trim()
            val maxOccupancyStr = binding.edtMaxOccupancy.text.toString().trim()
            val description = binding.edtDescription.text.toString().trim()
            val roomType = binding.actvRoomType.text.toString().trim().ifEmpty { "Phòng trọ khép kín" }

            // Validate sơ bộ
            if (title.isEmpty() || priceStr.isEmpty() || phone.isEmpty() || address.isEmpty() || distanceStr.isEmpty() || maxOccupancyStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ các thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            val distance = distanceStr.toDoubleOrNull() ?: 0.0
            val maxOccupancy = maxOccupancyStr.toIntOrNull() ?: 1

            // Khóa nút để chống gửi lặp nhiều lần
            isSubmitting = true
            binding.btnSubmitRoom.isEnabled = false
            binding.btnSubmitRoom.text = "⏳ Đang đăng tin phòng..."

            // Lấy danh sách tiện ích từ ChipGroup một cách an toàn
            val checkedChipIds = binding.cgAmenities.checkedChipIds
            val amenitiesList = mutableListOf<String>()
            for (id in checkedChipIds) {
                val chip = binding.cgAmenities.findViewById<com.google.android.material.chip.Chip>(id)
                chip?.text?.toString()?.let { amenitiesList.add(it) }
            }
            val amenities = amenitiesList.joinToString(", ")

            val imageFile = selectedImageUri?.let { getFileFromUri(it) }

            // Gọi ViewModel lưu vào DB
            viewModel.createNewRoom(
                title = title, price = price, phone = phone, address = address, district = district,
                university = university, distance = distance, maxOccupancy = maxOccupancy, amenities = amenities, description = description,
                roomType = roomType,
                imageFile = imageFile,
                onSuccess = {
                    Toast.makeText(requireContext(), "🎉 Đăng tin thành công!", Toast.LENGTH_LONG).show()

                    // Reset hoàn toàn toàn bộ form về trạng thái ban đầu
                    binding.edtTitle.text?.clear()
                    binding.edtPrice.text?.clear()
                    binding.edtPhone.text?.clear()
                    binding.edtAddress.text?.clear()
                    binding.edtDistance.text?.clear()
                    binding.edtMaxOccupancy.text?.clear()
                    binding.edtDescription.text?.clear()
                    binding.actvRoomType.setText("Phòng trọ khép kín", false)
                    binding.actvUniversity.setText("UTC", false)
                    binding.actvDistrict.setText("Cầu Giấy", false)
                    binding.cgAmenities.clearCheck()
                    binding.ivRoomImage.setImageDrawable(null)
                    binding.llImagePlaceholder.visibility = View.VISIBLE
                    selectedImageUri = null

                    // Mở lại nút đăng
                    binding.btnSubmitRoom.isEnabled = true
                    binding.btnSubmitRoom.text = "Đăng tin cho thuê ngay"
                    isSubmitting = false

                    // Cuộn trang lên trên cùng để sẵn sàng đăng tin mới
                    binding.scrollViewCreateRoom.smoothScrollTo(0, 0)
                },
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    binding.btnSubmitRoom.isEnabled = true
                    binding.btnSubmitRoom.text = "Đăng tin cho thuê ngay"
                    isSubmitting = false
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setupDropdowns()
    }

    private fun setupDropdowns() {
        // Khởi tạo menu Dropdown chọn trường
        val universities = arrayOf("UTC", "FTU", "BKHN", "VNU", "NEU")
        val uniAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, universities)
        binding.actvUniversity.setAdapter(uniAdapter)

        // Khởi tạo menu Dropdown chọn Quận
        val districts = arrayOf("Cầu Giấy", "Đống Đa", "Thanh Xuân", "Hai Bà Trưng", "Hoàng Mai", "Nam Từ Liêm", "Bắc Từ Liêm")
        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, districts)
        binding.actvDistrict.setAdapter(districtAdapter)

        // Khởi tạo menu Dropdown chọn Loại phòng
        val roomTypes = arrayOf("Phòng trọ khép kín", "Chung cư mini", "Căn hộ dịch vụ", "Ở ghép / Ký túc xá")
        val roomTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roomTypes)
        binding.actvRoomType.setAdapter(roomTypeAdapter)
    }

    private fun getFileFromUri(uri: Uri): java.io.File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = java.io.File.createTempFile("upload", ".jpg", requireContext().cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}