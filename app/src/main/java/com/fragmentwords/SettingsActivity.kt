package com.fragmentwords

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fragmentwords.data.WordRepository
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AlarmScheduler
import com.fragmentwords.utils.WorkManagerScheduler

class SettingsActivity : AppCompatActivity() {

    private lateinit var repository: WordRepository
    private var isPushEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        repository = WordRepository(this)
        initViews()
        loadSettings()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }

    private fun initViews() {
        val switchPush = findViewById<Switch>(R.id.switch_push)
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            isPushEnabled = isChecked
            onPushToggleChanged(isChecked)
        }

        findViewById<android.view.View>(R.id.btn_manage_libraries).setOnClickListener {
            startActivity(Intent(this, LibrarySelectActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btn_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("word_prefs", MODE_PRIVATE)
        isPushEnabled = prefs.getBoolean("notification_enabled", false)
        findViewById<Switch>(R.id.switch_push).isChecked = isPushEnabled
    }

    private fun onPushToggleChanged(enabled: Boolean) {
        val prefs = getSharedPreferences("word_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("notification_enabled", enabled).apply()

        if (enabled) {
            WordService.startService(this)
            AlarmScheduler.schedulePeriodicAlarm(this)
            WorkManagerScheduler.cancelRefresh(this)
            Toast.makeText(this, "已开启单词推送", Toast.LENGTH_SHORT).show()
        } else {
            WordService.stopService(this)
            AlarmScheduler.cancelAlarms(this)
            WorkManagerScheduler.cancelRefresh(this)
            Toast.makeText(this, "已关闭单词推送", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("关于碎片单词")
            .setMessage("碎片单词 v2.1.0\n\n一款利用碎片时间积累词汇的英语学习应用。")
            .setPositiveButton("确定", null)
            .show()
    }
}
