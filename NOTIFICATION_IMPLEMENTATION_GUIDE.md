# Android通知机制详解 - 微信实现方式

## 🎯 核心原理

### 1. **通知系统的三层架构**

```
┌─────────────────────────────────────────────┐
│   应用层 (Application Layer)               │
│   ┌──────────────┐      ┌──────────────┐    │
│   │ 微信/你的App │ ───> │ Notification │    │
│   │              │      │ Manager      │    │
│   └──────────────┘      └──────────────┘    │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│   系统服务层 (System Service Layer)        │
│   ┌──────────────────────────────────────┐  │
│   │ NotificationManagerService           │  │
│   │ - 权限检查                           │  │
│   │ - 通知排序                           │  │
│   │ - Doze模式处理                       │  │
│   └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│   UI显示层 (Presentation Layer)            │
│   ┌─────────┐  ┌─────────┐  ┌─────────┐   │
│   │ 锁屏界面 │  │ 通知栏   │  │ 横幅    │   │
│   └─────────┘  └─────────┘  └─────────┘   │
└─────────────────────────────────────────────┘
```

---

## 🔑 微信通知的5个关键技术

### 1. **MessagingStyle - 消息样式**

这是微信最核心的技术，让通知看起来像聊天对话：

```kotlin
val style = NotificationCompat.MessagingStyle(context)
    .setUserDisplayName("我")              // 当前用户
    .setConversationTitle("张三")           // 对话标题
    .addMessage(                          // 添加消息
        NotificationCompat.MessagingStyle.Message(
            "你好在吗？",                    // 消息文本
            System.currentTimeMillis(),       // 时间戳
            "张三"                          // 发送者
        )
    )
    .addMessage(
        "在的，怎么了？",
        System.currentTimeMillis(),
        "我"
    )
```

**效果**：通知展开后显示多条消息，类似聊天记录

---

### 2. **Notification Group - 通知分组**

微信将同一对话的多条消息合并：

```kotlin
// 所有的 "张三" 对话通知使用同一个 groupKey
val GROUP_KEY_CHAT = "com.tencent.mm.chat.zhangsan"

builder.setGroup(GROUP_KEY_CHAT)
       .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
```

**工作原理**：
```
通知1: "你好" ─┐
通知2: "在吗" ─┼─> 合并为一个通知 ─> "张三: 2条新消息"
通知3: "呵呵" ─┘
```

---

### 3. **Smart Lockscreen Control - 智能锁屏控制**

微信根据消息类型设置不同的锁屏可见性：

```kotlin
when (message.type) {
    MESSAGE_TEXT ->
        // 普通文本：锁屏显示内容
       setVisibility(Notification.VISIBILITY_PUBLIC)

    MESSAGE_MONEY ->
        // 钱财相关：锁屏隐藏内容
        .setVisibility(Notification.VISIBILITY_PRIVATE)
        .setContentTitle("收到新转账")  // 只显示标题

    MESSAGE_SYSTEM ->
        // 系统消息：完全不在锁屏显示
        .setVisibility(Notification.VISIBILITY_SECRET)
}
```

**锁屏显示级别**：
- `PUBLIC` - 完全可见（标题+内容）
- `PRIVATE` - 隐藏内容（仅显示"新消息"）
- `SECRET` - 完全隐藏

---

### 4. **HeadsUp Notification - 横幅通知**

Android 5.0+的浮动横幅：

```kotlin
// 关键配置
val channel = NotificationChannel("channel_id", "name",
    NotificationManager.IMPORTANCE_HIGH  // 必须是HIGH
).apply {
    enableVibration(true)   // 必须启用震动
    enableLights(true)      // 必须启用灯光
}

builder.setPriority(NotificationCompat.PRIORITY_HIGH)  // 必须是HIGH
       .setCategory(NotificationCompat.CATEGORY_MESSAGE)  // 消息类别
```

**触发条件**：
- 用户正在使用手机（屏幕亮起）
- 优先级设置为 HIGH 或 MAX
- 包含声音或震动

---

### 5. **PendingIntent - 点击交互**

通知点击后的跳转逻辑：

