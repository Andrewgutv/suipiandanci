package com.fragmentwords

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import com.fragmentwords.model.Word
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

/**
 * 生词本 Activity - 显示生词本单词详情
 */
class NotebookActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var tvWord: TextView
    private lateinit var tvPhonetic: TextView
    private lateinit var tvPartOfSpeech: TextView
    private lateinit var tvLibrary: TextView
    private lateinit var tvTranslation: TextView
    private lateinit var tvExample: TextView
    private lateinit var tvNotebookCount: TextView

    private lateinit var repository: WordRepository
    private lateinit var notebookWords: List<Word>
    private var currentWordIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notebook)

        repository = WordRepository(this)

        initViews()
        setupToolbar()
        setupBackPressedCallback()
        loadNotebookWords()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        tvWord = findViewById(R.id.tv_word)
        tvPhonetic = findViewById(R.id.tv_phonetic)
        tvPartOfSpeech = findViewById(R.id.tv_part_of_speech)
        tvLibrary = findViewById(R.id.tv_library)
        tvTranslation = findViewById(R.id.tv_translation)
        tvExample = findViewById(R.id.tv_example)

        // 获取侧边栏头部的生词数量
        val headerView = navigationView.getHeaderView(0)
        tvNotebookCount = headerView.findViewById(R.id.tv_notebook_count)

        // 侧边栏切换
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 点击侧边栏项目切换单词
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val index = menuItem.itemId
            if (index in notebookWords.indices) {
                currentWordIndex = index
                displayWord(notebookWords[index])
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            true
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.title = "生词本"
        toolbar.setNavigationOnClickListener {
            // 打开左侧侧边栏
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.notebook_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_word -> {
                deleteCurrentWord()
                true
            }
            R.id.action_clear_all -> {
                showClearConfirmDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun loadNotebookWords() {
        lifecycleScope.launch {
            try {
                notebookWords = repository.getNotebookWords()
                tvNotebookCount.text = "共 ${notebookWords.size} 个生词"

                if (notebookWords.isEmpty()) {
                    showEmptyDialog()
                } else {
                    updateSidebarMenu()
                    displayWord(notebookWords[0])
                }
            } catch (e: Exception) {
                Toast.makeText(this@NotebookActivity, "加载生词本失败: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateSidebarMenu() {
        try {
            val menu = navigationView.menu

            // 清空现有的菜单项
            menu.clear()

            // 创建新的 group
            val groupId = R.id.notebook_words_group

            // 动态添加生词列表
            notebookWords.forEachIndexed { index, word ->
                menu.add(groupId, index, 0, word.word)
                    .setIcon(android.R.drawable.ic_menu_edit)
                    .isCheckable = true
            }

            // 设置 group 可选中
            menu.setGroupCheckable(groupId, true, true)

            // 高亮当前单词
            if (currentWordIndex in notebookWords.indices) {
                menu.findItem(currentWordIndex)?.isChecked = true
            }
        } catch (e: Exception) {
            // 忽略菜单更新错误
        }
    }

    private fun displayWord(word: Word) {
        // 加载滑动动画
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)

        // 应用动画到各个视图
        tvWord.startAnimation(fadeIn)
        tvPhonetic.startAnimation(slideIn)
        tvPartOfSpeech.startAnimation(slideIn)
        tvLibrary.startAnimation(slideIn)
        tvTranslation.startAnimation(slideIn)
        tvExample.startAnimation(slideIn)

        // 设置文本内容
        tvWord.text = word.word
        tvPhonetic.text = word.phonetic
        tvPartOfSpeech.text = word.partOfSpeech.ifEmpty { "n." }
        tvLibrary.text = word.library.ifEmpty { "未知" }
        tvTranslation.text = word.translation
        tvExample.text = word.example

        // 高亮当前选中的菜单项（只处理生词列表group）
        try {
            val groupId = R.id.notebook_words_group
            // 遍历特定group的菜单项
            for (i in 0 until navigationView.menu.size()) {
                val item = navigationView.menu.getItem(i)
                if (item.groupId == groupId) {
                    item.isChecked = (item.itemId == currentWordIndex)
                }
            }
        } catch (e: Exception) {
            // 忽略菜单设置错误
        }
    }

    private fun showEmptyDialog() {
        AlertDialog.Builder(this)
            .setTitle("生词本")
            .setMessage("生词本为空\n\n标记为\"不认识\"的单词会自动添加到这里")
            .setPositiveButton("确定") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun deleteCurrentWord() {
        if (currentWordIndex !in notebookWords.indices) {
            return
        }

        val word = notebookWords[currentWordIndex]
        AlertDialog.Builder(this)
            .setTitle("删除生词")
            .setMessage("确定要删除「${word.word}」吗？")
            .setPositiveButton("删除") { _, _ ->
                repository.removeFromNotebook(word.word)
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()

                // 重新加载生词本
                loadNotebookWords()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showClearConfirmDialog() {
        if (notebookWords.isEmpty()) {
            Toast.makeText(this, "生词本为空", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("清空生词本")
            .setMessage("确定要清空所有 ${notebookWords.size} 个生词吗？")
            .setPositiveButton("清空") { _, _ ->
                val count = repository.clearNotebook()
                Toast.makeText(this, "已清空 $count 个生词", Toast.LENGTH_SHORT).show()

                // 重新加载生词本
                loadNotebookWords()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
