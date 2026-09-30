package com.unistay.android.presentation.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unistay.android.databinding.ItemChatSessionBinding
import com.unistay.android.domain.model.model.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private var chats: List<ChatSession>,
    private val currentUserId: String,
    private val onItemClick: (ChatSession) -> Unit,
    private val onPinClick: (ChatSession) -> Unit,
    private val onBlockClick: (ChatSession) -> Unit,
    private val onDeleteClick: (ChatSession) -> Unit
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
            // Đảm bảo phần nội dung chính chiếm trọn chiều rộng màn hình điện thoại
            val screenWidth = root.context.resources.displayMetrics.widthPixels
            val params = layoutMainContent.layoutParams
            if (params.width != screenWidth) {
                params.width = screenWidth
                layoutMainContent.layoutParams = params
            }

            // Đặt lại trạng thái vuốt về đóng khi tải lại
            swipeLayout.scrollX = 0

            val isLandlord = (chat.landlordId == currentUserId)
            tvSenderName.text = if (isLandlord) {
                if (!chat.studentName.isNullOrEmpty()) chat.studentName else "Khách thuê"
            } else {
                chat.landlordName.ifEmpty { "Chủ trọ" }
            }

            // Xử lý hiển thị trạng thái GHIM (PIN)
            if (chat.isPinned) {
                tvPinBadge.visibility = View.VISIBLE
                tvPinText.text = "Bỏ ghim"
                layoutMainContent.setBackgroundColor(Color.parseColor("#FFFBEB")) // Vàng kem nhẹ nổi bật
            } else {
                tvPinBadge.visibility = View.GONE
                tvPinText.text = "Ghim"
                layoutMainContent.setBackgroundColor(Color.WHITE)
            }

            // Xử lý hiển thị trạng thái CHẶN (BLOCK)
            if (chat.isBlocked) {
                tvBlockedBadge.visibility = View.VISIBLE
                tvBlockText.text = "Bỏ chặn"
                tvLastMessage.text = "[Đã chặn người dùng này]"
                tvLastMessage.setTextColor(Color.parseColor("#EF4444"))
            } else {
                tvBlockedBadge.visibility = View.GONE
                tvBlockText.text = "Chặn"
                val isImage = chat.lastMessage.startsWith("http") && (chat.lastMessage.contains("/images/") || chat.lastMessage.endsWith(".jpg") || chat.lastMessage.endsWith(".png"))
                tvLastMessage.text = if (isImage) "[Hình ảnh]" else chat.lastMessage
                tvLastMessage.setTextColor(Color.parseColor("#64748B"))
            }

            // Chuyển đổi timestamp thành giờ:phút
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(chat.lastUpdated))

            // Lấy avatar đối phương
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

            // Badge tin nhắn chưa đọc
            val unreadCount = if (isLandlord) chat.landlordUnreadCount else chat.studentUnreadCount
            if (unreadCount > 0 && !chat.isBlocked) {
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.text = if (unreadCount > 99) "99+" else unreadCount.toString()
            } else {
                tvUnreadCount.visibility = View.GONE
            }

            // Sự kiện bấm vào nội dung chính
            layoutMainContent.setOnClickListener {
                if (swipeLayout.scrollX > 20) {
                    // Nếu đang mở menu vuốt thì đóng lại
                    swipeLayout.smoothScrollTo(0, 0)
                } else {
                    onItemClick(chat)
                }
            }

            // Sự kiện 3 nút chức năng khi vuốt sang trái:
            btnPin.setOnClickListener {
                swipeLayout.smoothScrollTo(0, 0)
                onPinClick(chat)
            }

            btnBlock.setOnClickListener {
                swipeLayout.smoothScrollTo(0, 0)
                onBlockClick(chat)
            }

            btnDelete.setOnClickListener {
                swipeLayout.smoothScrollTo(0, 0)
                onDeleteClick(chat)
            }
        }
    }

    override fun getItemCount(): Int = chats.size

    fun updateData(newChats: List<ChatSession>) {
        chats = newChats
        notifyDataSetChanged()
    }
}