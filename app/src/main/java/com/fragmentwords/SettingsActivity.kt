package com.fragmentwords

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import com.fragmentwords.utils.AlarmScheduler
import com.fragmentwords.utils.WorkManagerScheduler
import com.fragmentwords.service.WordService
import kotlinx.coroutines.launch

/**
 * 设置 Activity - 应用设置界面（独立Activity版本）
 */
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

    private fun initViews() {
        // 推送开关
        val switchPush = findViewById<android.widget.Switch>(R.id.switch_push)
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            isPushEnabled = isChecked
            onPushToggleChanged(isChecked)
        }

        // 词库管理
        findViewById<android.view.View>(R.id.btn_manage_libraries).setOnClickListener {
            val intent = Intent(this, LibrarySelectActivity::class.java)
            startActivity(intent)
        }

        // 关于
        findViewById<android.view.View>(R.id.btn_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("word_prefs", MODE_PRIVATE)
        isPushEnabled = prefs.getBoolean("notification_enabled", false)

        val switchPush = findViewById<android.widget.Switch>(R.id.switch_push)
        switchPush.isChecked = isPushEnabled
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
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("关于碎片单词")
            .setMessage("碎片单词 v2.1.0\n\n一款优雅的单词学习应用\n利用碎片时间，轻松积累词汇")
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }
}
