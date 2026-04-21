# Android通知系统深度解析 - 微信实现方式

## 📱 微信通知的完整实现流程

### 1. 基础架构

```
服务器推送 → FCM/自建推送 → BroadcastReceiver → Service → NotificationManager → 系统UI
```

### 2. 关键技术点

#### 2.1 NotificationChannel（通知渠道）- Android 8.0+
```kotlin
// 微信创建的通知渠道
val channel = NotificationChannel(
    "wechat_message",  // Channel ID
    "消息通知",         // 用户可见名称
    NotificationManager.IMPORTANCE_HIGH  // 重要性级别
).apply {
    description = "新消息提醒"
    enableLights(true)
    enableVibration(true)
    lockscreenVisibility = Notification.VISIBILITY_PUBLIC  // 锁屏可见
    setShowBadge(true)  // 角标
}
```

**重要性级别对照：**
- `IMPORTANCE_HIGH` - 横幅通知 + 声音 + 震动
- `IMPORTANCE_DEFAULT` - 状态栏 + 下拉可见
- `IMPORTANCE_LOW` - 无声音，下拉可见
- `IMPORTANCE_MIN` - 无声音，仅下拉托盘可见
- `IMPORTANCE_NONE` - 不显示（用户需手动开启）

#### 2.2 MessagingStyle（消息样式）- 关键！

```kotlin
// 微信的核心实现
val person = Person.Builder()
    .setName("张三")
    .setIcon(icon)
    .build()

val style = NotificationCompat.MessagingStyle(this)
    .setUserDisplayName("我")
    .setConversationTitle("张三")
    .addMessage(messages)  // List<NotificationCompat.MessagingStyle.Message>
```

#### 2.3 通知分组（Group）

```kotlin
// 所有同一对话的通知使用同一个groupKey
val GROUP_KEY = "com.tencent.mm.chat.123456"

val builder = NotificationCompat.Builder(context, CHANNEL_ID)
    .setGroup(GROUP_KEY)
    .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)  // 只播放一次声音

// 只有最新的通知显示，旧的通知自动合并
```

#### 2.4 PendingIntent - 点击跳转

```kotlin
// 点击通知打开聊天界面
val intent = Intent(context, ChatActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    putExtra("chat_id", "123456")
}

val pendingIntent = PendingIntent.getActivity(
    context,
    0,
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

builder.setContentIntent(pendingIntent)
```

#### 2.5 远程视图（RemoteViews）- 自定义布局

微信的横幅通知使用了自定义RemoteViews：

```xml
<!-- 微信的自定义通知布局 -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="64dp"
    android:orientation="horizontal"
    android:gravity="center_vertical">

    <ImageView
        android:id="@+id/avatar"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:src="@drawable/default_avatar" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">

        <TextView
            android:id="@+id/title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="张三" />

        <TextView
            android:id="@+id/content"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="你好在吗？"
            android:ellipsize="end"
            android:singleLine="true" />
    </LinearLayout>
</LinearLayout>
```

```kotlin
val remoteViews = RemoteViews(packageName, R.layout.custom_notification)
remoteViews.setTextViewText(R.id.title, "张三")
remoteViews.setTextViewText(R.id.content, "你好在吗？")

builder.setCustomContentView(remoteViews)
```

---

## 🎯 锁屏背单词项目的优化方案

### 当前实现分析

你当前使用的 **MediaStyle**（媒体样式），这是音乐播放器专用样式。虽然能显示按钮，但不太适合学习场景。

### 推荐方案：混合样式

```kotlin
/**
 * 创建增强的学习通知
 * 结合 MessagingStyle + 自定义按钮
 */
private fun createEnhancedLearningNotification(word: Word): Notification {
    // 1. 使用 MessagingStyle 显示学习进度
    val messagingStyle = NotificationCompat.MessagingStyle(this)
        .setUserDisplayName("我")
        .setConversationTitle("今日学习")
        .addMessage(
            NotificationCompat.MessagingStyle.Message(
                "新单词：${word.word}\n${word.phonetic}\n${word.translation}",
                System.currentTimeMillis(),
                "学习助手"
            )
        )

    // 2. 创建学习记录样式（复习历史）
    val reviewHistory = NotificationCompat.InboxStyle()
        .addLine("上次复习：3分钟前")
        .addLine("今日已学：15个单词")
        .addLine("待复习：5个单词")
        .setBigContentTitle("学习进度")

    // 3. 构建通知
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_word)
        .setContentTitle(word.word)
        .setContentText("${word.phonetic}\n${word.translation}")
        .setStyle(messagingStyle)  // 使用消息样式
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)  // 消息类别
        .setOngoing(true)
        .setAutoCancel(false)

    // 4. 添加操作按钮
    builder.addAction(
        R.drawable.ic_unknown,
        "不认识",
        createPendingIntent(ACTION_UNKNOWN, word)
    )
    builder.addAction(
        R.drawable.ic_known,
        "认识",
        createPendingIntent(ACTION_KNOWN, word)
    )

    return builder.build()
}

private fun createPendingIntent(action: String, word: Word): PendingIntent {
    val intent = Intent(this, WordActionReceiver::class.java).apply {
        this.action = action
        putExtra(EXTRA_WORD, word.word)
    }
    return PendingIntent.getBroadcast(
        this,
        word.word.hashCode(),  // 使用word作为requestCode确保唯一
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
```

