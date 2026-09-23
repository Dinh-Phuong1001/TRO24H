package com.unistay.android.presentation.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    // Gắn ViewModel
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

        adapter = ChatListAdapter(emptyList(), viewModel.currentUserId) { chat ->
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
        }

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
                val filteredChats = viewModel.chatSessions.value.filter {
                    val partnerName = if (it.landlordId == viewModel.currentUserId) "Khách thuê" else it.landlordName
                    partnerName.lowercase().contains(query) || it.roomTitle.lowercase().contains(query) || it.lastMessage.lowercase().contains(query)
                }
                adapter.updateData(filteredChats)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Lắng nghe DỮ LIỆU THẬT từ Database đổ về
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