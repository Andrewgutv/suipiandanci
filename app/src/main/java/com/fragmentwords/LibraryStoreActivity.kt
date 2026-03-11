package com.fragmentwords

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import com.fragmentwords.service.LibraryDownloadService
import kotlinx.coroutines.launch

/**
 * 词库商城 Activity - 下载和管理词库
 */
class LibraryStoreActivity : AppCompatActivity() {

    private lateinit var repository: WordRepository
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library_store)

        repository = WordRepository(this)
        initViews()
        checkLibraryStatus()
    }

    private fun initViews() {
        // 返回按钮
        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // CET4 下载按钮
        findViewById<Button>(R.id.btn_download_cet4).setOnClickListener {
            downloadLibrary("CET4")
        }

        // CET6 下载按钮
        findViewById<Button>(R.id.btn_download_cet6).setOnClickListener {
            downloadLibrary("CET6")
        }

        // IELTS 下载按钮
        findViewById<Button>(R.id.btn_download_ielts).setOnClickListener {
            downloadLibrary("IELTS")
        }
    }

    private fun checkLibraryStatus() {
        lifecycleScope.launch {
            // 检查词库状态
            val totalWords = repository.getWordCount()

            // 简单检查：如果有单词就认为已下载
            val statusCet4 = findViewById<TextView>(R.id.status_cet4)
            val statusCet6 = findViewById<TextView>(R.id.status_cet6)
            val statusIelts = findViewById<TextView>(R.id.status_ielts)

            if (totalWords > 0) {
                statusCet4.text = "已下载"
                statusCet6.text = "部分下载"
                statusIelts.text = "未下载"
            } else {
                statusCet4.text = "未下载"
                statusCet6.text = "未下载"
                statusIelts.text = "未下载"
            }
        }
    }

    private fun downloadLibrary(libraryId: String) {
        // 显示进度对话框
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("正在下载词库...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
        progressDialog.show()

        // 启动下载服务
        val intent = Intent(this, LibraryDownloadService::class.java)
        intent.putExtra("library_id", libraryId)
        startService(intent)

        // 模拟下载完成（实际应该通过BroadcastReceiver或Service回调来更新）
        lifecycleScope.launch {
            // 等待一段时间模拟下载
            kotlinx.coroutines.delay(2000)

            progressDialog.dismiss()

            // 刷新状态
            checkLibraryStatus()

            Toast.makeText(this@LibraryStoreActivity, "词库下载完成", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkLibraryStatus()
    }
}
