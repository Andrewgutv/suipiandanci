# 碎片单词 - Fragment Words

一个用原生 Android 开发的英语单词学习 App，通过锁屏通知卡片帮助用户碎片化记忆单词。

## 功能特点

### 核心功能
- **锁屏单词卡片** - 在通知栏和锁屏显示单词卡片，类似音乐播放器样式
- **三大交互按钮**
  - **认识** - 直接跳过该单词
  - **不认识** - 自动加入生词本
  - **下一个** - 手动切换新单词
- **生词本** - 保存不认识的单词，支持复习和删除
- **自动刷新** - 每小时自动刷新新单词
- **开机自启** - 手机重启后自动启动单词服务

### 相比 uni-app 的突破

| 功能 | uni-app | Android Studio |
|------|---------|----------------|
| 锁屏通知 | 不稳定，各厂商不一致 | 完全控制，使用原生 Notification API |
| 交互按钮 | 无法自定义 | 支持多个 PendingIntent 按钮 |
| 前台服务 | 限制多 | 原生 ForegroundService，保活能力强 |
| 权限管理 | 不灵活 | 精确控制各项权限 |
| 性能 | 受框架限制 | 原生性能最优 |

## 快速体验

### 方式一：直接安装（推荐）

**最简单的方式，只需一步：**

📱 **下载地址：**
- [GitHub Releases](https://github.com/Andrewgutv/suipiandanci/releases) - 推荐从这里下载最新版本
- 或直接下载：`app/build/outputs/apk/debug/app-debug.apk`

下载后直接安装到手机即可体验！

> 💡 安装后请授予通知权限，即可在锁屏看到单词卡片

### 方式二：源码编译

详见下方 [如何编译运行](#如何编译运行)

---

## 项目结构

```
app/
├── src/main/java/com/fragmentwords/
│   ├── MainActivity.kt              # 主界面
│   ├── model/
│   │   └── Word.kt                 # 单词数据模型
│   ├── database/
│   │   └── WordDatabase.kt         # SQLite 数据库
│   ├── data/
│   │   └── WordRepository.kt       # 数据仓库
│   ├── service/
│   │   └── WordService.kt          # 前台服务（核心）
│   ├── receiver/
│   │   ├── WordActionReceiver.kt   # 按钮点击接收器
│   │   └── BootReceiver.kt         # 开机启动接收器
│   ├── worker/
│   │   └── WordRefreshWorker.kt    # 定期刷新 Worker
│   └── utils/
│       └── WorkManagerScheduler.kt # WorkManager 调度器
└── src/main/res/
    ├── layout/
    │   └── activity_main.xml       # 主界面布局
    └── values/
        ├── strings.xml             # 字符串资源
        ├── colors.xml              # 颜色资源
        └── themes.xml              # 主题样式
```

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: Android 8.0 (API 26)
- **目标 SDK**: Android 14 (API 34)
- **架构**: MVVM + Repository Pattern
- **数据库**: SQLite
- **后台任务**: WorkManager
- **通知**: NotificationCompat + MediaStyle

## 如何编译运行

### 前置要求
1. Android Studio (推荐最新版)
2. JDK 8 或以上
3. Android SDK 34

### 步骤

1. **打开项目**
   ```bash
   # 使用 Android Studio 打开 D:\workspace\app 目录
   ```

2. **配置 local.properties**
   - 复制 `local.properties.template` 为 `local.properties`
   - 设置 `sdk.dir` 为你的 Android SDK 路径
   - 例如：`sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`

3. **同步 Gradle**
   - Android Studio 会自动提示 "Gradle sync needed"
   - 点击 "Sync Now"

4. **运行 App**
   - 连接 Android 设备或启动模拟器
   - 点击 Run 按钮 (或按 Shift + F10)

## 权限说明

| 权限 | 用途 |
|------|------|
| POST_NOTIFICATIONS | 显示通知（Android 13+） |
| FOREGROUND_SERVICE | 前台服务 |
| WAKE_LOCK | 唤醒屏幕 |
| RECEIVE_BOOT_COMPLETED | 开机自启动 |
| SCHEDULE_EXACT_ALARM | 精确闹钟权限 |

## 核心实现原理

### 锁屏通知实现

```kotlin
// WordService.kt
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_word)
    .setContentTitle(word.word)
    .setContentText("${word.phonetic} - ${word.translation}")
    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏可见
    .setPriority(NotificationCompat.PRIORITY_HIGH)        // 高优先级
    .addAction(knownAction)     // "认识" 按钮
    .addAction(unknownAction)   // "不认识" 按钮
    .addAction(nextAction)      // "下一个" 按钮
    .build()
```

### 前台服务保活

```kotlin
// 启动前台服务，保持通知常驻
startForeground(NOTIFICATION_ID, notification)
```

### 定时刷新

```kotlin
// 使用 WorkManager 每小时刷新
val refreshRequest = PeriodicWorkRequestBuilder<WordRefreshWorker>(
    1, TimeUnit.HOUR
).build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    WordRefreshWorker.WORK_NAME,
    ExistingPeriodicWorkPolicy.REPLACE,
    refreshRequest
)
```

## 后续优化方向

1. **添加更多单词** - 目前只内置了 10 个示例单词
2. **单词难度分级** - 根据艾宾浩斯遗忘曲线智能复习
3. **发音功能** - 添加 TTS 朗读单词
4. **生词本导出** - 支持导出为 CSV 或 Anki 格式
5. **统计功能** - 记录学习进度和数据统计
6. **深色模式** - 支持系统深色模式

## License

MIT

## Author

Created with Android Studio - 突破 uni-app 限制的原生实现
