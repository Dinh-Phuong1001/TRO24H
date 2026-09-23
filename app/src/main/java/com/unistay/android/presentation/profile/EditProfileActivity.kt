package com.unistay.android.presentation.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.unistay.android.data.local.datastore.UserSessionManager
import com.unistay.android.data.remote.RetrofitClient
import com.unistay.android.data.remote.api.UpdateUserRequest
import com.unistay.android.databinding.ActivityEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var selectedImageUri: Uri? = null
    private var currentAvatarUrl: String? = null

    @Inject
    lateinit var sessionManager: UserSessionManager

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivAvatar.load(it) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
            binding.ivAvatar.imageTintList = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.ivAvatar.setOnClickListener { getContent.launch("image/*") }

        lifecycleScope.launch {
            binding.edtFullName.setText(sessionManager.userName.firstOrNull() ?: "")
            binding.edtEmail.setText(sessionManager.userEmail.firstOrNull() ?: "")
            binding.edtPhone.setText(sessionManager.userPhone.firstOrNull() ?: "")
            
            val rawAvatarUrl = sessionManager.userAvatar.firstOrNull()
            val avatarUrl = rawAvatarUrl?.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
            currentAvatarUrl = avatarUrl
            binding.ivAvatar.imageTintList = null
            if (!avatarUrl.isNullOrEmpty()) {
                binding.ivAvatar.load(avatarUrl) {
                    crossfade(true)
                    placeholder(com.unistay.android.R.drawable.ic_default_avatar)
                    error(com.unistay.android.R.drawable.ic_default_avatar)
                    transformations(CircleCropTransformation())
                }
            } else {
                binding.ivAvatar.setImageResource(com.unistay.android.R.drawable.ic_default_avatar)
            }
        }

        binding.btnSave.setOnClickListener {
            val fullName = binding.edtFullName.text.toString().trim()
            val phone = binding.edtPhone.text.toString().trim()
            
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Họ và tên không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            binding.btnSave.isEnabled = false
            binding.btnSave.text = "Đang lưu..."

            lifecycleScope.launch {
                try {
                    val userId = sessionManager.userId.firstOrNull()
                    if (userId == null) {
                        Toast.makeText(this@EditProfileActivity, "Lỗi đăng nhập", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    var uploadedAvatarUrl = currentAvatarUrl

                    // Upload new image if selected
                    if (selectedImageUri != null) {
                        val file = getFileFromUri(selectedImageUri!!)
                        if (file != null) {
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                            val response = RetrofitClient.instance.uploadImage(body)
                            uploadedAvatarUrl = response.imageUrl
                        }
                    }

                    // Update user profile via API
                    val updateRequest = UpdateUserRequest(fullName, phone, uploadedAvatarUrl)
                    RetrofitClient.instance.updateUser(userId, updateRequest)

                    // Update local session
                    sessionManager.updateProfile(fullName, uploadedAvatarUrl, phone)

                    Toast.makeText(this@EditProfileActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    finish()

                } catch (e: Exception) {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Lưu thay đổi"
                    Toast.makeText(this@EditProfileActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        try {
            val contentResolver = contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val fileName = nameIndex?.let { cursor.getString(it) } ?: "temp_image.jpg"
            cursor?.close()

            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
