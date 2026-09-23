package com.unistay.android.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unistay.android.databinding.ItemChatSessionBinding
import com.unistay.android.domain.model.model.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.load

class ChatListAdapter(
    private var chats: List<ChatSession>,
    private val currentUserId: String,
    private val onItemClick: (ChatSession) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatSessionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        with(holder.binding) {
            // Hiển thị tên (chủ trọ hoặc người thuê tuỳ ngữ cảnh)
            val isLandlord = (chat.landlordId == currentUserId)
            tvSenderName.text = if (isLandlord) {
                // Nếu mình là chủ trọ, hiển thị tên của người thuê
                if (!chat.studentName.isNullOrEmpty()) chat.studentName else "Khách thuê"
            } else {
                chat.landlordName.ifEmpty { "Chủ trọ" }
            }
            
            val isImage = chat.lastMessage.startsWith("http") && (chat.lastMessage.contains("/images/") || chat.lastMessage.endsWith(".jpg") || chat.lastMessage.endsWith(".png"))
            tvLastMessage.text = if (isImage) "[Hình ảnh]" else chat.lastMessage

            // Chuyển đổi timestamp (Long) thành chuỗi giờ phút (VD: 10:30)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(chat.lastUpdated))

            // Lấy avatar của đối phương
            val rawTargetAvatarUrl = if (isLandlord) chat.studentAvatarUrl else chat.landlordAvatarUrl
            val targetAvatarUrl = rawTargetAvatarUrl?.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
            
            ivAvatar.imageTintList = null
            if (!targetAvatarUrl.isNullOrEmpty()) {
                ivAvatar.load(targetAvatarUrl) {
                    crossfade(true)
                    placeholder(com.unistay.android.R.drawable.ic_default_avatar)
                    error(com.unistay.android.R.drawable.ic_default_avatar)
                    transformations(coil.transform.CircleCropTransformation())
                }
            } else {
                ivAvatar.setImageResource(com.unistay.android.R.drawable.ic_default_avatar)
            }
            
            // Xử lý đếm số tin nhắn chưa đọc
            val unreadCount = if (isLandlord) chat.landlordUnreadCount else chat.studentUnreadCount
            if (unreadCount > 0) {
                tvUnreadCount.visibility = android.view.View.VISIBLE
                tvUnreadCount.text = if (unreadCount > 99) "99+" else unreadCount.toString()
            } else {
                tvUnreadCount.visibility = android.view.View.GONE
            }

            root.setOnClickListener { onItemClick(chat) }
        }
    }

    override fun getItemCount(): Int = chats.size

    fun updateData(newChats: List<ChatSession>) {
        chats = newChats
        notifyDataSetChanged()
    }
}