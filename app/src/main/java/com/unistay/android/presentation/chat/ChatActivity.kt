package com.unistay.android.presentation.chat

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.unistay.android.databinding.ActivityChatBinding
import dagger.hilt.android.AndroidEntryPoint
import coil.load

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.unistay.android.data.local.datastore.UserSessionManager
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageAdapter
    
    private var currentSessionId: String = ""
    private var roomId: String = ""
    private var landlordId: String = ""

    @Inject
    lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSessionId = intent.getStringExtra("SESSION_ID") ?: ""
        val partnerName = intent.getStringExtra("PARTNER_NAME")
        val roomTitle = intent.getStringExtra("ROOM_TITLE") ?: "Không rõ phòng"
        val partnerPhone = intent.getStringExtra("PARTNER_PHONE") ?: ""
        roomId = intent.getStringExtra("ROOM_ID") ?: ""
        landlordId = intent.getStringExtra("LANDLORD_ID") ?: ""

        binding.tvPartnerName.text = partnerName ?: "Khách"
        binding.tvRoomTitle.text = roomTitle
        binding.btnBack.setOnClickListener { finish() }

        binding.btnCall.setOnClickListener {
            if (partnerPhone.isNotEmpty()) {
                val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:$partnerPhone")
                }
                startActivity(dialIntent)
            } else {
                Toast.makeText(this, "Không có số điện thoại", Toast.LENGTH_SHORT).show()
            }
        }

        // Cài đặt RecyclerView
        lifecycleScope.launch {
            val userId = sessionManager.userId.firstOrNull() ?: ""
            val rawAvatarUrl = intent.getStringExtra("PARTNER_AVATAR")
            val partnerAvatarUrl = rawAvatarUrl?.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
            binding.ivAvatar.imageTintList = null
            if (!partnerAvatarUrl.isNullOrEmpty()) {
                binding.ivAvatar.load(partnerAvatarUrl) {
                    crossfade(true)
                    placeholder(com.unistay.android.R.drawable.ic_default_avatar)
                    error(com.unistay.android.R.drawable.ic_default_avatar)
                    transformations(coil.transform.CircleCropTransformation())
                }
            } else {
                binding.ivAvatar.setImageResource(com.unistay.android.R.drawable.ic_default_avatar)
            }
            adapter = ChatMessageAdapter(userId, partnerAvatarUrl)
            
            val layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            binding.rvMessages.layoutManager = layoutManager
            binding.rvMessages.adapter = adapter

            // Load data bằng cách bắt đầu polling
            viewModel.startPolling(currentSessionId)

            // Quan sát tin nhắn
            viewModel.messages.collect { msgs ->
                adapter.submitList(msgs)
                if (msgs.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(msgs.size - 1)
                }
            }
        }

        binding.btnSend.setOnClickListener { attemptSendMessage() }

        binding.edtMessage.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                attemptSendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun attemptSendMessage() {
        val messageText = binding.edtMessage.text.toString().trim()
        if (messageText.isNotEmpty() && currentSessionId.isNotEmpty()) {
            val partnerName = intent.getStringExtra("PARTNER_NAME") ?: ""
            val roomTitle = intent.getStringExtra("ROOM_TITLE") ?: ""
            val partnerPhone = intent.getStringExtra("PARTNER_PHONE") ?: ""
            
            viewModel.sendMessage(
                currentSessionId, 
                messageText, 
                roomId, 
                landlordId,
                roomTitle,
                partnerName,
                partnerPhone
            )
            binding.edtMessage.text.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopPolling()
    }
}