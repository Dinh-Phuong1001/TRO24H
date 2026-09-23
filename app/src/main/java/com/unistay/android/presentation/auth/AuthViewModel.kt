package com.unistay.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistay.android.domain.model.model.User
import com.unistay.android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    // Hàm xử lý đăng nhập thực tế với Room Database
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // Gọi repository kiểm tra thông tin trong bảng users
            val user = repository.login(email, password)
            if (user != null) {
                onSuccess(user)
            } else {
                onError("Email hoặc mật khẩu không chính xác!")
            }
        }
    }
    fun requestOtp(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.requestOtp(email)
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = try {
                    val jsonObject = org.json.JSONObject(e.message ?: "")
                    jsonObject.getString("message")
                } catch (jsonEx: Exception) {
                    e.message ?: "Có lỗi kết nối xảy ra"
                }
                onError(errorMessage)
            }
        }
    }

    // Hàm xử lý đăng ký tài khoản
    fun registerUser(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        role: String,
        otp: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 2. Tạo đối tượng User mới với ID ngẫu nhiên
            val newUser = User(
                userId = UUID.randomUUID().toString(),
                fullName = fullName,
                email = email,
                passwordHash = password, // (Thực tế khi lên Production sẽ phải mã hóa MD5/Bcrypt ở đây)
                phone = phoneNumber,
                role = role
            )

            // 3. Gọi API Đăng ký
            try {
                repository.register(newUser, otp)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 400) {
                    onError("Mã OTP không hợp lệ hoặc đã hết hạn!")
                } else {
                    onError("Lỗi máy chủ (${e.code()})")
                }
            } catch (e: Exception) {
                onError("Có lỗi: ${e.message}")
            }
        }
    }

    // Hàm gửi OTP quên mật khẩu
    fun requestForgotPasswordOtp(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.requestForgotPasswordOtp(email)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi gửi mã OTP")
            }
        }
    }

    // Hàm đặt lại mật khẩu bằng OTP
    fun resetPassword(email: String, otp: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.resetPassword(email, otp, newPass)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi đặt lại mật khẩu")
            }
        }
    }
}