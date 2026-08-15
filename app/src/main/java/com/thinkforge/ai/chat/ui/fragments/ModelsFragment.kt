package com.thinkforge.ai.chat.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.thinkforge.ai.chat.databinding.FragmentModelsBinding
import com.thinkforge.ai.chat.models.ModelDownloadState
import com.thinkforge.ai.chat.models.ModelInfo
import com.thinkforge.ai.chat.viewmodels.ChatViewModel

class ModelsFragment : Fragment() {

    private var _binding: FragmentModelsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.availableModels.observe(viewLifecycleOwner) { models ->
            if (models.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.modelsList.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.modelsList.visibility = View.VISIBLE
                // Set up adapter with models
            }
        }

        binding.downloadButton.setOnClickListener {
            // Navigate to model download activity
            startActivity(
                android.content.Intent(requireContext(), 
                    com.thinkforge.ai.chat.ModelDownloadActivity::class.java)
            )
        }

        binding.ollamaToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch to Ollama backend
                binding.ollamaUrlLayout.visibility = View.VISIBLE
            } else {
                binding.ollamaUrlLayout.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}