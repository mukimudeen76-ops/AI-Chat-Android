package com.thinkforge.ai.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.thinkforge.ai.chat.ui.adapters.MainPagerAdapter
import com.thinkforge.ai.chat.ui.fragments.ChatFragment
import com.thinkforge.ai.chat.ui.fragments.SettingsFragment
import com.thinkforge.ai.chat.ui.fragments.ModelsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNavigation)

        setupViewPager()
        setupBottomNav()
    }

    private fun setupViewPager() {
        pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_models -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_settings -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
    }
}