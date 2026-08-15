package com.thinkforge.ai.chat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*

class OllamaService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> startOllama()
            ACTION_CHECK -> checkOllama()
        }
        return START_STICKY
    }

    private fun startOllama() {
        scope.launch {
            try {
                val process = ProcessBuilder()
                    .command("ollama", "serve")
                    .redirectErrorStream(true)
                    .start()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkOllama() {
        scope.launch {
            try {
                val url = java.net.URL("http://localhost:11434/api/tags")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 2000
                val isRunning = conn.responseCode == 200
                // Broadcast result
                val intent = Intent(ACTION_OLLAMA_STATUS).apply {
                    putExtra(EXTRA_IS_RUNNING, isRunning)
                }
                sendBroadcast(intent)
            } catch (e: Exception) {
                val intent = Intent(ACTION_OLLAMA_STATUS).apply {
                    putExtra(EXTRA_IS_RUNNING, false)
                }
                sendBroadcast(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val ACTION_START = "com.thinkforge.ai.action.START_OLLAMA"
        const val ACTION_CHECK = "com.thinkforge.ai.action.CHECK_OLLAMA"
        const val ACTION_OLLAMA_STATUS = "com.thinkforge.ai.action.OLLAMA_STATUS"
        const val EXTRA_IS_RUNNING = "is_running"
    }
}