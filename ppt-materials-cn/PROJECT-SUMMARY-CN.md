# 项目总结（中文）

## 项目简介

Fragment Words 是一个围绕“通知栏 / 锁屏碎片时间学习单词”构建的英语学习项目，核心功能包括：

- 通知推词
- 生词本记录
- 艾宾浩斯式复习
- 前后端联动同步

当前仓库里实际存在三条轨迹：

- `app/`：当前活跃的原生 Android 客户端主线
- `backend/`：当前活跃的 Spring Boot 后端主线
- `fragment-words/`：旧的 uni-app 原型，已不是产品主线

现在真正的产品方向已经明确为：

- 一个可演示、可继续迭代的 Android beta 客户端
- 一个可调用的 backend API 切片
- 一条已经打通的 Android 到 backend 学习链路

## 当前产品状态

### Android

Android app 已达到 beta 可用水平，当前既支持本地 fallback，也支持 backend-first 行为。

目前已经具备的能力：

- 从 assets 加载本地词库
- 展示单词通知
- 处理 `known / unknown`
- 将 `unknown` 写入本地生词本
- 在 app 内展示生词本
- 用艾宾浩斯式时间机制维护本地学习进度
- 切换词库并影响后续推词
- 推送关闭后清理运行时刷新行为
- 从 backend 优先获取下一词
- 将反馈同步到 backend
- 从 backend 优先读取生词本数量 / 列表
- 将当前词库选择同步到 backend

### Backend

backend 已经不再只是路由清理状态，而是一个已验证可用的核心 API 切片。

当前活跃接口族：

- `/api/v1/auth`
- `/api/v1/vocabs`
- `/api/v1/notebook`
- `/api/v1/learning`

最近 backend 的改进：

- UTF-8 响应头已明确
- 未认证返回 JSON `Result`
- 越权返回 JSON `Result`
- 内部错误返回 JSON `Result`
- 当前 auth info 接口要求认证
- auth 的冲突 / 未认证 / 越权语义已使用明确异常
- 已清理样例词条中异常音标和英文释义

## 本轮已完成内容

### Android 集成与工具链

- backend-first 下一词获取已接通
- backend-first 生词本读取已接通
- backend-first 反馈同步已接通
- 本地 fallback 已保留
- debug-only 网络安全配置已隔离
- API base URL 已切成按 build type 配置
- 本地辅助脚本已补齐：
  - backend 启动
  - debug 安装和启动
  - 本地 smoke 验证
  - deterministic `unknown` smoke

### Backend 响应与数据修复

- `JwtAuthInterceptor` 已返回 JSON `401`
- 全局异常处理统一返回 JSON `Result`
- 数据库相关内部错误不再回退到 Spring 默认错误页
- 样例词库中的脏数据已完成修复
- 已补一份面向已有数据库的一次性修复脚本

## 关键文件

### Android 主线

- `app/src/main/java/com/fragmentwords/data/WordRepository.kt`
- `app/src/main/java/com/fragmentwords/service/WordService.kt`
- `app/src/main/java/com/fragmentwords/receiver/WordActionReceiver.kt`
- `app/src/main/java/com/fragmentwords/HomeFragment.kt`
- `app/src/main/java/com/fragmentwords/NotebookFragment.kt`
- `app/src/main/java/com/fragmentwords/SettingsFragment.kt`
- `app/src/main/java/com/fragmentwords/network/ApiService.kt`
- `app/src/main/java/com/fragmentwords/network/ResolvedApiConfig.kt`

### Android 本地工具

- `backend/start-local.ps1`
- `install-debug-and-launch.bat`
- `run-local-smoke.bat`
- `run-local-unknown-smoke.bat`
- `run-local-unknown-smoke.cmd`
- `run-local-unknown-smoke.ps1`
- `app/src/debug/java/com/fragmentwords/debug/DebugWordActionActivity.kt`

### Backend

- `backend/src/main/java/com/fragmentwords/common/Result.java`
- `backend/src/main/java/com/fragmentwords/common/ConflictException.java`
- `backend/src/main/java/com/fragmentwords/common/ForbiddenException.java`
- `backend/src/main/java/com/fragmentwords/common/UnauthorizedException.java`
- `backend/src/main/java/com/fragmentwords/config/JwtAuthInterceptor.java`
- `backend/src/main/java/com/fragmentwords/config/GlobalExceptionHandler.java`
- `backend/src/main/java/com/fragmentwords/controller/VocabController.java`
- `backend/src/main/java/com/fragmentwords/controller/UnknownWordController.java`
- `backend/src/main/java/com/fragmentwords/controller/LearningController.java`
- `backend/src/main/java/com/fragmentwords/controller/UserController.java`
- `backend/src/main/resources/application.yml`
- `backend/start-local.ps1`
- `backend/start-local.bat`
- `backend/src/main/resources/sql/repair_sample_vocab_text.sql`

### 上下文 / 交接文档

- `CURRENT_STATUS.md`
- `PROJECT_CONTEXT.md`
- `PROJECT_HANDOFF_NEXT_STEPS.md`
- `SESSION_LOG.md`
- `LOCAL_INTEGRATION_QUICKSTART.md`

## 已验证项

当前仓库状态下，已经确认：

- Android `:app:compileDebugKotlin` 通过
- Android `:app:assembleDebug` 通过
- backend `mvn -q test` 通过
- backend `mvn -q -DskipTests compile` 通过
- 本地模拟器集成脚本可安装并启动 app
- deterministic debug smoke 可触发 `unknown` 动作链路，不依赖通知 UI 时序
- `/api/v1/auth/info/{userId}` 未认证返回 `401`
- 跨用户访问 `/api/v1/auth/info/{userId}` 返回 `403`
- `/api/v1/vocabs`
- `/api/v1/notebook/count`
- `/api/v1/learning/next`
- `/api/v1/learning/stats`

## 当前剩余风险

当前仍然值得关注的风险：

- backend 本地启动仍依赖正确的外部数据库凭据
- 真机短验尚未完整做完
- release 构建路径仍受本地 Gradle wrapper/cache 环境影响
- 多设备 / 云同步仍未完成
- 即使 deterministic debug 路径稳定，真实通知 UI 手点链路仍需最终真机确认

## 建议下一步

不建议再做大范围重写。

当前最有价值的下一步：

1. 继续收口本地 / release 环境
2. 做一次真机短验
3. 准备最终提交 / 演示 / 交付

## 最终判断

这个项目已经不再是单纯的 Android 原型。

现在更准确的描述是：

- 一个 beta 可用的 Android 客户端主线
- 一个已具备核心 API 的 backend 主线
- 一个处于发布前集成收口阶段的软件项目
