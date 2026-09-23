package com.unistay.android.data.remote.api

import com.unistay.android.domain.model.model.Room
import retrofit2.http.GET
import retrofit2.http.Query

interface UniStayApi {

    // API lấy danh sách phòng, có thể truyền tham số lọc
    @GET("api/rooms")
    suspend fun getRooms(
        @Query("university") university: String? = null,
        @Query("district") district: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("userId") userId: String? = null,
        @Query("roomType") roomType: String? = null
    ): List<Room>

    @retrofit2.http.POST("api/rooms")
    suspend fun createRoom(@retrofit2.http.Body room: Room): Room

    @retrofit2.http.DELETE("api/rooms/{id}")
    suspend fun deleteRoom(
        @retrofit2.http.Path("id") roomId: String,
        @Query("userId") userId: String
    )

    // API lưu phòng
    @GET("api/saved-rooms")
    suspend fun getSavedRooms(@Query("userId") userId: String): List<Room>

    @retrofit2.http.POST("api/saved-rooms")
    suspend fun saveRoom(
        @Query("userId") userId: String,
        @Query("roomId") roomId: String
    ): com.unistay.android.domain.model.model.SavedRoom

    @retrofit2.http.DELETE("api/saved-rooms")
    suspend fun unsaveRoom(
        @Query("userId") userId: String,
        @Query("roomId") roomId: String
    )

    // API Lịch sử xem phòng
    @GET("api/view-history")
    suspend fun getViewHistory(@Query("userId") userId: String): List<Room>

    @retrofit2.http.POST("api/view-history")
    suspend fun recordViewHistory(@Query("userId") userId: String, @Query("roomId") roomId: String)

    // API Tin nhắn
    @GET("api/chat/messages")
    suspend fun getChatMessages(
        @Query("sessionId") sessionId: String
    ): List<com.unistay.android.domain.model.model.Message>

    @retrofit2.http.POST("api/chat/messages")
    suspend fun sendChatMessage(@retrofit2.http.Body message: com.unistay.android.domain.model.model.Message)

    @GET("api/chat/sessions")
    suspend fun getChatSessions(@Query("userId") userId: String): List<com.unistay.android.domain.model.model.ChatSession>

    @retrofit2.http.POST("api/chat/read/{sessionId}")
    suspend fun markAsRead(
        @retrofit2.http.Path("sessionId") sessionId: String, 
        @Query("userId") userId: String
    )

    // API Upload ảnh
    @retrofit2.http.Multipart
    @retrofit2.http.POST("api/upload")
    suspend fun uploadImage(@retrofit2.http.Part file: okhttp3.MultipartBody.Part): UploadResponse

    // API Auth
    @retrofit2.http.POST("api/auth/login")
    suspend fun login(@retrofit2.http.Body request: LoginRequest): AuthResponse

    @retrofit2.http.POST("api/auth/request-otp")
    suspend fun requestOtp(@retrofit2.http.Body request: RequestOtpRequest): retrofit2.Response<Any>

    @retrofit2.http.POST("api/auth/register")
    suspend fun register(@retrofit2.http.Body request: RegisterRequest): retrofit2.Response<AuthResponse>

    @retrofit2.http.PUT("api/users/{id}")
    suspend fun updateUser(
        @retrofit2.http.Path("id") userId: String,
        @retrofit2.http.Body request: UpdateUserRequest
    ): com.unistay.android.domain.model.model.User
    @retrofit2.http.POST("api/auth/change-password")
    suspend fun changePassword(@retrofit2.http.Body request: ChangePasswordRequest): retrofit2.Response<Unit>

    @retrofit2.http.POST("api/auth/forgot-password-otp")
    suspend fun requestForgotPasswordOtp(@retrofit2.http.Body request: RequestOtpRequest): retrofit2.Response<Any>

    @retrofit2.http.POST("api/auth/reset-password")
    suspend fun resetPassword(@retrofit2.http.Body request: ResetPasswordRequest): retrofit2.Response<Any>

    @retrofit2.http.DELETE("api/users/{id}")
    suspend fun deleteUser(@retrofit2.http.Path("id") userId: String): retrofit2.Response<Unit>
}

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class ChangePasswordRequest(
    val userId: String,
    val oldPassword: String,
    val newPassword: String
)

data class UpdateUserRequest(
    val fullName: String?,
    val phoneNumber: String?,
    val avatarUrl: String?
)

data class UploadResponse(val imageUrl: String)

data class LoginRequest(
    val email: String, 
    val password: String
)

data class RequestOtpRequest(
    val email: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String,
    val phoneNumber: String,
    val otp: String
)

data class AuthResponse(
    val token: String,
    val user: com.unistay.android.domain.model.model.User
)