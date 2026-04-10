package com.fragmentwords

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AlarmScheduler
import com.fragmentwords.utils.WorkManagerScheduler

class SettingsFragment : Fragment() {

    private lateinit var switchPush: Switch
    private lateinit var tvCurrentLibrary: TextView
    private var suppressSwitchCallback = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners(view)
        loadSettings()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }

    private fun initViews(view: View) {
        switchPush = view.findViewById(R.id.switch_push)
        tvCurrentLibrary = view.findViewById(R.id.tv_current_library)
    }

    private fun setupClickListeners(view: View) {
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchCallback) {
                return@setOnCheckedChangeListener
            }
            onPushToggleChanged(isChecked)
        }

        view.findViewById<View>(R.id.btn_manage_libraries).setOnClickListener {
            startActivity(Intent(requireContext(), LibrarySelectActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("word_prefs", Context.MODE_PRIVATE)
        val pushEnabled = prefs.getBoolean("notification_enabled", false)
        val selectedLibraries = prefs.getStringSet("selected_libraries", null)
        val libraryName = when {
            selectedLibraries?.size == 1 && selectedLibraries.contains("ADVANCED") -> "高级词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("CET4") -> "四级词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("CET6") -> "六级词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("IELTS") -> "雅思词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("TOEFL") -> "托福词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("GRE") -> "GRE 词库"
            (selectedLibraries?.size ?: 0) > 1 -> "多个词库"
            else -> "四级词库"
        }

        suppressSwitchCallback = true
        switchPush.isChecked = pushEnabled
        suppressSwitchCallback = false
        tvCurrentLibrary.text = libraryName
    }

    private fun onPushToggleChanged(enabled: Boolean) {
        val prefs = requireContext().getSharedPreferences("word_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notification_enabled", enabled).apply()

        if (enabled) {
            WordService.startService(requireContext())
            AlarmScheduler.schedulePeriodicAlarm(requireContext())
            WorkManagerScheduler.cancelRefresh(requireContext())
        } else {
            WordService.stopService(requireContext())
            AlarmScheduler.cancelAlarms(requireContext())
            WorkManagerScheduler.cancelRefresh(requireContext())
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("关于碎片单词")
            .setMessage("碎片单词 v2.1.0\n\n一款利用碎片时间积累词汇的英语学习应用。")
            .setPositiveButton("确定", null)
            .show()
    }
}
