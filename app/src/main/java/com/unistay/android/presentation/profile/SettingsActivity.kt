package com.unistay.android.presentation.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unistay.android.databinding.ActivitySettingsBinding
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.unistay.android.data.local.datastore.UserSessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: UserSessionManager

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val isNotiEnabled = sessionManager.isNotificationsEnabled.firstOrNull() ?: true
            binding.switchNotifications.isChecked = isNotiEnabled
            
            binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    sessionManager.saveSettings(isChecked)
                    val msg = if (isChecked) "Đã bật thông báo" else "Đã tắt thông báo"
                    Toast.makeText(this@SettingsActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnClearCache.setOnClickListener {
            // Xóa cache của ứng dụng
            this.cacheDir.deleteRecursively()
            Toast.makeText(this, "Đã dọn dẹp bộ nhớ đệm", Toast.LENGTH_SHORT).show()
        }

        binding.btnShareApp.setOnClickListener {
            val sendIntent: android.content.Intent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, "Hãy tải ngay ứng dụng Tro24H để tìm phòng trọ dễ dàng nhé!")
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.btnRateApp.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("market://details?id=com.unistay.android")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                intent.data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.unistay.android")
                startActivity(intent)
            }
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordBottomSheet()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showDeleteAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xóa tài khoản")
            .setMessage("Bạn có chắc chắn muốn xóa tài khoản không? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa") { _, _ ->
                executeDeleteAccount()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun executeDeleteAccount() {
        lifecycleScope.launch {
            try {
                val userId = sessionManager.userId.firstOrNull()
                if (userId == null) {
                    Toast.makeText(this@SettingsActivity, "Lỗi đăng nhập", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = com.unistay.android.data.remote.RetrofitClient.instance.deleteUser(userId)
                if (response.isSuccessful) {
                    Toast.makeText(this@SettingsActivity, "Xóa tài khoản thành công", Toast.LENGTH_SHORT).show()
                    sessionManager.clearSession()
                    
                    // Chuyển về màn hình đăng nhập và xóa lịch sử
                    val intent = android.content.Intent(this@SettingsActivity, com.unistay.android.presentation.auth.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SettingsActivity, "Có lỗi xảy ra, vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordBottomSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(com.unistay.android.R.layout.bottom_sheet_change_password, null)
        bottomSheet.setContentView(view)

        val edtOldPassword = view.findViewById<android.widget.EditText>(com.unistay.android.R.id.edtOldPassword)
        val edtNewPassword = view.findViewById<android.widget.EditText>(com.unistay.android.R.id.edtNewPassword)
        val edtConfirmPassword = view.findViewById<android.widget.EditText>(com.unistay.android.R.id.edtConfirmPassword)
        val btnSubmit = view.findViewById<com.google.android.material.button.MaterialButton>(com.unistay.android.R.id.btnSubmitChangePassword)

        btnSubmit.setOnClickListener {
            val oldPassword = edtOldPassword.text.toString()
            val newPassword = edtNewPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Đang xử lý..."

            lifecycleScope.launch {
                try {
                    val userId = sessionManager.userId.firstOrNull()
                    if (userId == null) {
                        Toast.makeText(this@SettingsActivity, "Lỗi đăng nhập", Toast.LENGTH_SHORT).show()
                        bottomSheet.dismiss()
                        return@launch
                    }

                    val request = com.unistay.android.data.remote.api.ChangePasswordRequest(userId, oldPassword, newPassword)
                    val response = com.unistay.android.data.remote.RetrofitClient.instance.changePassword(request)

                    if (response.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                        bottomSheet.dismiss()
                    } else {
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Cập nhật mật khẩu"
                        Toast.makeText(this@SettingsActivity, "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Cập nhật mật khẩu"
                    Toast.makeText(this@SettingsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bottomSheet.show()
    }
}
