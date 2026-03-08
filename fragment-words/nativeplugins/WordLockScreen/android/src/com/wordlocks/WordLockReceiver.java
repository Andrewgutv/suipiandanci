package com.wordlocks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 处理锁屏通知按钮点击的广播接收器
 */
public class WordLockReceiver extends BroadcastReceiver {

    private static final String TAG = "WordLockReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        String word = intent.getStringExtra("word");

        switch (action) {
            case "com.wordlocks.ACTION_KNOWN":
                Log.d(TAG, "用户标记单词为认识: " + word);
                Toast.makeText(context, "已标记为认识: " + word, Toast.LENGTH_SHORT).show();
                // TODO: 发送事件到 Uni-app，更新统计数据
                break;

            case "com.wordlocks.ACTION_UNKNOWN":
                Log.d(TAG, "用户标记单词为不认识: " + word);
                Toast.makeText(context, "已加入生词本: " + word, Toast.LENGTH_SHORT).show();
                // TODO: 发送事件到 Uni-app，将单词加入生词本
                break;
        }
    }
}
