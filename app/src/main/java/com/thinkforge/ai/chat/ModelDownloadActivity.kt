package com.thinkforge.ai.chat

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thinkforge.ai.chat.databinding.ActivityModelDownloadBinding

class ModelDownloadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelDownloadBinding
    private var downloadId: Long = -1

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                Toast.makeText(this@ModelDownloadActivity, "Download complete!", Toast.LENGTH_LONG).show()
                binding.downloadButton.text = "Model Downloaded ✓"
                binding.downloadButton.isEnabled = false
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED)

        binding.downloadButton.setOnClickListener {
            startDownload()
        }

        binding.backButton.setOnClickListener { finish() }
    }

    private fun startDownload() {
        val url = "https://huggingface.co/deepseek-ai/DeepSeek-V4-Pro-0813-GGUF/resolve/main/deepseek-v4-pro-0813-Q4_K_M.gguf"
        
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("ThinkForge AI Pro Q4_K_M")
            setDescription("Downloading 36GB model... (large file, may take hours)")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ThinkForgeAI/models/deepseek-v4-pro-0813-Q4_K_M.gguf")
            setAllowedOverMetered(false)
            setAllowedOverRoaming(false)
        }

        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
        
        binding.downloadButton.text = "Downloading..."
        binding.downloadButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(downloadReceiver) } catch (_: Exception) {}
    }
}