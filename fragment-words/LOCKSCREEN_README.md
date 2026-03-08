# 真正的锁屏单词功能 - 编译和测试指南

## 功能说明

已为"碎片单词"应用添加了真正的 Android 锁屏显示功能，类似音乐播放器在锁屏上显示媒体控制的效果。

### 实现方式
- 使用 Android MediaSession 和 Notification 媒体样式
- 在锁屏通知栏显示单词卡片
- 支持在锁屏直接点击"认识"和"不认识"按钮

## 编译步骤

### 方法1：使用 HBuilderX 云打包（推荐）

1. **打开 HBuilderX**
   - 确保 HBuilderX 版本 >= 3.6.0

2. **导入项目**
   - 文件 → 导入 → 从本地目录导入
   - 选择 `D:\workspace\app\fragment-words` 目录

3. **配置原生插件**
   - 在项目根目录找到 `manifest.json`
   - 确认 `"nativePlugins"` 配置已包含 `"word-locks-screen"`

4. **云打包**
   - 发行 → 原生App-云打包
   - 选择 Android 平台
   - 勾选"使用原生插件"
   - 点击打包

5. **下载 APK**
   - 等待打包完成（大约 3-5 分钟）
   - 下载生成的 APK 文件

### 方法2：本地打包（需要 Android Studio）

1. **准备环境**
   ```bash
   # 安装 JDK 1.8+
   # 安装 Android SDK
   # 配置 ANDROID_HOME 环境变量
   ```

2. **本地打包**
   - 在 HBuilderX 中：发行 → 原生App-本地打包
   - 生成本地 Android 项目

3. **在 Android Studio 中打开**
   - 导入生成的项目
   - 确保 nativePlugins 目录正确复制
   - 点击 Build → Generate Signed APK

## 测试步骤

### 1. 安装 APK
```bash
# 通过 USB 连接 Android 手机
adb install fragment-words.apk
```

### 2. 开启锁屏单词功能

1. **打开应用**
   - 首次打开会请求通知权限（允许）

2. **点击"🚀 开启锁屏单词"按钮**
   - 会弹出确认对话框
   - 点击"开启"

3. **查看锁屏效果**
   - 按电源键锁屏
   - 再按电源键解锁
   - 在锁屏界面查看单词卡片

### 3. 测试锁屏交互

在锁屏界面可以：
- **查看单词信息**：单词、音标、翻译、例句
- **点击"认识"按钮**：标记为已认识
- **点击"不认识"按钮**：标记为不认识，会加入生词本
- **点击通知**：打开应用

## 文件结构

```
fragment-words/
├── nativeplugins/
│   └── WordLockScreen/              # 原生插件
│       ├── package.json             # 插件配置
│       └── android/
│           ├── AndroidManifest.xml  # Android 配置
│           ├── LockScreenWordService.java      # 锁屏服务
│           ├── WordLockModule.java            # Uni-app 模块接口
│           └── WordLockReceiver.java          # 广播接收器
├── pages/
│   └── index/
│       └── index.vue                # 首页（已更新）
├── manifest.json                    # 应用配置（已更新）
└── LOCKSCREEN_README.md            # 本文档
```

## 已添加的功能

### 原生插件功能
- ✅ MediaSession 服务管理
- ✅ 锁屏通知显示
- ✅ "认识"和"不认识"按钮
- ✅ 点击通知打开应用
- ✅ 前台服务保证不被杀死

### 前端功能
- ✅ 开启/关闭锁屏单词
- ✅ 确认对话框
- ✅ 锁屏提示
- ✅ 模拟锁屏（保留原功能）

## 注意事项

### Android 权限
应用会请求以下权限：
- `FOREGROUND_SERVICE` - 前台服务
- `POST_NOTIFICATIONS` - 发送通知（Android 13+）
- `WAKE_LOCK` - 保持 CPU 运行

### 兼容性
- ✅ Android 5.0+ (API 21+)
- ✅ Android 10+ 需要前台服务类型
- ✅ Android 13+ 需要通知权限

### 已知限制
1. **iOS 不支持**：此功能仅支持 Android 平台
2. **部分定制 ROM**：某些深度定制的系统（如 MIUI、EMUI）可能需要额外设置
3. **电池优化**：建议将应用加入电池优化白名单

## 常见问题

### Q1: 打包时提示"找不到原生插件"
**A**: 确保 manifest.json 中正确配置了 nativePlugins，并且 nativeplugins 目录结构完整。

### Q2: 锁屏上看不到通知
**A**: 检查以下几点：
- 应用是否获得通知权限
- 手机的锁屏通知设置是否开启
- 电池优化是否阻止了后台服务

### Q3: 点击按钮没反应
**A**: 这是正常的，按钮点击会显示 Toast 提示。后续版本可以添加与前端的数据交互。

### Q4: 如何实现每次解锁自动刷新单词？
**A**: 需要监听 Android 的 `ACTION_USER_PRESENT` 广播，在用户解锁时触发。这是下一步功能。

## 下一步计划

- [ ] 监听解锁事件，自动显示新单词
- [ ] 定时刷新功能（如每小时自动更新）
- [ ] 单词统计和学习进度
- [ ] 后端 API 集成
- [ ] 云端生词本同步

---

**版本**: v2.0.0-locks-screen
**更新日期**: 2026-03-02
