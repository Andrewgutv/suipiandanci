# Fragment Words 当前状态（中文）

本文档反映的是 2026 年 4 月当前仓库对应的真实项目状态。

## 总览

仓库当前有两条活跃主线：

- `app/`：原生 Android 客户端主线
- `backend/`：Spring Boot API 后端主线

旧的 `fragment-words/` uni-app 原型只保留为历史材料，不再是产品主线。

## 当前项目阶段

项目已经不再处于“本地 Android 原型”阶段，而是进入了“发布前集成收口”阶段。

当前特征：

- Android 本地学习闭环已达到 beta 可用水平
- backend 核心 API 已可调用
- 一条真实 Android 到 backend 的学习链路已完成验证
- 本地开发 smoke 脚本已可重复使用
- 已加入 deterministic debug smoke，用于稳定验证 `unknown` 动作链路
- 样例词库中的脏数据已完成清理

## Android 主线状态

Android 端当前支持：

- 本地 fallback 行为
- 后端优先的核心学习路径

已验证可用的 Android 能力：

- 从 `app/src/main/assets/data/` 加载本地词库
- 本地 SQLite 生词本与学习进度 fallback
- 通知式单词卡片
- `known / unknown` 动作
- App 内生词本展示
- 本地艾宾浩斯式复习调度
- 多词库切换
- 推送开关与运行时清理
- backend-first 的下一词获取
- backend-first 的生词本数量 / 列表读取
- `known / unknown` 反馈同步
- 当前词库同步到 backend
- debug-only 的 deterministic `unknown` 动作验证路径

最新 debug APK 路径：

- `app/build/outputs/apk/debug/app-debug.apk`

## Backend 主线状态

backend 已不再只是路由整理阶段，核心 API 切片已跑通。

当前活跃路由族：

- `/api/v1/auth`
- `/api/v1/vocabs`
- `/api/v1/notebook`
- `/api/v1/learning`

已验证的 backend 行为：

- `GET /api/v1/vocabs`
- `GET /api/v1/vocabs/current`
- `PUT /api/v1/vocabs/current`
- `GET /api/v1/notebook/count`
- `GET /api/v1/notebook`
- `POST /api/v1/notebook`
- `POST /api/v1/learning/next`
- `POST /api/v1/learning/feedback`
- `GET /api/v1/learning/stats`

最近 backend 的改进包括：

- UTF-8 JSON 响应头已明确设置
- 未认证请求返回 JSON `Result`
- 越权请求返回 JSON `Result`
- 内部错误返回 JSON `Result`
- `auth/info` 已由 `@RequireAuth` 保护
- auth 的冲突 / 未认证 / 越权场景已经用了明确的领域异常
- `IELTS` / `TOEFL` / `CET6` / `GRE` / `GRADUATE` 样例词条的音标和中文释义已完成修复

Backend 编译状态：

```powershell
cmd /c "C:\apache-maven-3.9.9\bin\mvn.cmd" -q -DskipTests compile
```

## 已验证内容

当前仓库状态下，已经验证的内容包括：

- Android `:app:compileDebugKotlin` 通过
- Android `:app:assembleDebug` 通过
- backend `mvn -q test` 通过
- backend `mvn -q -DskipTests compile` 通过
- Android 模拟器 smoke 链路可安装并启动 app
- deterministic `unknown` smoke 可触发 app 侧 `unknown` 动作链
- 未认证访问 `/api/v1/auth/info/{userId}` 返回 JSON `401`
- 已认证但跨用户访问 `/api/v1/auth/info/{userId}` 返回 JSON `403`
- backend `/api/v1/notebook` 已能返回修复后的中文释义和正常 IPA

## 当前高优先级风险

当前仍有意义的未收口风险包括：

- Android 通知与前台服务行为尚未完成真机短验
- backend 本地启动仍依赖正确注入 MySQL 凭据
- release 构建路径仍受本地 Gradle wrapper/cache 环境影响
- 多设备 / 云同步能力尚未完成
- 即使 deterministic debug smoke 已稳定，真实系统通知 UI 的手点链路仍需最终真机确认

## 建议下一步

不建议再做大范围重构。

建议顺序：

1. 继续完成发布前环境收口
   - 稳定本地数据库启动路径
   - 稳定 release API 地址注入
   - 确认 release 构建验证路径
2. 做一次真机短验
3. 如果未来新增更多受保护接口，再继续扩展同样的响应语义
4. 之后再考虑收缩剩余本地-only fallback 假设

## 实用总结

如果现在要对外描述项目阶段，可以这样说：

- Android 主线：beta 可用
- backend 主线：核心 API 切片已可用
- 集成状态：Android 到 backend 的核心学习链路已跑通
- 本地验证：已具备 repeatable smoke 工具，包括 deterministic `unknown` debug 路径
- 整体阶段：处于发布前集成收口，不再是原型探索阶段
