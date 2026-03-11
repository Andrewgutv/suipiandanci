package com.fragmentwords

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.WorkManagerScheduler
import com.fragmentwords.utils.AlarmScheduler
import kotlinx.coroutines.launch

/**
 * 首页 Fragment - 单词推送控制面板
 */
class HomeFragment : Fragment() {

    private lateinit var repository: WordRepository
    private lateinit var database: com.fragmentwords.database.WordDatabase
    private lateinit var tvStatus: TextView
    private lateinit var tvLibraryInfo: TextView
    private lateinit var tvNotebookInfo: TextView
    private lateinit var toggleContainer: FrameLayout
    private lateinit var toggleThumb: View

    private var isPushEnabled = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(requireContext(), "通知权限已授予", Toast.LENGTH_SHORT).show()
            startServiceIfEnabled()
        } else {
            showPermissionGuideDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = WordRepository(requireContext())
        database = com.fragmentwords.database.WordDatabase(requireContext())

        initViews(view)
        setupClickListeners()
        updateUI()

        lifecycleScope.launch {
            repository.initializeIfNeeded()
            updateUI()
        }

        checkAndRequestNotificationPermission()
    }

    private fun initViews(view: View) {
        tvStatus = view.findViewById(R.id.tv_status)
        tvLibraryInfo = view.findViewById(R.id.tv_library_info)
        tvNotebookInfo = view.findViewById(R.id.tv_notebook_info)
        toggleContainer = view.findViewById(R.id.toggle_container)
        toggleThumb = view.findViewById(R.id.toggle_thumb)

        val prefs = requireContext().getSharedPreferences("word_prefs", android.content.Context.MODE_PRIVATE)
        isPushEnabled = prefs.getBoolean("notification_enabled", false)
        updateToggleUI()
    }

    private fun setupClickListeners() {
        toggleContainer.setOnClickListener {
            isPushEnabled = !isPushEnabled
            updateToggleUI()

            requireContext().getSharedPreferences("word_prefs", android.content.Context.MODE_PRIVATE).edit()
                .putBoolean("notification_enabled", isPushEnabled)
                .apply()

            if (isPushEnabled) {
                WordService.startService(requireContext())
                AlarmScheduler.schedulePeriodicAlarm(requireContext())
                WorkManagerScheduler.cancelRefresh(requireContext())
                Toast.makeText(requireContext(), "已开启单词推送", Toast.LENGTH_SHORT).show()
            } else {
                WordService.stopService(requireContext())
                AlarmScheduler.cancelAlarms(requireContext())
                WorkManagerScheduler.cancelRefresh(requireContext())
                Toast.makeText(requireContext(), "已关闭单词推送", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }
    }

    private fun updateToggleUI() {
        // 取消之前的动画
        toggleThumb.animate().cancel()

        if (isPushEnabled) {
            toggleContainer.setBackgroundResource(R.drawable.toggle_track_on)
            toggleThumb.setBackgroundResource(R.drawable.toggle_thumb_on)
            // 滑动到右侧：使用30dp确保到达最右侧
            toggleThumb.animate()
                .translationX(30f)
                .setDuration(300)
                .start()
        } else {
            toggleContainer.setBackgroundResource(R.drawable.toggle_track_off)
            toggleThumb.setBackgroundResource(R.drawable.toggle_thumb)
            // 滑动到左侧
            toggleThumb.animate()
                .translationX(0f)
                .setDuration(300)
                .start()
        }
    }

    private fun updateUI() {
        lifecycleScope.launch {
            val notebookCount = repository.getNotebookCount()

            val selectedLibraries = repository.getSelectedLibraries()
            val libraryName = when {
                selectedLibraries.size == 1 && selectedLibraries[0] == "ADVANCED" -> "四级词库"
                selectedLibraries.size == 1 && selectedLibraries[0] == "CET4" -> "四级词库"
                selectedLibraries.size == 1 && selectedLibraries[0] == "CET6" -> "六级词库"
                selectedLibraries.size == 1 && selectedLibraries[0] == "IELTS" -> "雅思托福"
                selectedLibraries.size == 1 && selectedLibraries[0] == "TOEFL" -> "托福词库"
                selectedLibraries.size == 1 && selectedLibraries[0] == "GRE" -> "考研英语"
                selectedLibraries.size > 1 -> "多个词库"
                else -> "四级词库"
            }

            tvStatus.text = if (isPushEnabled) {
                "单词推送已开启"
            } else {
                "单词推送未开启"
            }

            tvLibraryInfo.text = "词库: $libraryName"
            tvNotebookInfo.text = "生词本: ${notebookCount}个单词"
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startServiceIfEnabled()
            }
        } else {
            startServiceIfEnabled()
        }
    }

    private fun showPermissionGuideDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("需要通知权限")
            .setMessage("碎片单词需要通知权限才能在锁屏显示单词卡片。\n\n请在设置中允许通知权限。")
            .setPositiveButton("去设置") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("取消") { _, _ ->
                isPushEnabled = false
                updateToggleUI()
                val prefs = requireContext().getSharedPreferences("word_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("notification_enabled", false).apply()
                updateUI()
            }
            .setCancelable(false)
            .show()
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            } else {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "无法打开设置页面", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startServiceIfEnabled() {
        val prefs = requireContext().getSharedPreferences("word_prefs", android.content.Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("notification_enabled", false)
        if (enabled) {
            toggleContainer.postDelayed({
                try {
                    WordService.startService(requireContext())
                    AlarmScheduler.schedulePeriodicAlarm(requireContext())
                    WorkManagerScheduler.cancelRefresh(requireContext())
                    Toast.makeText(requireContext(), "单词推送已开启", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Failed to start service: ${e.message}", e)
                    Toast.makeText(requireContext(), "启动服务失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }, 500)
        }
    }
}
