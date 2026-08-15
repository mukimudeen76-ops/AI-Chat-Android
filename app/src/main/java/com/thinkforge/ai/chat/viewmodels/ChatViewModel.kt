package com.thinkforge.ai.chat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.thinkforge.ai.chat.ThinkForgeApplication
import com.thinkforge.ai.chat.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    
    private val _messages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<ChatMessage>> = _messages

    private val _modelStatus = MutableLiveData("No model loaded")
    val modelStatus: LiveData<String> = _modelStatus

    private val _generationConfig = MutableLiveData(GenerationConfig())
    val generationConfig: LiveData<GenerationConfig> = _generationConfig

    private val _hardwareConfig = MutableLiveData(HardwareConfig())
    val hardwareConfig: LiveData<HardwareConfig> = _hardwareConfig

    private val _currentSession = MutableLiveData<ChatSession?>()
    val currentSession: LiveData<ChatSession?> = _currentSession

    private val _sessions = MutableLiveData<List<ChatSession>>(mutableListOf())
    val sessions: LiveData<List<ChatSession>> = _sessions

    private val _availableModels = MutableLiveData<List<ModelInfo>>(mutableListOf())
    val availableModels: LiveData<List<ModelInfo>> = _availableModels

    init {
        loadAvailableModels()
        loadSessions()
        loadConfig()
    }

    fun addMessage(message: ChatMessage) {
        val currentList = _messages.value?.toMutableList() ?: mutableListOf()
        currentList.add(message)
        _messages.value = currentList
    }

    fun appendToLastMessage(token: String) {
        val currentList = _messages.value?.toMutableList() ?: mutableListOf()
        if (currentList.isNotEmpty()) {
            val last = currentList.last()
            val updated = last.copy(content = last.content + token, isStreaming = true)
            currentList[currentList.size - 1] = updated
            _messages.value = currentList
        }
    }

    fun finalizeLastMessage() {
        val currentList = _messages.value?.toMutableList() ?: mutableListOf()
        if (currentList.isNotEmpty()) {
            val last = currentList.last()
            val updated = last.copy(isStreaming = false)
            currentList[currentList.size - 1] = updated
            _messages.value = currentList
        }
    }

    fun clearMessages() {
        _messages.value = mutableListOf()
    }

    fun getGenerationConfig(): GenerationConfig = _generationConfig.value ?: GenerationConfig()

    fun updateGenerationConfig(config: GenerationConfig) {
        _generationConfig.value = config
        saveConfig()
    }

    fun updateHardwareConfig(config: HardwareConfig) {
        _hardwareConfig.value = config
        saveConfig()
    }

    private fun loadAvailableModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val modelsDir = File(ThinkForgeApplication.MODELS_DIR)
            val models = mutableListOf<ModelInfo>()
            
            if (modelsDir.exists()) {
                modelsDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".gguf")) {
                        models.add(
                            ModelInfo(
                                name = file.nameWithoutExtension,
                                path = file.absolutePath,
                                fileSizeGB = file.length() / (1024.0 * 1024.0 * 1024.0)
                            )
                        )
                    }
                }
            }
            
            // Add Ollama model option
            models.add(
                ModelInfo(
                    name = "ThinkForge-Pro (Ollama)",
                    path = "ollama://thinkforge-pro:0813",
                    quantization = "Ollama"
                )
            )
            
            _availableModels.postValue(models)
            if (models.isEmpty()) {
                _modelStatus.postValue("No models found. Download one first.")
            } else {
                _modelStatus.postValue("${models.size} model(s) available")
            }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            val historyFile = File(ThinkForgeApplication.HISTORY_DIR, "sessions.json")
            if (historyFile.exists()) {
                try {
                    val json = historyFile.readText()
                    val type = object : TypeToken<List<ChatSession>>() {}.type
                    val sessions: List<ChatSession> = gson.fromJson(json, type)
                    _sessions.postValue(sessions)
                } catch (e: Exception) {
                    _sessions.postValue(emptyList())
                }
            }
        }
    }

    fun saveCurrentSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val session = _currentSession.value ?: return@launch
            val messages = _messages.value ?: return@launch
            
            val updatedSession = session.copy(
                messages = messages.toMutableList(),
                updatedAt = System.currentTimeMillis()
            )
            
            val currentSessions = _sessions.value?.toMutableList() ?: mutableListOf()
            val index = currentSessions.indexOfFirst { it.id == session.id }
            if (index >= 0) {
                currentSessions[index] = updatedSession
            } else {
                currentSessions.add(0, updatedSession)
            }
            
            _sessions.postValue(currentSessions)
            
            val historyFile = File(ThinkForgeApplication.HISTORY_DIR, "sessions.json")
            historyFile.writeText(gson.toJson(currentSessions))
        }
    }

    fun createNewSession() {
        val session = ChatSession(
            title = "New Chat ${_sessions.value?.size?.plus(1) ?: 1}"
        )
        _currentSession.value = session
        _messages.value = mutableListOf()
        _modelStatus.value = "New session ready"
    }

    private fun loadConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            val configFile = File(ThinkForgeApplication.instance.filesDir, "config.json")
            if (configFile.exists()) {
                try {
                    val json = configFile.readText()
                    val config = gson.fromJson(json, ConfigData::class.java)
                    _generationConfig.postValue(config.generation)
                    _hardwareConfig.postValue(config.hardware)
                } catch (e: Exception) {
                    // Use defaults
                }
            }
        }
    }

    private fun saveConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            val config = ConfigData(
                generation = _generationConfig.value ?: GenerationConfig(),
                hardware = _hardwareConfig.value ?: HardwareConfig()
            )
            val configFile = File(ThinkForgeApplication.instance.filesDir, "config.json")
            configFile.writeText(gson.toJson(config))
        }
    }

    data class ConfigData(
        val generation: GenerationConfig = GenerationConfig(),
        val hardware: HardwareConfig = HardwareConfig()
    )
}