package com.thinkforge.ai.chat

import android.app.Application
import android.content.Context
import java.io.File

class ThinkForgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeDirectories()
    }

    private fun initializeDirectories() {
        val modelsDir = File(filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()
        
        val historyDir = File(filesDir, "history")
        if (!historyDir.exists()) historyDir.mkdirs()
    }

    companion object {
        lateinit var instance: ThinkForgeApplication
            private set

        fun getContext(): Context = instance.applicationContext
        
        val MODELS_DIR: String
            get() = File(instance.filesDir, "models").absolutePath
            
        val HISTORY_DIR: String
            get() = File(instance.filesDir, "history").absolutePath
    }
}