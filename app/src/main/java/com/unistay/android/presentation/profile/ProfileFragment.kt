package com.unistay.android.presentation.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.databinding.FragmentProfileBinding
import com.unistay.android.presentation.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import coil.load

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Field Injection chuẩn cho Fragment trong Hilt (phải dùng visibility mặc định hoặc public, không dùng lateinit var private)
    @Inject
    lateinit var sessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lắng nghe dữ liệu phiên đăng nhập thực tế
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(
                sessionManager.userName,
                sessionManager.userRole
            ) { name, role ->
                Pair(name, role)
            }.collect { (name, role) ->
                if (!name.isNullOrEmpty()) {
                    binding.tvUserName.text = name
                    binding.btnLoginLogout.text = "Đăng xuất"
                    
                    if (role?.uppercase() == "LANDLORD") {
                        binding.tvUserEmail.text = "Chủ trọ Tro24H"
                        binding.btnMyRooms.visibility = View.VISIBLE
                    } else {
                        binding.tvUserEmail.text = "Sinh viên Tro24H"
                        binding.btnMyRooms.visibility = View.GONE
                    }
                } else {
                    binding.tvUserName.text = "Chào bạn,"
                    binding.tvUserEmail.text = "Vui lòng đăng nhập"
                    binding.btnLoginLogout.text = "Đăng nhập"
                    binding.ivAvatar.clearColorFilter()
                    binding.ivAvatar.imageTintList = null
                    binding.ivAvatar.setImageResource(com.unistay.android.R.drawable.ic_default_avatar)
                    binding.btnMyRooms.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sessionManager.userAvatar.collect { rawAvatarUrl ->
                val avatarUrl = rawAvatarUrl?.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
                binding.ivAvatar.imageTintList = null
                if (!avatarUrl.isNullOrEmpty()) {
                    binding.ivAvatar.load(avatarUrl) {
                        crossfade(true)
                        placeholder(com.unistay.android.R.drawable.ic_default_avatar)
                        error(com.unistay.android.R.drawable.ic_default_avatar)
                        transformations(coil.transform.CircleCropTransformation())
                    }
                } else {
                    binding.ivAvatar.clearColorFilter()
                    binding.ivAvatar.imageTintList = null
                    binding.ivAvatar.setImageResource(com.unistay.android.R.drawable.ic_default_avatar)
                }
            }
        }

        binding.btnEditProfile.setOnClickListener {
            val currentName = binding.tvUserName.text.toString()
            if (currentName != "Chào bạn,") {
                startActivity(Intent(requireContext(), EditProfileActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để chỉnh sửa", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnSupport.setOnClickListener {
            showSupportBottomSheet()
        }

        binding.btnLoginLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val currentName = binding.tvUserName.text.toString()
                if (currentName != "Chào bạn,") {
                    // Tiến hành Đăng xuất
                    sessionManager.clearSession()
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                } else {
                    // Mở màn hình Login
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                }
            }
        }
        // Xử lý mở màn hình Phòng đã lưu
        binding.btnSavedRooms.setOnClickListener {
            val currentName = binding.tvUserName.text.toString()
            if (currentName != "Chào bạn,") { // Đã đăng nhập
                startActivity(Intent(requireContext(), SavedRoomsActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem phòng đã lưu", Toast.LENGTH_SHORT).show()
            }
        }
        // Xử lý mở màn hình Lịch sử xem phòng
        binding.btnHistory.setOnClickListener {
            val currentName = binding.tvUserName.text.toString()
            if (currentName != "Chào bạn,") { // Đã đăng nhập
                startActivity(Intent(requireContext(), ViewHistoryActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show()
            }
        }
        // Xử lý mở màn hình Phòng đã đăng
        binding.btnMyRooms.setOnClickListener {
            val currentName = binding.tvUserName.text.toString()
            if (currentName != "Chào bạn,") { // Đã đăng nhập
                startActivity(Intent(requireContext(), MyRoomsActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem phòng đã đăng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSupportBottomSheet() {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(com.unistay.android.R.layout.bottom_sheet_support, null)
        bottomSheet.setContentView(view)

        view.findViewById<android.widget.LinearLayout>(com.unistay.android.R.id.btnFacebook).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.facebook.com/inhphuong.893177"))
            startActivity(intent)
            bottomSheet.dismiss()
        }

        view.findViewById<android.widget.LinearLayout>(com.unistay.android.R.id.btnZalo).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://chat.zalo.me/"))
            startActivity(intent)
            bottomSheet.dismiss()
        }

        view.findViewById<android.widget.LinearLayout>(com.unistay.android.R.id.btnInstagram).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.instagram.com/phuong100125/"))
            startActivity(intent)
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}