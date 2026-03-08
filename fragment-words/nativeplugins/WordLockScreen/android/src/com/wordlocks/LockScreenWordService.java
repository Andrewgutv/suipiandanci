package com.wordlocks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

/**
 * 锁屏单词卡片服务
 * 在 Android 锁屏上显示单词卡片，类似音乐播放器的锁屏控制
 */
public class LockScreenWordService extends Service {

    private static final String CHANNEL_ID = "word_lock_screen_channel";
    private static final int NOTIFICATION_ID = 1001;

    private MediaSessionCompat mediaSession;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        initMediaSession();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("word")) {
            String word = intent.getStringExtra("word");
            String phonetic = intent.getStringExtra("phonetic");
            String translation = intent.getStringExtra("translation");
            String example = intent.getStringExtra("example");

            showLockScreenWord(word, phonetic, translation, example);
        }
        return START_STICKY;
    }

    /**
     * 创建通知渠道（Android 8.0+ 需要）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "单词锁屏",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("在锁屏上显示单词卡片");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 初始化 MediaSession
     */
    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "WordLockScreen");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                             MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
    }

    /**
     * 在锁屏上显示单词卡片
     */
    private void showLockScreenWord(String word, String phonetic, String translation, String example) {
        // 创建点击通知打开应用的 Intent
        Intent notificationIntent = new Intent(this, getMainActivityClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 创建"认识"按钮的 Intent
        Intent knownIntent = new Intent("com.wordlocks.ACTION_KNOWN");
        knownIntent.putExtra("word", word);
        PendingIntent knownPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                knownIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 创建"不认识"按钮的 Intent
        Intent unknownIntent = new Intent("com.wordlocks.ACTION_UNKNOWN");
        unknownIntent.putExtra("word", word);
        PendingIntent unknownPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                unknownIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 使用 MediaStyle 在锁屏显示
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(word)
                .setContentText(phonetic + "\n" + translation)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(word + "\n" + phonetic + "\n" + translation + "\n\n例句：\n" + example))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_add, "认识", knownPendingIntent)
                .addAction(android.R.drawable.ic_menu_delete, "不认识", unknownPendingIntent)
                .setStyle(mediaStyle);

        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);

        // 启动前台服务
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 清除锁屏单词
     */
    private void clearLockScreenWord() {
        notificationManager.cancel(NOTIFICATION_ID);
        if (mediaSession != null) {
            mediaSession.release();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearLockScreenWord();
    }

    /**
     * 获取主 Activity 类名（需要在子类中实现）
     */
    private Class<?> getMainActivityClass() {
        try {
            String packageName = getPackageName();
            return Class.forName(packageName + ".io.dcloud.PandoraEntry");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
