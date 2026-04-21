# 本地运行与验收手册

这份手册只写当前有效的本地流程，不保留旧的实验性说明。

## 1. 当前主线

当前有效主线：

- Android 客户端：`app/`
- Spring Boot 后端：`backend/`

旧的 `fragment-words/` 不是当前产品主线。

当前已经打通的核心链路：

- Android 优先从后端获取下一词
- Android 同步当前词库到后端
- Android 同步 `known / unknown` 反馈到后端
- Android 优先读取后端生词本数量和列表
- 本地 SQLite fallback 仍保留

## 2. 前置条件

开始前请确认：

- Android SDK 已安装
- `local.properties` 里的 `sdk.dir` 正确
- JDK 17 可用
- MySQL 正在监听 `127.0.0.1:3307`
- 数据库 `fragment_words` 已初始化

初始化 SQL：

```text
backend/src/main/resources/sql/init.sql
```

## 3. 数据库密码怎么传

不要把真实数据库密码写进源码。

推荐通过环境变量传入。

### PowerShell

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "你的真实数据库密码"
.\start-local.ps1
```

如果还要显式指定其他数据库参数：

```powershell
$env:DB_HOST = "127.0.0.1"
$env:DB_PORT = "3307"
$env:DB_NAME = "fragment_words"
$env:DB_USER = "root"
$env:DB_PASSWORD = "你的真实数据库密码"
.\start-local.ps1
```

### cmd.exe

```cmd
cd /d D:\workspace\app\backend
set DB_PASSWORD=你的真实数据库密码 && start-local.bat
```

### 不要这样做

- 不要在 PowerShell 里用 `set DB_PASSWORD=...`
- 不要手工拼长串 `mvnw.cmd -Dspring.datasource.url=...`
- 不要把真实密码提交进 Git

## 4. 后端启动

### 推荐入口

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "你的真实数据库密码"
.\start-local.ps1
```

### 兼容入口

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "你的真实数据库密码"
.\start-local.bat
```

### 启动成功标志

控制台应出现：

```text
Tomcat started on port 8080
Started FragmentWordsApplication
```

### 后端快速确认

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/vocabs
```

返回 JSON 且 `code = 200`，说明后端主链路已起来。

## 5. Android 调试包

### 构建 debug APK

```powershell
cd D:\workspace\app
.\gradlew.bat :app:assembleDebug
```

输出路径：

```text
app\build\outputs\apk\debug\app-debug.apk
```

### 一键安装并启动

```powershell
cd D:\workspace\app
.\install-debug-and-launch.bat
```

这个脚本会：

- 检查在线 `adb` 设备
- 安装 debug APK
- 授予通知权限
- 启动 `MainActivity`

如果要强制先重建：

```powershell
cd D:\workspace\app
$env:FORCE_BUILD = "1"
.\install-debug-and-launch.bat
```

## 6. 本地联调脚本

### 基础 smoke

```powershell
cd D:\workspace\app
.\run-local-smoke.bat
```

功能：

1. 检查后端 `8080`
2. 安装并启动 app
3. 读取 app 的 `device_id`
4. 查询后端当前词库 / 生词本数量 / 学习统计

### 通知 `unknown` 动作 smoke

```powershell
cd D:\workspace\app
.\run-local-unknown-smoke.cmd
```

功能：

1. 安装并启动 app
2. 展开通知栏
3. 自动点击通知里的 `unknown`
4. 回查后端 notebook / stats 变化

注意：

- 如果当前词本来就已经在生词本里，`notebook count` 可能不增长
- 这不一定是失败，可能只是命中了去重逻辑

## 7. API 地址配置

Android 现在通过 `BuildConfig` 读取 API 地址。

### debug

优先级：

1. `local.properties` 中的 `debugApiBaseUrl`
2. 默认回退到 `http://10.0.2.2:8080/`

示例：

```properties
debugApiBaseUrl=http://10.0.2.2:8080/
```

### release

优先级：

1. `gradle.properties` 中的 `releaseApiBaseUrl`
2. 环境变量 `RELEASE_API_BASE_URL`
3. 占位默认值

release 地址不要写进 `local.properties`。

## 8. 最小验收步骤

### 步骤 A：启动后端

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "你的真实数据库密码"
.\start-local.ps1
```

### 步骤 B：安装并启动 app

```powershell
cd D:\workspace\app
.\install-debug-and-launch.bat
```

### 步骤 C：在 app 中验收

1. 打开 app，确认首页正常显示，不闪退。
2. 进入“设置”，确认当前词库是 `CET4`。
3. 回首页，确认“单词推送”可开启。
4. 下拉通知栏，确认出现单词通知卡片。
5. 确认通知中有：
   - 单词
   - 音标
   - 中文释义
   - `认识`
   - `不认识`
6. 点一次 `不认识`。
7. 回 app 的“生词本”页，确认该词出现，或数量有合理变化。
8. 再点一次新的通知动作，确认 app 不崩溃、不死循环刷新。
9. 回首页，确认：
   - 当前词库显示正常
   - 生词本数量显示正常
   - 推送状态正常
10. 关闭“单词推送”，确认不会继续生成新通知。

### 步骤 D：可选后端核对

```powershell
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/v1/notebook/count' -Headers @{'X-Device-Id'='你的设备ID'}
```

也可以直接跑：

```powershell
.\run-local-smoke.bat
```

## 9. 当前已验证接口

已验证通过：

- `GET /api/v1/vocabs`
- `GET /api/v1/vocabs/current`
- `PUT /api/v1/vocabs/current`
- `GET /api/v1/notebook/count`
- `GET /api/v1/notebook`
- `POST /api/v1/notebook`
- `POST /api/v1/learning/next`
- `POST /api/v1/learning/feedback`
- `GET /api/v1/learning/stats`
- `GET /api/v1/auth/info/{userId}` 未授权返回 JSON `401`

## 10. 当前仍需注意

- Debug 构建允许明文 HTTP 访问 `10.0.2.2`
- Main manifest 已移除开发态明文配置
- Release 地址必须外部注入
- 后端数据库凭据必须从环境变量传入
- 真机短验还没有完整做完

## 11. 推荐入口

后端：

```powershell
cd D:\workspace\app\backend
$env:DB_PASSWORD = "你的真实数据库密码"
.\start-local.ps1
```

Android：

```powershell
cd D:\workspace\app
.\install-debug-and-launch.bat
```
