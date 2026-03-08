package com.wordlocks;

import android.content.Intent;
import android.util.Log;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * Uni-app 单词锁屏模块
 * 提供 JS 调用原生功能的接口
 */
public class WordLockModule extends UniModule {

    private static final String TAG = "WordLockModule";

    /**
     * 在锁屏上显示单词卡片
     * @param word 单词
     * @param phonetic 音标
     * @param translation 翻译
     * @param example 例句
     */
    @UniJSMethod(uiThread = true)
    public void showWordCard(String word, String phonetic, String translation, String example) {
        Log.d(TAG, "显示锁屏单词: " + word);

        if (mWXSDKInstance != null && mWXSDKInstance.getContext() != null) {
            Context context = mWXSDKInstance.getContext();

            Intent intent = new Intent(context, LockScreenWordService.class);
            intent.putExtra("word", word);
            intent.putExtra("phonetic", phonetic);
            intent.putExtra("translation", translation);
            intent.putExtra("example", example);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    /**
     * 清除锁屏单词卡片
     */
    @UniJSMethod(uiThread = true)
    public void clearWordCard() {
        Log.d(TAG, "清除锁屏单词");

        if (mWXSDKInstance != null && mWXSDKInstance.getContext() != null) {
            Context context = mWXSDKInstance.getContext();
            Intent intent = new Intent(context, LockScreenWordService.class);
            context.stopService(intent);
        }
    }

    /**
     * 检查是否支持锁屏显示
     * @return true/false
     */
    @UniJSMethod(uiThread = false)
    public boolean isLockScreenSupported() {
        return true;
    }
}
