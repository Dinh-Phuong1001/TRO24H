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
                putExtra("ROOM_PHONE", room.contactPhone)
                putExtra("ROOM_TYPE", room.roomType)
                putExtra("OWNER_ID", room.ownerId)
                putExtra("OWNER_NAME", room.ownerName)
                putExtra("ROOM_DESCRIPTION", room.description)
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
            val actvPeopleCount = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.actvPeopleCount)
            val actvAcUsage = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.actvAcUsage)

            val tvElecCost = dialogView.findViewById<android.widget.TextView>(R.id.tvElecCost)
            val tvServiceCost = dialogView.findViewById<android.widget.TextView>(R.id.tvServiceCost)
            val tvTotalPerPerson = dialogView.findViewById<android.widget.TextView>(R.id.tvTotalPerPerson)

            val btnCloseDialog = dialogView.findViewById<android.widget.ImageView>(R.id.btnCloseDialog)
            val btnUnderstood = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnUnderstood)

            val peopleArray = arrayOf("1 bạn", "2 bạn", "3 bạn", "4 bạn")
            val acArray = arrayOf("Ít", "Trung bình", "Nhiều")

            actvPeopleCount.setAdapter(android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, peopleArray))
            actvAcUsage.setAdapter(android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, acArray))

            val formatter = java.text.DecimalFormat("#,###")
            fun calculateCosts() {
                val priceString = edtRoomPrice.text.toString()
                val roomPrice = if (priceString.isNotEmpty()) priceString.toDouble() else 0.0

                val peopleStr = actvPeopleCount.text.toString()
                val peopleCount = peopleStr.replace(" bạn", "").toIntOrNull() ?: 3

                val acStr = actvAcUsage.text.toString()
                val kwh = when (acStr) {
                    "Ít" -> 50.0
                    "Nhiều" -> 250.0
                    else -> 150.0
                }

                val elecCost = kwh * 3800.0
                val serviceCost = peopleCount * 150000.0
                val totalCostPerPerson = (roomPrice + elecCost + serviceCost) / peopleCount

                tvElecCost.text = "${formatter.format(elecCost).replace(',', '.')} đ"
                tvServiceCost.text = "${formatter.format(serviceCost).replace(',', '.')} đ"
                tvTotalPerPerson.text = "${formatter.format(totalCostPerPerson).replace(',', '.')} đ / người"
            }

            edtRoomPrice.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) { calculateCosts() }
            })

            actvPeopleCount.setOnItemClickListener { _, _, _, _ -> calculateCosts() }
            actvAcUsage.setOnItemClickListener { _, _, _, _ -> calculateCosts() }

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
