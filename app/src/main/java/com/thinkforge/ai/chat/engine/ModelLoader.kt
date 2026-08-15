package com.thinkforge.ai.chat.engine

import com.thinkforge.ai.chat.models.GenerationConfig
import com.thinkforge.ai.chat.models.ModelInfo
import java.io.File
import java.io.RandomAccessFile

class ModelLoader {

    private var loaded = false
    private var modelInfo: ModelInfo? = null
    private var nativeHandle: Long = 0
    
    companion object {
        init {
            try {
                System.loadLibrary("thinkforge_engine")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
        
        private external fun nativeLoadModel(path: String): Long
        private external fun nativeGenerate(
            handle: Long,
            prompt: String,
            temperature: Float,
            topP: Float,
            maxTokens: Int
        ): String
        private external fun nativeGenerateStream(
            handle: Long,
            prompt: String,
            temperature: Float,
            topP: Float,
            maxTokens: Int
        ): Boolean
        private external fun nativeRelease(handle: Long)
    }

    fun load(path: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists()) return false
            
            // Verify GGUF magic bytes
            val raf = RandomAccessFile(file, "r")
            val magic = ByteArray(4)
            raf.read(magic)
            raf.close()
            
            val magicStr = String(magic)
            if (magicStr != "GGUF") return false

            // Try native loading
            nativeHandle = nativeLoadModel(path)
            loaded = nativeHandle != 0L
            
            if (loaded) {
                modelInfo = ModelInfo(
                    name = file.nameWithoutExtension,
                    path = path,
                    totalParamsB = 671,
                    activeParamsB = 45,
                    contextTokens = 1_000_000,
                    quantization = detectQuantization(file.name)
                )
            }
            
            loaded
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generate(
        history: List<String>,
        config: GenerationConfig,
        callback: TokenCallback
    ) {
        if (!loaded || nativeHandle == 0L) {
            callback.onToken("\n\n[Model not loaded. Please download a model first.]")
            return
        }

        val prompt = history.joinToString("\n") + "\nAssistant:"
        
        if (config.streamTokens) {
            nativeGenerateStream(
                nativeHandle, prompt,
                config.temperature, config.topP, config.maxNewTokens
            )
            // In streaming mode, tokens come via JNI callback
        } else {
            val result = nativeGenerate(
                nativeHandle, prompt,
                config.temperature, config.topP, config.maxNewTokens
            )
            callback.onToken(result)
        }
    }

    fun info(): ModelInfo? = modelInfo

    fun release() {
        if (nativeHandle != 0L) {
            try {
                nativeRelease(nativeHandle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            nativeHandle = 0
            loaded = false
        }
    }

    private fun detectQuantization(filename: String): String {
        return when {
            filename.contains("Q4_K_M") -> "Q4_K_M"
            filename.contains("Q6_K") -> "Q6_K"
            filename.contains("Q8_0") -> "Q8_0"
            filename.contains("BF16") -> "BF16"
            else -> "unknown"
        }
    }
}