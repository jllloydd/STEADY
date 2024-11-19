package com.bonak.steady

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.text
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)

        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        tabLayout.setBackgroundColor(resources.getColor(R.color.black, theme))

        tabLayout.setTabTextColors(
            resources.getColor(R.color.text_light, theme),
            resources.getColor(R.color.brown, theme)
        )

        tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.light_brown, theme))

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Map"
                1 -> tab.text = "News"
                2 -> tab.text = "Stats"
            }
        }.attach()
    }
}