```kotlin
// 点击通知打开聊天界面
val intent = Intent(context, ChatActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or  // 新任务栈
             Intent.FLAG_ACTIVITY_CLEAR_TOP   // 清除栈顶
    putExtra("chat_id", "123456")
    putExtra("from_notification", true)
}

val pendingIntent = PendingIntent.getActivity(
    context,
    REQUEST_CODE,              // 请求码（区分不同Intent）
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or  // 如果存在则更新
    PendingIntent.FLAG_IMMUTABLE     // 不可变（Android 12+）
)

builder.setContentIntent(pendingIntent)
```

---

## 💡 你的项目实现对比

### 当前实现（MediaStyle）

```kotlin
// 你现在的实现
val mediaStyle = MediaStyle()
    .setShowActionsInCompactView(0, 1)

builder.setStyle(mediaStyle)
       .setCategory(NotificationCompat.CATEGORY_TRANSPORT)  // 媒体类别
```

**优点**：
- ✅ 可以显示按钮
- ✅ 适配性好

**缺点**：
- ⚠️ MediaStyle 是为音乐播放器设计的
- ⚠️ 不符合"学习"场景
- ⚠️ 无法显示历史记录

---

### 推荐实现（MessagingStyle + 自定义）

```kotlin
// 优化后的实现
val messagingStyle = NotificationCompat.MessagingStyle(context)
    .setUserDisplayName("我")
    .setConversationTitle("今日学习")
    .addMessage(
        NotificationCompat.MessagingStyle.Message(
            "单词：${word.word}",
            System.currentTimeMillis(),
            "学习助手"
        )
    )
    .addMessage(
        NotificationCompat.MessagingStyle.Message(
            "${word.phonetic}",
            System.currentTimeMillis(),
            "学习助手"
        )
    )

builder.setStyle(messagingStyle)
       .setCategory(NotificationCompat.CATEGORY_MESSAGE)  // 改为消息类别
```

**新增功能**：
- ✅ 显示学习进度
- ✅ 支持多条消息历史
- ✅ 符合学习场景

---

## 🔧 完整实现示例

### 步骤1：创建通知渠道

```kotlin
private fun createChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "learning_v2",
            "学习通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "单词学习和复习提醒"
            enableVibration(true)
            enableLights(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
        }

        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}
```

### 步骤2：构建通知

```kotlin
fun showWord(word: Word) {
    // 1. MessagingStyle - 显示学习进度
    val style = NotificationCompat.MessagingStyle(context)
        .setConversationTitle("今日学习")
        .addMessage("新单词：${word.word}", System.currentTimeMillis(), "系统")
        .addMessage(word.phonetic, System.currentTimeMillis(), "系统")
        .addMessage(word.translation, System.currentTimeMillis(), "系统")

    // 2. 创建按钮Intent
    val knownIntent = createIntent("KNOWN", word)
    val unknownIntent = createIntent("UNKNOWN", word)

    // 3. 构建通知
    val builder = NotificationCompat.Builder(context, "learning_v2")
        .setSmallIcon(R.drawable.ic_word)
        .setContentTitle(word.word)
        .setContentText(word.phonetic)
        .setStyle(style)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setGroup("learning_group")  // 分组
        .addAction(R.drawable.unknown, "不认识", unknownIntent)
        .addAction(R.drawable.known, "认识", knownIntent)

    // 4. 显示通知
    val nm = getSystemService(NotificationManager::class.java)
    nm.notify(word.word.hashCode(), builder.build())
}
```

### 步骤3：处理按钮点击

```kotlin
class WordActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val word = intent.getStringExtra("word") ?: return
        val action = intent.action

        when (action) {
            "com.fragmentwords.ACTION_KNOWN" -> {
                // 标记为认识
                LearningManager(context).handleUserFeedback(word, "known")
                // 显示下一个单词
                WordService.showNewWord(context)
            }
            "com.fragmentwords.ACTION_UNKNOWN" -> {
                // 标记为不认识
                LearningManager(context).handleUserFeedback(word, "unknown")
                // 加入生词本
                WordRepository(context).addToNotebook(getWord(word))
            }
        }

        // 取消当前通知
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.cancel(word.hashCode())
    }
}
```

---

## 📊 关键配置对照表

