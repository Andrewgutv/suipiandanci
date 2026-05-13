# 项目后续交接建议（中文）

## 当前优先级

第一条真实 Android 到 backend 的集成主线已经完成。

现在的首要目标已经不是“继续做大功能”，而是“发布前收口”。

## 建议执行顺序

1. 稳定本地与 release 环境配置
2. 做真机短验
3. 如果未来新增更多受保护接口，再把相同响应语义扩展过去
4. 准备干净的提交 / 推送 / 交付材料

## 最高优先级剩余工作

### 1. 运行环境收口

当前要求：

- backend 数据库凭据必须在运行时正确注入

保留的入口：

- `backend/start-local.ps1`
- `backend/start-local.bat`
- `install-debug-and-launch.bat`
- smoke 验证：
  - `run-local-smoke.bat`
  - `run-local-unknown-smoke.cmd`

目标：

- 让本地 backend 启动更稳定
- 继续保持 release API URL 外部注入

已经完成的改进：

- `DB_PASSWORD` 缺失时 fail-fast
- MySQL TCP 预检查失败时 fail-fast
- `APP_PORT` 冲突时 fail-fast
- smoke 脚本已支持 `APP_PORT`
- 已存在一份面向已有数据库的数据修复脚本：
  - `backend/src/main/resources/sql/repair_sample_vocab_text.sql`

### 2. Backend Auth 语义

当前状态：

- `/api/v1/auth` 路由族已存在
- `JwtAuthInterceptor` 已接入
- 未认证访问返回 JSON `401`
- 已认证但跨用户访问返回 JSON `403`
- `auth/info` 已被保护

剩余工作：

- 如果以后新增更多受保护接口，再按当前规则继续统一响应语义

### 3. Release 验证

当前状态：

- debug 构建已验证
- release 构建验证仍受本地 Gradle wrapper/cache 环境影响

目标：

- 在干净 Gradle 环境下验证 release 路径
- 继续通过 Gradle 属性 / CI 环境变量注入 release API URL

### 4. 设备验收

仍需完成：

- 真机上的短验，包括：
  - 通知展示
  - 前台服务行为
  - 推送开关开 / 关
  - `known / unknown` 通知动作

当前验证说明：

- 仓库里已经有 deterministic debug-only `unknown` smoke，可用于稳定验证 app 内部链路
- 但这不等于真实系统通知 UI 手点链路已经做完最终验收

## 下一里程碑

理想的下一里程碑应满足：

- backend 本地启动对外部 DB 凭据的依赖已经稳定
- 当前 auth 切片里的响应语义一致
- Android debug smoke 继续保持绿色
- 已完成一轮真机短验
- 修好的样例词库数据在设备侧稳定生效，不再因旧数据库残留而回退

## 工作原则

在发布前收口阶段：

- 不要再做大范围 Android UI 重写
- 不要再引入新的大产品功能
- 把重点放在环境稳定、auth 语义、数据正确性和验收上
