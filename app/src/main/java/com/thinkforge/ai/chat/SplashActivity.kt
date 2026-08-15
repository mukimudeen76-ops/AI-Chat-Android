package com.thinkforge.ai.chat

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.thinkforge.ai.chat.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate the logo
        binding.splashLogo.alpha = 0f
        binding.splashLogo.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        binding.splashSubtitle.alpha = 0f
        binding.splashSubtitle.animate()
            .alpha(1f)
            .setDuration(800)
            .startDelay = 300

        // Initialize engine in background then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2000)
    }
}