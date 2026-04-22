# Fragment Words

一个以通知栏和锁屏碎片时间为入口的英语单词学习 Android 应用。

当前仓库里真正可运行、应优先关注的主线是原生 Android 应用 `app/` 加 Spring Boot 后端 `backend/`。旧的 `fragment-words/` 已不是主运行路径。

## 当前状态

当前 Android 主线已经达到 `beta-usable` 状态，后端主线也已经进入可联调状态。

已经确认可用的本地闭环包括：

- 本地词库加载
- 通知卡片展示单词
- `认识 / 不认识` 两个反馈动作
- 本地艾宾浩斯式学习进度记录
- 生词本写入与读取
- 生词本页面展示
- 首页显示当前词库和生词本数量

已经确认可用的前后端联调链路包括：

- Android 优先从后端获取下一词
- Android 同步当前词库到后端
- Android 将 `认识 / 不认识` 反馈同步到后端
- Android 优先从后端读取生词本数量和列表

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

### Android + Backend 联调

如果你要跑当前真实联调链路，直接看：

- [LOCAL_RUNBOOK.md](./LOCAL_RUNBOOK.md)

后端本地启动脚本：

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "your_real_password"
.\start-local.ps1
```

兼容旧入口：

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "your_real_password"
.\start-local.bat
```

启动脚本现在会在以下场景直接失败并给出明确提示：

- `DB_PASSWORD` 未设置
- `java` 不在 `PATH` 中
- `backend\mvnw.cmd` 缺失
- 无法连到 `DB_HOST:DB_PORT` 指向的 MySQL 端口
- `APP_PORT` 指向的本地应用端口已被占用

如果你只想跳过数据库 TCP 预检查，可显式设置：

```powershell
$env:SKIP_DB_PREFLIGHT = "1"
.\start-local.ps1
```

如果你本机的 `8080` 已被占用，可显式改后端端口：

```powershell
$env:APP_PORT = "8081"
.\start-local.ps1
```

如果你在 `cmd.exe` 里启动，用：

```cmd
set DB_PASSWORD=your_real_password && start-local.bat
```

Android debug 安装并启动脚本：

```powershell
cd D:\workspace\app
.\install-debug-and-launch.bat
```

本地联调 smoke 脚本：

```powershell
cd D:\workspace\app
.\run-local-smoke.bat
```

如果后端不跑在 `8080`，先设置：

```powershell
$env:APP_PORT = "8081"
.\run-local-smoke.bat
```

通知 `unknown` 动作 smoke 脚本：

```powershell
cd D:\workspace\app
.\run-local-unknown-smoke.cmd
```

这个脚本同样会读取 `APP_PORT`。
如果同时设置了 `DB_PASSWORD`，它也会像基础 smoke 一样尝试自动拉起后端。

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

`backend/` 现在已经可以作为 Android 联调依赖使用。

当前客户端仍保留本地 SQLite fallback，但下一词、反馈、生词本读取已经支持后端优先。

## 版本判断

如果现在有人问这个项目能不能继续用，比较准确的回答是：

- 可以作为 Android + backend 联调主线继续开发和演示
- 本地闭环和一条真实前后端学习链路都已经能跑通
- 真机兼容性、正式环境配置、前台服务尾问题仍未完全收口
