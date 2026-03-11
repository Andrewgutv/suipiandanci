package com.fragmentwords

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import kotlinx.coroutines.launch

/**
 * 词库选择Activity - 新UI设计
 */
class LibrarySelectActivity : AppCompatActivity() {

    private lateinit var repository: WordRepository
    private val selectedLibraries = mutableSetOf<String>()

    private lateinit var checkCet4: TextView
    private lateinit var checkCet6: TextView
    private lateinit var checkIelts: TextView
    private lateinit var checkToefl: TextView
    private lateinit var checkGre: TextView
    private lateinit var checkAdvanced: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library_select)

        repository = WordRepository(this)
        initViews()
        loadSelectedLibraries()
    }

    private fun initViews() {
        // 返回按钮
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // 初始化勾选标记
        checkCet4 = findViewById(R.id.check_cet4)
        checkCet6 = findViewById(R.id.check_cet6)
        checkIelts = findViewById(R.id.check_ielts)
        checkToefl = findViewById(R.id.check_toefl)
        checkGre = findViewById(R.id.check_gre)
        checkAdvanced = findViewById(R.id.check_advanced)

        // 设置词库点击事件
        findViewById<View>(R.id.btn_cet4).setOnClickListener {
            toggleLibrary("CET4", checkCet4)
        }
        findViewById<View>(R.id.btn_cet6).setOnClickListener {
            toggleLibrary("CET6", checkCet6)
        }
        findViewById<View>(R.id.btn_ielts).setOnClickListener {
            toggleLibrary("IELTS", checkIelts)
        }
        findViewById<View>(R.id.btn_toefl).setOnClickListener {
            toggleLibrary("TOEFL", checkToefl)
        }
        findViewById<View>(R.id.btn_gre).setOnClickListener {
            toggleLibrary("GRE", checkGre)
        }
        findViewById<View>(R.id.btn_advanced).setOnClickListener {
            toggleLibrary("ADVANCED", checkAdvanced)
        }

        // 确认按钮
        findViewById<View>(R.id.btn_confirm).setOnClickListener {
            confirmSelection()
        }
    }

    private fun loadSelectedLibraries() {
        lifecycleScope.launch {
            val libraries = repository.getSelectedLibraries()
            selectedLibraries.clear()
            selectedLibraries.addAll(libraries)

            // 更新UI显示
            updateCheckMarks()
        }
    }

    private fun toggleLibrary(libraryId: String, checkView: TextView) {
        if (selectedLibraries.contains(libraryId)) {
            selectedLibraries.remove(libraryId)
            checkView.visibility = View.INVISIBLE
        } else {
            selectedLibraries.add(libraryId)
            checkView.visibility = View.VISIBLE
        }
    }

    private fun updateCheckMarks() {
        checkCet4.visibility = if (selectedLibraries.contains("CET4")) View.VISIBLE else View.INVISIBLE
        checkCet6.visibility = if (selectedLibraries.contains("CET6")) View.VISIBLE else View.INVISIBLE
        checkIelts.visibility = if (selectedLibraries.contains("IELTS")) View.VISIBLE else View.INVISIBLE
        checkToefl.visibility = if (selectedLibraries.contains("TOEFL")) View.VISIBLE else View.INVISIBLE
        checkGre.visibility = if (selectedLibraries.contains("GRE")) View.VISIBLE else View.INVISIBLE
        checkAdvanced.visibility = if (selectedLibraries.contains("ADVANCED")) View.VISIBLE else View.INVISIBLE
    }

    private fun confirmSelection() {
        if (selectedLibraries.isEmpty()) {
            Toast.makeText(this, "请至少选择一个词库", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            repository.saveSelectedLibraries(selectedLibraries.toList())
            Toast.makeText(this@LibrarySelectActivity, "已选择 ${selectedLibraries.size} 个词库", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
