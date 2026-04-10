package com.fragmentwords

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)
            initViews()
            setupViewPager()
            setupBottomNavigation()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            finish()
        }
    }

    private fun initViews() {
        viewPager = findViewById(R.id.view_pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                try {
                    bottomNavigation.selectedItemId = when (position) {
                        0 -> R.id.navigation_home
                        1 -> R.id.navigation_notebook
                        2 -> R.id.navigation_settings
                        else -> R.id.navigation_home
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error in onPageSelected: ${e.message}", e)
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            try {
                when (item.itemId) {
                    R.id.navigation_home -> {
                        viewPager.currentItem = 0
                        true
                    }
                    R.id.navigation_notebook -> {
                        viewPager.currentItem = 1
                        true
                    }
                    R.id.navigation_settings -> {
                        viewPager.currentItem = 2
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in navigation: ${e.message}", e)
                false
            }
        }

        bottomNavigation.selectedItemId = R.id.navigation_home
    }
}
