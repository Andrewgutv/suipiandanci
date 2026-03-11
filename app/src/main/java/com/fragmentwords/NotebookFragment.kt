package com.fragmentwords

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fragmentwords.adapter.WordListAdapter
import com.fragmentwords.data.WordRepository
import com.fragmentwords.model.Word
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 生词本 Fragment - 显示生词本单词详情
 */
class NotebookFragment : Fragment() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvWord: TextView
    private lateinit var tvPhonetic: TextView
    private lateinit var tvPartOfSpeech: TextView
    private lateinit var tvLibrary: TextView
    private lateinit var tvTranslation: TextView
    private lateinit var tvExample: TextView
    private lateinit var tvNotebookCount: TextView
    private lateinit var rvWords: RecyclerView
    private lateinit var wordAdapter: WordListAdapter
    private lateinit var btnPronounce: ImageButton

    private lateinit var repository: WordRepository
    private lateinit var notebookWords: List<Word>
    private var currentWordIndex: Int = 0
    private var textToSpeech: TextToSpeech? = null
    private var currentWord: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notebook, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = WordRepository(requireContext())

        initViews(view)
        initTextToSpeech()
        loadNotebookWords()
    }

    private fun initViews(view: View) {
        drawerLayout = view.findViewById(R.id.drawer_layout)
        tvWord = view.findViewById(R.id.tv_word)
        tvPhonetic = view.findViewById(R.id.tv_phonetic)
        tvPartOfSpeech = view.findViewById(R.id.tv_part_of_speech)
        tvLibrary = view.findViewById(R.id.tv_library)
        tvTranslation = view.findViewById(R.id.tv_translation)
        tvExample = view.findViewById(R.id.tv_example)
        tvNotebookCount = view.findViewById(R.id.tv_notebook_count)
        rvWords = view.findViewById(R.id.rv_words)
        btnPronounce = view.findViewById(R.id.btn_pronounce)

        // 设置RecyclerView
        wordAdapter = WordListAdapter(emptyList()) { word, position ->
            currentWordIndex = position
            displayWord(word)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        rvWords.layoutManager = LinearLayoutManager(requireContext())
        rvWords.adapter = wordAdapter

        // 菜单按钮点击事件
        view.findViewById<ImageButton>(R.id.btn_menu).setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // 发音按钮点击事件
        btnPronounce.setOnClickListener {
            pronounceWord()
        }
    }

    private fun loadNotebookWords() {
        lifecycleScope.launch {
            try {
                notebookWords = repository.getNotebookWords()
                tvNotebookCount.text = "共 ${notebookWords.size} 个生词"

                if (notebookWords.isEmpty()) {
                    showEmptyDialog()
                } else {
                    wordAdapter.updateWords(notebookWords)
                    displayWord(notebookWords[0])
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "加载生词本失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayWord(word: Word) {
        // 加载滑动动画
        val slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_scale)

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

        // 保存当前单词用于发音
        currentWord = word.word

        // 更新侧边栏选中状态
        wordAdapter.setSelectedPosition(currentWordIndex)
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.ENGLISH
                textToSpeech?.setSpeechRate(0.9f)
            }
        }
    }

    private fun pronounceWord() {
        if (currentWord.isEmpty()) {
            Toast.makeText(requireContext(), "没有可发音的单词", Toast.LENGTH_SHORT).show()
            return
        }

        textToSpeech?.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        textToSpeech?.shutdown()
        super.onDestroy()
    }

    private fun showEmptyDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("生词本")
            .setMessage("生词本为空\n\n标记为不认识的单词会自动添加到这里")
            .setPositiveButton("确定", null)
            .setCancelable(false)
            .show()
    }
}
