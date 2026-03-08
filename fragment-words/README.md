# 碎片单词 - 原型版本

轻量级、无压力的碎片化英语学习App原型

## 项目简介

这是一个基于Uni-app开发的Android App原型，实现了核心的锁屏单词学习功能：

- ✅ 单词卡片展示（随机显示30个常用英语单词）
- ✅ 自动消失效果（3秒倒计时后自动关闭）
- ✅ 认识/不认识按钮交互
- ✅ 生词本功能（本地存储）
- ✅ 模拟锁屏触发

## 技术栈

- **前端框架**: Uni-app (Vue 3)
- **数据源**: 本地JSON文件（30个常用英语单词）
- **存储**: 本地Storage
- **目标平台**: Android

## 项目结构

```
fragment-words/
├── pages/                    # 页面
│   ├── index/               # 首页（触发锁屏）
│   ├── wordcard/            # 单词卡片页
│   └── notebook/            # 生词本
├── static/                   # 静态资源
│   └── data/
│       └── daily_use.json   # 单词数据
├── App.vue                  # 应用入口
├── main.js                  # 主入口
├── pages.json               # 页面配置
└── manifest.json            # 应用配置
```

## 快速开始

### 方法1：使用HBuilderX（推荐）

1. **下载HBuilderX**
   - 访问：https://www.dcloud.io/hbuilderx.html
   - 下载"App开发版"

2. **导入项目**
   - 打开HBuilderX
   - 文件 → 导入 → 从本地目录导入
   - 选择 `D:\workspace\app\fragment-words` 文件夹

3. **运行到手机**
   - 连接Android手机（开启USB调试）
   - 点击工具栏的"运行" → "运行到手机或模拟器" → 选择你的设备
   - 首次运行需要安装HBuilder调试基座

4. **打包APK**（可选）
   - 发行 → 原生App-云打包
   - 选择Android平台
   - 点击打包（需要DCloud账号）

### 方法2：使用命令行

```bash
# 安装依赖
npm install -g @dcloudio/uvm
npm install -g @dcloudio/cli

# 创建uni-app项目（如果需要）
# 已创建项目，直接进入目录
cd D:\workspace\app\fragment-words

# 编译到Android
npm run build:app-android
```

## 功能演示

### 1. 首页
- 紫色渐变背景
- "显示单词卡片"按钮 - 触发模拟锁屏
- 生词本入口
- 显示当前生词数量

### 2. 单词卡片
- 随机显示一个单词
- 3秒倒计时自动消失
- 点击"看过了" → 关闭卡片
- 点击"不认识" → 显示翻译和例句 → 可加入生词本

### 3. 生词本
- 查看所有不认识的单词
- 点击单词查看详情
- 删除单个生词
- 清空所有生词

## 数据说明

### 单词数据
- 位置：`static/data/daily_use.json`
- 数量：30个常用英语单词
- 格式：
  ```json
  {
    "word": "ambience",
    "phonetic": "/ˈæmbiəns/",
    "translation": "n. 气氛，氛围，情调",
    "example": "The restaurant has a pleasant ambience.",
    "difficulty": 3
  }
  ```

### 生词本存储
- 存储位置：uni.getStorageSync('unknown_words')
- 数据格式：JSON数组
- 持久化：本地存储

## 调试技巧

### Chrome调试
1. 在HBuilderX中运行项目
2. 打开Chrome浏览器
3. 访问控制台显示的调试地址

### 真机调试
1. 手机开启开发者选项
2. 开启USB调试
3. 用数据线连接电脑
4. 在HBuilderX中选择设备运行

## 后续开发计划

### 第一阶段（当前原型）
- ✅ 基础单词卡片
- ✅ 生词本
- ✅ 本地存储

### 第二阶段（完整版）
- [ ] 真实锁屏权限
- [ ] 更多单词库（500+）
- [ ] 单词发音（TTS）
- [ ] 后端API
- [ ] 用户登录
- [ ] 云端同步

## 注意事项

1. **Android权限**：真实锁屏需要申请特殊权限，原型中使用按钮触发
2. **单词数据**：当前只包含30个单词，完整版需要导入ECDICT数据
3. **云打包限制**：免费版每天有打包次数限制

## 常见问题

**Q: 为什么不能直接锁屏显示？**
A: Android锁屏权限需要特殊申请，原型使用按钮模拟效果。

**Q: 如何添加更多单词？**
A: 编辑 `static/data/daily_use.json` 文件，添加单词对象即可。

**Q: 生词本数据会丢失吗？**
A: 不会，数据存储在本地，清除应用数据才会丢失。

**Q: 如何生成正式APK？**
A: 使用HBuilderX的"发行"功能进行云打包。

## 技术支持

- Uni-app文档：https://uniapp.dcloud.net.cn/
- HBuilderX教程：https://hx.dcloud.net.cn/
- Vue3文档：https://cn.vuejs.org/

---

**版本**: v1.0.0-prototype
**状态**: 原型完成，可演示
**更新日期**: 2026-02-19
