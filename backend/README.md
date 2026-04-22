# 锁屏背单词 - 后端服务

> **锁屏背单词项目**的Spring Boot后端模块，提供单词学习、词库管理、生词本等核心功能。

---

## 📋 项目简介

本后端服务是锁屏背单词项目的核心服务端，采用前后端分离架构，为Android客户端提供RESTful API接口。

### 核心功能

- ✅ **词库管理**：支持CET4/6/IELTS/TOEFL/GRE等多种词库
- ✅ **单词学习**：随机单词推荐
- ✅ **生词本**：设备维度隔离的生词本管理
- ✅ **设备偏好**：记住用户的词库选择和设置
- ⏳ **艾宾浩斯算法**：8个复习节点的智能记忆系统（待实现）
- ⏳ **学习统计**：学习进度、记忆保持率等数据分析（待实现）

---

## 🚀 快速开始

### 前置要求

- **JDK 17+**
- **Maven 3.6+**
- **MySQL 8.0+**

### 数据库配置

1. **启动MySQL服务**
   ```bash
   net start MySQL80
   ```

2. **创建数据库并导入初始数据**
   ```bash
   mysql -uroot -p123456
   ```

   在MySQL命令行中执行：
   ```sql
   source D:/workspace/app/backend/src/main/resources/sql/init.sql
   ```

   如果数据库已经存在，但 `IELTS` / `TOEFL` 样例词仍保留旧的英文释义或异常音标，可额外执行：
   ```sql
   source D:/workspace/app/backend/src/main/resources/sql/repair_sample_vocab_text.sql
   ```

3. **验证数据库**
   ```sql
   USE fragment_words;
   SHOW TABLES;
   SELECT * FROM vocab;
   ```

### 启动后端服务

#### 方式1：使用启动脚本（推荐）

```bash
cd D:\workspace\app\backend
start.bat
```

#### 方式2：使用Maven命令

```bash
cd D:\workspace\app\backend
mvnw.cmd spring-boot:run
```

#### 方式3：在IDE中运行

1. 使用IntelliJ IDEA打开 `backend` 目录
2. 等待Maven依赖下载完成
3. 运行 `FragmentWordsApplication.java`

### 验证服务是否启动成功

- **服务地址**：http://localhost:8080
- **API文档**：http://localhost:8080/swagger-ui/index.html
- **健康检查**：http://localhost:8080/actuator/health（如已配置）

---

## 📡 API接口文档

### 1. 词库管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/vocab/list` | GET | 获取所有词库 |
| `/api/vocab/selected?deviceId=xxx` | GET | 获取当前设备选中的词库 |
| `/api/vocab/select?deviceId=xxx&vocabId=1` | POST | 保存设备的词库选择 |

### 2. 单词学习

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/word/random?vocabId=1` | GET | 随机获取单词 |
| `/api/word/refresh?vocabId=1` | POST | 手动刷新单词 |

### 3. 生词本

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/unknown/list?deviceId=xxx&pageNum=1&pageSize=10` | GET | 获取生词本列表（分页） |
| `/api/unknown/add?deviceId=xxx&wordId=1` | POST | 添加单词到生词本 |
| `/api/unknown/remove?deviceId=xxx&wordId=1` | DELETE | 从生词本移除单词 |
| `/api/unknown/count?deviceId=xxx` | GET | 获取生词总数 |

---

## 🗄️ 数据库设计

### 核心表结构

```
fragment_words (数据库)
├── user                 # 用户表（可选，未来扩展）
├── vocab                # 词库表
├── word                 # 单词表
├── unknown_word         # 生词本表
├── learning_progress    # 学习进度表（艾宾浩斯算法核心）
└── device_preference    # 设备偏好表
```

### 关键设计

- **设备ID隔离**：使用 `device_id` 实现无需登录的用户数据隔离
- **艾宾浩斯算法**：`learning_progress` 表记录8个复习阶段
- **词库扩展**：通过 `vocab` 表动态添加新词库

详细SQL脚本：`src/main/resources/sql/init.sql`

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.4 | 基础框架 |
| MySQL | 8.0+ | 数据库 |
| MyBatis-Plus | 3.5.6 | ORM框架 |
| Lombok | 1.18.30 | 简化代码 |
| SpringDoc | 2.4.0 | API文档（Swagger） |

---

## 📁 项目结构

```
backend/
├── src/main/java/com/fragmentwords/
│   ├── FragmentWordsApplication.java  # 启动类
│   ├── common/                        # 公共类
│   │   └── Result.java                # 统一返回结果
│   ├── config/                        # 配置类
│   │   └── MyBatisPlusConfig.java
│   ├── controller/                    # 控制器
│   │   ├── VocabController.java       # 词库管理
│   │   ├── WordController.java        # 单词学习
│   │   └── UnknownWordController.java # 生词本
│   ├── model/                         # 数据模型
│   │   ├── entity/                    # 实体类
│   │   └── dto/                       # 数据传输对象
│   ├── service/                       # 服务层
│   │   └── impl/                      # 服务实现
│   └── mapper/                        # MyBatis Mapper
├── src/main/resources/
│   ├── application.yml                # 应用配置
│   └── sql/
│       └── init.sql                   # 数据库初始化脚本
├── pom.xml                            # Maven配置
├── start.bat                          # 启动脚本
└── README.md                          # 本文档
```

---

## 🔧 配置说明

### application.yml 配置项

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fragment_words
    username: root
    password: 123456  # 建议修改为实际密码

server:
  port: 8080
  servlet:
    context-path: /api
```

### 修改数据库密码

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
```

---

## 🚧 待实现功能

- [ ] **艾宾浩斯遗忘曲线算法**
  - [ ] 8个复习节点的自动计算
  - [ ] 智能推荐需要复习的单词
  - [ ] 记忆保持率统计

- [ ] **用户系统**
  - [ ] 手机号注册/登录
  - [ ] 微信登录（可选）
  - [ ] 跨设备数据同步

- [ ] **学习统计**
  - [ ] 每日学习数据
  - [ ] 学习曲线可视化
  - [ ] 单词掌握度分析

- [ ] **词库扩展**
  - [ ] 在线词库更新
  - [ ] 用户自定义词库
  - [ ] 词库导入/导出

- [ ] **性能优化**
  - [ ] Redis缓存
  - [ ] 单词推荐算法优化
  - [ ] 数据库索引优化

---

## 🐛 常见问题

### Q1: 启动时报错 "Access denied for user 'root'"

**A**: 修改 `application.yml` 中的数据库密码：
```yaml
spring:
  datasource:
    password: 你的实际密码
```

### Q2: 找不到数据库 'fragment_words'

**A**: 先创建数据库：
```bash
mysql -uroot -p
# 在MySQL中执行：
source D:/workspace/app/backend/src/main/resources/sql/init.sql
```

### Q3: 端口 8080 被占用

**A**: 修改 `application.yml` 中的端口：
```yaml
server:
  port: 8081  # 改为其他端口
```

---

## 📝 开发规范

### 代码规范

- 遵循阿里巴巴Java开发规范
- 使用Lombok简化代码
- 统一使用 `Result` 类封装返回结果

### API命名规范

- RESTful风格
- 名词复数形式：`/api/vocabs`
- 驼峰命名：`deviceId`, `vocabId`

---

## 👥 团队协作

| 成员 | 负责模块 | 联系方式 |
|------|---------|---------|
| 朋友C | 后端API、艾宾浩斯算法 | - |
| 你 | 整体架构、Android客户端 | - |

---

## 📄 License

MIT License

---

**最后更新**：2026-03-13
**版本**：v1.0.0