| 配置项 | 微信 | 你的项目 | 优化建议 |
|--------|------|----------|----------|
| **Style** | MessagingStyle | MediaStyle | 改用MessagingStyle |
| **Category** | MESSAGE | TRANSPORT | 改用MESSAGE |
| **Priority** | HIGH | MAX | 保持即可 |
| **Importance** | HIGH | MAX | 改为HIGH |
| **Visibility** | PUBLIC | PUBLIC | ✅ 保持 |
| **Group** | ✅ | ❌ | 添加分组 |
| **Actions** | 2-3个 | 2个 | ✅ 已实现 |
| **HeadsUp** | ✅ | ✅ | ✅ 已实现 |

---

## 🎬 通知显示流程图

```
用户收到消息
      ↓
┌─────────────────────────────┐
│ 1. 创建 NotificationChannel │
│    - ID: "wechat_message"   │
│    - Importance: HIGH       │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│ 2. 构建 Notification        │
│    - 设置 MessagingStyle    │
│    - 添加 Actions           │
│    - 设置 Group             │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│ 3. 通过 notify() 发送       │
│    - notificationManager    │
│      .notify(id, notify)    │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│ 4. 系统处理                 │
│    - 权限检查                │
│    - Doze模式处理            │
│    - 通知排序                │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│ 5. UI显示                   │
│    ├─ 锁屏：PUBLIC          │
│    ├─ 通知栏：展开视图       │
│    └─ 横幅：HeadsUp         │
└─────────────────────────────┘
```

---

## 🚀 高级特性

### 1. **富文本通知**

```kotlin
// 支持表情符号、图片、按钮
val style = NotificationCompat.BigTextStyle()
    .bigText("""
        📚 新单词：${word.word}
        🔊 ${word.phonetic}
        💡 ${word.translation}

        📝 例句：${word.example}
    """.trimIndent())
```

### 2. **进度条通知**

```kotlin
// 下载词库时显示进度
builder.setProgress(100, 50, false)  // 总数100，当前50
```

### 3. **自定义通知布局**

```kotlin
// 完全自定义通知外观
val remoteViews = RemoteViews(packageName, R.layout.custom_notification)
remoteViews.setTextViewText(R.id.tv_word, word.word)
remoteViews.setImageViewBitmap(R.id.iv_image, bitmap)

builder.setCustomContentView(remoteViews)
```

### 4. **直接回复（Android N+）**

```kotlin
// 通知内直接回复
val remoteInput = RemoteInput.Builder("key_text_reply").build()

val replyIntent = Intent(context, ReplyReceiver::class.java)
val replyPendingIntent = PendingIntent.getBroadcast(
    context, 0, replyIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

val action = NotificationCompat.Action.Builder(
    R.drawable.ic_reply,
    "回复",
    replyPendingIntent
).addRemoteInput(remoteInput)
.build()

builder.addAction(action)
```

---

## ⚠️ 常见陷阱

### 1. **忘记创建渠道**

```kotlin
// ❌ 错误：直接发送通知
notificationManager.notify(id, notification)  // Android 8.0+不会显示

// ✅ 正确：先创建渠道
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    notificationManager.createNotificationChannel(channel)
}
notificationManager.notify(id, notification)
```

### 2. **PendingIntent使用不当**

```kotlin
// ❌ 错误：没有设置FLAG_IMMUTABLE
val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

// ✅ 正确：Android 12+必须设置FLAG_IMMUTABLE
val pendingIntent = PendingIntent.getActivity(
    context, 0, intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
```

### 3. **通知ID重复**

```kotlin
// ❌ 错误：所有通知使用相同ID
notificationManager.notify(1, notification1)
notificationManager.notify(1, notification2)  // 覆盖了通知1

// ✅ 正确：使用唯一ID
notificationManager.notify(word1.hashCode(), notification1)
notificationManager.notify(word2.hashCode(), notification2)
```

---

## 📚 参考资料

- [Notification官方文档](https://developer.android.com/guide/topics/ui/notifications)
- [MessagingStyle指南](https://developer.android.com/reference/androidx/core/app/NotificationCompat.MessagingStyle)
- [通知最佳实践](https://android-developers.googleblog.com/2016/06/notifications-best-practices.html)
