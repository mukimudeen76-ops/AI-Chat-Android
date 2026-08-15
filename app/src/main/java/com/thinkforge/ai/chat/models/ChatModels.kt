package com.thinkforge.ai.chat.models

import java.util.UUID

enum class MessageRole {
    SYSTEM, USER, ASSISTANT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val modelUsed: String = "ThinkForge-Pro-Q4"
)

data class ModelInfo(
    val name: String,
    val path: String,
    val totalParamsB: Int = 671,
    val activeParamsB: Int = 45,
    val contextTokens: Int = 1_000_000,
    val quantization: String = "Q4_K_M",
    val fileSizeGB: Double = 36.0
)

data class GenerationConfig(
    var contextWindow: Int = 1_000_000,
    var maxNewTokens: Int = 8192,
    var temperature: Float = 0.6f,
    var topP: Float = 0.95f,
    var streamTokens: Boolean = true
)

data class HardwareConfig(
    var gpuLayers: Int = 28,
    var cpuThreads: Int = 6,
    var lowVramMode: Boolean = false
)

sealed class InferenceBackend {
    object GGUF : InferenceBackend()
    data class Ollama(val url: String = "http://localhost:11434") : InferenceBackend()
}

data class ChatStats(
    val tokensOut: Long = 0,
    val lastLatencyMs: Long = 0,
    val tokensPerSecond: Float = 0f
)

enum class ModelDownloadState {
    NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED, ERROR
}