package com.unistay.android.presentation.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unistay.android.R
import com.unistay.android.domain.model.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.load

class ChatMessageAdapter(
    private val currentUserId: String,
    private val partnerAvatarUrl: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<Message>()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    fun submitList(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        private val ivMessageImage: android.widget.ImageView = itemView.findViewById(R.id.ivMessageImage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: Message) {
            tvTime.text = formatTime(message.timestamp)

            if (isImageUrl(message.content)) {
                tvMessageContent.visibility = View.GONE
                ivMessageImage.visibility = View.VISIBLE
                val fixedUrl = message.content.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
                ivMessageImage.load(fixedUrl) {
                    crossfade(true)
                    placeholder(R.drawable.bg_search_bar)
                    error(R.drawable.bg_search_bar)
                }
            } else {
                tvMessageContent.visibility = View.VISIBLE
                ivMessageImage.visibility = View.GONE
                tvMessageContent.text = message.content
            }
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        private val ivMessageImage: android.widget.ImageView = itemView.findViewById(R.id.ivMessageImage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val ivPartnerAvatar: android.widget.ImageView = itemView.findViewById(R.id.ivPartnerAvatar)

        fun bind(message: Message) {
            tvTime.text = formatTime(message.timestamp)

            if (isImageUrl(message.content)) {
                tvMessageContent.visibility = View.GONE
                ivMessageImage.visibility = View.VISIBLE
                val fixedUrl = message.content.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
                ivMessageImage.load(fixedUrl) {
                    crossfade(true)
                    placeholder(R.drawable.bg_search_bar)
                    error(R.drawable.bg_search_bar)
                }
            } else {
                tvMessageContent.visibility = View.VISIBLE
                ivMessageImage.visibility = View.GONE
                tvMessageContent.text = message.content
            }
            
            ivPartnerAvatar.imageTintList = null
            val fixedPartnerAvatarUrl = partnerAvatarUrl?.replace(Regex("https?://[^/]+(:5148)?/"), "http://tro24h.runasp.net/")
            if (!fixedPartnerAvatarUrl.isNullOrEmpty()) {
                ivPartnerAvatar.load(fixedPartnerAvatarUrl) {
                    crossfade(true)
                    placeholder(com.unistay.android.R.drawable.ic_default_avatar)
                    error(com.unistay.android.R.drawable.ic_default_avatar)
                    transformations(coil.transform.CircleCropTransformation())
                }
            } else {
                ivPartnerAvatar.setImageResource(com.unistay.android.R.drawable.ic_default_avatar)
            }
        }
    }

    private fun isImageUrl(content: String): Boolean {
        val trimmed = content.trim()
        val lower = trimmed.lowercase()
        return (lower.startsWith("http://") || lower.startsWith("https://")) &&
                (lower.contains("/images/") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp"))
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
