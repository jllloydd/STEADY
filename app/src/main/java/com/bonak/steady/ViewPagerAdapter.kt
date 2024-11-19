package com.bonak.steady

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 3 // Replace with the actual number of fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Home1() // Replace with your fragment class
            1 -> Home2() // Replace with your fragment class
            2 -> Home3() // Replace with your fragment class
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}