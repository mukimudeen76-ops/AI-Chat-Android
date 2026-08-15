package com.thinkforge.ai.chat.ui.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.thinkforge.ai.chat.databinding.FragmentChatBinding
import com.thinkforge.ai.chat.engine.ChatEngine
import com.thinkforge.ai.chat.models.ChatMessage
import com.thinkforge.ai.chat.models.MessageRole
import com.thinkforge.ai.chat.ui.adapters.ChatAdapter
import com.thinkforge.ai.chat.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    
    private lateinit var chatEngine: ChatEngine

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        chatEngine = ChatEngine()
        chatEngine.setSystemPrompt("You are ThinkForge AI Pro running locally on the user's Android device. You are a powerful AI assistant with 671B total parameters (45B active per token) and a 1M token context window. You excel at reasoning, coding, math, and general knowledge tasks.")
        
        setupRecyclerView()
        setupSendButton()
        setupModelStatus()
        
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages.toList())
            if (messages.isNotEmpty()) {
                binding.chatRecyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { message ->
            // Click listener for messages
        }
        
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                sendMessage(text)
                binding.messageInput.text?.clear()
            }
        }

        binding.messageInput.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                val text = binding.messageInput.text?.toString()?.trim()
                if (!text.isNullOrEmpty()) {
                    sendMessage(text)
                    binding.messageInput.text?.clear()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupModelStatus() {
        viewModel.modelStatus.observe(viewLifecycleOwner) { status ->
            binding.modelStatusText.text = status
        }
    }

    private fun sendMessage(text: String) {
        val userMessage = ChatMessage(role = MessageRole.USER, content = text)
        viewModel.addMessage(userMessage)
        
        binding.sendButton.isEnabled = false
        binding.messageInput.isEnabled = false

        lifecycleScope.launch {
            val assistantMessage = ChatMessage(
                role = MessageRole.ASSISTANT,
                content = "",
                isStreaming = true
            )
            viewModel.addMessage(assistantMessage)
            
            val config = viewModel.getGenerationConfig()
            
            chatEngine.streamReply(text, { token ->
                viewModel.appendToLastMessage(token)
            }, config)
            
            binding.sendButton.isEnabled = true
            binding.messageInput.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatEngine.release()
        _binding = null
    }
}