package com.fragmentwords

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AppPreferences
import com.fragmentwords.utils.LibrarySelection
import kotlinx.coroutines.launch

/**
 * 词库选择Activity - 新UI设计
 */
class LibrarySelectActivity : AppCompatActivity() {

    private lateinit var repository: WordRepository
    private val selectedLibraries = linkedSetOf<String>()

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
            toggleLibrary(LibrarySelection.CET4, checkCet4)
        }
        findViewById<View>(R.id.btn_cet6).setOnClickListener {
            toggleLibrary(LibrarySelection.CET6, checkCet6)
        }
        findViewById<View>(R.id.btn_ielts).setOnClickListener {
            toggleLibrary(LibrarySelection.IELTS, checkIelts)
        }
        findViewById<View>(R.id.btn_toefl).setOnClickListener {
            toggleLibrary(LibrarySelection.TOEFL, checkToefl)
        }
        findViewById<View>(R.id.btn_gre).setOnClickListener {
            toggleLibrary(LibrarySelection.GRE, checkGre)
        }
        findViewById<View>(R.id.btn_advanced).setOnClickListener {
            toggleLibrary(LibrarySelection.ADVANCED, checkAdvanced)
        }

        // 确认按钮
        findViewById<View>(R.id.btn_confirm).setOnClickListener {
            confirmSelection()
        }
    }

    private fun loadSelectedLibraries() {
        lifecycleScope.launch {
            repository.initializeIfNeeded()
            val libraries = repository.getSelectedLibraries()
            selectedLibraries.clear()
            selectedLibraries.addAll(libraries)

            // 更新UI显示
            updateCheckMarks()
        }
    }

    private fun toggleLibrary(libraryId: String, checkView: TextView) {
        if (selectedLibraries.size == 1 && selectedLibraries.contains(libraryId)) {
            checkView.visibility = View.VISIBLE
            return
        }

        selectedLibraries.clear()
        selectedLibraries.add(libraryId)
        updateCheckMarks()
    }

    private fun updateCheckMarks() {
        checkCet4.visibility = if (selectedLibraries.contains(LibrarySelection.CET4)) View.VISIBLE else View.INVISIBLE
        checkCet6.visibility = if (selectedLibraries.contains(LibrarySelection.CET6)) View.VISIBLE else View.INVISIBLE
        checkIelts.visibility = if (selectedLibraries.contains(LibrarySelection.IELTS)) View.VISIBLE else View.INVISIBLE
        checkToefl.visibility = if (selectedLibraries.contains(LibrarySelection.TOEFL)) View.VISIBLE else View.INVISIBLE
        checkGre.visibility = if (selectedLibraries.contains(LibrarySelection.GRE)) View.VISIBLE else View.INVISIBLE
        checkAdvanced.visibility = if (selectedLibraries.contains(LibrarySelection.ADVANCED)) View.VISIBLE else View.INVISIBLE
    }

    private fun confirmSelection() {
        if (selectedLibraries.isEmpty()) {
            Toast.makeText(this, getString(R.string.select_at_least_one_library), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val saved = repository.applyLibrarySelection(selectedLibraries.toList())
            repository.clearCurrentWord()
            if (AppPreferences.isNotificationEnabled(this@LibrarySelectActivity)) {
                WordService.showNewWord(this@LibrarySelectActivity)
            }
            if (!saved) {
                Toast.makeText(this@LibrarySelectActivity, getString(R.string.library_load_partial_failure), Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(
                this@LibrarySelectActivity,
                getString(R.string.library_selected_count, selectedLibraries.size),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
