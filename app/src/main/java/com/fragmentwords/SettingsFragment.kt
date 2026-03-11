package com.fragmentwords

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

/**
 * 设置 Fragment - 应用设置界面
 */
class SettingsFragment : Fragment() {

    private lateinit var tvCurrentLibrary: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        loadSettings()
    }

    private fun initViews(view: View) {
        tvCurrentLibrary = view.findViewById(R.id.tv_current_library)
    }

    private fun setupClickListeners() {
        // 词库管理按钮
        view?.findViewById<View>(R.id.btn_manage_libraries)?.setOnClickListener {
            val intent = Intent(requireContext(), LibrarySelectActivity::class.java)
            startActivity(intent)
        }

        // 关于按钮
        view?.findViewById<View>(R.id.btn_about)?.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadSettings() {
        // 显示当前选择的词库
        val prefs = requireContext().getSharedPreferences("word_prefs", android.content.Context.MODE_PRIVATE)
        val selectedLibraries = prefs.getStringSet("selected_libraries", null)
        val libraryName = when {
            selectedLibraries?.size == 1 && selectedLibraries.contains("ADVANCED") -> "高级词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("CET4") -> "四级词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("CET6") -> "六级词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("IELTS") -> "雅思词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("TOEFL") -> "托福词库"
            selectedLibraries?.size == 1 && selectedLibraries.contains("GRE") -> "GRE词库"
            selectedLibraries?.size ?: 0 > 1 -> "多个词库"
            else -> "四级词库"
        }
        tvCurrentLibrary.text = libraryName
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
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
