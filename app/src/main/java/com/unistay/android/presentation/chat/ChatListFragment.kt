package com.unistay.android.presentation.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.unistay.android.databinding.FragmentChatListBinding
import com.unistay.android.domain.model.model.ChatSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatListAdapter(
            chats = emptyList(),
            currentUserId = viewModel.currentUserId,
            onItemClick = { chat ->
                if (chat.isBlocked) {
                    Toast.makeText(requireContext(), "Cuộc trò chuyện này đang bị chặn. Vuốt sang trái để bỏ chặn!", Toast.LENGTH_SHORT).show()
                    return@ChatListAdapter
                }
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    val isLandlord = chat.landlordId == viewModel.currentUserId
                    val partnerName = if (isLandlord) {
                        if (!chat.studentName.isNullOrEmpty()) chat.studentName else "Khách thuê"
                    } else {
                        if (!chat.landlordName.isNullOrEmpty()) chat.landlordName else "Chủ trọ"
                    }

                    putExtra("SESSION_ID", chat.sessionId)
                    putExtra("PARTNER_NAME", partnerName)
                    putExtra("ROOM_TITLE", chat.roomTitle)
                    putExtra("PARTNER_PHONE", chat.landlordPhone)
                    putExtra("ROOM_ID", chat.roomId)
                    putExtra("LANDLORD_ID", chat.landlordId)

                    val targetAvatarUrl = if (isLandlord) chat.studentAvatarUrl else chat.landlordAvatarUrl
                    putExtra("PARTNER_AVATAR", targetAvatarUrl)
                }
                startActivity(intent)
            },
            onPinClick = { chat ->
                val isNowPinned = viewModel.togglePin(chat.sessionId)
                val msg = if (isNowPinned) "Đã ghim cuộc trò chuyện lên đầu danh sách 📌" else "Đã bỏ ghim cuộc trò chuyện"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            },
            onBlockClick = { chat ->
                if (!chat.isBlocked) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Chặn người dùng")
                        .setMessage("Bạn có chắc muốn chặn cuộc trò chuyện này? Bạn sẽ không nhận tin nhắn mới từ người này nữa.")
                        .setPositiveButton("Chặn") { _, _ ->
                            viewModel.toggleBlock(chat.sessionId)
                            Toast.makeText(requireContext(), "Đã chặn người dùng 🚫", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                } else {
                    viewModel.toggleBlock(chat.sessionId)
                    Toast.makeText(requireContext(), "Đã bỏ chặn người dùng", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { chat ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa cuộc trò chuyện")
                    .setMessage("Bạn có chắc chắn muốn xóa toàn bộ tin nhắn trong cuộc trò chuyện này không? Hành động này không thể hoàn tác.")
                    .setPositiveButton("Xóa") { _, _ ->
                        viewModel.deleteSession(chat.sessionId)
                        Toast.makeText(requireContext(), "Đã xóa cuộc trò chuyện 🗑️", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        )

        binding.rvChatList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatList.adapter = adapter

        // Toggle thanh tìm kiếm
        binding.ivSearch.setOnClickListener {
            if (binding.etSearch.visibility == View.VISIBLE) {
                binding.etSearch.visibility = View.GONE
                binding.etSearch.text.clear()
            } else {
                binding.etSearch.visibility = View.VISIBLE
                binding.etSearch.requestFocus()
            }
        }

        // Tìm kiếm realtime
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                val currentList = viewModel.chatSessions.value
                val filteredChats = if (query.isEmpty()) {
                    currentList
                } else {
                    currentList.filter {
                        val partnerName = if (it.landlordId == viewModel.currentUserId) "Khách thuê" else it.landlordName
                        partnerName.lowercase().contains(query) || it.roomTitle.lowercase().contains(query) || it.lastMessage.lowercase().contains(query)
                    }
                }
                adapter.updateData(filteredChats)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Lắng nghe DỮ LIỆU THẬT từ Database đổ về (Đã sắp xếp tin nhắn mới nhất & ghim lên đầu)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatSessions.collect { realChats ->
                val query = binding.etSearch.text.toString().trim().lowercase()
                if (query.isEmpty()) {
                    adapter.updateData(realChats)
                } else {
                    val filteredChats = realChats.filter {
                        val partnerName = if (it.landlordId == viewModel.currentUserId) "Khách thuê" else it.landlordName
                        partnerName.lowercase().contains(query) || it.roomTitle.lowercase().contains(query) || it.lastMessage.lowercase().contains(query)
                    }
                    adapter.updateData(filteredChats)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}