### 高级特性：通知分组和摘要

```kotlin
// 将所有学习通知分组
const val GROUP_KEY_LEARNING = "com.fragmentwords.learning.group"
const val SUMMARY_ID = 0

// 创建单个学习通知
fun showWordNotification(word: Word) {
    val notification = createEnhancedLearningNotification(word)

    notificationManager.notify(word.word.hashCode(), notification)
}

// 创建学习摘要通知（汇总）
fun showLearningSummary() {
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setGroup(GROUP_KEY_LEARNING)
        .setGroupSummary(true)
        .setContentTitle("今日学习")
        .setContentText("已学习 50 个单词，待复习 5 个")
        .setSmallIcon(R.drawable.ic_word)
        .setStyle(
            NotificationCompat.InboxStyle()
                .addLine("CET4: 20词")
                .addLine("CET6: 15词")
                .addLine("IELTS: 15词")
                .setSummaryText("总计 50 词")
        )

    notificationManager.notify(SUMMARY_ID, builder.build())
}
```

---

## 🔧 锁屏显示的关键配置

### AndroidManifest.xml 配置

```xml
<activity
    android:name=".LockScreenActivity"
    android:showOnLockScreen="true"
    android:turnScreenOn="true">
</activity>
```

### 通知可见性控制

```kotlin
// 完全公开 - 锁屏显示所有内容
.setVisibility(Notification.VISIBILITY_PUBLIC)

// 私密 - 锁屏隐藏内容，只显示"新消息"
.setVisibility(Notification.VISIBILITY_PRIVATE)

// 秘密 - 完全不在锁屏显示
.setVisibility(Notification.VISIBILITY_SECRET)
```

### 锁屏权限检查

```kotlin
// 检查锁屏是否启用
fun isLockScreenEnabled(): Boolean {
    val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return km.isDeviceLocked || km.isKeyguardLocked
}
```

---

## 📊 完整对比表

| 特性 | 微信 | 你的项目（当前） | 优化建议 |
|------|------|-----------------|---------|
| **通知样式** | MessagingStyle | MediaStyle | 改用MessagingStyle |
| **分组** | ✅ Group | ❌ 无 | 添加GROUP_KEY |
| **摘要** | ✅ Summary | ❌ 无 | 显示学习统计 |
| **历史** | ✅ 多行消息 | ❌ 单行 | InboxStyle显示历史 |
| **横幅** | ✅ HeadsUp | ✅ MediaStyle | 保持即可 |
| **锁屏** | ✅ PUBLIC | ✅ PUBLIC | ✅ 已实现 |
| **按钮** | 2-3个 | 2个 | ✅ 已实现 |
| **跳转** | 聊天界面 | ❌ 无 | 可添加跳转功能 |

---

## 🎬 实现步骤建议

### 第一步：优化通知样式
- 从 MediaStyle 改为 MessagingStyle
- 添加 InboxStyle 显示学习进度

### 第二步：添加通知分组
- 所有学习通知使用同一个 GROUP_KEY
- 创建汇总通知显示今日统计

### 第三步：增强交互
- 点击通知跳转到学习详情页
- 长按显示单词解释卡片

### 第四步：智能提醒
- 根据艾宾浩斯算法显示"该复习了"
- 学习统计和成就提醒

---

## 💻 完整示例代码

见附件：`EnhancedNotificationService.kt`

## 📚 参考资料

- [Android Notification官方文档](https://developer.android.com/guide/topics/ui/notifications)
- [MessagingStyle最佳实践](https://developer.android.com/training/notify-user/build-notification#style)
- [锁屏通知指南](https://developer.android.com/guide/topics/ui/notifiers/notifications#lockscreen-notification)
