package com.unistay.android.domain.repository

import com.unistay.android.data.remote.RetrofitClient
import com.unistay.android.data.remote.api.LoginRequest
import com.unistay.android.data.remote.api.RegisterRequest
import com.unistay.android.domain.model.model.User
import javax.inject.Inject

class UserRepository @Inject constructor() {
    suspend fun requestOtp(email: String) {
        val request = com.unistay.android.data.remote.api.RequestOtpRequest(email)
        val response = RetrofitClient.instance.requestOtp(request)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw Exception(errorBody ?: "Lỗi không xác định")
        }
    }

    suspend fun register(user: User, otp: String) {
        val request = RegisterRequest(
            fullName = user.fullName,
            email = user.email,
            password = user.passwordHash,
            role = user.role,
            phoneNumber = user.phone,
            otp = otp
        )
        val response = RetrofitClient.instance.register(request)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw Exception(errorBody ?: "Lỗi không xác định")
        }
    }

    suspend fun login(email: String, password: String): User? {
        return try {
            val request = LoginRequest(email, password)
            val response = RetrofitClient.instance.login(request)
            response.user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun requestForgotPasswordOtp(email: String) {
        val request = com.unistay.android.data.remote.api.RequestOtpRequest(email)
        val response = RetrofitClient.instance.requestForgotPasswordOtp(request)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val message = try {
                org.json.JSONObject(errorBody ?: "").optString("message", "Lỗi gửi mã OTP")
            } catch (e: Exception) {
                errorBody ?: "Lỗi gửi mã OTP"
            }
            throw Exception(message)
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPass: String) {
        val request = com.unistay.android.data.remote.api.ResetPasswordRequest(email, otp, newPass)
        val response = RetrofitClient.instance.resetPassword(request)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val message = try {
                org.json.JSONObject(errorBody ?: "").optString("message", "Lỗi đặt lại mật khẩu")
            } catch (e: Exception) {
                errorBody ?: "Lỗi đặt lại mật khẩu"
            }
            throw Exception(message)
        }
    }

    // API không có check email rời, ta có thể bỏ qua hoặc return false, 
    // vì nếu trùng email API sẽ bắn lỗi lúc đăng ký.
    suspend fun checkEmailExists(email: String): Boolean {
        return false 
    }
}