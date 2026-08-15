package com.thinkforge.ai.chat.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkforge.ai.chat.databinding.ItemChatMessageBinding
import com.thinkforge.ai.chat.models.ChatMessage
import com.thinkforge.ai.chat.models.MessageRole

class ChatAdapter(
    private val onMessageClick: (ChatMessage) -> Unit
) : ListAdapter<ChatMessage, ChatAdapter.MessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding, onMessageClick)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        private val binding: ItemChatMessageBinding,
        private val onMessageClick: (ChatMessage) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.messageContent.text = message.content
            binding.messageTimestamp.text = formatTimestamp(message.timestamp)
            
            when (message.role) {
                MessageRole.USER -> {
                    binding.messageRole.text = "You"
                    binding.messageRole.setTextColor(
                        binding.root.context.getColor(
                            com.thinkforge.ai.chat.R.color.user_text_color
                        )
                    )
                    binding.messageBubble.setBackgroundResource(
                        com.thinkforge.ai.chat.R.drawable.bg_user_bubble
                    )
                }
                MessageRole.ASSISTANT -> {
                    binding.messageRole.text = "ThinkForge AI"
                    binding.messageRole.setTextColor(
                        binding.root.context.getColor(
                            com.thinkforge.ai.chat.R.color.ai_text_color
                        )
                    )
                    binding.messageBubble.setBackgroundResource(
                        com.thinkforge.ai.chat.R.drawable.bg_ai_bubble
                    )
                    
                    if (message.isStreaming) {
                        binding.streamingIndicator.visibility = android.view.View.VISIBLE
                    } else {
                        binding.streamingIndicator.visibility = android.view.View.GONE
                    }
                }
                MessageRole.SYSTEM -> {
                    binding.messageRole.text = "System"
                    binding.messageBubble.setBackgroundResource(
                        com.thinkforge.ai.chat.R.drawable.bg_system_bubble
                    )
                }
            }

            binding.root.setOnClickListener { onMessageClick(message) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}