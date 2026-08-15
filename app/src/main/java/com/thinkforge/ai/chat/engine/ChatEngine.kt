package com.thinkforge.ai.chat.engine

import com.thinkforge.ai.chat.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class ChatEngine(private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())) {

    private var modelLoader: ModelLoader? = null
    private var backend: InferenceBackend = InferenceBackend.GGUF
    
    private val _stats = MutableStateFlow(ChatStats())
    val stats: StateFlow<ChatStats> = _stats.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isResponding = MutableStateFlow(false)
    val isResponding: StateFlow<Boolean> = _isResponding.asStateFlow()
    
    private val history = mutableListOf<ChatMessage>()
    private val maxContextTokens = 1_000_000

    fun setSystemPrompt(prompt: String) {
        history.clear()
        history.add(ChatMessage(role = MessageRole.SYSTEM, content = prompt))
    }

    fun loadModel(modelPath: String): Boolean {
        _isLoading.value = true
        return try {
            val loader = ModelLoader()
            val success = loader.load(modelPath)
            if (success) {
                modelLoader = loader
            }
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun setBackend(newBackend: InferenceBackend) {
        backend = newBackend
    }

    suspend fun streamReply(
        prompt: String,
        onToken: (String) -> Unit,
        config: GenerationConfig = GenerationConfig()
    ) {
        _isResponding.value = true
        history.add(ChatMessage(role = MessageRole.USER, content = prompt))

        val startTime = System.currentTimeMillis()
        val output = StringBuilder()

        try {
            when (backend) {
                is InferenceBackend.GGUF -> {
                    modelLoader?.generate(
                        history.map { it.content },
                        config,
                        object : TokenCallback {
                            override fun onToken(token: String) {
                                output.append(token)
                                onToken(token)
                            }
                        }
                    )
                }
                is InferenceBackend.Ollama -> {
                    val ollama = (backend as InferenceBackend.Ollama)
                    generateWithOllama(ollama.url, prompt, config) { token ->
                        output.append(token)
                        onToken(token)
                    }
                }
            }

            val elapsed = System.currentTimeMillis() - startTime
            val tokens = output.length / 4 // approximate
            _stats.value = ChatStats(
                tokensOut = _stats.value.tokensOut + tokens,
                lastLatencyMs = elapsed,
                tokensPerSecond = if (elapsed > 0) (tokens.toFloat() / elapsed * 1000) else 0f
            )

            history.add(ChatMessage(role = MessageRole.ASSISTANT, content = output.toString()))
            trimHistory()

        } catch (e: Exception) {
            onToken("\n\n[Error: ${e.message}]")
        } finally {
            _isResponding.value = false
        }
    }

    private fun generateWithOllama(
        baseUrl: String,
        prompt: String,
        config: GenerationConfig,
        onToken: (String) -> Unit
    ) {
        // Ollama API call implementation
        val url = "$baseUrl/api/generate"
        val requestBody = """
            {
                "model": "thinkforge-pro:0813",
                "prompt": ${jsonEncode(prompt)},
                "stream": ${config.streamTokens},
                "options": {
                    "temperature": ${config.temperature},
                    "top_p": ${config.topP},
                    "num_predict": ${config.maxNewTokens}
                }
            }
        """.trimIndent()
        
        // HTTP client implementation
        httpPostStream(url, requestBody, onToken)
    }

    private fun trimHistory() {
        var totalTokens = 0
        for (msg in history) {
            totalTokens += msg.content.length / 4
        }
        while (totalTokens > maxContextTokens && history.size > 2) {
            totalTokens -= history[1].content.length / 4
            history.removeAt(1) // keep system prompt at [0]
        }
    }

    fun getHistory(): List<ChatMessage> = history.toList()

    fun clearHistory() {
        val systemPrompt = history.firstOrNull { it.role == MessageRole.SYSTEM }
        history.clear()
        if (systemPrompt != null) {
            history.add(systemPrompt)
        }
    }

    fun release() {
        modelLoader?.release()
        modelLoader = null
    }

    private fun jsonEncode(s: String): String {
        return "\"" + s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }

    private fun httpPostStream(url: String, body: String, onToken: (String) -> Unit) {
        try {
            val httpUrl = java.net.URL(url)
            val conn = httpUrl.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 60000

            conn.outputStream.use { os ->
                os.write(body.toByteArray())
            }

            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        try {
                            val json = com.google.gson.JsonParser.parseString(line).asJsonObject
                            if (json.has("response")) {
                                onToken(json.get("response").asString)
                            }
                        } catch (e: Exception) {
                            // partial line, skip
                        }
                    }
                }
            }
        } catch (e: Exception) {
            onToken("\n\n[Connection Error: ${e.message}]")
        }
    }
}

interface TokenCallback {
    fun onToken(token: String)
}