package com.thinkforge.ai.chat.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thinkforge.ai.chat.ui.fragments.ChatFragment
import com.thinkforge.ai.chat.ui.fragments.ModelsFragment
import com.thinkforge.ai.chat.ui.fragments.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatFragment()
            1 -> ModelsFragment()
            2 -> SettingsFragment()
            else -> ChatFragment()
        }
    }
}