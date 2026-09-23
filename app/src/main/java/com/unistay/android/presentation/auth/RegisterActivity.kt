package com.unistay.android.presentation.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.unistay.android.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    // Gọi ViewModel
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvGoToLogin.setOnClickListener { finish() }

        val text = "Tôi đồng ý với Điều khoản dịch vụ và Chính sách bảo mật của Tro24H"
        val spannableString = android.text.SpannableString(text)
        
        val clickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                val intent = android.content.Intent(this@RegisterActivity, TermsActivity::class.java)
                startActivity(intent)
            }
            
            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.color = android.graphics.Color.parseColor("#3B82F6")
                ds.isUnderlineText = true
            }
        }
        
        // "Điều khoản dịch vụ và Chính sách bảo mật" starts at index 15, length is 42
        val startIndex = text.indexOf("Điều khoản dịch vụ và Chính sách bảo mật")
        val endIndex = startIndex + "Điều khoản dịch vụ và Chính sách bảo mật".length
        
        if (startIndex != -1) {
            spannableString.setSpan(clickableSpan, startIndex, endIndex, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        binding.cbTerms.text = spannableString
        binding.cbTerms.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        // Social Buttons
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Tính năng đăng nhập bằng Google đang phát triển", Toast.LENGTH_SHORT).show()
        }

        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Tính năng đăng nhập bằng Facebook đang phát triển", Toast.LENGTH_SHORT).show()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.edtFullName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val phone = binding.edtPhoneNumber.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val confirm = binding.edtConfirmPassword.text.toString().trim()

            // Xác định Role
            val role = if (binding.rgRole.checkedRadioButtonId == binding.rbLandlord.id) {
                "LANDLORD"
            } else {
                "STUDENT"
            }

            // Validate cơ bản
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Regex Validate Email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEmail.error = "Email không đúng định dạng"
                return@setOnClickListener
            }

            // Regex Validate Phone Number (bắt đầu bằng 0, tổng cộng 10 số)
            if (!phone.matches(Regex("^0[0-9]{9}$"))) {
                binding.edtPhoneNumber.error = "Số điện thoại không hợp lệ"
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra điều khoản
            if (!binding.cbTerms.isChecked) {
                Toast.makeText(this, "Vui lòng đồng ý với Điều khoản dịch vụ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // GỌI API YÊU CẦU MÃ OTP
            viewModel.requestOtp(
                email = email,
                onSuccess = {
                    // Hiện Dialog yêu cầu nhập OTP
                    val editText = android.widget.EditText(this).apply {
                        hint = "Nhập mã 6 số"
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER
                        setPadding(48, 48, 48, 48)
                    }
                    
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Xác thực Email")
                        .setMessage("Một mã OTP đã được gửi đến email $email. Vui lòng kiểm tra hộp thư (và mục Spam) để lấy mã.")
                        .setView(editText)
                        .setPositiveButton("Xác nhận") { _, _ ->
                            val otp = editText.text.toString().trim()
                            if (otp.length != 6) {
                                Toast.makeText(this, "Mã OTP phải gồm 6 chữ số", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }
                            
                            // Tiến hành đăng ký với OTP
                            viewModel.registerUser(
                                fullName = name,
                                email = email,
                                password = password,
                                phoneNumber = phone,
                                role = role,
                                otp = otp,
                                onSuccess = {
                                    Toast.makeText(this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show()
                                    finish() // Đóng trang đăng ký, quay về trang Đăng nhập
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                        .setNegativeButton("Hủy", null)
                        .setCancelable(false)
                        .show()
                },
                onError = {
                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}