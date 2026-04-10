package com.fragmentwords

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
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

class NotebookFragment : Fragment() {

    companion object {
        private const val TAG = "NotebookFragment"
    }

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
    private var notebookWords: List<Word> = emptyList()
    private var currentWordIndex: Int = 0
    private var textToSpeech: TextToSpeech? = null
    private var currentWord: String = ""
    private var emptyDialogShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notebook, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = WordRepository(requireContext())
        initViews(view)
        initTextToSpeech()
        loadNotebookWords(selectLatest = true)
    }

    override fun onResume() {
        super.onResume()
        loadNotebookWords(selectLatest = true)
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

        wordAdapter = WordListAdapter(emptyList()) { word, position ->
            currentWordIndex = position
            displayWord(word)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        rvWords.layoutManager = LinearLayoutManager(requireContext())
        rvWords.adapter = wordAdapter

        view.findViewById<ImageButton>(R.id.btn_menu).setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        btnPronounce.setOnClickListener { pronounceWord() }
    }

    private fun loadNotebookWords(selectLatest: Boolean) {
        lifecycleScope.launch {
            try {
                notebookWords = repository.getNotebookWords()
                tvNotebookCount.text = "共 ${notebookWords.size} 个单词"
                Log.d(TAG, "Loaded notebook words: ${notebookWords.joinToString { it.word }}")

                if (notebookWords.isEmpty()) {
                    currentWordIndex = 0
                    currentWord = ""
                    wordAdapter.updateWords(emptyList())
                    showEmptyState()
                    return@launch
                }

                emptyDialogShown = false
                currentWordIndex = if (selectLatest) 0 else currentWordIndex.coerceIn(notebookWords.indices)
                wordAdapter.updateWords(notebookWords)
                displayWord(notebookWords[currentWordIndex])
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load notebook words", e)
                Toast.makeText(
                    requireContext(),
                    "加载生词本失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showEmptyState() {
        tvWord.text = getString(R.string.empty_notebook)
        tvPhonetic.text = ""
        tvPartOfSpeech.text = ""
        tvLibrary.text = ""
        tvTranslation.text = getString(R.string.no_words)
        tvExample.text = "点击通知卡片中的“不认识”即可加入生词本。"
        if (!emptyDialogShown) {
            emptyDialogShown = true
            showEmptyDialog()
        }
    }

    private fun displayWord(word: Word) {
        Log.d(TAG, "Displaying notebook word: ${word.word}, library=${word.library}, pos=${word.partOfSpeech}")

        val slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_scale)

        tvWord.startAnimation(fadeIn)
        tvPhonetic.startAnimation(slideIn)
        tvPartOfSpeech.startAnimation(slideIn)
        tvLibrary.startAnimation(slideIn)
        tvTranslation.startAnimation(slideIn)
        tvExample.startAnimation(slideIn)

        tvWord.text = word.word
        tvPhonetic.text = word.phonetic
        tvPartOfSpeech.text = word.partOfSpeech.ifEmpty { "-" }
        tvLibrary.text = word.library.ifEmpty { "未分类" }
        tvTranslation.text = word.translation
        tvExample.text = word.example.ifBlank { "暂无例句" }

        currentWord = word.word
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
        if (currentWord.isBlank()) {
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
            .setMessage("生词本为空\n\n标记为不认识的单词会自动加入这里。")
            .setPositiveButton("确定", null)
            .show()
    }
}
