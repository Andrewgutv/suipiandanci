# 锁屏背单词 - Fragment Words

> 一个基于**艾宾浩斯遗忘曲线**的英语单词学习App，通过锁屏通知卡片帮助用户利用碎片时间高效记忆单词。

**目标**：参加省级创新创业大赛，冲击金奖 🏆

---

## 📱 项目简介

锁屏背单词采用**前后端分离架构**：

- **Android客户端**：原生Kotlin开发，提供锁屏单词卡片、生词本等功能
- **Spring Boot后端**：提供词库管理、学习进度同步、数据统计等API服务
- **核心算法**：艾宾浩斯遗忘曲线（8个复习节点），提升3倍记忆效率

---

## 🎯 核心功能
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
D:\workspace\app\
├── app/                            # Android客户端模块
│   ├── src/main/java/com/fragmentwords/
│   │   ├── MainActivity.kt         # 主界面
│   │   ├── manager/
│   │   │   ├── EbbinghausManager.kt # 艾宾浩斯算法核心 ⭐
│   │   │   └── LearningManager.kt   # 学习管理器
│   │   ├── database/
│   │   │   └── WordDatabase.kt     # SQLite数据库
│   │   ├── service/
│   │   │   └── WordService.kt      # 前台服务
│   │   └── receiver/               # 广播接收器
│   └── src/main/res/
│       └── layout/                 # UI布局
│
├── backend/                        # Spring Boot后端模块 ⭐
│   ├── src/main/java/com/fragmentwords/
│   │   ├── controller/             # REST API控制器
│   │   ├── service/                # 业务逻辑层
│   │   ├── model/                  # 实体类和DTO
│   │   └── mapper/                 # MyBatis Mapper
│   ├── src/main/resources/
│   │   ├── application.yml         # 配置文件
│   │   └── sql/init.sql            # 数据库初始化脚本
│   ├── start.bat                   # 启动脚本
│   └── README.md                   # 后端文档
│
└── fragment-words/                 # 旧的uni-app项目（已废弃）
```

## 技术栈

### Android客户端
- **语言**: Kotlin 1.9.20
- **SDK**: API 26-34 (Android 8.0-14)
- **架构**: MVVM + Repository Pattern
- **本地数据库**: SQLite
- **核心依赖**: WorkManager, Coroutines, Material Design 3

### 后端服务
- **语言**: Java 17
- **框架**: Spring Boot 3.3.4
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis-Plus 3.5.6
- **API文档**: SpringDoc 2.4.0

### 核心算法
- **艾宾浩斯遗忘曲线**: 8个复习节点
  - 5分钟 → 30分钟 → 12小时 → 1天 → 2天 → 4天 → 7天 → 15天

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

## 🚀 快速开始

### Android客户端

详见下方 [如何编译运行](#如何编译运行)

### 后端服务

1. **启动MySQL数据库**
   ```bash
   net start MySQL80
   ```

2. **初始化数据库**
   ```bash
   mysql -uroot -p123456
   source D:/workspace/app/backend/src/main/resources/sql/init.sql
   ```

3. **启动后端服务**
   ```bash
   cd D:\workspace\app\backend
   start.bat
   ```

4. **访问服务**
   - 服务地址: http://localhost:8080
   - API文档: http://localhost:8080/swagger-ui/index.html

详细文档：[backend/README.md](backend/README.md)

---

## 📊 项目进度

### ✅ 已完成功能
- [x] 锁屏通知显示单词卡片
- [x] 三大按钮交互（认识/不认识/下一个）
- [x] 生词本管理
- [x] 多词库选择（CET4/6/IELTS/TOEFL/GRE）
- [x] 前台服务（确保常驻）
- [x] WorkManager定时任务
- [x] 开机自启动
- [x] **艾宾浩斯遗忘曲线算法**（Android端）⭐
- [x] **Spring Boot后端服务** ⭐
- [x] MySQL数据库设计

### 🚧 待开发功能
- [ ] 后端集成艾宾浩斯算法
- [ ] Android客户端对接后端API
- [ ] 扩展词库到5000+单词
- [ ] 数据统计模块
- [ ] 语音朗读（TTS）
- [ ] 用户注册/登录系统

---

## 📖 相关文档

- [SESSION_LOG.md](SESSION_LOG.md) - 开发会话日志
- [backend/README.md](backend/README.md) - 后端服务文档
- [词库扩展指南](词库扩展指南.md) - 如何添加新词库

---

## License

MIT

## Author

Created with Android Studio - 突破 uni-app 限制的原生实现
