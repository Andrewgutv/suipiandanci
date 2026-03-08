package com.fragmentwords

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fragmentwords.data.WordRepository
import com.fragmentwords.model.WordLibrary
import com.fragmentwords.model.Word
import kotlinx.coroutines.launch

/**
 * 词库选择Activity
 */
class LibrarySelectActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnConfirm: View
    private lateinit var repository: WordRepository
    private val selectedLibraries = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library_select)

        repository = WordRepository(this)
        initViews()
        setupRecyclerView()
        loadSelectedLibraries()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view)
        btnConfirm = findViewById(R.id.btn_confirm)

        btnConfirm.setOnClickListener {
            confirmSelection()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = LibraryAdapter(WordLibrary.ALL_LIBRARIES) { library ->
            onLibraryToggle(library)
        }
    }

    private fun loadSelectedLibraries() {
        lifecycleScope.launch {
            val libraries = repository.getSelectedLibraries()
            selectedLibraries.clear()
            selectedLibraries.addAll(libraries)

            // 更新RecyclerView的选中状态
            (recyclerView.adapter as? LibraryAdapter)?.updateSelections(selectedLibraries)
        }
    }

    private fun onLibraryToggle(library: WordLibrary) {
        if (selectedLibraries.contains(library.id)) {
            selectedLibraries.remove(library.id)
        } else {
            selectedLibraries.add(library.id)
        }
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

    /**
     * RecyclerView Adapter
     */
    private class LibraryAdapter(
        private val libraries: List<WordLibrary>,
        private val onToggle: (WordLibrary) -> Unit
    ) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

        private val selectedLibraries = mutableSetOf<String>()

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_library_select, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val library = libraries[position]
            holder.bind(library, selectedLibraries.contains(library.id))
        }

        override fun getItemCount(): Int = libraries.size

        fun updateSelections(selections: Set<String>) {
            selectedLibraries.clear()
            selectedLibraries.addAll(selections)
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            val tvDescription = itemView.findViewById<TextView>(R.id.tv_description)
            val tvCount = itemView.findViewById<TextView>(R.id.tv_count)
            val checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)

            fun bind(library: WordLibrary, isSelected: Boolean) {
                tvName.text = library.name
                tvDescription.text = library.description
                tvCount.text = "共 ${library.totalWords} 个单词"
                checkbox.isChecked = isSelected

                itemView.setOnClickListener {
                    checkbox.isChecked = !checkbox.isChecked
                    onToggle(library)
                }

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedLibraries.add(library.id)
                    } else {
                        selectedLibraries.remove(library.id)
                    }
                }
            }
        }
    }
}
