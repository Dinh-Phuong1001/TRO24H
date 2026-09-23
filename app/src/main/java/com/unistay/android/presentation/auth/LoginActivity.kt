package com.unistay.android.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordBottomSheet()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.loginUser(
                email = email,
                password = password,
                onSuccess = { user ->
                    lifecycleScope.launch {
                        // Lưu phiên đăng nhập bằng DataStore
                        sessionManager.saveUserSession(
                            userId = user.userId, 
                            name = user.fullName, 
                            email = user.email,
                            avatarUrl = user.avatarUrl,
                            phone = user.phone,
                            role = user.role
                        )
                        Toast.makeText(this@LoginActivity, "Chào mừng trở lại, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                },
                onError = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun showForgotPasswordBottomSheet() {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(com.unistay.android.R.layout.bottom_sheet_forgot_password, null)
        bottomSheet.setContentView(view)

        val edtEmail = view.findViewById<android.widget.EditText>(com.unistay.android.R.id.edtForgotEmail)
        val btnSendOtp = view.findViewById<com.google.android.material.button.MaterialButton>(com.unistay.android.R.id.btnSendForgotOtp)
        val edtOtp = view.findViewById<android.widget.EditText>(com.unistay.android.R.id.edtForgotOtp)
        val edtNewPass = view.findViewById<android.widget.EditText>(com.unistay.android.R.id.edtForgotNewPassword)
        val edtConfirmPass = view.findViewById<android.widget.EditText>(com.unistay.android.R.id.edtForgotConfirmPassword)
        val btnSubmit = view.findViewById<com.google.android.material.button.MaterialButton>(com.unistay.android.R.id.btnSubmitForgotPassword)

        val currentLoginEmail = binding.edtEmail.text.toString().trim()
        if (currentLoginEmail.isNotEmpty()) {
            edtEmail.setText(currentLoginEmail)
        }

        var otpTimer: android.os.CountDownTimer? = null

        btnSendOtp.setOnClickListener {
            val email = edtEmail.text.toString().trim().lowercase()
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email trước", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSendOtp.isEnabled = false
            btnSendOtp.text = "Đang gửi..."

            viewModel.requestForgotPasswordOtp(
                email = email,
                onSuccess = {
                    Toast.makeText(this, "Mã OTP đã được gửi đến email của bạn", Toast.LENGTH_LONG).show()
                    otpTimer = object : android.os.CountDownTimer(60000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            btnSendOtp.text = "${millisUntilFinished / 1000}s"
                        }
                        override fun onFinish() {
                            btnSendOtp.isEnabled = true
                            btnSendOtp.text = "Gửi lại"
                        }
                    }.start()
                },
                onError = { error ->
                    btnSendOtp.isEnabled = true
                    btnSendOtp.text = "Gửi mã"
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        btnSubmit.setOnClickListener {
            val email = edtEmail.text.toString().trim().lowercase()
            val otp = edtOtp.text.toString().trim()
            val newPass = edtNewPass.text.toString().trim()
            val confirmPass = edtConfirmPass.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (otp.length != 6) {
                Toast.makeText(this, "Vui lòng nhập đúng 6 số OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass.length < 6) {
                Toast.makeText(this, "Mật khẩu mới tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Đang xử lý..."

            viewModel.resetPassword(
                email = email,
                otp = otp,
                newPass = newPass,
                onSuccess = {
                    otpTimer?.cancel()
                    bottomSheet.dismiss()
                    binding.edtEmail.setText(email)
                    binding.edtPassword.setText("")
                    Toast.makeText(this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "ĐẶT LẠI MẬT KHẨU"
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        }

        bottomSheet.setOnDismissListener {
            otpTimer?.cancel()
        }

        bottomSheet.show()
    }
}