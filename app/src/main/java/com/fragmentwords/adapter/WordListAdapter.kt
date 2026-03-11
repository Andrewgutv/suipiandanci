package com.fragmentwords.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fragmentwords.R
import com.fragmentwords.model.Word

/**
 * 单词列表适配器 - 用于生词本侧边栏
 */
class WordListAdapter(
    private var words: List<Word>,
    private val onWordClick: (Word, Int) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    private var selectedPosition = 0

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWord: TextView = itemView.findViewById(R.id.tv_word_item)
        val tvPhonetic: TextView = itemView.findViewById(R.id.tv_phonetic_item)
        val root: View = itemView.findViewById(R.id.word_item_root)

        fun bind(word: Word, position: Int, isSelected: Boolean) {
            tvWord.text = word.word
            tvPhonetic.text = word.phonetic

            // 更新选中状态
            if (isSelected) {
                root.setBackgroundColor(0xFFD99A9A.toInt()) // @color/primary
                tvWord.setTextColor(0xFFFFFFFF.toInt())
                tvPhonetic.setTextColor(0xFFFFFFFF.toInt())
            } else {
                root.setBackgroundColor(0x00000000) // 透明
                tvWord.setTextColor(0xFF333333.toInt()) // @color/text_primary
                tvPhonetic.setTextColor(0xFF888888.toInt()) // @color/text_secondary
            }

            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onWordClick(word, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word_list, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(words[position], position, position == selectedPosition)
    }

    override fun getItemCount(): Int = words.size

    fun updateWords(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }
}
