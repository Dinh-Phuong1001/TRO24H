package com.unistay.android.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.NavigationUI
import com.unistay.android.R
import com.unistay.android.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject
import com.unistay.android.data.local.datastore.UserSessionManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Tự custom sự kiện click vào các tab thay vì dùng setupWithNavController
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_placeholder) return@setOnItemSelectedListener true
            
            val startDestId = navController.graph.startDestinationId
            navController.popBackStack(startDestId, false)
            if (item.itemId != startDestId) {
                navController.navigate(item.itemId)
            }
            true
        }

        // Xử lý lỗi: Khi bấm vào tab cũ nhưng đang ở màn hình khác (Đăng tin)
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_placeholder) return@setOnItemReselectedListener
            
            if (navController.currentDestination?.id != item.itemId) {
                val startDestId = navController.graph.startDestinationId
                navController.popBackStack(startDestId, false)
                if (item.itemId != startDestId) {
                    navController.navigate(item.itemId)
                }
            }
        }

        // Lấy thông tin user hiện tại
        var currentRole = ""
        var currentUserId = ""
        lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(sessionManager.userRole, sessionManager.userId) { role, id ->
                Pair(role, id)
            }.collect { (role, id) ->
                currentRole = role ?: ""
                currentUserId = id ?: ""
            }
        }

        // Bắt sự kiện khi bấm vào nút dấu cộng (FAB) ở giữa
        binding.fabPostRoom.setOnClickListener {
            if (currentUserId.isEmpty()) {
                android.widget.Toast.makeText(this, "Vui lòng đăng nhập để đăng tin", android.widget.Toast.LENGTH_SHORT).show()
                startActivity(android.content.Intent(this, com.unistay.android.presentation.auth.LoginActivity::class.java))
                return@setOnClickListener
            }

            if (currentRole.uppercase() != "LANDLORD") {
                android.widget.Toast.makeText(this, "Tài khoản sinh viên không thể đăng tin", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (navController.currentDestination?.id != R.id.nav_create) {
                // Ép thanh menu chuyển trạng thái active sang nút ẩn (placeholder)
                binding.bottomNavigation.selectedItemId = R.id.nav_placeholder
                navController.navigate(R.id.nav_create)
            }
        }

        // Đồng bộ lại thanh menu khi người dùng bấm nút Back (Trở về) của điện thoại
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.nav_create && destination.id != R.id.nav_placeholder) {
                binding.bottomNavigation.menu.findItem(destination.id)?.isChecked = true
            }
        }

        // Vòng lặp lấy thông báo tin nhắn chưa đọc
        lifecycleScope.launch {
            while (true) {
                val userId = sessionManager.userId.firstOrNull()
                if (!userId.isNullOrEmpty()) {
                    try {
                        val sessions = com.unistay.android.data.remote.RetrofitClient.instance.getChatSessions(userId)
                        var totalUnread = 0
                        for (session in sessions) {
                            if (session.landlordId == userId) {
                                totalUnread += session.landlordUnreadCount
                            } else {
                                totalUnread += session.studentUnreadCount
                            }
                        }
                        
                        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_chat)
                        if (totalUnread > 0) {
                            badge.isVisible = true
                            badge.number = totalUnread
                            badge.backgroundColor = android.graphics.Color.parseColor("#EF4444")
                            badge.badgeTextColor = android.graphics.Color.WHITE
                        } else {
                            badge.isVisible = false
                            binding.bottomNavigation.removeBadge(R.id.nav_chat)
                        }
                    } catch (e: Exception) {
                        // Bỏ qua lỗi mạng
                    }
                }
                delay(3000)
            }
        }
    }
}