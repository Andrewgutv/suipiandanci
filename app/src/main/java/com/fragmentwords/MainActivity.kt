package com.fragmentwords

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.WorkManagerScheduler
import kotlinx.coroutines.launch

/**
 * 主界面 - 碎片单词
 */
class MainActivity : AppCompatActivity() {

    private lateinit var repository: WordRepository
    private lateinit var database: com.fragmentwords.database.WordDatabase
    private lateinit var tvStatus: TextView
    private lateinit var tvNotebookCount: TextView
    private lateinit var switchNotification: Switch
    private lateinit var btnNotebook: Button
    private lateinit var btnSelectLibrary: Button
    private lateinit var btnRefreshNow: Button

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
        private const val PREFS_NAME = "word_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = WordRepository(this)
        database = com.fragmentwords.database.WordDatabase(this)

        initViews()
        setupClickListeners()
        updateUI()

        // 初始化词库
        lifecycleScope.launch {
            repository.initializeIfNeeded()
            updateUI()
        }

        // 检查首次启动
        checkFirstLaunch()

        // 检查通知权限
        checkAndRequestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        // onResume 时不再自动启动 WorkManager，避免重复启动
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tv_status)
        tvNotebookCount = findViewById(R.id.tv_notebook_count)
        switchNotification = findViewById(R.id.switch_notification)
        btnNotebook = findViewById(R.id.btn_notebook)
        btnSelectLibrary = findViewById(R.id.btn_select_library)
        btnRefreshNow = findViewById(R.id.btn_refresh_now)

        // 加载用户设置
        val prefs = getSharedPreferences("word_prefs", MODE_PRIVATE)
        switchNotification.isChecked = prefs.getBoolean("notification_enabled", true)
    }

    private fun setupClickListeners() {
        // 开启/关闭通知开关
        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("word_prefs", MODE_PRIVATE).edit()
                .putBoolean("notification_enabled", isChecked)
                .apply()

            if (isChecked) {
                // 启动前台服务，保持通知常驻
                WordService.startService(this@MainActivity)
                // 取消WorkManager定时任务，改为前台服务模式
                WorkManagerScheduler.cancelRefresh(this@MainActivity)
                Toast.makeText(this@MainActivity, "已开启单词推送", Toast.LENGTH_SHORT).show()
            } else {
                // 停止前台服务
                WordService.stopService(this@MainActivity)
                WorkManagerScheduler.cancelRefresh(this@MainActivity)
                Toast.makeText(this@MainActivity, "已关闭单词推送", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }

        // 生词本按钮
        btnNotebook.setOnClickListener {
            val intent = Intent(this@MainActivity, NotebookActivity::class.java)
            startActivity(intent)
        }

        // 词库选择按钮
        btnSelectLibrary.setOnClickListener {
            val intent = Intent(this@MainActivity, LibrarySelectActivity::class.java)
            startActivity(intent)
        }

        // 立即刷新按钮
        btnRefreshNow.setOnClickListener {
            WordService.showNewWord(this@MainActivity)
            Toast.makeText(this@MainActivity, "已刷新单词", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        lifecycleScope.launch {
            val notebookCount = repository.getNotebookCount()

            // 获取已选择的词库
            val selectedLibraries = repository.getSelectedLibraries()
            val libraryNames = when {
                selectedLibraries.size == 1 && selectedLibraries[0] == "ADVANCED" -> "高级词汇"
                selectedLibraries.containsAll(listOf("CET4", "CET6", "IELTS", "TOEFL", "GRE", "ADVANCED")) -> "全部词库"
                selectedLibraries.isNotEmpty() -> {
                    val names = selectedLibraries.map { lib ->
                        when (lib) {
                            "CET4" -> "四级"
                            "CET6" -> "六级"
                            "IELTS" -> "雅思"
                            "TOEFL" -> "托福"
                            "GRE" -> "GRE"
                            "ADVANCED" -> "高级"
                            else -> lib
                        }
                    }
                    names.joinToString("、")
                }
                else -> "未选择"
            }

            // 统计所选词库的单词数
            val wordCount = if (selectedLibraries.isNotEmpty()) {
                database.getWordCountByLibraries(selectedLibraries)
            } else {
                database.getWordCount()
            }

            tvNotebookCount.text = "生词本：$notebookCount 个单词\n已选词库：$libraryNames ($wordCount 个)"

            val prefs = getSharedPreferences("word_prefs", MODE_PRIVATE)
            val enabled = prefs.getBoolean("notification_enabled", true)

            tvStatus.text = if (enabled) {
                "单词推送已开启"
            } else {
                "单词推送已关闭"
            }
        }
    }

    /**
     * 检查是否首次启动，显示欢迎引导
     */
    private fun checkFirstLaunch() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            // 显示欢迎引导
            AlertDialog.Builder(this)
                .setTitle("欢迎使用碎片单词")
                .setMessage(
                    "「碎片单词」帮您利用碎片时间学习英语单词\n\n" +
                    "✅ 像淘宝推送一样，定期收到单词通知\n" +
                    "✅ 不需要app挂在后台，不占用资源\n" +
                    "✅ 点击「认识」跳过，点击「不认识」加入生词本\n" +
                    "✅ 在生词本中复习难词\n\n" +
                    "祝您学习愉快！"
                )
                .setPositiveButton("开始学习") { _, _ ->
                    prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
                }
                .setCancelable(false)
                .show()
        }
    }

    /**
     * 检查并请求通知权限
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要检查 POST_NOTIFICATIONS 权限
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 未授权，请求权限
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            } else {
                // 已授权，直接启动服务
                startServiceIfEnabled()
            }
        } else {
            // Android 13 以下不需要运行时权限，直接启动
            startServiceIfEnabled()
        }
    }

    /**
     * 权限请求结果回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权了
                Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show()
                startServiceIfEnabled()
            } else {
                // 用户拒绝了，引导去设置页面
                showPermissionGuideDialog()
            }
        }
    }

    /**
     * 显示权限引导对话框
     */
    private fun showPermissionGuideDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要通知权限")
            .setMessage("碎片单词需要通知权限才能在锁屏显示单词卡片。\n\n请在设置中允许通知权限。")
            .setPositiveButton("去设置") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("取消") { _, _ ->
                // 用户取消，不启动服务
                val prefs = getSharedPreferences("word_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("notification_enabled", false).apply()
                switchNotification.isChecked = false
                updateUI()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * 打开系统通知设置页面
     */
    private fun openNotificationSettings() {
        try {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            } else {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开设置页面", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 如果用户开启了通知开关，则启动服务
     */
    private fun startServiceIfEnabled() {
        val prefs = getSharedPreferences("word_prefs", MODE_PRIVATE)
        val enabled = prefs.getBoolean("notification_enabled", true)
        if (enabled) {
            // 延迟启动前台服务，避免影响app启动
            switchNotification.postDelayed({
                try {
                    // 启动前台服务，保持通知常驻
                    WordService.startService(this@MainActivity)
                    // 取消WorkManager定时任务
                    WorkManagerScheduler.cancelRefresh(this@MainActivity)

                    Toast.makeText(this@MainActivity, "单词推送已开启，每次解锁手机显示新单词", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to start service: ${e.message}", e)
                    Toast.makeText(this@MainActivity, "启动服务失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }, 500) // 延迟500ms启动
        }
    }
}
