package com.thinkforge.ai.chat.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.thinkforge.ai.chat.R
import com.thinkforge.ai.chat.databinding.FragmentSettingsBinding
import com.thinkforge.ai.chat.models.GenerationConfig
import com.thinkforge.ai.chat.viewmodels.ChatViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Generation settings
        binding.temperatureSlider.addOnChangeListener { _, value, _ ->
            viewModel.updateGenerationConfig(
                viewModel.getGenerationConfig().copy(temperature = value)
            )
            binding.temperatureValue.text = String.format("%.2f", value)
        }

        binding.topPSlider.addOnChangeListener { _, value, _ ->
            viewModel.updateGenerationConfig(
                viewModel.getGenerationConfig().copy(topP = value)
            )
            binding.topPValue.text = String.format("%.2f", value)
        }

        binding.maxTokensSlider.addOnChangeListener { _, value, _ ->
            viewModel.updateGenerationConfig(
                viewModel.getGenerationConfig().copy(maxNewTokens = value.toInt())
            )
            binding.maxTokensValue.text = value.toInt().toString()
        }

        binding.streamToggle.isChecked = viewModel.getGenerationConfig().streamTokens
        binding.streamToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateGenerationConfig(
                viewModel.getGenerationConfig().copy(streamTokens = isChecked)
            )
        }

        // Theme
        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.themeLight -> { /* Set light theme */ }
                R.id.themeDark -> { /* Set dark theme */ }
                R.id.themeSystem -> { /* Follow system */ }
            }
        }

        // About
        binding.aboutText.text = getString(R.string.about_description)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}