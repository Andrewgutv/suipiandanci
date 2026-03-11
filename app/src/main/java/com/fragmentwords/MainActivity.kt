package com.fragmentwords

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 主界面 - 碎片单词
 * 使用ViewPager2实现滑动切换
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupViewPager()
        setupBottomNavigation()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.view_pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupViewPager() {
        // 创建Adapter
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // 禁用预加载，提升性能
        viewPager.offscreenPageLimit = 1

        // ViewPager2页面变化回调，同步更新底部导航栏
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bottomNavigation.selectedItemId = R.id.navigation_home
                    1 -> bottomNavigation.selectedItemId = R.id.navigation_notebook
                    2 -> bottomNavigation.selectedItemId = R.id.navigation_settings
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
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
        }

        // 设置默认选中首页
        bottomNavigation.selectedItemId = R.id.navigation_home
    }
}
