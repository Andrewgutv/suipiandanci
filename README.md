# Fragment Words

一个以通知栏和锁屏碎片时间为入口的英语单词学习 Android 应用。

当前仓库里真正可运行、应优先关注的主线是原生 Android 应用 `app/`。后端 `backend/` 仍然是半集成状态，旧的 `fragment-words/` 已不是主运行路径。

## 当前状态

当前 Android 主线已经达到 `beta-usable` 状态，适合继续开发和做真机短验。

已经确认可用的本地闭环包括：

- 本地词库加载
- 通知卡片展示单词
- `认识 / 不认识` 两个反馈动作
- 本地艾宾浩斯式学习进度记录
- 生词本写入与读取
- 生词本页面展示
- 首页显示当前词库和生词本数量

当前仍有一个已知尾问题：

- Android 14/15 模拟器环境中，前台服务启动仍可能出现前台服务类型 warning

这个 warning 目前不阻断本地主线功能，但还不适合直接视为正式完成版。

## 核心功能

### 1. 通知单词卡片

应用会将单词以通知卡片形式展示在通知栏，适合用零碎时间快速学习。

### 2. 认识 / 不认识反馈

每张单词卡片包含两个关键动作：

- `认识`：记录当前单词已认识，并进入后续复习节奏
- `不认识`：记录学习反馈，同时加入生词本

### 3. 生词本

被标记为 `不认识` 的单词会写入本地 SQLite 生词本，并可在应用内查看。

### 4. 本地学习进度

应用使用本地 SQLite 记录学习阶段、复习时间和基础学习统计，当前主线不依赖后端即可运行。

## 仓库结构

```text
D:\workspace\app
├── app/                  # 当前 Android 主线
├── backend/              # Spring Boot 后端（部分集成）
├── fragment-words/       # 旧 uni-app 原型
├── CURRENT_STATUS.md     # 当前项目状态摘要
├── SESSION_LOG.md        # 会话与开发记录
└── ANDROID_VALIDATION_CHECKLIST.md  # 真机最小验收清单
```

## 关键代码路径

Android 主线重点看这些文件：

- `app/src/main/java/com/fragmentwords/service/WordService.kt`
- `app/src/main/java/com/fragmentwords/receiver/WordActionReceiver.kt`
- `app/src/main/java/com/fragmentwords/receiver/ScreenUnlockReceiver.kt`
- `app/src/main/java/com/fragmentwords/HomeFragment.kt`
- `app/src/main/java/com/fragmentwords/NotebookFragment.kt`
- `app/src/main/java/com/fragmentwords/manager/LearningManager.kt`
- `app/src/main/java/com/fragmentwords/data/WordRepository.kt`
- `app/src/main/java/com/fragmentwords/database/WordDatabase.kt`

## 本地运行

### Android 主线

前置条件：

- Android Studio
- Android SDK 34+
- JDK 17 或 Android Studio 自带运行时
- 正确配置的 `local.properties`

步骤：

1. 用 Android Studio 打开 `D:\workspace\app`
2. 确认 `local.properties` 中 `sdk.dir` 指向本机 Android SDK
3. 等待 Gradle Sync 完成
4. 运行 `app` 模块到模拟器或真机
5. 首次启动时允许通知权限
6. 在首页开启单词推送并验证通知与生词本链路

### 命令行构建

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:installDebug
```

## 推荐验收路径

如果你只想快速判断当前版本是否可用，建议按这个顺序测试：

1. 首页开启推送
2. 等待单词通知出现
3. 点击 `不认识`
4. 切到生词本页确认该词可见
5. 点击 `认识`
6. 锁屏再解锁，确认只刷新一次
7. 关闭推送，确认不再出现新通知

完整检查清单见：

- [ANDROID_VALIDATION_CHECKLIST.md](./ANDROID_VALIDATION_CHECKLIST.md)
- [CURRENT_STATUS.md](./CURRENT_STATUS.md)

## 后端说明

`backend/` 目前仍然不是 Android 主线稳定依赖。

你仍然可以单独运行它做开发或接口检查，但当前 Android 可用闭环仍以本地 SQLite + 本地学习逻辑为主。

## 版本判断

如果现在有人问这个项目能不能继续用，比较准确的回答是：

- 可以作为 Android 本地主线 beta 继续开发和演示
- 本地通知 + 生词本学习闭环已经能跑通
- 后端整合、真机兼容性、前台服务尾问题仍未完全收口
