package com.bonak.steady

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.util.Log
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setupToolbar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)

        val menuIcon: ImageButton = findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.info -> {
                    startActivity(Intent(this, InfoActivity::class.java))
                }
                R.id.faq -> {
                    startActivity(Intent(this, FAQActivity::class.java))
                }
                R.id.help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                }
                R.id.terms -> {
                    startActivity(Intent(this, TermsActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)

        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        viewPager.offscreenPageLimit = 2

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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


}