package com.fragmentwords

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.viewpager2.widget.ViewPager2

/**
 * 主界面 - 碎片单词
 * 使用ViewPager2实现滑动切换
 */
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
        try {
            viewPager = findViewById(R.id.view_pager)
            bottomNavigation = findViewById(R.id.bottom_navigation)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in initViews: ${e.message}", e)
            throw e
        }
    }

    private fun setupViewPager() {
        try {
            // 创建Adapter
            val adapter = ViewPagerAdapter(this)
            viewPager.adapter = adapter

            // 禁用预加载，提升性能
            viewPager.offscreenPageLimit = 1

            // ViewPager2页面变化回调，同步更新底部导航栏
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    try {
                        when (position) {
                            0 -> bottomNavigation.selectedItemId = R.id.navigation_home
                            1 -> bottomNavigation.selectedItemId = R.id.navigation_notebook
                            2 -> bottomNavigation.selectedItemId = R.id.navigation_settings
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error in onPageSelected: ${e.message}", e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in setupViewPager: ${e.message}", e)
            throw e
        }
    }

    private fun setupBottomNavigation() {
        try {
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

            // 设置默认选中首页
            bottomNavigation.selectedItemId = R.id.navigation_home
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in setupBottomNavigation: ${e.message}", e)
        }
    }
}
